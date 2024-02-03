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

  if echo "${1}" | grep -iq 'microsoft' || echo "${1}" | grep -iq 'windows'; then
    echo 'Building Docker image based on Windows.';
    dockerBaseOS='windows';
  else
    echo 'Building Docker image based on Unix.';
    dockerBaseOS='unix';
  fi

  docker build \
    -f deploy/Dockerfile."${dockerBaseOS}" \
    --no-cache=false \
    --build-arg BASE_IMAGE="${1}" \
    --build-arg BRANCH="${2}" \
    --build-arg BUILD_TYPE=release \
    .;

  imageId=$(docker images | awk '{print $3}' | awk 'NR==2');
  echo "MobileRT imageId: ${imageId}";
  docker tag "${imageId}" ptpuscas/mobile_rt:"${3}";
  docker images;
}

# Helper command to pull the MobileRT docker image.
# The parameters are:
# * VERSION
pullDockerImage() {
  rm -f /tmp/fd3;
  exec 3<> /tmp/fd3; # Open file descriptor 3.
  (docker pull ptpuscas/mobile_rt:"${1}" || true) | tee /tmp/fd3;
  output=$(cat /tmp/fd3);
  echo "Docker: ${output}";
  if echo "${output}" | grep -q 'up to date' || echo "${output}" | grep -q 'Downloaded newer image for'; then
    echo 'Docker image found!';
    export BUILD_IMAGE='no';
  else
    echo 'Did not find the Docker image. Will have to build the image.';
    export BUILD_IMAGE='yes';
  fi
  exec 3>&-; # Close file descriptor 3.
}

# Helper command to update and compile the MobileRT in a docker container.
# It builds the MobileRT in release mode.
# The parameters are:
# * VERSION
compileMobileRTInDockerContainer() {
  if echo "${1}" | grep -iq 'microsoft' || echo "${1}" | grep -iq 'windows'; then
    echo 'Compiling MobileRT in Windows based Docker image.';
    currentPath=$(echo "${PWD}" | sed 's/\\/\//g' | sed 's/\/d\//D:\//' );
    mobilertVolumeInContainer='C:/MobileRT/MobileRT_volume';
  else
    echo 'Compiling MobileRT in Unix based Docker image.';
    currentPath="${PWD}";
    mobilertVolumeInContainer='/MobileRT_volume';
  fi
  echo "Current path: ${currentPath}";
  echo "Volume path: ${mobilertVolumeInContainer}";
  docker run -t \
    --name="mobile_rt_built_${1}" \
    --volume="${currentPath}":"${mobilertVolumeInContainer}" \
    ptpuscas/mobile_rt:"${1}" \
      "echo Current path in container: $(pwd) \
      && cp -rpf ${mobilertVolumeInContainer}/* . \
      && rm -rf app/third_party/boost \
      && rm -rf app/third_party/glm \
      && rm -rf app/third_party/googletest \
      && rm -rf app/third_party/pcg-cpp \
      && rm -rf app/third_party/stb \
      && rm -rf app/third_party/tinyobjloader \
      && rm -rf build_* \
      && git init \
      && ls -lahp . \
      && sh scripts/compile_native.sh -t release -c g++ -r yes";
}

# Helper command to execute the MobileRT unit tests in the docker container.
# The parameters are:
# * VERSION of the docker image
# * PARAMETERS for the unit tests
executeUnitTestsInDockerContainer() {
  docker run -t \
    -e DISPLAY="${DISPLAY}" \
    --name="mobile_rt_tests_${1}" \
    ptpuscas/mobile_rt:"${1}" "./build_release/bin/UnitTests ${2}";
  docker rm --force --volumes "mobile_rt_tests_${1}";
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
  docker commit mobile_rt_built_"${1}" ptpuscas/mobile_rt:"${1}";
  docker rm mobile_rt_built_"${1}";
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

  brew update;
  echo 'Install docker & colima.';
  brew install --ignore-dependencies --skip-cask-deps --skip-post-install docker colima;
  # For testcontainers to find the Colima socket
  # https://github.com/abiosoft/colima/blob/main/docs/FAQ.md#cannot-connect-to-the-docker-daemon-at-unixvarrundockersock-is-the-docker-daemon-running
  sudo ln -sf "${HOME}/.colima/default/docker.sock /var/run/docker.sock";

  echo 'Symlink Docker plugins, so Docker can find them.';
  mkdir -p "${HOME}/.docker/cli-plugins";
  ln -sfn /usr/local/opt/docker-buildx/bin/docker-buildx "${HOME}/.docker/cli-plugins/docker-buildx";
  ln -sfn /usr/local/opt/docker-compose/bin/docker-compose "${HOME}/.docker/cli-plugins/docker-compose";

  echo 'Start colima.';
  colima start || true;

  echo 'Docker commands detected:';
  echo "${PATH}" | sed 's/:/ /g' | xargs ls 2> /dev/null | grep -i docker 2> /dev/null || true;

  echo 'Validating docker command works.';
  docker --version;
  docker info;
  docker image ls -a;
  docker ps -a;
}
