#!/usr/bin/env bash

###############################################################################
# README
###############################################################################
# This script uses OpenAI Models to open Pull Requests that fixes and improvements
# for MobileRT.
#
# Environment variables necessary:
# * GITHUB_TOKEN - Token to access Github API
# Input parameters:
# * The context for AI Model
# Output files:
# * response.log - response from AI Model
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

printEnvironment() {
  echo 'Selected arguments:';
  echo "AI Model: ${aiModel}";
  echo "Sleep between batches: ${sleepBetweenRequestsSeconds} sec";
  echo "Max payload size: ${maxPayloadSizeBytes} bytes";
  echo "AI Model context: ${aiModelContext}";
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
    --arg context "${aiModelContext}" \
    '
    def inject(name; value):
      gsub(name; (value | @json));

    walk(
      if type == "string" then
        .
        | gsub("__AI_MODEL__"; $aiModel)
        | inject("__CONTEXT__"; $context)
      else
        .
      end
    )
    ' \
    < .github/workflows/ml_model.-payload.json \
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

echo "AI Model context: ${aiModelContext}";

OFFSET=0;
TOTAL=1;
BATCH_INDEX=1;
while true; do
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
  echo "${RESPONSE}" >> response.log;
  awk '$0 ~ /^#/ || !seen[$0]++' response.log > tmp && mv tmp response.log; # Remove duplicate lines

  OFFSET=$((OFFSET + ${#SHAS[@]}));
  BATCH_INDEX=$((BATCH_INDEX + 1));
done

cat response.log;
wc -c response.log;
ls -lahp response.log;
