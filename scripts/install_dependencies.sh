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
    cpulimit \
    sudo;
# if MacOS
elif [ -x "$(command -v brew)" ]; then
  callCommand brew update;
  callCommand brew tap cartr/qt4;
  callCommand brew uninstall --force openssl@1.0;
  callCommand brew install openssl@1.0;
  callCommand brew install qt@4;
  callCommand brew install llvm;

  # Install OpenMP
  brew install libomp;
  callCommand wget https://homebrew.bintray.com/bottles/libomp-11.1.0.catalina.bottle.tar.gz;
  # Fallback to manually extract lib
  callCommand tar -xzvf libomp-11.1.0.catalina.bottle.tar.gz;
  callCommand sudo mv libomp /usr/local/Cellar/libomp;

  # The following might give this error:
  # curl: (35) error:1400410B:SSL routines:CONNECT_CR_SRVR_HELLO:wrong version number
  # Error: Failed to download resource "cpulimit"
  # Download failed: https://homebrew.bintray.com/bottles/cpulimit-0.2.catalina.bottle.tar.gz
  brew install cpulimit;
  brew install lcov;
  brew install python3;
  brew install pyenv;

  MAJOR_MAC_VERSION=$(sw_vers | grep ProductVersion | cut -d ':' -f2 | cut -d '.' -f1 | tr -d '[:space:]')
  callCommand echo "MacOS '${MAJOR_MAC_VERSION}' detected"
  # This command needs sudo.
  callCommand sudo xcode-select --switch /System/Volumes/Data/Applications/Xcode_12.4.app/Contents/Developer;
fi

# Install Python
if [ -x "$(command -v choco)" ]; then
  callCommand choco install python --version 3.8.0;
fi

# if not Debian based Linux
if [ ! -x "$(command -v apt-get)" ]; then
  # Ensure pip is used by default
  callCommand python3 -m ensurepip --default-pip;
fi

python3 -m pip install --upgrade pip;
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
checkCommand python3
checkCommand pip
checkCommand pip3

# Can't install in docker container:
#checkCommand clang++
#checkCommand cpulimit
###############################################################################
###############################################################################
