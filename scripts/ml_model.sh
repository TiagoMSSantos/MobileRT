#!/usr/bin/env bash

###############################################################################
# README
###############################################################################
# This script uses OpenAI Models to open Pull Requests that fixes and improvements
# for MobileRT.
#
# Environment variables necessary:
# * GITHUB_TOKEN - Token to access Github API
# * GITHUB_REPOSITORY - Repository to create Pull Request
# Input parameters:
# * The context for AI Model
# Output files:
# * response.log - response from AI Model
# * response_code.log - code response from AI Model
###############################################################################
###############################################################################


###############################################################################
# Exit immediately if a command exits with a non-zero status.
###############################################################################
set -Eeuo pipefail;
shopt -s failglob;
shopt -s inherit_errexit;
###############################################################################
###############################################################################


###############################################################################
# Change directory to MobileRT root.
###############################################################################
if [ $# -ge 1 ]; then
  cd "$(dirname "${0}")/.." || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
# shellcheck disable=SC1091
. scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Execute Shellcheck on this script.
###############################################################################
if [ $# -ge 1 ] && command -v shellcheck > /dev/null; then
  shellcheck "${0}" --exclude=SC1017 || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Set default arguments.
###############################################################################
# AI Model from Github to use. Check available in: https://models.inference.ai.azure.com/models
aiModel='gpt-4o-mini';
# To avoid HTTP 429 "Too Many Requests" - 59sec fails, but 60sec seems to work fine
# UserByModelByDay: Rate limit of 150 per 86400s
sleepBetweenRequestsSeconds='0';
# To avoid HTTP 413 "Content Too Large" - 32KB is max payload size, 32007 bytes fails, but 32001 bytes seems to work fine
# gpt-4o-mini model - Max size: 8000 tokens
maxPayloadSizeBytes='32006';
# Context for AI Model
aiModelContext="${1}";
# File with additional context for AI Model
aiModelFile="${2}";
# Branch to create Pull Request
BRANCH='ml_model';
# Max number of requests to AI Model
MAX_REQUESTS=100;

printEnvironment() {
  echo 'Selected arguments:';
  echo "GITHUB_REPOSITORY: ${GITHUB_REPOSITORY}";
  echo "BRANCH: ${BRANCH}";
  echo "AI Model: ${aiModel}";
  echo "Sleep between batches: ${sleepBetweenRequestsSeconds} sec";
  echo "Max payload size: ${maxPayloadSizeBytes} bytes";
  echo "AI Model context: ${aiModelContext}";
  echo "AI Model file: ${aiModelFile}";
}
###############################################################################
###############################################################################


###############################################################################
# Parse arguments.
###############################################################################
printEnvironment;

if [[ -v GITHUB_TOKEN ]]; then
  echo 'GITHUB_TOKEN is set';
else
  echo 'GITHUB_TOKEN is not set';
  exit 1;
fi
###############################################################################
###############################################################################

# Produce payload.json.log file with request for AI Model to resume release notes based on commits provided.
# Parameters:
# *
producePayload() {
  jq -c \
    --arg aiModel ${aiModel} \
    --rawfile file "${aiModelFile}" \
    --arg context "${aiModelContext}" \
    '
    def inject(name; value):
      gsub(name; (value | @json));

    walk(
      if type == "string" then
        .
        | gsub("__AI_MODEL__"; $aiModel)
        | inject("__FILE__"; $file)
        | inject("__CONTEXT__"; $context)
      else
        .
      end
    )
    ' \
    < .github/workflows/ml_model-payload.json \
    | sed -r 's/\\\\\\/\\/g; s/\[[0-9;]*[A-Za-z]//g; s/[[:space:]]+/ /g' > payload.json.log;
}

# Requests AI Model to write some release notes based on commits information. Output is written to file response.json.log.
# Output environment variables:
# * RESPONSE - response HTTP code from AI Model
# * CURL_EXIT - curl exit code
# Parameters:
# * startTs - timestamp (seconds) when last request was made
# * sleepBetweenRequestsSeconds - time (seconds) to sleep between requests
# * BATCH_INDEX - index of current batch
# * OFFSET - current offset
# * TOTAL - total number of commits which will be sent to Ai Model
# * SIZE - payload size in bytes
# * WORDS - number of words in payload
requestAiModel() {
  _retry=0;
  endTs=$(date +%s);
  elapsed=$(( endTs - startTs ));
  waitSeconds=$(( sleepBetweenRequestsSeconds - elapsed ));
  # echo "Payload: $(cat payload.json.log)";
  echo "Processing batch ${BATCH_INDEX} (offset ${OFFSET}/${TOTAL} - Payload size: ${SIZE} bytes | ${WORDS} words - sleep: ${waitSeconds}sec)";
  if [ ${waitSeconds} -gt 0 ]; then
    sleep ${waitSeconds};
  fi

  while true; do
    RESPONSE=$(
      curl -w "%{http_code}" -o response.json.log -s -S \
        --retry 3 --retry-delay ${sleepBetweenRequestsSeconds} --retry-all-errors --retry-max-time 120 --connect-timeout 20 --max-time 180 \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer ${GITHUB_TOKEN}" \
        -d @payload.json.log \
        "https://models.inference.ai.azure.com/openai/deployments/${aiModel}/chat/completions"
    );
    CURL_EXIT=$?;
    startTs=$(date +%s);

    if (( CURL_EXIT == 0 && RESPONSE == 429 )); then
      # _retry=$((_retry + 1));
      # echo "HTTP ${RESPONSE} - Too Many Requests - surpassed rate limit threshold. Response: $(cat response.json.log). Retrying '${_retry}' batch '${BATCH_INDEX}' in 5 min ...";
      # sleep 300;
      break
    fi

    break
  done

  if [[ "${RESPONSE}" -lt 100 || "${RESPONSE}" -ge 400 || "${CURL_EXIT}" -ne 0 ]]; then
    printf 'Payload: ';
    cat payload.json.log;
    printf 'Response: ';
    cat response.json.log;
    echo "HTTP error: ${RESPONSE}. Curl exit code: ${CURL_EXIT}";
    exit 1;
  fi
}
###############################################################################

startTs=$(date +%s);

jq . .github/workflows/ml_model-payload.json  > /dev/null; # validate JSON
ls -lahp "${aiModelFile}";

for ((BATCH_INDEX=1; BATCH_INDEX<MAX_REQUESTS; BATCH_INDEX++)); do
  OFFSET=0;
  TOTAL=1;
  for ((i=OFFSET; i<TOTAL; i++)); do

    producePayload;
    SIZE=$(wc -c < payload.json.log);
    WORDS=$(wc -w < payload.json.log);

    if (( SIZE > maxPayloadSizeBytes )); then
      echo "Context too large for payload limit. Payload size: ${SIZE} bytes | ${WORDS} words. Payload: $(cat payload.json.log)";
      exit 1;
    fi
  done

  requestAiModel;
  RESPONSE=$(jq -r '.choices[0].message.content' response.json.log);
  echo "${RESPONSE}" > response.log;

  # 1. Extract the code text from the JSON response
  # shellcheck disable=SC2016
  jq -r '.choices[0].message.content' response.json.log \
    | sed -n '/```cpp/,/```/p' \
    | sed '1d;$d' > response_code.log;

  echo "AI Model code 'BATCH_INDEX: ${BATCH_INDEX}' 'chars: $(wc -c response_code.log)' 'size: $(ls -lahp response_code.log)' response: $(head -1 response_code.log)";

  # 2. Encode content
  CONTENT=$(base64 -w 0 "response_code.log");

  # 3. Escape context
  ESC_CONTEXT=$(echo "${aiModelContext}" | sed 's/"/\\"/g' | awk '{printf "%s\\n", $0}' | sed 's/\\n$//');

  # 4. Ensure branch exists
  HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${GITHUB_TOKEN}" "https://api.github.com/repos/${GITHUB_REPOSITORY}/branches/${BRANCH}");
  if [ "$HTTP_STATUS" != "200" ]; then
    curl -L -X POST -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    "https://api.github.com/repos/${GITHUB_REPOSITORY}/git/refs" \
    -d "{\"ref\": \"refs/heads/${BRANCH}\", \"sha\": \"${GITHUB_SHA}\"}";
  fi

  # 5. Get SHA from GitHub only
  SHA=$(curl -s -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    "https://api.github.com/repos/${GITHUB_REPOSITORY}/contents/${aiModelFile}?ref=${BRANCH}" \
    | jq -r '.sha');

  # 6. Build SHA string ONLY if it exists on GitHub
  if [ "${SHA}" != "null" ] && [ -n "${SHA}" ]; then
    JSON_SHA="\"sha\": \"${SHA}\",";
  else
    JSON_SHA="";
  fi

  echo 'Compiling MobileRT locally with AI Model suggestion';
  cat response_code.log > "${aiModelFile}";

  set +e;
  sh scripts/compile_native.sh -t release -c g++ -r yes > compiled.log 2>&1;
  RESULT="${?}";
  set -e;

  # shellcheck disable=SC2181
  if [ "${RESULT}" -eq 0 ]; then
    echo 'Compiled MobileRT successfully!';
    break;
  else
    echo "Failed to compile MobileRT with AI Model [$(ls -lahp compiled.log)] response: ${RESULT}";
    # echo 'tail compiled.log:';
    # tail -n 20 compiled.log;
    # echo 'error compiled.log:';
    # grep -ine 'error:' -C10 compiled.log;
    aiModelContext="Finish implementation of file ${aiModelFile} and fix the error occurred. Always use ::std:: instead of std:: for stdlib functions. $(grep -ine 'error:' -C10 compiled.log)";
    # shellcheck disable=SC2086
    echo "Replacing context with current error: $(grep -ine 'error:' -C0 compiled.log)";
  fi
done

if [ "${RESULT}" -ne 0 ]; then
  echo "Failed to compile MobileRT. Build log: $(cat compiled.log)";
  echo "Last response: $(cat response.log)";
  exit 1;
fi

echo 'Creating commit with AI Model response';
curl -L -X PUT \
  -H "Authorization: Bearer ${GITHUB_TOKEN}" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "https://api.github.com/repos/${GITHUB_REPOSITORY}/contents/${aiModelFile}" \
  -d "{
    \"message\": \"${ESC_CONTEXT}\",
    \"content\": \"${CONTENT}\",
    ${JSON_SHA}
    \"branch\": \"${BRANCH}\"
  }";

# 7. Check if a PR already exists for '${BRANCH}' to avoid 422 errors
OWNER=$(echo "${GITHUB_REPOSITORY}" | cut -d'/' -f1);
# Ensure the variable is used within the URL string correctly
PR_EXISTS=$(curl -s -H "Authorization: Bearer ${GITHUB_TOKEN}" \
  "https://api.github.com/repos/${GITHUB_REPOSITORY}/pulls?head=${OWNER}:${BRANCH}&state=open" \
  | jq '. | length');

if [ "${PR_EXISTS}" -eq "0" ]; then
  echo 'No existing PR found. Creating a new one';
  curl -L \
    -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "https://api.github.com/repos/${GITHUB_REPOSITORY}/pulls" \
    -d "{
      \"title\": \"AI Model Update: ${aiModelFile}\",
      \"body\": \"This PR contains the latest updates from the AI model context: ${ESC_CONTEXT}\",
      \"head\": \"${BRANCH}\",
      \"base\": \"master\"
    }";
else
  echo "Pull Request already exists for branch ${BRANCH}. Skipping PR creation.";
fi

echo 'Trigger Github workflow to run tests';
curl --fail-with-body -S -X POST \
  -H "Authorization: Bearer ${GITHUB_TOKEN}" \
  -H "Accept: application/vnd.github.v3+json" \
  "https://api.github.com/repos/${GITHUB_REPOSITORY}/actions/workflows/native.yml/dispatches" \
  -d "{\"ref\":\"${BRANCH}\",\"inputs\":{\"code-coverage-only\":\"false\"}}";
