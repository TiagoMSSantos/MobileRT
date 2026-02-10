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
set -eu;
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
  PAGER= LESS= git --no-pager show -w -U0 --no-renames --name-only \
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
    | awk 'NF' | awk '
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
    ' | tr -d '\000-\010\013\014\016-\037\177' | sed -r 's/\[[0-9;]*[A-Za-z]//g' | sed -r 's/[[:space:]]+/ /g' > commits.raw;

  if [ ${limit} = true ]; then
    head -c 22000 commits.raw > commits_limited.raw;
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
    > payload.json.log;
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
  mapfile -t ALL_SHAS < <(grep -v '^$' shas.log)
  TOTAL=${#ALL_SHAS[@]};

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

  endTs=$(date +%s);
  elapsed=$(( endTs - startTs ));
  waitSeconds=$(( sleepBetweenRequestsSeconds - elapsed ));
  # echo "Payload: $(cat payload.json.log)";
  echo "Processing batch ${BATCH_INDEX} with ${#SHAS[@]} commits (offset ${OFFSET}/${TOTAL} - $(wc -c release_notes.log) - Payload size: ${SIZE} bytes | ${WORDS} words - sleep: ${waitSeconds}sec) [$(wc -c commits.raw)]: $(head -1 commits.raw)";
  if [ ${waitSeconds} -gt 0 ]; then
    sleep ${waitSeconds};
  fi

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
  if [[ "${RESPONSE}" -lt 100 || "${RESPONSE}" -ge 400 || "${CURL_EXIT}" -ne 0 ]]; then
    echo 'Payload:';
    cat payload.json.log;
    echo 'Response:';
    cat response.json.log;
    echo "HTTP error: ${RESPONSE}. Curl exit code: ${CURL_EXIT}";
    exit 1;
  fi

  RELEASE_NOTES=$(cat response.json.log | jq -r '.choices[0].message.content');
  echo "${RELEASE_NOTES}" >> release_notes.log;

  awk '$0 ~ /^#/ || !seen[$0]++' release_notes.log > tmp && mv tmp release_notes.log; # Remove duplicate lines

  OFFSET=$((OFFSET + ${#SHAS[@]}));
  BATCH_INDEX=$((BATCH_INDEX + 1));
done

cat release_notes.log;
wc -c release_notes.log;
ls -lahp release_notes.log;

echo 'Merging all release notes';
PARTS=3;
TOTAL_LINES=$(wc -l < release_notes.log);
MIDPOINT=$((TOTAL_LINES / PARTS));

# First cut: first "## " after 1/3 of the file
CUT1=$(awk -v mid="$MIDPOINT" 'NR >= mid && /^## / {print NR; exit}' release_notes.log);
[[ -z "$CUT1" ]] && CUT1=$((MIDPOINT + 1));

# Second cut: first "## " after CUT1
CUT2=$(awk -v start="$CUT1" 'NR > start && /^## / {print NR; exit}' release_notes.log);
[[ -z "$CUT2" ]] && CUT2=$((CUT1 + 1));

# Create the three parts
head -n $((CUT1 - 1)) release_notes.log > release_notes_part1.log;
sed -n "${CUT1},$((CUT2 - 1))p" release_notes.log > release_notes_part2.log;
tail -n +$CUT2 release_notes.log > release_notes_part3.log;

for ((part=1; part<=PARTS; part++)); do
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

  endTs=$(date +%s);
  elapsed=$(( endTs - startTs ));
  waitSeconds=$(( sleepBetweenRequestsSeconds - elapsed ));
  # echo "Payload: $(cat payload.json.log)";
  echo "Sleeping for ${waitSeconds} seconds to avoid HTTP 429 Too Many Requests. Payload size: ${SIZE} bytes | ${WORDS} words.";
  if [ ${waitSeconds} -gt 0 ]; then
    sleep ${waitSeconds};
  fi

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
  if [[ "${RESPONSE}" -lt 100 || "${RESPONSE}" -ge 400 || "${CURL_EXIT}" -ne 0 ]]; then
    echo 'Payload:';
    cat payload.json.log;
    echo 'Response:';
    cat response.json.log;
    echo "HTTP error: ${RESPONSE}. Curl exit code: ${CURL_EXIT}";
    exit 1;
  fi

  RESPONSE=$(cat response.json.log | jq -r '.choices[0].message.content');
  echo "Response: ${RESPONSE}";
  echo "${RESPONSE}" >> merged_release_notes.log;
done

cat merged_release_notes.log;
wc -c merged_release_notes.log;
ls -lahp merged_release_notes.log;

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

endTs=$(date +%s);
elapsed=$(( endTs - startTs ));
waitSeconds=$(( sleepBetweenRequestsSeconds - elapsed ));
# echo "Payload: $(cat payload.json.log)";
echo "Sleeping for ${waitSeconds} seconds to avoid HTTP 429 Too Many Requests. Payload size: ${SIZE} bytes | ${WORDS} words.";
if [ ${waitSeconds} -gt 0 ]; then
  sleep ${waitSeconds};
fi

RESPONSE=$(
  curl -w "%{http_code}" -o response.json.log -s -S \
    --retry 3 --retry-delay ${sleepBetweenRequestsSeconds} --retry-all-errors --retry-max-time 120 --connect-timeout 20 --max-time 180 \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    -d @payload.json.log \
    "https://models.inference.ai.azure.com/openai/deployments/${aiModel}/chat/completions"
);
CURL_EXIT=$?;
if [[ "${RESPONSE}" -lt 100 || "${RESPONSE}" -ge 400 || "${CURL_EXIT}" -ne 0 ]]; then
  echo 'Payload:';
  cat payload.json.log;
  echo 'Response:';
  cat response.json.log;
  echo "HTTP error: ${RESPONSE}. Curl exit code: ${CURL_EXIT}";
  exit 1;
fi

RELEASE_NOTES=$(cat response.json.log | jq -r '.choices[0].message.content');
echo "Release notes: ${RELEASE_NOTES}";
echo "${RELEASE_NOTES}" > release_notes.log;
