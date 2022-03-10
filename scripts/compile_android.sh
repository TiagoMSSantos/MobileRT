#!/bin/bash

###############################################################################
# README
###############################################################################
# This script compiles MobileRT for Android, in debug or release mode.
# It also allows to setup the desired NDK and CMake versions.
###############################################################################
###############################################################################


###############################################################################
# Exit immediately if a command exits with a non-zero status.
###############################################################################
set -euo pipefail;
###############################################################################
###############################################################################


###############################################################################
# Change directory to MobileRT root.
###############################################################################
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit;
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
# shellcheck source=scripts/helper_functions.sh
source scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Set default arguments.
###############################################################################
type="release";
recompile="no";
ndk_version="21.3.6528147";
cmake_version="3.10.2";
parallelizeBuild;

function printEnvironment() {
  echo "";
  echo "Selected arguments:";
  echo "type: ${type}";
  echo "recompile: ${recompile}";
  echo "ndk_version: ${ndk_version}";
  echo "cmake_version: ${cmake_version}";
}
###############################################################################
###############################################################################


###############################################################################
# Parse arguments.
###############################################################################
parseArgumentsToCompileAndroid "$@";
printEnvironment;
###############################################################################
###############################################################################


###############################################################################
# Compile for Android.
###############################################################################

# Set path to reports.
reports_path=./app/build/reports;
mkdir -p ${reports_path};

type=$(capitalizeFirstletter "${type}");
echo "type: '${type}'";

function clearAllBuildFiles() {
  set +e;
  rm -rf ./app/build/;
  set -e;

  if [ "${recompile}" == "yes" ]; then
    rm -rf ./app/.cxx/;
    rm -rf ./build/;
  fi
}

function clearOldBuildFiles() {

  files_being_used=$(find . -iname "*.fuse_hidden*" | grep -i ".fuse_hidden" || true);
  echo "files_being_used: '${files_being_used}'";

  if [ "${files_being_used}" != "" ]; then
    while IFS= read -r file; do
      while [[ -f "${file}" ]]; do
        killProcessUsingFile "${file}";
        echo "sleeping 1 sec";
        sleep 1;
      done
    done <<<"${files_being_used}";
  fi

  clearAllBuildFiles;
}

function build() {
  echo "Calling the Gradle assemble to compile code for Android.";
  echo "Increasing ADB timeout to 10 minutes.";
  export ADB_INSTALL_TIMEOUT=60000;
  ./gradlew --stop;
  ./gradlew clean assemble"${type}" --profile --parallel \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    --console plain \
    2>&1 | tee log_build_android_"${type}".log;
  resCompile=${PIPESTATUS[0]};
}
###############################################################################
###############################################################################

# Install C++ Conan dependencies.
function install_conan_dependencies() {
  conan install \
  -s compiler=clang \
  -s compiler.version="9" \
  -s compiler.libcxx=libstdc++11 \
  -s os="Android" \
  -s build_type=Release \
  --build missing \
  --profile default \
  ./app/third_party/conan/Android;

  export CONAN="TRUE";
}

clearOldBuildFiles;
#install_conan_dependencies;
build;
checkLastModifiedFiles;

###############################################################################
# Exit code
###############################################################################
printCommandExitCode "${resCompile}" "Compilation";
###############################################################################
###############################################################################
