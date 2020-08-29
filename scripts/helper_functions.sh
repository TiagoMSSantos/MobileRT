#!/bin/bash

###############################################################################
# Helper functions
###############################################################################

# Call function and exit the process if it fails
function callCommand() {
  echo ""
  echo "Calling '$*'"
  "$@"
  local lastResult=${PIPESTATUS[0]}
  local lastCommand="$*"
  if [ "${lastResult}" -eq 0 ]; then
    echo "${lastCommand}: success - '${lastResult}'"
  else
    echo "${lastCommand}: failed - '${lastResult}'"
    echo ""
    exit "${lastResult}"
  fi
}

# Call function multiple times until it fails and exit the process
function callCommandUntilError() {
  echo ""
  echo "Calling until error '$*'"
  local retry=0
  "$@"
  local lastResult=${PIPESTATUS[0]}
  while [[ "${lastResult}" -eq 0 ]]; do
    echo "Retry: ${retry}"
    retry=$(("${retry}" + 1))
    "$@"
    lastResult=${PIPESTATUS[0]}
  done
  exit "${lastResult}"
}

# Call function multiple times until it doesn't fail and then return
function callCommandUntilSuccess() {
  echo ""
  echo "Calling until success '$*'"
  local retry=0
  local lastOutput=$("$@")
  local lastResult=${PIPESTATUS[0]}
  echo "result: '${lastResult}'"
  while [[ "${lastResult}" -ne 0 || ${lastOutput} == *"Can't find"* ]]; do
    echo "Retry: ${retry}"
    retry=$(("${retry}" + 1))
    lastOutput=$("$@")
    lastResult=${PIPESTATUS[0]}
    echo "result: '${lastResult}'"
    sleep 1
  done
}

# Outputs the exit code received by argument and exits the current process with
# that exit code
function printCommandExitCode() {
  echo "######################################################################"
  echo "Results:"
  if [ "${1}" -eq 0 ]; then
    echo "${2}: success"
  else
    echo "${2}: failed"
    exit "${1}"
  fi
}

# Kill a process that is using a file
function killProcessUsingFile() {
  local processes_using_file=$(lsof "${1}" | tail -n +2 | tr -s ' ')
  echo "processes_using_file: '${processes_using_file}'"
  local process_id_using_file=$(echo "${processes_using_file}" | cut -d ' ' -f 2 | head -1)
  echo "Going to kill this process: '${process_id_using_file}'"
  kill -9 "${process_id_using_file}"
}
###############################################################################
###############################################################################
