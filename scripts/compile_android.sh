#!/usr/bin/env sh

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
type='release';
recompile='no';
android_api_version='14';
cpu_architecture='"x86","x86_64"';
parallelizeBuild;

printEnvironment() {
  echo '';
  echo 'Selected arguments:';
  echo "type: ${type}";
  echo "recompile: ${recompile}";
  echo "android_api_version: ${android_api_version}";
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

clearAllBuildFiles() {
  callCommandUntilSuccess 5 rm -rf build;
  # Note that Android Studio might still be using this folder because some process might still be using a ".fuse_hidden<id>" file.
  callCommandUntilSuccess 5 rm -rf app/build;

  if [ "${recompile}" = 'yes' ]; then
    callCommandUntilSuccess 5 rm -rf app/.cxx;
  fi
}

build() {
  build_wrapper="";
  if echo "${type}" | grep -iq 'debug' && uname -a | grep -iq 'linux'; then
    # Download Build Wrapper from: https://docs.sonarqube.org/latest/analyzing-source-code/languages/c-family/
    # * https://sonarcloud.io/static/cpp/build-wrapper-linux-x86.zip
    # * https://sonarcloud.io/static/cpp/build-wrapper-macosx-x86.zip
    # * https://sonarcloud.io/static/cpp/build-wrapper-win-x86.zip
    # shellcheck disable=SC2034
    build_wrapper="./build-wrapper-linux-x86-64 --out-dir build_wrapper_output_directory";
  fi

  echo 'Increasing ADB timeout to 10 minutes.';
  export ADB_INSTALL_TIMEOUT=60000;

  jobsFlags="-j$((NCPU_CORES * 2 - 1))";
  export MAKEFLAGS="${jobsFlags}";
  export CMAKE_BUILD_PARALLEL_LEVEL="$((NCPU_CORES * 2 - 1))";

  echo 'Calling the Gradle assemble to compile code for Android.';
  sh gradlew \
    -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]" \
    --no-rebuild --stop --info --warning-mode fail --stacktrace;
  echo "Setting Gradle Wrapper to a version that is compatible with Android API: '${android_api_version}'".;
  sh gradlew wrapper -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]";
  sh gradlew clean \
    build"${typeWithCapitalLetter}" \
    assemble"${typeWithCapitalLetter}" \
    assemble"${typeWithCapitalLetter}"AndroidTest \
    bundle"${typeWithCapitalLetter}" \
    bundle"${typeWithCapitalLetter}"ClassesToCompileJar \
    bundle"${typeWithCapitalLetter}"ClassesToRuntimeJar \
    package"${typeWithCapitalLetter}"Bundle \
    compile"${typeWithCapitalLetter}"Sources \
    compile"${typeWithCapitalLetter}"UnitTestSources \
    --profile --parallel \
    -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]" \
    --console plain --info --warning-mode fail --stacktrace;
  resCompile=${?};
  echo 'Compiling APK to execute Android instrumentation tests.';
  sh gradlew createDebugAndroidTestApkListingFileRedirect \
    -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]" \
    --profile --parallel --console plain --info --warning-mode fail --stacktrace;
  echo 'Android application compiled.';
}
###############################################################################
###############################################################################

# Install C++ Conan dependencies.
install_conan_dependencies() {
  conan install \
  -s compiler=clang \
  -s compiler.version='9' \
  -s compiler.libcxx=c++_shared \
  -s compiler.cppstd=17 \
  -s os='Android' \
  -s os.api_level='16' \
  -s build_type=Release \
  -o bzip2:shared=True \
  -c tools.android:ndk_path="${ANDROID_NDK_PATH}" \
  --build missing \
  --profile default \
  --install-folder build_conan-android \
  ./app/third_party/conan/Android;

  export CONAN='TRUE';
}

# Increase memory for heap.
export GRADLE_OPTS='-Xms4G -Xmx4G -XX:ActiveProcessorCount=4';
rm -rf app/build/ || true;
clearOldBuildFiles;
clearAllBuildFiles;
#install_conan_dependencies;
createReportsFolders;
build;
validateNativeLibCompiled;
# checkLastModifiedFiles;

echo 'Searching for generated APK';
find . -iname "*.apk" | grep -i "output";
apkPath=$(find . -iname "*.apk" | grep -i "output" | grep -i "test" | grep -i "${type}");
echo "Generated Test APK: ${apkPath}";

###############################################################################
# Exit code
###############################################################################
printCommandExitCode "${resCompile}" 'Compilation';
###############################################################################
###############################################################################
