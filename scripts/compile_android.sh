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
  cd "$(dirname "${0}")/.." || exit 1;
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
if command -v shellcheck > /dev/null; then
  shellcheck "${0}" || exit 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Set default arguments.
###############################################################################
type='release';
recompile='no';
ndk_version='23.2.8568313';
cmake_version='3.22.1';
cpu_architecture='"x86","x86_64"';
parallelizeBuild;

printEnvironment() {
  echo '';
  echo 'Selected arguments:';
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

clearAllBuildFiles() {
  callCommandUntilSuccess rm -rf build/;
  set +e;
  rm -rf app/build/; # Note that Android Studio might still be using this folder because some process might still be using a ".fuse_hidden<id>" file.
  set -e;

  if [ "${recompile}" = 'yes' ]; then
    callCommandUntilSuccess rm -rf app/.cxx/;
  fi
}

build() {
  build_wrapper="";
  if echo "${type}" | grep -iq "debug" && uname -a | grep -iq "linux"; then
    # Download Build Wrapper from: https://docs.sonarqube.org/latest/analyzing-source-code/languages/c-family/
    # * https://sonarcloud.io/static/cpp/build-wrapper-linux-x86.zip
    # * https://sonarcloud.io/static/cpp/build-wrapper-macosx-x86.zip
    # * https://sonarcloud.io/static/cpp/build-wrapper-win-x86.zip
    # shellcheck disable=SC2034
    build_wrapper="./build-wrapper-linux-x86-64 --out-dir build_wrapper_output_directory";
  fi

  echo 'Calling the Gradle assemble to compile code for Android.';
  echo 'Increasing ADB timeout to 10 minutes.';
  export ADB_INSTALL_TIMEOUT=60000;
  sh gradlew --no-rebuild --stop --info --warning-mode fail --stacktrace;
  sh gradlew clean \
    build"${typeWithCapitalLetter}" \
    assemble"${typeWithCapitalLetter}" \
    assemble"${typeWithCapitalLetter}"AndroidTest \
    bundle"${typeWithCapitalLetter}" \
    bundle"${typeWithCapitalLetter}"ClassesToCompileJar \
    bundle"${typeWithCapitalLetter}"ClassesToRuntimeJar \
    package"${typeWithCapitalLetter}"Bundle \
    -DtestType="${type}" \
    --profile --parallel \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    -DabiFilters="[${cpu_architecture}]" \
    --console plain --info --warning-mode fail --stacktrace;
  resCompile=${?};
  echo 'Compiling APK to execute Android instrumentation tests.';
  sh gradlew createDebugAndroidTestApkListingFileRedirect \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    -DabiFilters="[${cpu_architecture}]" \
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
export GRADLE_OPTS="-Xms4G -Xmx4G -XX:ActiveProcessorCount=3";
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
echo "Generated APK: ${apkPath}";

###############################################################################
# Exit code
###############################################################################
printCommandExitCode "${resCompile}" 'Compilation';
###############################################################################
###############################################################################
