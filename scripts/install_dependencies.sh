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
# shellcheck disable=SC1091
source scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Execute Shellcheck on this script.
###############################################################################
if [ -x "$(command -v shellcheck)" ]; then
  shellcheck "${0}" || exit
fi
###############################################################################
###############################################################################


###############################################################################
# Install dependencies.
###############################################################################
function install_dependencies() {
  if [ -x "$(command -v apt-get)" ]; then
    echo "Detected Debian based Linux";
    install_dependencies_debian;
  elif [ -x "$(command -v yum)" ]; then
    echo "Detected Red Hat based Linux";
    install_dependencies_red_hat;
  elif [ -x "$(command -v pacman)" ]; then
    echo "Detected Arch based Linux";
    install_dependencies_arch;
  elif [ -x "$(command -v apk)" ]; then
    echo "Detected Alpine based Linux";
    install_dependencies_alpine;
  elif [ -x "$(command -v emerge)" ]; then
    echo "Detected Gentoo based Linux";
    install_dependencies_gentoo;
  elif [ -x "$(command -v brew)" ]; then
    echo "Detected MacOS";
    install_dependencies_macos;
  else
    echo "Detected unknown Operating System";
  fi
  update_python;
}

function install_dependencies_debian() {
  sudo apt-get update -y;
  sudo apt-get install --no-install-recommends -y \
    xorg-dev \
    libx11-xcb-dev \
    libxcb-render-util0-dev libxcb-xkb-dev libxcb-icccm4-dev libxcb-image0-dev \
    libxcb-keysyms1-dev libxcb-xinerama0-dev libxcb-randr0-dev libxcb-shape0-dev \
    libxcb-sync-dev libxcb-xfixes0-dev \
    pkg-config \
    bzip2 \
    sqlite3 \
    vim \
    findutils \
    sudo git bash ca-certificates shellcheck \
    libatomic1 \
    qtbase5-dev qtbase5-dev-tools qtchooser qt5-qmake \
    g++ build-essential cmake make \
    lcov \
    python3 python3-pip python3-dev python3-setuptools \
    cpulimit;
}

function install_dependencies_red_hat() {
  yum update -y;
  dnf install -y \
    python3-pip \
    ShellCheck;
  yum install -y \
    vim \
    findutils \
    gcc-c++ cmake make \
    bash \
    git ca-certificates \
    which \
    qt5-qtbase-devel;
}

function install_dependencies_arch() {
  pacman -Syu --noconfirm --needed;
  pacman -Sy --noconfirm --needed \
    glibc lib32-glibc \
    vim \
    findutils \
    cmake make \
    bash shellcheck \
    git ca-certificates \
    which \
    qt5-base \
    python3 \
    gcc;
}

function install_dependencies_alpine() {
  apk update;
  apk add \
    vim \
    findutils \
    cmake make \
    bash shellcheck \
    git ca-certificates \
    qt5-qtbase-dev \
    which \
    g++ gcc \
    py3-pip;
}

function install_dependencies_gentoo() {
  emerge --sync;
  emerge sys-apps/portage;
  emerge app-portage/layman;
  emerge dev-libs/icu;
  echo 'FEATURES="-sandbox -usersandbox"' >> /etc/portage/make.conf;
  echo 'USE="dev-libs/libpcre2-10.35 pcre16 x11-libs/libxkbcommon-1.0.3 media-libs/libglvnd-1.3.2-r2 X"' >> /etc/portage/make.conf;
  emerge \
    vim \
    findutils \
    sys-devel/gcc cmake make \
    bash shellcheck \
    dev-vcs/git ca-certificates \
    which \
    dev-qt/qtcore dev-qt/qtgui dev-qt/qtwidgets;
}

function install_dependencies_macos() {
  echo "Update homebrew (to use the new repository).";
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/uninstall.sh)";
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)";

  brew --version;
  brew update;
  brew cleanup;

  echo "Install and configure git.";
  brew install git;
  git config --global http.postBuffer 1048576000;
  git config --global https.postBuffer 1048576000;
  git config --global core.compression -1;
  git config --global http.sslVerify "false";
  if [[ -z "$(git config credential.https://github.com)" ]]; then
    echo "Configuring github credentials.";
    git config --global credential.https://github.com "ci-user";
  fi
  if [[ -z "$(git config user.name)" ]]; then
    echo "Configuring git user.";
    git config --global user.name "CI User";
  fi
  if [[ -z "$(git config user.email)" ]]; then
    echo "Configuring git email.";
    git config --global user.email "user@ci.com";
  fi

  echo "Change Homebrew to a specific version since the latest one might break some packages URLs.";
  # E.g.: version 3.3.15 breaks the Qt4 package.
  cd /usr/local/Homebrew;
  git fetch --tags --all;
  git checkout 3.3.14;
  echo "Avoid homebrew from auto-update itself every time its installed something.";
  export HOMEBREW_NO_AUTO_UPDATE=1;
  brew --version;

  echo "Add more repositories with Qt to the list of formulae that brew tracks.";
  brew tap cartr/qt4;
  # brew tap cartr/qt5;

  echo "Install packages separately, so it continues regardless if some error occurs in one.";
  brew install openssl@1.0;
  brew install openssl@1.1;
  brew install shellcheck;
  brew install llvm;
  brew install libomp;
  brew install cpulimit;
  brew install lcov;
  brew install python3;
  brew install pyenv;
  # brew install qt;
  brew install qt@4;
  # brew install qt@5;
  # brew install qt --build-from-source --HEAD;

  MAJOR_MAC_VERSION=$(sw_vers | grep ProductVersion | cut -d ':' -f2 | cut -d '.' -f1 | tr -d '[:space:]');
  echo "MacOS '${MAJOR_MAC_VERSION}' detected";
  # This command needs sudo.
  sudo xcode-select --switch /System/Volumes/Data/Applications/Xcode.app/Contents/Developer;
}

# Update Python, PIP and CMake versions if necessary.
function update_python() {
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
  pip install --upgrade pip --user;
  pip3 install --upgrade pip --user;
  executeWithoutExiting python3 -m pip install --upgrade pip --user;
  pip3 install cmake --upgrade --user;

  echo "Adding cmake to PATH.";
  set +e;
  CMAKE_PATH=$(find ~/ -iname "cmake" 2> /dev/null | head -1);
  set -e;
  echo "CMAKE_PATH: ${CMAKE_PATH}";
  export PATH=${CMAKE_PATH%/cmake}:${PATH};
}
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
function check_qt() {
  echo "Checking Qt installation.";
  QT_PATH=$(find /usr/local -name "QDialog" -not -path "*/MobileRT" 2> /dev/null);
  echo "QT path Qt 1: ${QT_PATH}";
  QT_PATH=$(find /usr/local -name "qtwidgetsglobal.h" -not -path "*/MobileRT" 2> /dev/null);
  echo "QT path QtWidget 2: ${QT_PATH}";
  QT_PATH=$(find /usr/local -name "qtguiglobal.h" -not -path "*/MobileRT" 2> /dev/null);
  echo "QT path QtGui 3: ${QT_PATH}";
  QT_PATH=$(find /usr/local -name "qglobal.h" -not -path "*/MobileRT" 2> /dev/null);
  echo "QT path QtCore 4: ${QT_PATH}";
  QT_PATH=$(find /usr/local -name "QDesktopServices" -not -path "*/MobileRT" 2> /dev/null);
  echo "QT path Qt 5: ${QT_PATH}";
}

function test_commands() {
  echo "Checking required bash commands.";
  # check_qt;

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
}
###############################################################################
###############################################################################


###############################################################################
# Execute script.
###############################################################################
executeWithoutExiting install_dependencies;
test_commands;
###############################################################################
###############################################################################
