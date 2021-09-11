#!/bin/bash

###############################################################################
# README
###############################################################################
# This script contains a bunch of helper functions for the bash scripts.
###############################################################################
###############################################################################

# Helper command for compilation scripts
function helpCompile() {
  echo "Usage: cmd [-h] [-t type] [-c compiler] [-r recompile]";
  exit 0;
}

# Helper command for Android compilation scripts
function helpCompileAndroid() {
  echo "Usage: cmd [-h] [-t type] [-c compiler] [-r recompile] [-n ndk_version] [-m cmake_version]";
  exit 0;
}

# Helper command for Android run tests scripts
function helpTestAndroid() {
  echo "Usage: cmd [-h] [-t type] [-r run_test] [-n ndk_version] [-m cmake_version] [-k kill_previous]";
  exit 0;
}

# Helper command for compilation scripts
function helpCheck() {
  echo "Usage: cmd [-h] [-n ndk_version] [-m cmake_version]";
  exit 0;
}

# Argument parser for compilation scripts
function parseArgumentsToCompile() {
  while getopts ":ht:c:r:" opt; do
    case ${opt} in
      t )
        export type=${OPTARG};
        ;;
      c )
        export compiler=${OPTARG};
        checkCommand "${compiler}";
        ;;
      r )
        export recompile=${OPTARG};
        ;;
      h )
        helpCompile;
        ;;
      \? )
        helpCompile;
        ;;
    esac
  done
}

# Argument parser for Android compilation scripts
function parseArgumentsToCompileAndroid() {
  while getopts ":ht:c:r:n:m:" opt; do
    case ${opt} in
      n )
        export ndk_version=${OPTARG};
        ;;
      m )
        export cmake_version=${OPTARG};
        ;;
      t )
        export type=${OPTARG};
        ;;
      c )
        export compiler=${OPTARG};
        checkCommand "${compiler}";
        ;;
      r )
        export recompile=${OPTARG};
        ;;
      h )
        helpCompileAndroid;
        ;;
      \? )
        helpCompileAndroid;
        ;;
    esac
  done
}

# Argument parser for Android run tests scripts
function parseArgumentsToTestAndroid() {
  while getopts ":ht:r:k:n:m:" opt; do
    case ${opt} in
      n )
        export ndk_version=${OPTARG};
        ;;
      m )
        export cmake_version=${OPTARG};
        ;;
      t )
        export type=${OPTARG};
        ;;
      r )
        export run_test=${OPTARG};
        ;;
      k )
        export kill_previous=${OPTARG};
        ;;
      h )
        helpTestAndroid;
        ;;
      \? )
        helpTestAndroid;
        ;;
    esac
  done
}

# Argument parser for linter scripts
function parseArgumentsToCheck() {
  while getopts ":hm:n:" opt; do
    case ${opt} in
      n )
        export ndk_version=${OPTARG};
        ;;
      m )
        export cmake_version=${OPTARG};
        ;;
      h )
        helpCheck;
        ;;
      \? )
        helpCheck;
        ;;
    esac
  done
}

# Call function multiple times until it fails and exit the process.
function callCommandUntilError() {
  echo ""
  echo "Calling until error '$*'"
  local retry=0
  "$@"
  local lastResult=${PIPESTATUS[0]}
  while [[ "${lastResult}" -eq 0 && retry -lt 5 ]]; do
    retry=$((${retry} + 1))
    "$@"
    lastResult=${PIPESTATUS[0]}
    echo "Retry: ${retry} of command '$*'; result: '${lastResult}'"
    sleep 5
  done
  if [ "${lastResult}" -eq 0 ]; then
    echo "$*: success - '${lastResult}'"
  else
    echo "$*: failed - '${lastResult}'"
    echo ""
    exit "${lastResult}"
  fi
}

# Call function multiple times until it doesn't fail and then return.
function callCommandUntilSuccess() {
  echo "";
  echo "Calling until success '$*'";
  local retry=0;
  set +e;
  "$@";
  local lastResult=${PIPESTATUS[0]};
  echo "result: '${lastResult}'";
  while [[ "${lastResult}" -ne 0 && retry -lt 10 ]]; do
    retry=$(("${retry}" + 1));
    "$@";
    lastResult=${PIPESTATUS[0]};
    echo "Retry: ${retry} of command '$*'; result: '${lastResult}'";
    sleep 3;
  done
  set -e;
  if [ "${lastResult}" -eq 0 ]; then
    echo "'$*': success";
  else
    echo "'$*': failed";
    exit "${lastResult}";
  fi
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
  local processes_using_file
  processes_using_file=$(lsof "${1}" | tail -n +2 | tr -s ' ')
  echo "processes_using_file: '${processes_using_file}'"
  local process_id_using_file
  process_id_using_file=$(echo "${processes_using_file}" | cut -d ' ' -f 2 | head -1)
  echo "Going to kill this process: '${process_id_using_file}'"
  kill -9 "${process_id_using_file}"
}

# Check command is available
function checkCommand() {
  if [ -x "$(command -v ${@})" ]; then
    echo "Command '$*' installed!";
  else
    echo "Command '$*' is NOT installed.";
    if [[ $(uname -a) == *"MINGW"* ]]; then
      echo "Detected Windows OS, so ignoring this error ...";
      exit 0;
    fi
    exit 1;
  fi
}

# Capitalize 1st letter
function capitalizeFirstletter() {
  local res
  res="$(tr '[:lower:]' '[:upper:]' <<<"${1:0:1}")${1:1}"
  echo "${res}"
}

# Parallelize building of MobileRT
function parallelizeBuild() {
  if [ -x "$(command -v nproc)" ]; then
    MAKEFLAGS=-j$(nproc --all);
  else
    # Assuming MacOS
    MAKEFLAGS=-j$(sysctl -n hw.logicalcpu);
  fi
  export MAKEFLAGS;
}
###############################################################################
###############################################################################
