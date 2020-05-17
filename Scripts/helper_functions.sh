#!/bin/bash

###############################################################################
# Helper functions
###############################################################################
function callCommand() {
  echo ""
  echo "Calling '$*'"
  $@
  local lastResult=${PIPESTATUS[0]}
  local lastCommand="$*"
  if [ $lastResult -eq 0 ]; then
    echo "${lastCommand}: success - '${lastResult}'"
  else
    echo "${lastCommand}: failed - '${lastResult}'"
    echo ""
    exit ${lastResult}
  fi
}
###############################################################################
###############################################################################
