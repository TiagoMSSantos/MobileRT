#!/usr/bin/env bash

###############################################################################
# README
###############################################################################
# This script contains a bunch of helper functions for the bash scripts.
###############################################################################
###############################################################################


###############################################################################
# Execute Shellcheck on this script.
###############################################################################
if [ -x "$(command -v shellcheck)" ]; then
  shellcheck "${0}";
fi
###############################################################################
###############################################################################

# Helper command for compilation scripts.
function helpCompile() {
  echo "Usage: cmd [-h] [-t type] [-c compiler] [-r recompile]";
  exit 0;
}

# Helper command for Android compilation scripts.
function helpCompileAndroid() {
  echo "Usage: cmd [-h] [-t type] [-c compiler] [-r recompile] [-n ndk_version] [-m cmake_version]";
  exit 0;
}

# Helper command for Android run tests scripts.
function helpTestAndroid() {
  echo "Usage: cmd [-h] [-t type] [-r run_test] [-n ndk_version] [-m cmake_version] [-k kill_previous]";
  exit 0;
}

# Helper command for compilation scripts.
function helpCheck() {
  echo "Usage: cmd [-h] [-n ndk_version] [-m cmake_version]";
  exit 0;
}

# Argument parser for compilation scripts.
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

# Argument parser for Android compilation scripts.
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

# Argument parser for Android run tests scripts.
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

# Argument parser for linter scripts.
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
  echo "";
  echo "Calling until error '$*'";
  local retry=0;
  "$@";
  local lastResult=${PIPESTATUS[0]};
  while [[ "${lastResult}" -eq 0 && retry -lt 5 ]]; do
    retry=$(( retry + 1 ));
    "$@";
    lastResult=${PIPESTATUS[0]};
    echo "Retry: ${retry} of command '$*'; result: '${lastResult}'";
    sleep 5;
  done
  if [ "${lastResult}" -eq 0 ]; then
    echo "$*: success - '${lastResult}'";
  else
    echo "$*: failed - '${lastResult}'";
    echo "";
    exit "${lastResult}";
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
  while [[ "${lastResult}" -ne 0 && retry -lt 20 ]]; do
    retry=$(( retry + 1 ));
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

# Call an ADB shell function multiple times until it doesn't fail and then return.
function callAdbShellCommandUntilSuccess() {
  echo "";
  echo "Calling ADB shell command until success '$*'";
  local retry=0;
  local output;
  output=$("$@");
  # echo "Output of command: '${output}'";
  local lastResult;
  lastResult=$(echo "${output}" | grep '::.*::' | sed 's/:://g'| tr -d '[:space:]');
  echo "result: '${lastResult}'";
  while [[ "${lastResult}" -ne 0 && retry -lt 10 ]]; do
    retry=$(( retry + 1 ));
    output=$("$@");
    echo "Output of command: '${output}'";
    lastResult=$(echo "${output}" | grep '::.*::' | sed 's/:://g' | tr -d '[:space:]');
    echo "Retry: ${retry} of command '$*'; result: '${lastResult}'";
    sleep 3;
  done
  if [ "${lastResult}" -eq 0 ]; then
    echo "'$*': success";
  else
    echo "'$*': failed";
    exit "${lastResult}";
  fi
}

# Outputs the exit code received by argument and exits the current process with
# that exit code.
function printCommandExitCode() {
  echo "######################################################################";
  echo "Results:";
  if [ "${1}" -eq 0 ]; then
    echo "${2}: success";
  else
    echo "${2}: failed";
    exit "${1}";
  fi
}

# Check command is available.
function checkCommand() {
  if [ -x "$(command -v "${@}")" ]; then
    echo "Command '$*' installed!";
  else
    echo "Command '$*' is NOT installed.";
    if [[ $(uname -a) == *"MINGW"* ]]; then
      echo "Detected Windows OS, so ignoring this error ...";
      return 0;
    fi
    exit 1;
  fi
}

# Capitalize 1st letter.
function capitalizeFirstletter() {
  local res;
  res="$(tr '[:lower:]' '[:upper:]' <<<"${1:0:1}")${1:1}";
  echo "${res}";
}

# Parallelize building of MobileRT.
function parallelizeBuild() {
  if [ -x "$(command -v nproc)" ]; then
    MAKEFLAGS=-j$(nproc --all);
  else
    # Assuming MacOS.
    MAKEFLAGS=-j$(sysctl -n hw.logicalcpu);
  fi
  export MAKEFLAGS;
}

# Check the files that were modified in the last few minutes.
function checkLastModifiedFiles() {
  local MINUTES;
  MINUTES=15;
  set +e;
  echo "#####################################################################";
  echo "Files modified in home:";
  find ~/ -type f -mmin -${MINUTES} -print 2> /dev/null | grep -v "mozilla" | grep -v "thunderbird" | grep -v "java";
  echo "#####################################################################";
  echo "Files modified in workspace:";
  find . -type f -mmin -${MINUTES} -print 2> /dev/null;
  echo "#####################################################################";
  set -e;
}

# Check if a path exists.
# Parameters:
# * path that should exist
# * file that should also exist in the provided path
function checkPathExists() {
  du -h -d 1 "${1}";
  if [[ $# -eq 1 ]] ; then
    return 0;
  fi
  ls -lah "${1}"/"${2}";
}

# Change the mode of all binaries/scripts to be able to be executed.
function prepareBinaries() {
  local rootDir;
  rootDir=$(dirname "${BASH_SOURCE[0]}")/..;
  chmod +x "${rootDir}"/test-reporter-latest-linux-amd64;
  chmod +x "${rootDir}"/test-reporter-latest-darwin-amd64;
}

# Helper command to execute a command / function without exiting the script (without the set -e).
function executeWithoutExiting () {
  set +e;
  "$@";
  set -e;
}

# Private method which kills a process that is using a file.
function _killProcessUsingFile() {
  local processes_using_file;
  processes_using_file=$(lsof "${1}" | tail -n +2 | tr -s ' ');
  local retry=0;
  while [[ "${processes_using_file}" != "" && retry -lt 5 ]]; do
    retry=$(( retry + 1 ));
    echo "processes_using_file: '${processes_using_file}'";
    local process_id_using_file;
    process_id_using_file=$(echo "${processes_using_file}" | cut -d ' ' -f 2 | head -1);
    echo "Going to kill this process: '${process_id_using_file}'";
    set +e;
    kill -SIGKILL "${process_id_using_file}";
    processes_using_file=$(lsof "${1}" | tail -n +2 | tr -s ' ');
    set -e;
  done
}

# Delete all old build files (commonly called ".fuse_hidden<id>") that might not be able to be
# deleted due to some process still using it. So this method detects which process uses them and
# kills it first.
function clearOldBuildFiles() {
  files_being_used=$(find . -iname "*.fuse_hidden*" || true);
  local retry=0;
  while [[ "${files_being_used}" != "" && retry -lt 5 ]]; do
    retry=$(( retry + 1 ));
    echo "files_being_used: '${files_being_used}'";
    while IFS= read -r file; do
      while [[ -f "${file}" ]]; do
        _killProcessUsingFile "${file}";
        echo "sleeping 2 secs";
        sleep 2;
        set +e;
        rm "${file}";
        set -e;
      done
    done <<<"${files_being_used}";
    files_being_used=$(find . -iname "*.fuse_hidden*" | grep -i ".fuse_hidden" || true);
  done
}

# Create the reports' folders.
function createReportsFolders() {
  echo "Creating reports folders.";
  mkdir -p build/reports;
  mkdir -p app/build/reports;
  echo "Created reports folders.";
}

# Validate MobileRT native lib was compiled.
function validateNativeLibCompiled() {
  local nativeLib;
  nativeLib=$(find . -iname "*mobilert*.so");
  find . -iname "*.so" 2> /dev/null;
  echo "nativeLib: ${nativeLib}";
  if [ "$(echo "${nativeLib}" | wc -l)" -eq 0 ]; then
    exit 1;
  fi
}

# Extract and check files from downloaded artifact.
# Parameters:
# * path of zip file (of the artifact) to be extracted
# * name of zip file to be extracted
function extractFilesFromArtifact() {
  du -h -d 1 "${1}"/..;
  du -h -d 1 "${1}";
  unzip -o "${1}"/"${2}" -d "${1}";
  rm "${1}"/"${2}";
  du -h -d 1 "${1}"/..;
  du -h -d 1 "${1}";
}

# Generate code coverage.
function generateCodeCoverage() {
  lcov -c -d . --no-external -o code_coverage_test.info;
  lcov -a code_coverage_base.info -a code_coverage_test.info -o code_coverage.info;
  lcov --remove code_coverage.info -o code_coverage.info '*third_party*' '*build*';
  genhtml code_coverage.info -o code_coverage_report --no-branch-coverage -t MobileRT_code_coverage;
  _validateCodeCoverage;
}

# Validate generated files for code coverage.
function _validateCodeCoverage() {
  ls -lah code_coverage_base.info;
  ls -lah code_coverage_test.info;
  ls -lah code_coverage.info;
}

###############################################################################
###############################################################################
