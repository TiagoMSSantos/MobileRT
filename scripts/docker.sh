#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# This script contains a bunch of helper functions for some docker operations.
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
# shellcheck disable=SC1091
. scripts/helper_functions.sh;
###############################################################################
###############################################################################

# Helper command to check the available version of the docker command.
checkAvailableVersion() {
  echo "${PATH}" | sed 's/:/ /g' | xargs ls 2> /dev/null | grep -i docker 2> /dev/null || true;
  docker --version;
}

# Helper command to build the MobileRT docker image.
# It builds the image in release mode.
# The parameters are:
# * BASE_IMAGE
# * BRANCH
# * VERSION
buildDockerImage() {
  prepareBinaries .;
  du -h -d 1 scripts;
  docker build \
    -t ptpuscas/mobile_rt:"${3}" \
    -f deploy/Dockerfile \
    --no-cache=false \
    --build-arg BASE_IMAGE="${1}" \
    --build-arg BRANCH="${2}" \
    --build-arg BUILD_TYPE=release \
    .;
}

# Helper command to pull the MobileRT docker image.
# The parameters are:
# * VERSION
pullDockerImage() {
  exec 5>&1;
  output=$(docker pull ptpuscas/mobile_rt:"${1}" | tee /dev/fd/5 || true);
  echo "Docker: ${output}";
  if echo "${output}" | grep -q "up to date" || echo "${output}" | grep -q "Downloaded newer image for"; then
    echo 'Docker image found!';
    export BUILD_IMAGE='no';
  else
    echo 'Did not find the Docker image. Will have to build the image.';
    export BUILD_IMAGE='yes';
  fi
}

# Helper command to update and compile the MobileRT in a docker container.
# It builds the MobileRT in release mode.
# The parameters are:
# * VERSION
compileMobileRTInDockerContainer() {
  docker run -t \
    --entrypoint sh \
    --name="mobile_rt_${1}" \
    --volume="${PWD}":/MobileRT_volume \
    ptpuscas/mobile_rt:"${1}" \
    -c "git init \
      && cd / \
      && cp -rpf /MobileRT_volume/* /MobileRT/ \
      && cd /MobileRT/ \
      && find ./app/third_party/ -mindepth 1 -maxdepth 1 -type d ! -regex '^./app/third_party\(/conan*\)?' -exec rm -rf {} \; \
      && ls -lahp ./ \
      && chmod -R +x ./scripts/ \
      && ls -lahp ./scripts/ \
      && sh ./scripts/install_dependencies.sh \
      && sh ./scripts/compile_native.sh -t release -c g++ -r yes";
}

# Helper command to execute the MobileRT unit tests in the docker container.
# The parameters are:
# * VERSION of the docker image
# * PARAMETERS for the unit tests
executeUnitTestsInDockerContainer() {
  docker run -t \
    --entrypoint sh \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    -e DISPLAY="${DISPLAY}" \
    --name="mobile_rt_${1}" \
    ptpuscas/mobile_rt:"${1}" -c "./build_release/bin/UnitTests ${2}";
  docker rm --force --volumes "mobile_rt_${1}";
}

# Helper command to push the MobileRT docker image into the docker registry.
# The parameters are:
# * VERSION
pushMobileRTDockerImage() {
  docker push ptpuscas/mobile_rt:"${1}";
}

# Helper command to commit a layer into the MobileRT docker image.
# The parameters are:
# * VERSION
commitMobileRTDockerImage() {
  docker commit mobile_rt_"${1}" ptpuscas/mobile_rt:"${1}";
  docker rm mobile_rt_"${1}";
}

# Helper command to squash all the layers of the MobileRT docker image.
# The parameters are:
# * VERSION
squashMobileRTDockerImage() {
  _installDockerSquashCommand;
  echo 'docker history 1';
  docker history ptpuscas/mobile_rt:"${1}" || true;
  echo 'docker history 2';
  docker history ptpuscas/mobile_rt:"${1}" | grep -v "<missing>" || true;
  echo 'docker history 3';
  docker history ptpuscas/mobile_rt:"${1}" | grep -v "<missing>" || true;
  echo 'docker history 4';
  docker history ptpuscas/mobile_rt:"${1}" | grep -v "<missing>" | head -2 || true;
  echo 'docker history 5';
  docker history ptpuscas/mobile_rt:"${1}" | grep -v "<missing>" | head -2 | tail -1 || true;
  LAST_LAYER_ID=$(docker history ptpuscas/mobile_rt:"${1}" | grep -v "<missing>" | head -2 | tail -1 | tr -s ' ' | cut -d ' ' -f 1 || true);
  echo "LAST_LAYER_ID=${LAST_LAYER_ID}";
  docker-squash -v --tag ptpuscas/mobile_rt:"${1}" ptpuscas/mobile_rt:"${1}";
  echo 'docker squash finished';
  docker history ptpuscas/mobile_rt:"${1}" || true;
}

# Helper command to install the docker-squash command.
_installDockerSquashCommand() {
  pip install --upgrade pip --user;
  pip3 install --upgrade pip --user;
  python -m pip install --upgrade pip --user || true;
  python3 -m pip install --upgrade pip --user || true;

  pip install -v docker==5.0.3;
  pip install -v docker-squash;

  pip list -v | grep -i docker;
  pip list -v --outdated | grep -i docker;
  pip show -v docker docker-squash;
  pip freeze -v | grep -i docker;
}

# Helper function to install the docker command for MacOS.
# Its necessary to install Docker Desktop on Mac:
# https://docs.docker.com/docker-for-mac/install/
installDockerCommandForMacOS() {
  echo 'Select XCode.';
  sudo xcode-select --switch /System/Volumes/Data/Applications/Xcode.app/Contents/Developer;
  # To avoid error: "Bash must not run in POSIX mode. Please unset POSIXLY_CORRECT and try again."
  set +e;
  echo 'Update Homebrew';
  sh -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)";
  set -e;
  echo 'Install docker & colima';
  brew install docker docker-compose colima;
  echo 'Start colima';
  colima start;
  # For testcontainers to find the Colima socket
  # https://github.com/abiosoft/colima/blob/main/docs/FAQ.md#cannot-connect-to-the-docker-daemon-at-unixvarrundockersock-is-the-docker-daemon-running
  sudo ln -sf "${HOME}/.colima/default/docker.sock /var/run/docker.sock";

  echo 'Docker commands detected:';
  echo "${PATH}" | sed 's/:/ /g' | xargs ls 2> /dev/null | grep -i docker 2> /dev/null || true;

  echo 'Validating docker command works';
  docker --version;
  docker ps -a;
  docker image ls -a;
  docker info;
}
