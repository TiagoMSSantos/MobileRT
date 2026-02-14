#!/usr/bin/env bash

###############################################################################
# README
###############################################################################
# This script uses OpenAI Models to write automatically the release notes of a
# MobileRT release. The release notes are written based on the information
# provided in the commits.
#
# Environment variables necessary:
# * GITHUB_TOKEN - Token to access Github API
# Output files:
# * release_notes.log - Release notes written by AI based on commits information
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
  shellcheck "${0}" || return 1;
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

printEnvironment() {
  echo 'Selected arguments:';
  echo "AI Model: ${aiModel}";
  echo "Sleep between batches: ${sleepBetweenRequestsSeconds} sec";
  echo "Max payload size: ${maxPayloadSizeBytes} bytes";
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
# * SHAs of commits
# * Whether to limit commit information up to 23k characters.
producePayload() {
  local shas=("$@");
  local limit="${shas[${#shas[@]}-1]}"; # last element
  unset 'shas[${#shas[@]}-1]'; # remove last element

  # echo "SHAs of commits: ${shas[@]}";
  # echo "Limit: ${limit}";
  PAGER= LESS= git --no-pager show -w -U0 --no-renames \
    --no-color --no-color-moved --no-ext-diff --no-textconv --ws-error-highlight=none \
    --ignore-all-space --ignore-blank-lines --ignore-cr-at-eol --ignore-space-at-eol --ignore-space-change \
    --pretty=format:"%B" "${shas[@]}" -- . \
    ':(exclude)*jscpd-report*' \
    ':(exclude)*docs*' \
    ':(exclude)*.github*' \
    ':(exclude)*app/src/androidTest*' \
    ':(exclude)*app/src/test*' \
    ':(exclude)*app/Unit_Testing*' \
    ':(exclude)*scripts/test*' \
    ':(exclude)*WavefrontOBJs*' \
    ':(exclude)*.obj' \
    ':(exclude)*.mtl' \
    ':(exclude)*.cam' \
    ':(exclude)*.apk' \
    | tr -d '\000-\010\013\014\016-\037\177' \
    | awk '
      NF == 0 { next }
      /^diff --git/ {
          file=$4
          sub(/^b\//, "", file)
          print ""
          print file
          next
      }
      /^index / { next }
      /^--- / { next }
      /^\+\+\+ / { next }
      { print }
    ' \
    | sed -r 's/\\\\\\/\\/g; s/\[[0-9;]*[A-Za-z]//g; s/[[:space:]]+/ /g; /^@@ /d; /[[:alnum:]]/!d; /^Test plan:/d; /^\* Pipeline passes/d; /^\* N\/A/d; /^[+-]Subproject commit [0-9a-zAZ]+$/d;' > commits.raw;

  if [ ${limit} = true ]; then
    head -c 23000 commits.raw > commits_limited.raw;
    mv commits_limited.raw commits.raw;
    wc commits.raw;
  fi

  jq -c \
    --arg aiModel ${aiModel} \
    --rawfile agents AGENTS.md \
    --rawfile commits commits.raw \
    '
    def inject(name; value):
      gsub(name; (value | @json));

    walk(
      if type == "string" then
        .
        | gsub("__AI_MODEL__"; $aiModel)
        | inject("__AGENTS__"; $agents)
        | inject("__COMMITS__"; $commits)
      else
        .
      end
    )
    ' \
    < .github/workflows/release-notes-payload.json \
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
# * SHAS - shas of commits which will be sent to Ai Model
# * OFFSET - current offset
# * TOTAL - total number of commits which will be sent to Ai Model
# * file release_notes.log - file which contain all release notes already written by AI Model
# * SIZE - payload size in bytes
# * WORDS - number of words in payload
# * file commits.raw - file which contains data from commits which will be sent to AI Model
requestAiModel() {
  _retry=0;
  endTs=$(date +%s);
  elapsed=$(( endTs - startTs ));
  waitSeconds=$(( sleepBetweenRequestsSeconds - elapsed ));
  # echo "Payload: $(cat payload.json.log)";
  echo "Processing batch ${BATCH_INDEX} with ${#SHAS[@]} commits (offset ${OFFSET}/${TOTAL} - $(wc -c release_notes.log) - Payload size: ${SIZE} bytes | ${WORDS} words - sleep: ${waitSeconds}sec) [$(wc -c commits.raw)]: $(head -1 commits.raw)";
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

jq . .github/workflows/release-notes-payload.json  > /dev/null; # validate JSON
jq . .github/workflows/release-notes-merge.json  > /dev/null; # validate JSON

ALL_TAGS=$(git for-each-ref --sort=-creatordate --format '%(refname:short)' refs/tags);
echo "All tags: ${ALL_TAGS}";

PREVIOUS_TAG=$(echo "${ALL_TAGS}" | sed -n '2p');
echo "Previous tag: ${PREVIOUS_TAG}";

git log "${PREVIOUS_TAG}.." --pretty=format:"%H" > shas.log;

OFFSET=0;
BATCH_INDEX=1;
touch release_notes.log;
echo "Processing $(wc -l < shas.log) commits.";
while true; do
  mapfile -t ALL_SHAS < <(grep -v '^$' shas.log);
  TOTAL=${#ALL_SHAS[@]};
  PARTS=$(( (TOTAL + 499) / 500 ));
  echo "Will split all release notes in '${PARTS}' parts at the end";

  [[ ${OFFSET} -ge ${TOTAL} ]] && {
    echo "Stopping since no more commits to process. OFFSET: ${OFFSET}. TOTAL: ${TOTAL}.";
    break
  }

  SHAS=();

  for ((i=OFFSET; i<TOTAL; i++)); do
    SHAS+=("${ALL_SHAS[$i]}");

    producePayload "${SHAS[@]}" false;
    SIZE=$(wc -c < payload.json.log);
    WORDS=$(wc -w < payload.json.log);

    if (( SIZE > maxPayloadSizeBytes )); then
      if (( ${#SHAS[@]} == 1 )); then
        echo "Single commit too large for payload limit. Payload size: ${SIZE} bytes | ${WORDS} words. Offending commit: ${ALL_SHAS[$i]}. Commits: $(wc commits.raw). Payload: $(cat payload.json.log)";
        producePayload "${ALL_SHAS[$i]}" true;
        SIZE=$(wc -c < payload.json.log);
        WORDS=$(wc -w < payload.json.log);
        break
      fi

      unset 'SHAS[-1]'; # Remove last commit to avoid surpassing 32k payload limit
      mv payload_previous.json.log payload.json.log; # restore previous valid payload
      SIZE=$(wc -c < payload.json.log);
      WORDS=$(wc -w < payload.json.log);
      break
    fi

    cp payload.json.log payload_previous.json.log; # save previous valid payload
  done

  requestAiModel;
  RELEASE_NOTES=$(cat response.json.log | jq -r '.choices[0].message.content');
  echo "${RELEASE_NOTES}" >> release_notes.log;
  awk '$0 ~ /^#/ || !seen[$0]++' release_notes.log > tmp && mv tmp release_notes.log; # Remove duplicate lines

  OFFSET=$((OFFSET + ${#SHAS[@]}));
  BATCH_INDEX=$((BATCH_INDEX + 1));
done

cat release_notes.log;
wc -c release_notes.log;
ls -lahp release_notes.log;

PARTS=$(( (TOTAL + 499) / 500 ));
echo "Splitting all release notes in '${PARTS}' parts";
FILE="release_notes.log";

TOTAL_LINES=$(wc -l < "${FILE}");
CHUNK=$((TOTAL_LINES / PARTS));

# Array to store cut points
CUTS=();

# Generate cut points dynamically
for ((i=1; i<PARTS; i++)); do
  POS=$((CHUNK * i));

  CUT=$(awk -v pos="${POS}" 'NR >= pos && /^## / {print NR; exit}' "${FILE}");

  # Fallback if no header found after POS
  if [[ -z "$CUT" ]]; then
      CUT=$((POS + 1));
  fi

  CUTS+=("${CUT}");
done

# Add final cut (end of file)
CUTS+=($((TOTAL_LINES + 1)));

# Create output parts
START=1;
for ((i=0; i<PARTS; i++)); do
  END=$((CUTS[i] - 1));
  OUT="release_notes_part$((i+1)).log";

  sed -n "${START},${END}p" "${FILE}" > "${OUT}";

  START=${CUTS[i]};
done

echo "Merging release notes of all '${PARTS}' parts";
for ((part=1; part<=PARTS; part++)); do
  echo "Merging release notes of part '${part}'";
  jq -c \
    --arg aiModel ${aiModel} \
    --rawfile agents AGENTS.md \
    --rawfile release_notes release_notes_part${part}.log \
    '
    def inject(name; value):
      gsub(name; (value | @json));

    walk(
      if type == "string" then
        .
        | gsub("__AI_MODEL__"; $aiModel)
        | inject("__AGENTS__"; $agents)
        | inject("__RELEASE_NOTES__"; $release_notes)
      else
        .
      end
    )
    ' \
    < .github/workflows/release-notes-merge.json \
    > payload.json.log;
  SIZE=$(wc -c < payload.json.log);
  WORDS=$(wc -w < payload.json.log);

  requestAiModel;
  RELEASE_NOTES=$(cat response.json.log | jq -r '.choices[0].message.content');
  echo "Response: ${RELEASE_NOTES}";
  echo "${RELEASE_NOTES}" >> merged_release_notes.log;
done

cat merged_release_notes.log;
wc -c merged_release_notes.log;
ls -lahp merged_release_notes.log;

echo "Final merge of release notes of all '${PARTS}' parts";
jq -c \
  --arg aiModel ${aiModel} \
  --rawfile agents AGENTS.md \
  --rawfile release_notes merged_release_notes.log \
  '
  def inject(name; value):
    gsub(name; (value | @json));

  walk(
    if type == "string" then
      .
      | gsub("__AI_MODEL__"; $aiModel)
      | inject("__AGENTS__"; $agents)
      | inject("__RELEASE_NOTES__"; $release_notes)
    else
      .
    end
  )
  ' \
  < .github/workflows/release-notes-merge.json \
  > payload.json.log;
SIZE=$(wc -c < payload.json.log);
WORDS=$(wc -w < payload.json.log);

requestAiModel;
RELEASE_NOTES=$(cat response.json.log | jq -r '.choices[0].message.content');
echo "Release notes: ${RELEASE_NOTES}";
echo "${RELEASE_NOTES}" > release_notes.log;
