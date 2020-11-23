#!/bin/bash

###############################################################################
# Change directory to MobileRT root
###############################################################################
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit
###############################################################################
###############################################################################

###############################################################################
# Get helper functions
###############################################################################
source scripts/helper_functions.sh
###############################################################################
###############################################################################

###############################################################################
# Install dependencies
###############################################################################
# if Debian based Linux
if [ -x "$(command -v apt-get)" ]; then
  callCommand sudo apt-get update -y;
  callCommand sudo apt-get install --no-install-recommends -y \
    xorg-dev \
    libxcb-render-util0-dev \
    libxcb-xkb-dev \
    libxcb-icccm4-dev \
    libxcb-image0-dev \
    libxcb-keysyms1-dev \
    libxcb-xinerama0-dev \
    libx11-xcb-dev \
    libxcb-randr0-dev \
    libxcb-shape0-dev \
    libxcb-sync-dev \
    libxcb-xfixes0-dev \
    pkg-config \
    bzip2 \
    sqlite3 \
    vim \
    findutils \
    make \
    bash \
    ca-certificates \
    git \
    libatomic1 \
    qt5-default \
    g++ \
    build-essential \
    lcov \
    python3 \
    python3-pip \
    python3-dev \
    python3-setuptools \
    sudo \
    || true;
# if MacOS
elif [ -x "$(command -v brew)" ]; then
  callCommand brew update || true;
  callCommand brew tap cartr/qt4 || true;
  callCommand brew uninstall --force openssl@1.0 || true;
  callCommand brew install libomp || true;
  callCommand brew install openssl@1.0 || true;
  callCommand brew install qt@4 || true;
  callCommand brew install qt || true;
  callCommand brew install llvm || true;
  callCommand brew install lcov || true;
  brew install python3;
  brew install pyenv;
fi

# Install Python
if [ -x "$(command -v choco)" ]; then
  callCommand choco install python --version 3.8.0;
fi
callCommand python3 -m pip install --upgrade pip;
callCommand pip3 install --upgrade setuptools pip;
callCommand pip3 install scikit-build;
###############################################################################
###############################################################################

###############################################################################
# Install Conan package manager
###############################################################################
function install_conan() {
  callCommand pip3 install cmake --upgrade;
  callCommand pip3 install conan;
  callCommand pip3 install clang;

  PATH=$(pip3 list -v | grep -i cmake | tr -s ' ' | cut -d ' ' -f 3):${PATH}
  PATH=$(pip3 list -v | grep -i conan | tr -s ' ' | cut -d ' ' -f 3):${PATH}

  CONAN_PATH=$(find ~/ -name "conan" -not -path "*/MobileRT/**/conan*");
  echo "Conan binary: ${CONAN_PATH}"
  echo "Conan location: ${CONAN_PATH%/conan}"
  export PATH=${CONAN_PATH%/conan}:${PATH}

  callCommand conan -v
  checkCommand conan

  CLANG_PATH=$(find / -name "clang");
  echo "Clang binary: ${CLANG_PATH}"
  echo "Clang location: ${CLANG_PATH%/clang}"
  PATH=${CLANG_PATH%/clang}:${PATH}

  echo "PATH: ${PATH}"
}

#install_conan
###############################################################################
###############################################################################

###############################################################################
# Test dependencies
###############################################################################
checkCommand vim
checkCommand cmake
checkCommand make
checkCommand bash
checkCommand git
checkCommand g++
# Can't install clang++ in docker container
#checkCommand clang++
checkCommand python3
checkCommand pip
checkCommand pip3
###############################################################################
###############################################################################
