#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# This script contains a bunch of helper functions for the shell scripts.
###############################################################################
###############################################################################

# Helper command for compilation scripts.
helpCompile() {
  echo 'Usage: cmd [-h] [-t type] [-c compiler] [-r recompile]';
  exit 0;
}

# Helper command for Android compilation scripts.
helpCompileAndroid() {
  echo 'Usage: cmd [-h] [-t type] [-f cpu_architecture] [-c compiler] [-r recompile] [-n ndk_version] [-m cmake_version]';
  exit 0;
}

# Helper command for Android run tests scripts.
helpTestAndroid() {
  echo 'Usage: cmd [-h] [-t type] [-f cpu_architecture] [-r run_test] [-n ndk_version] [-m cmake_version] [-k kill_previous]';
  exit 0;
}

# Helper command for compilation scripts.
helpCheck() {
  echo 'Usage: cmd [-h] [-f cpu_architecture] [-n ndk_version] [-m cmake_version]';
  exit 0;
}

# Argument parser for compilation scripts.
parseArgumentsToCompile() {
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
parseArgumentsToCompileAndroid() {
  while getopts ":ht:c:r:n:m:f:" opt; do
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
      f )
        export cpu_architecture=${OPTARG};
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
parseArgumentsToTestAndroid() {
  while getopts ":ht:r:k:n:m:f:" opt; do
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
      f )
        export cpu_architecture=${OPTARG};
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
parseArgumentsToCheck() {
  while getopts ":hm:n:f:" opt; do
    case ${opt} in
      n )
        export ndk_version=${OPTARG};
        ;;
      m )
        export cmake_version=${OPTARG};
        ;;
      f )
        export cpu_architecture=${OPTARG};
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
callCommandUntilError() {
  echo '';
  echo "Calling until error '$*'";
  retry=0;
  set +e;
  "$@";
  lastResult=${?};
  while [ "${lastResult}" -eq 0 ] && [ ${retry} -lt 5 ]; do
    retry=$(( retry + 1 ));
    "$@";
    lastResult=${?};
    echo "Retry: ${retry} of command '$*'; result: '${lastResult}'";
    sleep 2;
  done
  set -e;
  if [ "${lastResult}" -eq 0 ]; then
    echo "$*: success - '${lastResult}'";
  else
    echo "$*: failed - '${lastResult}'";
    echo '';
    exit "${lastResult}";
  fi
}

# Call function multiple times until it doesn't fail and then return.
callCommandUntilSuccess() {
  echo '';
  echo "Calling until success '$*'";
  retry=0;
  set +e;
  "$@";
  lastResult=${?};
  echo "result: '${lastResult}'";
  while [ "${lastResult}" -ne 0 ] && [ ${retry} -lt 10 ]; do
    retry=$(( retry + 1 ));
    "$@";
    lastResult=${?};
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
callAdbShellCommandUntilSuccess() {
  echo '';
  retry=0;
  set +e;
  echo "Calling ADB shell command until success '$*'";
  output=$("$@");
  echo "Output of command: '${output}'";
  lastResult=$(echo "${output}" | grep '::.*::' | sed 's/:://g'| tr -d '[:space:]');
  echo "result: '${lastResult}'";
  while [ "${lastResult}" != '0' ] && [ ${retry} -lt 60 ]; do
    retry=$(( retry + 1 ));
    output=$("$@");
    echo "Output of command: '${output}'";
    lastResult=$(echo "${output}" | grep '::.*::' | sed 's/:://g' | tr -d '[:space:]');
    echo "Retry: ${retry} of command '$*'; result: '${lastResult}'";
    sleep 3;
  done
  set -e;
  if [ "${lastResult}" = '0' ]; then
    echo "'$*': success";
  else
    echo "'$*': failed";
    exit "${lastResult}";
  fi
}

# Outputs the exit code received by argument and exits the current process with
# that exit code.
# Parameters:
# * Error code
# * Text to be printed
printCommandExitCode() {
  echo '#####################################################################';
  echo 'Results:';
  if [ "${1}" = '0' ]; then
    echo "${2}: success (${1})";
  else
    echo "${2}: failed (${1})";
    exit "${1}";
  fi
}

# Check command is available.
checkCommand() {
  if command -v "${@}" > /dev/null; then
    echo "Command '$*' installed!";
  else
    echo "Command '$*' is NOT installed.";
    if (uname -a | grep -iq "MINGW.*"); then
      echo 'Detected Windows OS, so ignoring this error ...';
      return 0;
    fi
    exit 1;
  fi
}

# Capitalize 1st letter.
capitalizeFirstletter() {
  res="$(echo "${1}" | cut -c 1 | tr '[:lower:]' '[:upper:]')$(echo "${1}" | cut -c 2-)";
  echo "${res}";
}

# Parallelize building of MobileRT.
parallelizeBuild() {
  if command -v nproc > /dev/null; then
    MAKEFLAGS="-j$(nproc --all)";
  else
    # Assuming MacOS.
    MAKEFLAGS="-j$(sysctl -n hw.logicalcpu)";
  fi
  export MAKEFLAGS;
}

# Check the files that were modified in the last few minutes.
checkLastModifiedFiles() {
  MINUTES=15;
  echo '#####################################################################';
  echo 'Files modified in home:';
  find ~/ -type f -mmin -${MINUTES} -print 2> /dev/null | grep -v "mozilla" | grep -v "thunderbird" | grep -v "java" || true;
  echo '#####################################################################';
  echo 'Files modified in workspace:';
  find . -type f -mmin -${MINUTES} -print 2> /dev/null || true;
  echo '#####################################################################';
}

# Check if a path exists.
# Parameters:
# * path that should exist
# * file that should also exist in the provided path
checkPathExists() {
  du -h -d 1 "${1}";
  if [ $# -eq 1 ] ; then
    return 0;
  fi
  ls -lahp "${1}"/"${2}";
}

# Change the mode of all binaries/scripts to be able to be executed.
# Parameters:
# * Optional - path to MobileRT
prepareBinaries() {
  rootDir="${1:-${PWD}}";
  chmod +x "${rootDir}"/test-reporter-latest-linux-amd64;
  chmod +x "${rootDir}"/test-reporter-latest-darwin-amd64;
}

# Helper command to execute a command / function without exiting the script (without the set -e).
executeWithoutExiting () {
  "$@" || true;
}

# Private method which kills a process that is using a file.
_killProcessUsingFile() {
  processes_using_file=$(lsof "${1}" | tail -n +2 | tr -s ' ');
  retry=0;
  while [ "${processes_using_file}" != '' ] && [ ${retry} -lt 5 ]; do
    retry=$(( retry + 1 ));
    echo "processes_using_file: '${processes_using_file}'";
    process_id_using_file=$(echo "${processes_using_file}" | cut -d ' ' -f 2 | head -1);
    if ps aux "${process_id_using_file}" | grep -iq "android-studio"; then
      echo "Not killing process: '${process_id_using_file}' because it is the Android Studio";
      return;
    else
      echo "Going to kill this process: '${process_id_using_file}'";
      kill -KILL "${process_id_using_file}" || true;
    fi
    processes_using_file=$(lsof "${1}" | tail -n +2 | tr -s ' ' || true);
  done
}

# Method which kills the processes that are using a port.
killProcessesUsingPort() {
  processes_using_port=$(lsof -i ":${1}" | tail -n +2 | tr -s ' ' | cut -d ' ' -f 2);
  retry=0;
  while [ "${processes_using_port}" != '' ] && [ ${retry} -lt 5 ]; do
    retry=$(( retry + 1 ));
    echo "processes_using_port: '${processes_using_port}'";
    process_id_using_port=$(echo "${processes_using_port}" | head -1);
    echo "Going to kill this process: '${process_id_using_port}'";
    kill -KILL "${process_id_using_port}" || true;
    processes_using_port=$(lsof -i ":${1}" | tail -n +2 | tr -s ' ' | cut -d ' ' -f 2 || true);
  done
}

# Delete all old build files (commonly called ".fuse_hidden<id>") that might not be able to be
# deleted due to some process still using it. So this method detects which process uses them and
# kills it first.
clearOldBuildFiles() {
  files_being_used=$(find . -iname "*.fuse_hidden*" || true);
  retry=0;
  while [ "${files_being_used}" != '' ] && [ ${retry} -lt 3 ]; do
    retry=$(( retry + 1 ));
    echo "files_being_used: '${files_being_used}'";
    for file_being_used in ${files_being_used}; do
      echo "file_being_used: '${file_being_used}'";
      retry_file=0;
      while [ -f "${file_being_used}" ] && [ ${retry_file} -lt 2 ]; do
        retry_file=$(( retry_file + 1 ));
        _killProcessUsingFile "${file_being_used}";
        echo 'sleeping 1 sec';
        sleep 1;
        rm "${file_being_used}" || true;
      done
    done;
    files_being_used=$(find . -iname "*.fuse_hidden*" | grep -i ".fuse_hidden" || true);
  done
}

# Create the reports' folders.
# Also delete any logs previously created.
createReportsFolders() {
  echo 'Creating reports folders.';
  rm -rf build/reports;
  rm -rf app/build/reports;
  mkdir -p build/reports;
  mkdir -p app/build/reports;
  echo 'Created reports folders.';
}

# Validate MobileRT native lib was compiled.
validateNativeLibCompiled() {
  nativeLib=$(find . -iname "*mobilert*.so");
  find . -iname "*.so" 2> /dev/null;
  echo "nativeLib: ${nativeLib}";
  if [ "$(echo "${nativeLib}" | wc -l)" -eq 0 ]; then
    exit 1;
  fi
}

# Extract and check files from downloaded artifact.
# This functions expects to receive a path where a zip file is stored, in order
# to extract it there.
# Parameters:
# * path where a zip file is stored (from the artifact) to be extracted
extractFilesFromArtifact() {
  du -h -d 1 "${1}";
  ls -lahp "${1}";

  # Unzip every zip file found.
  find "${1}" -maxdepth 1 -iname "*.zip" | while read -r filename; do
    echo "Unzipping file: ${filename}";
    unzip -o -d "${1}" "${filename}";
    find "${1}" -maxdepth 1 -iname "*.zip" | while read -r filenameInside; do
      echo "Unzipping file that was inside the previous zip: ${filenameInside}";
      unzip -o -d "${1}" "${filenameInside}";
      rm -v -- "${filenameInside}" || true;
    done;
    rm -v -- "${filename}" || true;
  done;
  # Delete every zip file found.
  find "${1}" -maxdepth 1 -iname "*.zip" | while read -r filename; do
    rm -v -- "${filename}" || true;
  done;

  du -h -d 1 "${1}";
  ls -lahp "${1}";
}

# Compact files for an artifact to be uploaded.
# Parameters:
# * path of a folder to be compacted
# * name for the new zip file
zipFilesForArtifact() {
  pathName=$(basename "${1}");
  du -h -d 1 "${1}";
  ls -lahp "${1}";

  oldpath=$(pwd);
  cd "${1}" || exit;

  echo "Zipping path: ${pathName}";
  zip -9 -v -r "${2}" ./*;
  cd "${oldpath}" || exit;

  du -h -d 1 "${1}";
  ls -lahp "${1}";
}

# Generate code coverage.
generateCodeCoverage() {
  lcov -c -d . --no-external -o code_coverage_test.info;
  lcov -a code_coverage_base.info -a code_coverage_test.info -o code_coverage.info;
  lcov --remove code_coverage.info '*third_party*' '*build*' '*Unit_Testing*' -o code_coverage_filtered.info;
  genhtml code_coverage_filtered.info -o code_coverage_report --no-branch-coverage -t MobileRT_code_coverage;
  _validateCodeCoverage;
}

# Validate generated files for code coverage.
_validateCodeCoverage() {
  ls -lahp code_coverage_base.info;
  ls -lahp code_coverage_test.info;
  ls -lahp code_coverage.info;
  ls -lahp code_coverage_filtered.info;
}

# Add command to the PATH environment variable.
# Parameters
# 1) command name
addCommandToPath() {
  echo "Adding '${1}' to PATH.";
  if command -v "${1}" > /dev/null; then
    echo "Command '${1}' already available.";
    return;
  fi
  COMMAND_PATHS=$(find /usr/ ~/../ -type f -iname "${1}" 2> /dev/null | grep -i "bin" || true);
  for COMMAND_PATH in ${COMMAND_PATHS}; do
    echo "Command path to executable: ${COMMAND_PATH}";
    echo "Command location: ${COMMAND_PATH%/"${1}"}";
    export PATH="${PATH}:${COMMAND_PATH%/"${1}"}";
  done;

  echo "PATH: ${PATH}";
}

###############################################################################
###############################################################################
