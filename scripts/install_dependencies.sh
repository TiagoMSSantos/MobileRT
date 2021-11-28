#!/bin/bash

###############################################################################
# README
###############################################################################
# This script installs all the necessary dependencies in order to compile
# MobileRT.
#
# At the moment, this script installs the necessary dependencies for the
# following Operating Systems:
# * Linux
# * MacOS
#
# And it is compatible with the following Linux distributions:
# * Debian
# * Red Hat
# * Arch
# * Alpine
# * Gentoo
###############################################################################
###############################################################################


###############################################################################
# Exit immediately if a command exits with a non-zero status.
###############################################################################
set -euo pipefail;
###############################################################################
###############################################################################


###############################################################################
# Change directory to MobileRT root.
###############################################################################
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit;
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
source scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Install dependencies.
###############################################################################
set +e;
if [ -x "$(command -v apt-get)" ]; then
  echo "Detected Debian based Linux";

  sudo apt-get update -y;
  sudo apt-get install --no-install-recommends -y;
  sudo apt-get install --no-install-recommends -y \
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
    cmake \
    sudo;
elif [ -x "$(command -v yum)" ]; then
  echo "Detected Red Hat based Linux";

  yum update -y;
  yum install -y \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    git \
    which \
    qt5-qtbase-devel \
    python36 \
    gcc-c++;
elif [ -e "$(command -v pacman)" ]; then
  echo "Detected Arch based Linux";

  patched_glibc=glibc-linux4-2.33-4-x86_64.pkg.tar.zst && \
  curl -LO "https://repo.archlinuxcn.org/x86_64/${patched_glibc}" && \
  bsdtar -C / -xvf "${patched_glibc}";

  pacman -Syu --noconfirm --needed;
  pacman -Sy --noconfirm --needed \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    git \
    which \
    qt5-base \
    python3 \
    gcc;
elif [ -x "$(command -v apk)" ]; then
  echo "Detected Alpine based Linux";

  apk update;
  apk add \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    git \
    qt5-qtbase-dev \
    which \
    g++ \
    py3-pip \
    gcc;
elif [ -x "$(command -v emerge)" ]; then
  echo "Detected Gentoo based Linux";

  emerge --sync;
  emerge sys-apps/portage;
  emerge app-portage/layman;
  emerge dev-libs/icu;
  echo 'FEATURES="-sandbox -usersandbox"' >> /etc/portage/make.conf;
  echo 'USE="dev-libs/libpcre2-10.35 pcre16 x11-libs/libxkbcommon-1.0.3 media-libs/libglvnd-1.3.2-r2 X"' >> /etc/portage/make.conf;
  emerge \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    dev-vcs/git \
    which \
    sys-devel/gcc \
    dev-qt/qtcore \
    dev-qt/qtgui \
    dev-qt/qtwidgets;
elif [ -x "$(command -v brew)" ]; then
  echo "Detected MacOS";

  # Update homebrew (to use the new repository).
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/uninstall.sh)";
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)";

  brew update;
  brew cleanup;
  brew tap cartr/qt4;
  brew uninstall --force openssl@1.0;
  brew install openssl@1.0;
  brew install qt@4;
  brew install llvm;
  brew install libomp;
  brew install cpulimit;
  brew install lcov;
  brew install python3;
  brew install pyenv;

  MAJOR_MAC_VERSION=$(sw_vers | grep ProductVersion | cut -d ':' -f2 | cut -d '.' -f1 | tr -d '[:space:]');
  echo "MacOS '${MAJOR_MAC_VERSION}' detected";
  # This command needs sudo.
  sudo xcode-select --switch /System/Volumes/Data/Applications/Xcode_12.4.app/Contents/Developer;
else
  echo "Detected unknown Operating System";
fi
set -e;

if [ -x "$(command -v choco)" ]; then
  echo "Install Python with choco";
  choco install python --version 3.8.0;
fi

if [ ! -x "$(command -v apt-get)" ]; then
  echo "Not Debian based Linux detected";
  echo "Ensure pip is used by default";
  python3 -m ensurepip --default-pip;
fi

echo "Upgrade pip";
set +e;
python3 -m pip install --upgrade pip;
set -e;
pip3 install cmake --upgrade;
###############################################################################
###############################################################################


###############################################################################
# Install Conan package manager.
###############################################################################
function install_conan() {
  pip3 install conan;
  pip3 install clang;

  PATH=$(pip3 list -v | grep -i cmake | tr -s ' ' | cut -d ' ' -f 3):${PATH};
  PATH=$(pip3 list -v | grep -i conan | tr -s ' ' | cut -d ' ' -f 3):${PATH};

  CONAN_PATH=$(find ~/ -iname "conan" -not -path "*/MobileRT/**/conan*");
  echo "Conan binary: ${CONAN_PATH}";
  echo "Conan location: ${CONAN_PATH%/conan}";
  export PATH=${CONAN_PATH%/conan}:${PATH};

  conan -v;
  checkCommand conan;

  CLANG_PATH=$(find / -iname "clang");
  echo "Clang binary: ${CLANG_PATH}";
  echo "Clang location: ${CLANG_PATH%/clang}";
  PATH=${CLANG_PATH%/clang}:${PATH};

  echo "PATH: ${PATH}";
}


#install_conan;
###############################################################################
###############################################################################


###############################################################################
# Test dependencies.
###############################################################################
checkCommand vim;
checkCommand cmake;
checkCommand make;
checkCommand bash;
checkCommand git;
checkCommand g++;
checkCommand python3;
checkCommand pip;
checkCommand pip3;

# Can't install in docker container:
#checkCommand clang++;
#checkCommand cpulimit;
###############################################################################
###############################################################################
