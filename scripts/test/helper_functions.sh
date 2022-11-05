#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# Tests for the functions in the `helper_functions.sh` script.
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
cd "$(dirname "${0}")/../.." || exit;
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
  shellcheck "${0}" || exit;
fi
###############################################################################
###############################################################################

# Whether the tests passed or failed.
# 0 -> success (every test passed).
# 1 -> failure (at least one test failed).
exitValue=0;

# Helper function which asserts that 2 parameters are equal.
#
# Arguments:
#   1) Expected value
#   2) Actual value
#   3) Assertion's context message
#
# Output:
#   exit code 0 if expected equals actual
#   exit code 1 if expected doesn't equals actual
assertEqual() {
  if [ $# -ne 3 ]; then
    echo "Usage: cmd <expected> <actual> <message>";
    # return 1;
  fi

  expected="${1}";
  actual="${2}";
  message="${3}";

  if [ "${expected}" != "${actual}" ]; then
    echo "$(tput -T xterm setaf 1) [FAILED] $(tput -T xterm sgr0) ${message} | Expected '${expected}', but the actual value is '${actual}'";
    exitValue=1;
  else
   echo "$(tput -T xterm setaf 2) [PASSED] $(tput -T xterm sgr0) ${message}";
  fi
}


# Tests the helpCompile function.
testHelpCompile() {
  # shellcheck disable=SC2091
  $(helpCompile > /dev/null 2>&1);
  returnValue="$?";

  expected="0";
  assertEqual "${expected}" "${returnValue}" "testHelpCompile";
}

# Tests the helpCompileAndroid function.
testHelpCompileAndroid() {
  # shellcheck disable=SC2091
  $(helpCompileAndroid > /dev/null 2>&1);
  returnValue="$?";

  expected="0";
  assertEqual "${expected}" "${returnValue}" "testHelpCompileAndroid";
}

# Tests the helpTestAndroid function.
testHelpTestAndroid() {
  # shellcheck disable=SC2091
  $(helpTestAndroid > /dev/null 2>&1);
  returnValue="$?";

  expected="0";
  assertEqual "${expected}" "${returnValue}" "testHelpTestAndroid";
}

# Tests the helpCheck function.
testHelpCheck() {
  # shellcheck disable=SC2091
  $(helpCheck > /dev/null 2>&1);
  returnValue="$?";

  expected="0";
  assertEqual "${expected}" "${returnValue}" "testHelpCheck";
}

# Helper function which validates that an environment variable is not set.
#
# Parameters:
# 1) Name of environment variable
#
# Returns 1 if the variable was set previously.
_validateEnvVariableDoesNotExist() {
  set +u;
  variableValue=$(eval "echo \"\$${1}\"");
  # shellcheck disable=SC2154
  if [ -n "${variableValue}" ]; then
    echo "The '${1}' environment variable should not be set beforehand. It has the value: '${variableValue}'";
    set -u;
    return 1;
  fi
  set -u;
}

# Helper function which validates that an environment variable is set.
#
# Parameters:
# 1) Name of environment variable
# 2) Expected value
#
# Returns 1 if the variable was not set previously.
# Returns 2 if the variable was set but does not have the expected value.
_validateEnvVariableValue() {
  set +u;
  variableValue=$(eval "echo \"\$${1}\"");
  # shellcheck disable=SC2154
  if [ -z "${variableValue}" ]; then
    echo "The '${1}' environment variable should be set beforehand. It doesn't have any value.";
    set -u;
    return 1;
  fi
  if [ "${variableValue}" != "${2}" ]; then
    echo "The '${1}' environment variable does not have the expected value '${2}'. Instead it has the value: '${variableValue}'";
    set -u;
    return 2;
  fi
  set -u;
}

# Tests the parseArgumentsToCompile function.
testParseArgumentsToCompile() {
  unset type compiler recompile;
  # Validate the `type` variable is set properly.
  expected="type_test";
  parseArgumentsToCompile -t "${expected}" > /dev/null 2>&1;
  _validateEnvVariableValue "type" "${expected}";
  _validateEnvVariableDoesNotExist "compiler";
  _validateEnvVariableDoesNotExist "recompile";
  # shellcheck disable=SC2154
  assertEqual "${expected}" "${type}" "testParseArgumentsToCompile -t";

  unset type compiler recompile;
  # Validate the `compiler` variable is set properly.
  expected="g++";
  parseArgumentsToCompile -c "${expected}" > /dev/null 2>&1;
  _validateEnvVariableDoesNotExist "type";
  _validateEnvVariableValue "compiler" "${expected}";
  _validateEnvVariableDoesNotExist "recompile";
  # shellcheck disable=SC2154
  assertEqual "${expected}" "${compiler}" "testParseArgumentsToCompile -c";

  unset type compiler recompile;
  # Validate the `recompile` variable is set properly.
  expected="recompile_test";
  parseArgumentsToCompile -r "${expected}" > /dev/null 2>&1;
  _validateEnvVariableDoesNotExist "type";
  _validateEnvVariableDoesNotExist "compiler";
  _validateEnvVariableValue "recompile" "${expected}";
  # shellcheck disable=SC2154
  assertEqual "${expected}" "${recompile}" "testParseArgumentsToCompile -r";
}


# Execute all tests.
testHelpCompile;
testHelpCompileAndroid;
testHelpTestAndroid;
testHelpCheck;
testParseArgumentsToCompile;

# Exit and return whether the tests passed or failed.
exit "${exitValue}";
