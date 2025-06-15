#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# Tests for the entrypoint in the `Dockerfile` file.
# Note that these tests remove all running containers in order to make sure the
# container, volume and network launched by the test are removed.
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
if [ $# -ge 1 ]; then
  cd "$(dirname "${0}")/../../.." || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
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
if [ $# -lt 1 ]; then
  echo 'Usage: dockerfile.sh <VERSION> [EXPECTED_RETURN_VALUE]';
  return 1;
fi
###############################################################################
###############################################################################

if [ "$#" -lt 2 ]; then
  expectedReturnValue='124'; # Expects to return timeout.
else
  expectedReturnValue="${2}"; # Expects a custom return value.
fi

if echo "${1}" | grep -q 'osx'; then
  # Define timeout for MacOS docker image.
  MOBILERT_TIMEOUT='45';
elif echo "${1}" | grep -q 'windows'; then
  # Define timeout for Windows docker image.
  MOBILERT_TIMEOUT='15';
else
  MOBILERT_TIMEOUT='15';
fi

# Whether the tests passed or failed.
# 0 -> success (every test passed).
# 1 -> failure (at least one test failed).
exitValue=0;


# Tests the MobileRT in docker container.
# It uses the command 'timeout' as entrypoint in order to make MobileRT automatically exit after some seconds.
# Flag '--init': Run an init inside the container that forwards signals and reaps processes
# Args:
# * Version of MobileRT docker image
# * Mode to be used by the 'profile.sh' script.
testMobileRTContainer() {
  _mobilertVersion="${1}";
  _mode="${2}";
  _logFile='docker_test.log';

  removeAllContainers;
  echo "Starting test - testMobileRTContainer: ${_mobilertVersion} (expecting return ${expectedReturnValue}) (timeout: ${MOBILERT_TIMEOUT})";

  rm -f /tmp/fd3;
  set +e;
  exec 3<> /tmp/fd3; # Open file descriptor 3.
  (docker run -t \
    -e DISPLAY=':0' \
    -e QT_QPA_PLATFORM='offscreen' \
    -e BUILD_TYPE='release' \
    --init \
    --name="${_mobilertVersion}" \
    ptpuscas/mobile_rt:"${_mobilertVersion}" \
    -c "timeout ${MOBILERT_TIMEOUT} sh scripts/profile.sh ${_mode} 100 conference true" || echo "$?" >&3) | tee "${_logFile}";
  returnValue=$(cat /tmp/fd3);
  exec 3>&-; # Close file descriptor 3.
  set -e;

  echo 'Validating if MobileRT finished rendering the Conference scene.';
  ls -lahp "${_logFile}";
  grep -qne 'Executing Qt app asynchronously.' "${_logFile}";
  grep -qne 'RayTrace ended.' "${_logFile}";
  grep -qne 'Finished rendering scene' "${_logFile}";
  grep -qne 'Total Millions rays per second' "${_logFile}";
  grep -qne 'work_thread ended.' "${_logFile}";
  echo 'Validating number of primitives and lights.';
  grep -qne 'TRIANGLES = 331179' "${_logFile}";
  grep -qne 'LIGHTS = 2' "${_logFile}";
  echo 'Validating selected shader, scene and accelerator.';
  grep -qne 'shader = 1' "${_logFile}";
  grep -qne 'scene = 4' "${_logFile}";
  grep -qne 'accelerator = 3' "${_logFile}";
  echo 'Validating number of samples.'
  grep -qne 'samplesPixel = 1' "${_logFile}";
  grep -qne 'samplesLight = 1' "${_logFile}";
  echo 'Validating resolution.';
  grep -qne 'width = 96' "${_logFile}";
  grep -qne 'height = 96' "${_logFile}";
  echo 'Validating repetitions.';
  grep -qne 'repeats = 1' "${_logFile}";
  echo 'Validating time was printed.';
  grep -qne 'Loading Time in secs ' "${_logFile}";
  grep -qne 'Filling Time in secs ' "${_logFile}";
  grep -qne 'Creating Time in secs' "${_logFile}";
  grep -qne 'Rendering Time in sec' "${_logFile}";
  rm "${_logFile}";
  echo 'Validated that MobileRT finished rendering the scene.';

  assertEqual "${expectedReturnValue}" "${returnValue}" "testMobileRTContainer: ${_mobilertVersion}";
  removeAllContainers;
}

# Execute all tests for a specific version of MobileRT container.
testMobileRTContainer "${1}" 'release';

# Exit and return whether the tests passed or failed.
if [ "${exitValue}" != 0 ]; then
  # Only return 'exitValue' if some test(s) failed, because 'return' can only be used from a function or sourced script in bash shell.
  return "${exitValue}";
fi
