#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# Utility functions for the shell script tests.
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


# Helper function which prints something to the shell console as an error.
# This means that the text received as parameter will be printed with the red color.
#
# Arguments:
#   1) String to be printed in the shell console
logError() {
  if [ $# -ne 1 ]; then
    echo 'Usage: logError <message>';
    exitValue=1;
    return 1;
  fi
  printf "%s ${1} %s" "$(tput -T xterm setaf 1)" "$(tput -T xterm sgr0)";
}

# Helper function which prints something to the shell console as a success.
# This means that the text received as parameter will be printed with the green color.
#
# Arguments:
#   1) String to be printed in the shell console
#
# Output:
#   exit code 1 if passed wrong parameters
logSuccess() {
  if [ $# -ne 1 ]; then
    echo 'Usage: logSuccess <message>';
    exitValue=1;
    return 1;
  fi
  printf "%s ${1} %s" "$(tput -T xterm setaf 2)" "$(tput -T xterm sgr0)";
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
assertEqual() {
  if [ $# -ne 3 ]; then
    echo 'Usage: assertEqual <expected> <actual> <message>';
    exitValue=1;
    return 1;
  fi

  expected="${1}";
  actual="${2}";
  message="${3}";

  if [ "${expected}" != "${actual}" ]; then
    logError '[FAILED]';
    echo "${message} | Expected '${expected}', but the actual value is '${actual}'";
    # shellcheck disable=SC2034
    exitValue=1;
  else
    logSuccess '[PASSED]';
    echo "${message}";
  fi
}
