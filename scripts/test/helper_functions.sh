#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# Tests for the functions in the `helper_functions.sh` script.
###############################################################################
###############################################################################

# shellcheck disable=SC2154
# Ignore SC2154 checks because, by using the `assertEqual` function, it can
# complain of certain variables not being explicitly set before calling the
# function. E.g.:
# assertEqual "${expected}" "${variableUsed}" "${_testName} <-flag>";

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
  cd "$(dirname "${0}")/../.." || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
# shellcheck disable=SC1091
. scripts/helper_functions.sh;
# shellcheck disable=SC1091
. scripts/test/utils/utils.sh;
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

# Whether the tests passed or failed.
# 0 -> success (every test passed).
# 1 -> failure (at least one test failed).
exitValue=0;

# Helper function which validates that an environment variable is not set.
#
# Parameters:
# 1) Assertion's context message
# 2+) Name(s) of environment(s) variable(s)
#
# Returns 1 if any variable was set previously.
_validateEnvVariablesDoNotExist() {
  if [ $# -lt 2 ]; then
    echo 'Usage: _validateEnvVariablesDoNotExist <message> <var_name_1> <var_name_2> ... <var_name_n>';
    exitValue=1;
    return 1;
  fi
  message="${1}";
  # Perform a shift of the parameters, so it's ignored the 1st element (message) in the next for loop.
  shift;
  for variable in "${@}"; do
    variableValue=$(eval "echo \"\$${variable}\"");
    if [ -n "${variableValue}" ]; then
      logError '[FAILED]';
      echo "${message} | The '${variable}' environment variable should not be set beforehand. It has the value: '${variableValue}'";
      exitValue=1;
      return 1;
    fi
  done
}

# Helper function which validates that an environment variable is set.
#
# Parameters:
# 1) Name of environment variable
# 2) Expected value
# 3) Assertion's context message
#
# Output:
# Returns 1 if the variable was not set previously.
# Returns 2 if the variable was set but does NOT have the expected value.
_validateEnvVariableValue() {
  if [ $# -ne 3 ]; then
    echo 'Usage: _validateEnvVariableValue <name_env_var> <expected_value_env_var> <message>';
    exitValue=1;
    return 1;
  fi
  variableValue=$(eval "echo \"\$${1}\"");
  if [ -z "${variableValue}" ]; then
    logError '[FAILED]';
    echo "${3} | The '${1}' environment variable should be set beforehand. It doesn't have any value.";
    exitValue=1;
    return 1;
  fi
  if [ "${variableValue}" != "${2}" ]; then
    logError '[FAILED]';
    echo "${3} | The '${1}' environment variable does NOT have the expected value '${2}'. Instead it has the value: '${variableValue}'";
    exitValue=1;
    return 2;
  fi
}

# Helper function which clears the environment variables used by these tests.
_clearEnvVariables() {
  unset type compiler recompile;
  unset android_api_version cpu_architecture;
  unset run_test kill_previous;
  unset NCPU_CORES;
}

# Helper function which asserts that 2 parameters are equal.
#
# Arguments:
#   1) Expected value
#   2) Actual value
#   3) Assertion's context message
#
# Output:
#   exit code 1 if passed wrong parameters
#
# Output variable:
#   exitValue: 1 if assert fails
assertEqual() {
  if [ $# -ne 3 ]; then
    echo 'Usage: assertEqual <expected> <actual> <message>';
    export exitValue=1;
    return 1;
  fi

  expected="${1}";
  actual="${2}";
  message="${3}";

  if [ "${expected}" != "${actual}" ]; then
    logError '[FAILED]';
    echo "${message} | Expected '${expected}', but the actual value is '${actual}'";
    export exitValue=1;
  else
    logSuccess '[PASSED]';
    echo "${message}";
  fi
}


# Tests the helpCompile function.
testHelpCompile() {
  eval '$(helpCompile > /dev/null)';
  returnValue="$?";

  expected='1';
  assertEqual "${expected}" "${returnValue}" 'testHelpCompile';
}

# Tests the helpCompileAndroid function.
testHelpCompileAndroid() {
  eval '$(helpCompileAndroid > /dev/null)';
  returnValue="$?";

  expected='1';
  assertEqual "${expected}" "${returnValue}" 'testHelpCompileAndroid';
}

# Tests the helpTestAndroid function.
testHelpTestAndroid() {
  eval '$(helpTestAndroid > /dev/null)';
  returnValue="$?";

  expected='1';
  assertEqual "${expected}" "${returnValue}" 'testHelpTestAndroid';
}

# Tests the helpCheck function.
testHelpCheck() {
  eval '$(helpCheck > /dev/null)';
  returnValue="$?";

  expected='1';
  assertEqual "${expected}" "${returnValue}" 'testHelpCheck';
}

# Tests the parseArgumentsToCompile function.
testParseArgumentsToCompile() {
  _functionName='parseArgumentsToCompile';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  _clearEnvVariables;
  # Validate the `type` variable is set properly.
  expected='type_test';
  eval ${_functionName} -t "${expected}" > /dev/null;
  _validateEnvVariableValue 'type' "${expected}" "${_testName} -t";
  _validateEnvVariablesDoNotExist "${_testName} -t" 'compiler' 'recompile';
  assertEqual "${expected}" "${type}" "${_testName} -t";

  _clearEnvVariables;
  # Validate the `compiler` variable is set properly.
  expected='g++';
  eval ${_functionName} -c "${expected}" > /dev/null;
  _validateEnvVariableValue 'compiler' "${expected}" "${_testName} -c";
  _validateEnvVariablesDoNotExist "${_testName} -c" 'recompile' 'type';
  assertEqual "${expected}" "${compiler}" "${_testName} -c";

  _clearEnvVariables;
  # Validate the `recompile` variable is set properly.
  expected='recompile_test';
  eval ${_functionName} -r "${expected}" > /dev/null;
  _validateEnvVariableValue 'recompile' "${expected}" "${_testName} -r";
  _validateEnvVariablesDoNotExist "${_testName} -r" 'type' 'compiler';
  assertEqual "${expected}" "${recompile}" "${_testName} -r";

  _clearEnvVariables;
  # Validate the help message returns the expected value.
  eval '$(${_functionName} -h > /dev/null)';
  returnValue="$?";
  expected='1';
  assertEqual "${expected}" "${returnValue}" "${_testName} -h";

  _clearEnvVariables;
  # Validate the help message returns the expected value, when using the wrong parameter.
  eval '$(${_functionName} -z > /dev/null 2>&1)';
  returnValue="$?";
  expected='1';
  assertEqual "${expected}" "${returnValue}" "${_testName} ?";
}

# Tests the parseArgumentsToCompileAndroid function.
testParseArgumentsToCompileAndroid() {
  _functionName='parseArgumentsToCompileAndroid';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  _clearEnvVariables;
  # Validate the `android_api_version` variable is set properly.
  expected='android_api_version_test';
  eval ${_functionName} -a "${expected}" > /dev/null;
  _validateEnvVariableValue 'android_api_version' "${expected}" "${_testName} -a";
  _validateEnvVariablesDoNotExist "${_testName} -a" 'type' 'compiler' 'recompile' 'cpu_architecture';
  assertEqual "${expected}" "${android_api_version}" "${_testName} -a";

  _clearEnvVariables;
  # Validate the `type` variable is set properly.
  expected='type_test';
  eval ${_functionName} -t "${expected}" > /dev/null;
  _validateEnvVariableValue 'type' "${expected}" "${_testName} -t";
  _validateEnvVariablesDoNotExist "${_testName} -t" 'android_api_version' 'compiler' 'recompile' 'cpu_architecture';
  assertEqual "${expected}" "${type}" "${_testName} -t";

  _clearEnvVariables;
  # Validate the `compiler` variable is set properly.
  expected='g++';
  eval ${_functionName} -c "${expected}" > /dev/null;
  _validateEnvVariableValue 'compiler' "${expected}" "${_testName} -c";
  _validateEnvVariablesDoNotExist "${_testName} -c" 'android_api_version' 'type' 'recompile' 'cpu_architecture';
  assertEqual "${expected}" "${compiler}" "${_testName} -c";

  _clearEnvVariables;
  # Validate the `recompile` variable is set properly.
  expected='recompile_test';
  eval ${_functionName} -r "${expected}" > /dev/null;
  _validateEnvVariableValue 'recompile' "${expected}" "${_testName} -r";
  _validateEnvVariablesDoNotExist "${_testName} -r" 'android_api_version' 'type' 'compiler' 'cpu_architecture';
  assertEqual "${expected}" "${recompile}" "${_testName} -r";

  _clearEnvVariables;
  # Validate the `cpu_architecture` variable is set properly.
  expected='cpu_architecture_test';
  eval ${_functionName} -f "${expected}" > /dev/null;
  _validateEnvVariableValue 'cpu_architecture' "${expected}" "${_testName} -f";
  _validateEnvVariablesDoNotExist "${_testName} -f" 'android_api_version' 'type' 'compiler' 'recompile';
  assertEqual "${expected}" "${cpu_architecture}" "${_testName} -f";

  _clearEnvVariables;
  # Validate the help message returns the expected value.
  eval '$(${_functionName} -h > /dev/null)';
  returnValue="$?";
  expected='1';
  assertEqual "${expected}" "${returnValue}" "${_testName} -h";

  _clearEnvVariables;
  # Validate the help message returns the expected value, when using the wrong parameter.
  eval '$(${_functionName} -z > /dev/null 2>&1)';
  returnValue="$?";
  expected='1';
  assertEqual "${expected}" "${returnValue}" "${_testName} ?";
}

# Tests the parseArgumentsToTestAndroid function.
testParseArgumentsToTestAndroid() {
  _functionName='parseArgumentsToTestAndroid';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  _clearEnvVariables;
  # Validate the `type` variable is set properly.
  expected='type_test';
  eval ${_functionName} -t "${expected}" > /dev/null;
  _validateEnvVariableValue 'type' "${expected}" "${_testName} -t";
  _validateEnvVariablesDoNotExist "${_testName} -t" 'run_test' 'kill_previous' 'cpu_architecture';
  assertEqual "${expected}" "${type}" "${_testName} -t";

  _clearEnvVariables;
  # Validate the `run_test` variable is set properly.
  expected='run_test_test';
  eval ${_functionName} -r "${expected}" > /dev/null;
  _validateEnvVariableValue 'run_test' "${expected}" "${_testName} -r";
  _validateEnvVariablesDoNotExist "${_testName} -r" 'type' 'kill_previous' 'cpu_architecture';
  assertEqual "${expected}" "${run_test}" "${_testName} -r";

  _clearEnvVariables;
  # Validate the `kill_previous` variable is set properly.
  expected='kill_previous_test';
  eval ${_functionName} -k "${expected}" > /dev/null;
  _validateEnvVariableValue 'kill_previous' "${expected}" "${_testName} -k";
  _validateEnvVariablesDoNotExist "${_testName} -k" 'type' 'run_test' 'cpu_architecture';
  assertEqual "${expected}" "${kill_previous}" "${_testName} -k";

  _clearEnvVariables;
  # Validate the `cpu_architecture` variable is set properly.
  expected='cpu_architecture_test';
  eval ${_functionName} -f "${expected}" > /dev/null;
  _validateEnvVariableValue 'cpu_architecture' "${expected}" "${_testName} -f";
  _validateEnvVariablesDoNotExist "${_testName} -f" 'type' 'run_test' 'kill_previous';
  assertEqual "${expected}" "${cpu_architecture}" "${_testName} -f";

  _clearEnvVariables;
  # Validate the help message returns the expected value.
  eval '$(${_functionName} -h > /dev/null)';
  returnValue="$?";
  expected='1';
  assertEqual "${expected}" "${returnValue}" "${_testName} -h";

  _clearEnvVariables;
  # Validate the help message returns the expected value, when using the wrong parameter.
  eval '$(${_functionName} -z > /dev/null 2>&1)';
  returnValue="$?";
  expected='1';
  assertEqual "${expected}" "${returnValue}" "${_testName} ?";
}

# Tests the parseArgumentsToCheck function.
testParseArgumentsToCheck() {
  _functionName='parseArgumentsToCheck';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  _clearEnvVariables;
  # Validate the `cpu_architecture` variable is set properly.
  expected='cpu_architecture_test';
  eval ${_functionName} -f "${expected}" > /dev/null;
  _validateEnvVariableValue 'cpu_architecture' "${expected}" "${_testName} -f";
  _validateEnvVariablesDoNotExist "${_testName} -f" 'android_api_version';
  assertEqual "${expected}" "${cpu_architecture}" "${_testName} -f";

  _clearEnvVariables;
  # Validate the help message returns the expected value.
  eval '$(${_functionName} -h > /dev/null)';
  returnValue="$?";
  expected='1';
  assertEqual "${expected}" "${returnValue}" "${_testName} -h";

  _clearEnvVariables;
  # Validate the help message returns the expected value, when using the wrong parameter.
  eval '$(${_functionName} -z > /dev/null 2>&1)';
  returnValue="$?";
  expected='1';
  assertEqual "${expected}" "${returnValue}" "${_testName} ?";
}

# Tests the printCommandExitCode function.
testPrintCommandExitCode() {
  _functionName='printCommandExitCode';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  # Validate the exit code is the one provided as argument.
  expectedExitCode='135';
  eval '$(${_functionName} "${expectedExitCode}" "message to be printed" > /dev/null)';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${expectedExitCode}";

  # Validate the exit code is the one provided as argument, even if the exit code is 0.
  expectedExitCode='0';
  eval ${_functionName} "${expectedExitCode}" > /dev/null;
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${expectedExitCode}";
}

# Tests the checkCommand function.
testCheckCommand() {
  _functionName='checkCommand';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  # Validate the exit code is the expected one when command does NOT exist.
  expectedExitCode='1';
  commandToCheck='commandThatDoesNotExist';
  eval '$(${_functionName} "${commandToCheck}" > /dev/null)';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${commandToCheck}";

  # Validate the exit code is the expected one when command DOES exist.
  expectedExitCode='0';
  commandToCheck='wc';
  eval '$(${_functionName} "${commandToCheck}" > /dev/null)';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${commandToCheck}";

  # Validate the exit code is 0 if python command DOES exist on Windows OS.
  # Setup mocks:
  uname() { echo 'msys'; } # Emulate Windows OS.
  command() { return 0; } # Emulate command found.
  expectedExitCode='0';
  commandToCheck='python';
  eval '$(${_functionName} "${commandToCheck}" > /dev/null)';
  returnValue="$?";
  unset -f uname command; # Restore original functions.
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${commandToCheck} exists on Windows OS";

  # Validate the exit code is 0 if python command does NOT exist on Windows OS.
  # Setup mocks:
  uname() { echo 'msys'; } # Emulate Windows OS.
  command() { return 1; } # Emulate command not found.
  expectedExitCode='0';
  commandToCheck='python';
  eval '$(${_functionName} "${commandToCheck}" > /dev/null)';
  returnValue="$?";
  unset -f uname command; # Restore original functions.
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${commandToCheck} does NOT exist on Windows OS";
}

# Tests the addCommandToPath function.
testAddCommandToPath() {
  _functionName='addCommandToPath';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  # Validate the exit code is 0 if the command 'wc' was already added to the PATH environment variable.
  expectedExitCode='0';
  commandToAdd='wc';
  eval '$(${_functionName} "${commandToAdd}" > /dev/null)';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${commandToAdd} was already in PATH";

  # Validate the exit code is 0 after adding the command 'wc' to the PATH environment variable.
  # Setup mocks:
  command() { return 1; } # Emulate command not found.
  find() { echo "$(which wc)"; } # Emulate command was found.
  expectedExitCode='0';
  commandToAdd='wc';
  eval '$(${_functionName} "${commandToAdd}" > /dev/null)';
  returnValue="$?";
  unset -f command; # Restore original function.
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${commandToAdd} was added to PATH";

  # Validate the exit code is 1 if adding the command 'wc' is not successful since the command was not found.
  # Setup mocks:
  command() { return 1; } # Emulate command not found.
  find() { echo ''; } # Emulate command was not found.
  expectedExitCode='1';
  commandToAdd='wc';
  eval '$(${_functionName} "${commandToAdd}" > /dev/null)';
  returnValue="$?";
  unset -f command find; # Restore original functions.
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} ${commandToAdd} was NOT added to PATH";
}

# Tests the parallelizeBuild function.
testParallelizeBuild() {
  _functionName='parallelizeBuild';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  _clearEnvVariables;
  # Validate the exit code is 0 if using Linux, and the 'NCPU_CORES' is properly set.
  # Setup mocks:
  uname() { echo 'linux'; } # Emulate Linux OS.
  nproc() { echo '11'; } # Emulate 11 CPU cores available.
  expectedExitCode='0';
  eval '${_functionName} > /dev/null';
  returnValue="$?";
  unset -f nproc; # Restore original function.
  expected='11';
  _validateEnvVariableValue 'NCPU_CORES' "${expected}" "${_testName} Linux";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} Linux";

  _clearEnvVariables;
  # Validate the exit code is 0 if using MacOS, and the 'NCPU_CORES' is properly set.
  # Setup mocks:
  uname() { echo 'darwin'; } # Emulate MacOS.
  sysctl() { echo '22'; } # Emulate 22 CPU cores available.
  expectedExitCode='0';
  eval '${_functionName} > /dev/null';
  returnValue="$?";
  unset -f uname sysctl; # Restore original functions.
  expected='22';
  _validateEnvVariableValue 'NCPU_CORES' "${expected}" "${_testName} MacOS";  
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} MacOS";

  _clearEnvVariables;
  # Validate the exit code is 0 if using Windows, and the 'NCPU_CORES' is properly set.
  # Setup mocks:
  uname() { echo 'msys'; } # Emulate Windows OS.
  nproc() { echo '33'; } # Emulate 33 CPU cores available.
  expectedExitCode='0';
  eval '${_functionName} > /dev/null';
  returnValue="$?";
  unset -f uname nproc; # Restore original functions.
  expected='33';
  _validateEnvVariableValue 'NCPU_CORES' "${expected}" "${_testName} Windows";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} Windows";
}

# Tests the checkPathExists function.
testCheckPathExists() {
  _functionName='checkPathExists';
  _testName="test$(capitalizeFirstletter ${_functionName})";

  # Validate the exit code is 1 if path do NOT exist.
  expectedExitCode='1';
  pathToValidate='path/that/does/not/exist';
  eval '${_functionName} "${pathToValidate}" > /dev/null 2>&1';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} path do NOT exist";

  # Validate the exit code is 0 if path DOES exist.
  expectedExitCode='0';
  pathToValidate='app';
  eval '${_functionName} "${pathToValidate}" > /dev/null';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} path DOES exist";

  # Validate the exit code is 1 if path exists but the file does NOT.
  expectedExitCode='1';
  pathToValidate='app';
  fileToValidate='fileThatDoesNotExist';
  eval '${_functionName} "${pathToValidate}" "${fileToValidate}" > /dev/null 2>&1';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} file do NOT exist";

  # Validate the exit code is 1 if path & file exist but the file is EMPTY.
  expectedExitCode='1';
  pathToValidate='app';
  fileToValidate='emptyFile.log';
  rm -f "${pathToValidate}/${fileToValidate}";
  touch "${pathToValidate}/${fileToValidate}";
  eval '${_functionName} "${pathToValidate}" "${fileToValidate}" > /dev/null 2>&1';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} file is empty";

  # Validate the exit code is 1 if path & file exist but the file has less than 190 words in it.
  expectedExitCode='1';
  pathToValidate='app';
  fileToValidate='fileWithLessThan190words.log';
  rm -f "${pathToValidate}/${fileToValidate}";
  touch "${pathToValidate}/${fileToValidate}";
  for VARIABLE in $(seq 1 189); do
    echo "${VARIABLE}" >> "${pathToValidate}/${fileToValidate}";
  done;
  eval '${_functionName} "${pathToValidate}" "${fileToValidate}" > /dev/null 2>&1';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} file has less than 190 words";

  # Validate the exit code is 0 if path & file exist and the file has more or equal than 190 words in it.
  expectedExitCode='0';
  pathToValidate='app';
  fileToValidate='fileWith190words.log';
  rm -f "${pathToValidate}/${fileToValidate}";
  touch "${pathToValidate}/${fileToValidate}";
  for VARIABLE in $(seq 1 190); do
    echo "${VARIABLE}" >> "${pathToValidate}/${fileToValidate}";
  done;
  eval '${_functionName} "${pathToValidate}" "${fileToValidate}" > /dev/null';
  returnValue="$?";
  assertEqual "${expectedExitCode}" "${returnValue}" "${_testName} file has 190 words";
}


set +eu;
# Execute all tests.
testHelpCompile;
testHelpCompileAndroid;
testHelpTestAndroid;
testHelpCheck;
testParseArgumentsToCompile;
testParseArgumentsToCompileAndroid;
testParseArgumentsToTestAndroid;
testParseArgumentsToCheck;
testPrintCommandExitCode;
testCheckCommand;
testAddCommandToPath;
testParallelizeBuild;
testCheckPathExists;
set -eu;

# Exit and return whether the tests passed or failed.
if [ "${exitValue}" != 0 ]; then
  # Only return 'exitValue' if some test(s) failed, because 'return' can only be used from a function or sourced script in bash shell.
  return "${exitValue}";
fi
