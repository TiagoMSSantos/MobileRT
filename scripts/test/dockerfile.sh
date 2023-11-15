#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# Tests for the entrypoint in the `Dockerfile` file.
#
# Parameters:
# * VERSION - Version (or tag) of the docker container of MobileRT.
# * EXPECTED_RETURN_VALUE - Expected return value from the 'timeout' command.
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
cd "$(dirname "${0}")/../.." || exit;
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
# shellcheck disable=SC1091
. scripts/test/utils.sh;
###############################################################################
###############################################################################


###############################################################################
# Execute Shellcheck on this script.
###############################################################################
if command -v shellcheck > /dev/null; then
  shellcheck "${0}" || exit;
fi
###############################################################################
###############################################################################

if [ "$#" -lt 2 ]; then
  expected='124'; # Expects to return timeout.
else
  expected="${2}"; # Expects a custom return value.
fi

# Whether the tests passed or failed.
# 0 -> success (every test passed).
# 1 -> failure (at least one test failed).
exitValue=0;


# Tests the MobileRT in docker container.
# It uses the command 'timeout' as entrypoint in order to make MobileRT automatically exit after 5 seconds.
# Flag '--init': Run an init inside the container that forwards signals and reaps processes
# Args:
# * Version of MobileRT docker image
# * Mode to be used by the 'profile.sh' script.
testMobileRTContainer() {
  _mobilertVersion="${1}";
  _mode="${2}";

  echo "Starting test - testMobileRTContainer: ${_mobilertVersion} (expecting return ${expected})";
  docker run -t \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    -e DISPLAY=':0' \
    -e QT_QPA_PLATFORM='offscreen' \
    --init \
    --entrypoint timeout \
    --name="${_mobilertVersion}" \
    ptpuscas/mobile_rt:"${_mobilertVersion}" \
    4 ./scripts/profile.sh "${_mode}" 100;

  returnValue="$?";
  assertEqual "${expected}" "${returnValue}" "testMobileRTContainer: ${_mobilertVersion}";
}

set +eu;
# Execute all tests for a specific version of MobileRT container.
testMobileRTContainer "${1}" 'release';
removeAllContainers;
set -eu;

# Exit and return whether the tests passed or failed.
exit "${exitValue}";
