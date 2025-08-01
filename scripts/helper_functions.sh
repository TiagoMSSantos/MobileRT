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
  return 1;
}

# Helper command for Android compilation scripts.
helpCompileAndroid() {
  echo 'Usage: cmd [-h] [-t type] [-f cpu_architecture] [-c compiler] [-r recompile] [-a android_api_version]';
  return 1;
}

# Helper command for Android run tests scripts.
helpTestAndroid() {
  echo 'Usage: cmd [-h] [-t type] [-f cpu_architecture] [-r run_test] [-a android_api_version] [-k kill_previous]';
  return 1;
}

# Helper command for compilation scripts.
helpCheck() {
  echo 'Usage: cmd [-h] [-f cpu_architecture] [-a android_api_version]';
  return 1;
}

# Argument parser for compilation scripts.
parseArgumentsToCompile() {
  # Reset the index of the last option argument processed by the getopts.
  OPTIND=0;
  while getopts "ht:c:r:" opt; do
    case ${opt} in
      t )
        export type="${OPTARG}";
        echo "Setting type: ${type}";
        ;;
      c )
        export compiler="${OPTARG}";
        echo "Setting compiler: ${compiler}";
        ;;
      r )
        export recompile="${OPTARG}";
        echo "Setting recompile: ${recompile}";
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
  # Reset the index of the last option argument processed by the getopts.
  OPTIND=0;
  while getopts "ht:c:r:a:f:" opt; do
    case ${opt} in
      a )
        export android_api_version="${OPTARG}";
        ;;
      t )
        export type="${OPTARG}";
        ;;
      c )
        export compiler="${OPTARG}";
        checkCommand "${compiler}";
        ;;
      r )
        export recompile="${OPTARG}";
        ;;
      f )
        export cpu_architecture="${OPTARG}";
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
  # Reset the index of the last option argument processed by the getopts.
  OPTIND=0;
  while getopts "ht:r:k:a:f:" opt; do
    case ${opt} in
      a )
        export android_api_version="${OPTARG}";
        ;;
      t )
        export type="${OPTARG}";
        ;;
      r )
        export run_test="${OPTARG}";
        ;;
      k )
        export kill_previous="${OPTARG}";
        ;;
      f )
        export cpu_architecture="${OPTARG}";
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
  # Reset the index of the last option argument processed by the getopts.
  OPTIND=0;
  while getopts "ha:f:" opt; do
    case ${opt} in
      a )
        export android_api_version="${OPTARG}";
        ;;
      f )
        export cpu_architecture="${OPTARG}";
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
  _retry=0;
  set +e;
  "$@";
  lastResult=${?};
  while [ "${lastResult}" -eq 0 ] && [ ${_retry} -lt 5 ]; do
    _retry=$(( _retry + 1 ));
    "$@";
    lastResult=${?};
    echo "Retry: ${_retry} of command '$*'; result: '${lastResult}'";
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
# Every attempt are made with a 3 seconds delay.
# Parameters:
# * The maximum number of retries.
callCommandUntilSuccess() {
  echo '';
  echo "Calling until success '$*'";
  _retry=0;
  _maxRetries=${1};
  set +e;
  # Perform a shift of the parameters, so it's ignored the 1st element (number of retries).
  shift;
  "${@}";
  lastResult=${?};
  echo "result: '${lastResult}'";
  # Android API 33 can take more than 1 minute to boot.
  while [ "${lastResult}" -ne 0 ] && [ "${_retry}" -lt "${_maxRetries}" ]; do
    _retry=$(( _retry + 1 ));
    "${@}";
    lastResult=${?};
    echo "Retry: ${_retry} of command '$*'; result: '${lastResult}'";
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
  _retry=0;
  set +e;
  echo "Calling ADB shell command until success '$*'";
  output=$("$@");
  # echo "Output of command: '${output}'";
  lastResult=$(echo "${output}" | grep '::.*::' | sed 's/:://g'| tr -d '[:space:]');
  echo "result: '${lastResult}'";
  while [ "${lastResult}" != '0' ] && [ ${_retry} -lt 60 ]; do
    _retry=$(( _retry + 1 ));
    output=$("$@");
    # echo "Output of command: '${output}'";
    lastResult=$(echo "${output}" | grep '::.*::' | sed 's/:://g' | tr -d '[:space:]');
    echo "Retry: ${_retry} of command '$*'; result: '${lastResult}'";
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
# Parameters:
# * command to check if is available.
checkCommand() {
  if command -v "${@}" > /dev/null; then
    echo "Command '$*' installed!";
  else
    echo "Command '$*' is NOT installed.";
    if uname -a | grep -iq 'msys' && echo "$*" | grep -iq 'python'; then
      echo "Detected Windows OS, so ignoring not having 'python' ...";
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
  uname -a;
  if uname -a | grep -iq 'msys' && command -v nproc > /dev/null; then
    NCPU_CORES="$(nproc --all)";
    echo "Assuming Windows with ${NCPU_CORES} cores.";
  elif uname -a | grep -iq 'linux' && command -v nproc > /dev/null; then
    NCPU_CORES="$(nproc --all)";
    echo "Assuming Linux with ${NCPU_CORES} cores.";
  elif uname -a | grep -iq 'darwin' && command -v sysctl > /dev/null; then
    NCPU_CORES="$(sysctl -n hw.logicalcpu)";
    echo "Assuming MacOS with ${NCPU_CORES} cores.";
  fi
  export NCPU_CORES;
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
  du -h -d 1 "${1}" > /dev/null;
  returnValue="$?";
  if [ "${returnValue}" = '0' ] && [ $# -eq 1 ] ; then
    return 0;
  fi
  _validateFileExistsAndHasSomeContent "${1}"/"${2}";
}

# Check if code coverage report is not zero.
# Parameters:
# * path to the code coverage report file
# * code coverage report file
checkCodeCoverageIsNotZero() {
  checkPathExists "${1}" "${2}";
  set +e;
  grep -iq 'lines-valid="0"' "${1}"/"${2}";
  returnValue="$?";
  if [ "${returnValue}" = '0' ]; then
    set -e;
    echo 'Code coverage report is not valid.';
    return 1;
  fi
  grep -iq 'lines-covered="0"' "${1}"/"${2}";
  returnValue="$?";
  set -e;
  if [ "${returnValue}" = '0' ]; then
    echo 'Code coverage report is empty.';
    return 1;
  fi
}

# Private method which kills a process that is using a file.
_killProcessUsingFile() {
  processes_using_file=$(lsof "${1}" | tail -n +2 | tr -s ' ');
  _retry=0;
  while [ "${processes_using_file}" != '' ] && [ ${_retry} -lt 5 ]; do
    _retry=$(( _retry + 1 ));
    echo "processes_using_file: '${processes_using_file}'";
    process_id_using_file=$(echo "${processes_using_file}" | tr -s ' ' | cut -d ' ' -f 2 | head -1);
    # shellcheck disable=SC2009
    if ps aux | grep -i "${process_id_using_file}" | grep -iq 'android-studio'; then
      echo "Not killing process: '${process_id_using_file}' because it is the Android Studio";
      return 0;
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
  _retry=0;
  while [ "${processes_using_port}" != '' ] && [ ${_retry} -lt 5 ]; do
    _retry=$(( _retry + 1 ));
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
  retry_files=0;
  while [ "${files_being_used}" != '' ] && [ ${retry_files} -lt 10 ]; do
    retry_files=$(( retry_files + 1 ));
    echo "files_being_used: '${files_being_used}'";
    # Changing IFS to allow finding commands in paths with spaces.
    old_IFS="${IFS}";
    IFS="$(printf '\n')";
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
    IFS="${old_IFS}";
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
  set +e;
  nativeLib=$(find . -iname "*mobilert*.a" -or -iname "*mobilert*.dll*" -or -iname "*mobilert*.so");
  find . -iname "*mobilert*.a" -or -iname "*mobilert*.dll*" -or -iname "*mobilert*.so" 2> /dev/null;
  set -e;
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
  cd "${1}" || exit 1;

  echo "Zipping path: ${pathName}";
  zip -9 -v -r "${2}" ./*;
  cd "${oldpath}" || exit 1;

  du -h -d 1 "${1}";
  ls -lahp "${1}";
}

# Generate code coverage.
generateCodeCoverage() {
  echo 'Generating code coverage';
  uname -a;
  lcov --version;
  if lcov --version | grep -iq 'version 2.'; then
    echo 'Detected LCOV version 2.';
    lcov -c -d . --no-external --ignore-errors mismatch,inconsistent,format,negative,corrupt -o code_coverage_test.info;
  else
    # LCOV v1 doesn't support flags mismatch & inconsistent & format.
    echo 'Detected LCOV version 1.';
    lcov -c -d . --no-external -o code_coverage_test.info;
  fi
  echo 'Merging code coverage';
  lcov --ignore-errors inconsistent,format,corrupt -a code_coverage_base.info -a code_coverage_test.info -o code_coverage.info;
  echo 'Removing coverage of third party code and of tests';
  if [ -d "Qt" ]; then
    qtPathExcludeFilter='*/Qt/*/*/include/*';
  fi
  set +u;
  lcov --ignore-errors format --remove code_coverage.info '*third_party*' '*build*' '*Unit_Testing*' ${qtPathExcludeFilter} -o code_coverage_filtered.info;
  set -u;
  echo 'Generating HTML page with code coverage';
  genhtml code_coverage_filtered.info -o code_coverage_report --no-branch-coverage -t MobileRT_code_coverage;
  echo 'Validating code coverage files';
  _validateCodeCoverage;
}

# Validate generated files for code coverage.
_validateCodeCoverage() {
  _validateFileExistsAndHasSomeContent code_coverage_base.info;
  _validateFileExistsAndHasSomeContent code_coverage_test.info;
  _validateFileExistsAndHasSomeContent code_coverage.info;
  _validateFileExistsAndHasSomeContent code_coverage_filtered.info;
}

# Add command to the PATH environment variable.
# Parameters
# 1) command name
addCommandToPath() {
  echo "Adding '${1}' to PATH.";
  if command -v "${1}" > /dev/null; then
    echo "Command '${1}' already available.";
    return 0;
  fi
  # shellcheck disable=SC2010
  visualStudioVersion=$(ls -la "/c/Program Files/Microsoft Visual Studio/" | grep ".*[[:digit:]]$" | awk '{print $(NF)}' | tail -1 || true);
  echo "Detected Visual Studio version: ${visualStudioVersion}";
  # shellcheck disable=SC2010
  msvcVersion=$(ls -la "/c/Program Files/Microsoft Visual Studio/${visualStudioVersion}/Enterprise/VC/Tools/MSVC/" | grep "[[:digit:]]\.[[:digit:]].*" | awk '{print $(NF)}' | tail -1 || true);
  echo "Detected Visual Studio C++ Compiler version: ${msvcVersion}";
  COMMAND_PATHS=$(find \
    /opt/intel/oneapi/compiler \
    ~/.. \
    /usr \
    "/c/Program Files/Microsoft Visual Studio/${visualStudioVersion}/Enterprise/VC/Tools/MSVC/${msvcVersion}/bin/Hostx64/x64" \
    "/c/Program Files/Microsoft Visual Studio/${visualStudioVersion}/Enterprise/MSBuild/Current/Bin/amd64" \
    -type f -iname "${1}" -or -iname "${1}.exe" 2> /dev/null | grep -i "bin" || true);
  # Changing IFS to allow finding commands in paths with spaces.
  old_IFS="${IFS}";
  IFS="$(printf '\n')";
  for COMMAND_PATH in ${COMMAND_PATHS}; do
    echo "Command path to executable: ${COMMAND_PATH}";
    echo "Command location Unix: ${COMMAND_PATH%/"${1}"}";
    echo "Command location Windows: ${COMMAND_PATH%/"${1}.exe"}";
    export PATH="${PATH}:${COMMAND_PATH%/"${1}"}";
    export PATH="${PATH}:${COMMAND_PATH%/"${1}.exe"}";
  done;
  IFS="${old_IFS}";
  if [ -z "${COMMAND_PATHS}" ]; then
    echo "Command '${1}' was not found in the system.";
    return 1;
  fi

  echo "PATH: ${PATH}";
}

# Validate whether a file exists or not.
# Also check if the file has some content.
# Parameters
# 1) path to file
_validateFileExistsAndHasSomeContent() {
  filePath="${1}";
  if [ ! -f "${filePath}" ]; then
    echo "File '${filePath}' does NOT exist." >&2;
    return 1;
  fi
  if [ ! -s "${filePath}" ]; then
    echo "File '${filePath}' is empty." >&2;
    return 1;
  fi
  fileSize=$(wc -w "${filePath}" | awk '{print $1}' | tr -d '[:space:]');
  if [ "${fileSize}" = '' ]; then
    return 2;
  fi
  if [ "${fileSize}" -lt 110 ]; then
    echo "File '${filePath}' contains '${fileSize}' words, which is less than 110 words." >&2;
    cat "${filePath}";
    return 1;
  fi
}
###############################################################################
###############################################################################
