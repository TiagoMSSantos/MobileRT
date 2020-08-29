#!/bin/bash

###############################################################################
# Change directory to MobileRT root
###############################################################################
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit
###############################################################################
###############################################################################

###############################################################################
# Get arguments
###############################################################################
type="${1:-Release}"
recompile="${2:-no}"
ndk_version="${3:-21.3.6528147}"
cmake_version="${4:-3.10.2}"
###############################################################################
###############################################################################

###############################################################################
# Get helper functions
###############################################################################
source scripts/helper_functions.sh
###############################################################################
###############################################################################

###############################################################################
# Compile for Android
###############################################################################

# Capitalize 1st letter
type="$(tr '[:lower:]' '[:upper:]' <<<"${type:0:1}")${type:1}"
echo "type: '${type}'"

# Set path to reports
reports_path=./app/build/reports
callCommand mkdir -p ${reports_path}

function clearAllBuildFiles() {
  rm -rf ./app/build/

  if [ "${recompile}" == "yes" ]; then
    rm -rf ./app/.cxx/
    rm -rf ./build/
  fi
}

function clearOldBuildFiles() {

  clearAllBuildFiles

  files_being_used=$(find . -name "*.fuse_hidden*" | grep -i ".fuse_hidden")
  echo "files_being_used: '${files_being_used}'"

  if [ "${files_being_used}" != "" ]; then
    while IFS= read -r file; do
      while [[ -f "${file}" ]]; do
        killProcessUsingFile "${file}"
        echo "sleeping 1 sec"
        sleep 1
      done
    done <<<"${files_being_used}"
  fi

  clearAllBuildFiles
}

function build() {
  echo "Calling the Gradle assemble to compile code for Android"
  callCommand ./gradlew clean assemble"${type}" --profile --parallel \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    --console plain \
    2>&1 | tee ${reports_path}/log_build_"${type}".log
  resCompile=${PIPESTATUS[0]}
}
###############################################################################
###############################################################################

clearOldBuildFiles
build

###############################################################################
# Exit code
###############################################################################
printCommandExitCode "${resCompile}" "Compilation"
###############################################################################
###############################################################################
