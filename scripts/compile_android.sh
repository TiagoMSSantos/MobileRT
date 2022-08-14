#!/usr/bin/env bash

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
# shellcheck disable=SC1091
. scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Execute Shellcheck on this script.
###############################################################################
if [ -x "$(command -v shellcheck)" ]; then
  shellcheck "${0}" || exit
fi
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
typeWithCapitalLetter=$(capitalizeFirstletter "${type}");
###############################################################################
###############################################################################


###############################################################################
# Compile for Android.
###############################################################################

# Set path to reports.
echo "type: '${type}'";

function clearAllBuildFiles() {
  callCommandUntilSuccess rm -rf ./app/build/;

  if [ "${recompile}" == "yes" ]; then
    callCommandUntilSuccess rm -rf ./app/.cxx/;
    callCommandUntilSuccess rm -rf ./build/;
  fi
}

function build() {
  echo "Calling the Gradle assemble to compile code for Android.";
  echo "Increasing ADB timeout to 10 minutes.";
  export ADB_INSTALL_TIMEOUT=60000;
  bash gradlew --stop;
  bash gradlew clean assembleAndroidTest bundle"${typeWithCapitalLetter}" \
    -DtestType="${type}" \
    --profile --parallel \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    --console plain;
  resCompile=${PIPESTATUS[0]};
  echo "Android application compiled.";
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

set +e;
rm -rf ./app/build/;
set -e;
clearOldBuildFiles;
clearAllBuildFiles;
#install_conan_dependencies;
createReportsFolders;
build;
checkLastModifiedFiles;
validateNativeLibCompiled;

###############################################################################
# Exit code
###############################################################################
printCommandExitCode "${resCompile}" "Compilation";
###############################################################################
###############################################################################
