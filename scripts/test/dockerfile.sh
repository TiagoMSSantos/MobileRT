#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# Tests for the entrypoint in the `Dockerfile` file.
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

# Whether the tests passed or failed.
# 0 -> success (every test passed).
# 1 -> failure (at least one test failed).
exitValue=0;


# Tests the MobileRT in docker container.
testMobileRTContainer() {
  _mobilertVersion="${1}";
  echo "Starting test - testMobileRTContainer: ${_mobilertVersion}";
  addCommandToPath 'xhost';
  xhost +;
  docker run -t \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    -e DISPLAY=':0' \
    -e QT_QPA_PLATFORM='offscreen' \
    --entrypoint timeout \
    --name="${_mobilertVersion}" \
    ptpuscas/mobile_rt:"${_mobilertVersion}" \
    5 ./scripts/profile.sh release;

  returnValue="$?";
  expected='124'; # Expects to return timeout.

  assertEqual "${expected}" "${returnValue}" "testMobileRTContainer: ${_mobilertVersion}";
}

set +eu;
# Execute all tests.
testMobileRTContainer 'gentoo-stage3-latest';
testMobileRTContainer 'sickcodes-docker-osx-latest';
testMobileRTContainer 'tgagor-centos-stream';
testMobileRTContainer 'archlinux-archlinux-base-devel';
testMobileRTContainer 'ubuntu-22.04';
# TODO: Necessary to find an alternative to `timeout` from busybox.
# testMobileRTContainer 'alpine-3.17';
set -eu;

# Remove all docker containers.
docker rm -f "$(docker ps -a | grep -v 'portainer' | awk 'NR>1 {print $1}')";

# Exit and return whether the tests passed or failed.
exit "${exitValue}";
