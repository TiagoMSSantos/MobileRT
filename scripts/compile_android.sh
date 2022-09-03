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
ndk_version="23.2.8568313";
cmake_version="3.22.1";
cpu_architecture="\"x86\"";
parallelizeBuild;

function printEnvironment() {
  echo "";
  echo "Selected arguments:";
  echo "type: ${type}";
  echo "recompile: ${recompile}";
  echo "ndk_version: ${ndk_version}";
  echo "cmake_version: ${cmake_version}";
  echo "cpu_architecture: ${cpu_architecture}";
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
  callCommandUntilSuccess rm -rf app/build/;

  if [ "${recompile}" == "yes" ]; then
    callCommandUntilSuccess rm -rf app/.cxx/;
    callCommandUntilSuccess rm -rf build/;
  fi
}

function build() {
  echo "Calling the Gradle assemble to compile code for Android.";
  echo "Increasing ADB timeout to 10 minutes.";
  export ADB_INSTALL_TIMEOUT=60000;
  bash gradlew --no-rebuild --stop;
  bash gradlew clean \
    build"${typeWithCapitalLetter}" \
    assemble"${typeWithCapitalLetter}" \
    assemble"${typeWithCapitalLetter}"AndroidTest \
    bundle"${typeWithCapitalLetter}" \
    bundle"${typeWithCapitalLetter}"ClassesToCompileJar \
    bundle"${typeWithCapitalLetter}"ClassesToRuntimeJar \
    bundle"${typeWithCapitalLetter}"AndroidTestClassesToCompileJar \
    bundle"${typeWithCapitalLetter}"AndroidTestClassesToRuntimeJar \
    package"${typeWithCapitalLetter}"Bundle \
    -DtestType="${type}" \
    --profile --parallel \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    -DabiFilters="[${cpu_architecture}]" \
    --console plain;
  resCompile=${PIPESTATUS[0]};
  echo "Compiling APK to execute Android instrumentation tests.";
  bash gradlew createDebugAndroidTestApkListingFileRedirect \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    -DabiFilters="[${cpu_architecture}]" \
    --profile --parallel --console plain;
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
rm -rf app/build/;
set -e;
clearOldBuildFiles;
clearAllBuildFiles;
#install_conan_dependencies;
createReportsFolders;
build;
checkLastModifiedFiles;
validateNativeLibCompiled;

echo "Searching for generated APK";
find . -iname "*.apk" | grep -i "output";
apkPath=$(find . -iname "*.apk" | grep -i "output" | grep -i "test" | grep -i "${type}");
echo "Generated APK: ${apkPath}";

###############################################################################
# Exit code
###############################################################################
printCommandExitCode "${resCompile}" "Compilation";
###############################################################################
###############################################################################
