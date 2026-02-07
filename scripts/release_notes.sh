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
# Output environment variables:
# * RELEASE_NOTES - Release notes written by AI based on commits information
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
sleepBetweenRequestsSeconds='60';
# To avoid HTTP 413 "Content Too Large" - 32KB is max payload size, 32007 bytes fails, but 32001 bytes seems to work fine
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

    git show -w -U0 --name-only --no-renames \
      --ignore-all-space --ignore-blank-lines --ignore-cr-at-eol --ignore-space-at-eol --ignore-space-change \
      --pretty=format:"%B" "${SHAS[@]}" -- . \
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
      ' > commits.raw;

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
    SIZE=$(wc -c < payload.json.log);

    if (( SIZE > maxPayloadSizeBytes )); then
      unset 'SHAS[-1]'; # Remove last commit to avoid surpassing 32k payload limit

      if (( ${#SHAS[@]} == 0 )); then
        echo "Single commit too large for payload limit. Payload size: ${SIZE} bytes. Offending commit: ${ALL_SHAS[$i]}. Commits: $(wc commits.raw). Payload: $(cat payload.json.log)";
        exit 1;
      fi

      mv payload_previous.json.log payload.json.log; # restore previous valid payload
      SIZE=$(wc -c < payload.json.log);
      break
    fi

    cp payload.json.log payload_previous.json.log; # save previous valid payload
  done

  endTs=$(date +%s);
  elapsed=$(( endTs - startTs ));
  waitSeconds=$(( sleepBetweenRequestsSeconds - elapsed ));
  echo "Payload: $(cat payload.json.log)";
  echo "Processing batch ${BATCH_INDEX} with ${#SHAS[@]} commits (offset ${OFFSET}/${TOTAL} - $(wc -c release_notes.log) - payload size: ${SIZE} bytes - sleep: ${waitSeconds}sec) [$(wc -l commits.raw)]: $(cat commits.raw | head -1)";
  if [ ${waitSeconds} -gt 0 ]; then
    sleep ${waitSeconds};
  fi

  RESPONSE=$(
    curl --fail-with-body -s -S \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${GITHUB_TOKEN}" \
      -d @payload.json.log \
      "https://models.inference.ai.azure.com/openai/deployments/${aiModel}/chat/completions"
  );
  startTs=$(date +%s);

  RELEASE_NOTES=$(echo "${RESPONSE}" | jq -r '.choices[0].message.content');
  echo "${RELEASE_NOTES}" >> release_notes.log;

  awk '$0 ~ /^#/ || !seen[$0]++' release_notes.log > tmp && mv tmp release_notes.log; # Remove duplicate lines

  OFFSET=$((OFFSET + ${#SHAS[@]}));
  BATCH_INDEX=$((BATCH_INDEX + 1));
done

cat release_notes.log;
wc -c release_notes.log;
ls -lahp release_notes.log;

echo 'Merging all release notes';
PARTS=2;
TOTAL_LINES=$(wc -l < release_notes.log);
MIDPOINT=$((TOTAL_LINES / PARTS));
CUT_LINE=$(awk -v mid="$MIDPOINT" 'NR >= mid && /^## / {print NR; exit}' release_notes.log);
# Fallback if no section header exists after midpoint
if [[ -z "$CUT_LINE" ]]; then
  CUT_LINE=$((MIDPOINT + 1));
fi
head -n $((CUT_LINE-1)) release_notes.log > release_notes_part1.log;
tail -n +$CUT_LINE release_notes.log > release_notes_part2.log;

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

  endTs=$(date +%s);
  elapsed=$(( endTs - startTs ));
  waitSeconds=$(( sleepBetweenRequestsSeconds - elapsed ));
  echo "Payload: $(cat payload.json.log)";
  echo "Sleeping for ${waitSeconds} seconds to avoid HTTP 429 Too Many Requests. Payload size: ${SIZE} bytes.";
  if [ ${waitSeconds} -gt 0 ]; then
    sleep ${waitSeconds};
  fi

  RESPONSE=$(
    curl --fail-with-body -s -S \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer ${GITHUB_TOKEN}" \
      -d @payload.json.log \
      "https://models.inference.ai.azure.com/openai/deployments/${aiModel}/chat/completions"
  );
  startTs=$(date +%s);

  RESPONSE=$(echo "${RESPONSE}" | jq -r '.choices[0].message.content');
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

endTs=$(date +%s);
elapsed=$(( endTs - startTs ));
waitSeconds=$(( sleepBetweenRequestsSeconds - elapsed ));
echo "Payload: $(cat payload.json.log)";
echo "Sleeping for ${waitSeconds} seconds to avoid HTTP 429 Too Many Requests. Payload size: ${SIZE} bytes.";
if [ ${waitSeconds} -gt 0 ]; then
  sleep ${waitSeconds};
fi

RESPONSE=$(
  curl --fail-with-body -s -S \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${GITHUB_TOKEN}" \
    -d @payload.json.log \
    "https://models.inference.ai.azure.com/openai/deployments/${aiModel}/chat/completions"
);
export RELEASE_NOTES=$(echo "${RESPONSE}" | jq -r '.choices[0].message.content');
echo "Release notes: ${RELEASE_NOTES}";
echo "${RELEASE_NOTES}" > release_notes.log;
