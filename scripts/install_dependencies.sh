#!/usr/bin/env sh

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
set -eu;
###############################################################################
###############################################################################


###############################################################################
# Change directory to MobileRT root.
###############################################################################
cd "$(dirname "${0}")/.." || exit;
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
# shellcheck disable=SC1091
. scripts/helper_functions.sh;
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
install_dependencies() {
  if [ -x "$(command -v apt-get)" ]; then
    echo 'Detected Debian based Linux';
    install_dependencies_debian;
  elif [ -x "$(command -v yum)" ]; then
    echo 'Detected Red Hat based Linux';
    install_dependencies_red_hat;
  elif [ -x "$(command -v pacman)" ]; then
    echo 'Detected Arch based Linux';
    install_dependencies_arch;
  elif [ -x "$(command -v apk)" ]; then
    echo 'Detected Alpine based Linux';
    install_dependencies_alpine;
  elif [ -x "$(command -v emerge)" ]; then
    echo 'Detected Gentoo based Linux';
    install_dependencies_gentoo;
  elif [ -x "$(command -v brew)" ]; then
    echo 'Detected MacOS';
    install_dependencies_macos;
  else
    echo 'Detected unknown Operating System';
  fi
  update_python;
}

install_dependencies_debian() {
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
    sudo git ca-certificates shellcheck \
    libomp-dev \
    libatomic1 \
    qtbase5-dev qtbase5-dev-tools qtchooser qt5-qmake \
    g++ build-essential cmake make \
    lcov \
    python3 python3-pip python3-dev python3-setuptools \
    cpulimit;
    echo 'Installing dependencies that conan might use.';
    sudo apt-get install --no-install-recommends -y clang libc++-dev libc++abi-dev;
}

install_dependencies_red_hat() {
  yum update -y;
  dnf install -y \
    python3-pip \
    ShellCheck;
  yum install -y \
    vim \
    findutils \
    gcc-c++ cmake make \
    git ca-certificates \
    which \
    qt5-qtbase-devel;
  echo 'Installing dependencies that conan might use.';
  yum install -y mesa-libGL-devel;
  yum install -y libXaw-devel libXcomposite-devel libXcursor-devel \
  libXtst-devel libXinerama-devel \
  libXrandr-devel libXScrnSaver-devel libXdamage-devel \
  libXv-devel libuuid-devel xkeyboard-config-devel;
  yum install -y libfontenc libXdmcp libxkbfile libXres \
  xcb-util-wm xcb-util-image \
  xcb-util-keysyms xcb-util-renderutil libXxf86vm xcb-util;
  yum install -y libXvMC xorg-x11-xtrans;
}

install_dependencies_arch() {
  # https://wiki.archlinux.org/title/Pacman/Package_signing#Upgrade_system_regularly
  echo 'Removing packages from cache';
  rm -rf /var/cache/pacman/pkg/;
  pacman -Sy archlinux-keyring --noconfirm --needed;
  echo 'Resetting all the keys';
  pacman-key --init;
  pacman-key --populate;
  echo 'Upgrade system';
  pacman -Syu --noconfirm --needed;
  pacman -Sy --noconfirm --needed \
    glibc lib32-glibc \
    vim \
    findutils \
    cmake make \
    shellcheck \
    git ca-certificates \
    which \
    qt5-base \
    python3 \
    gcc;
}

install_dependencies_alpine() {
  apk update;
  apk add \
    vim \
    findutils \
    cmake make ncurses \
    shellcheck \
    git ca-certificates \
    qt5-qtbase-dev \
    which \
    g++ gcc \
    py3-pip;
  echo 'Installing dependencies that conan might use.';
  apk add libfontenc-dev libxaw-dev libxcomposite-dev libxcursor-dev libxi-dev \
  libxinerama-dev libxkbfile-dev libxrandr-dev libxres-dev libxscrnsaver-dev \
  libxtst-dev libxv-dev libxvmc-dev xcb-util-wm-dev;
}

install_dependencies_gentoo() {
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
    shellcheck \
    dev-vcs/git ca-certificates \
    which \
    dev-qt/qtcore dev-qt/qtgui dev-qt/qtwidgets;
}

install_dependencies_macos() {
  echo 'Update homebrew (to use the new repository).';
  sh -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/uninstall.sh)";
  sh -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)";

  brew --version;
  brew update;
  brew cleanup;

  echo 'Install and configure git.';
  brew install git;
  git config --global http.postBuffer 1048576000;
  git config --global https.postBuffer 1048576000;
  git config --global core.compression -1;
  git config --global http.sslVerify "false";
  if [ -z "$(git config credential.https://github.com)" ]; then
    echo 'Configuring github credentials.';
    git config --global credential.https://github.com "ci-user";
  fi
  if [ -z "$(git config user.name)" ]; then
    echo 'Configuring git user.';
    git config --global user.name "CI User";
  fi
  if [ -z "$(git config user.email)" ]; then
    echo 'Configuring git email.';
    git config --global user.email "user@ci.com";
  fi

  echo 'Change Homebrew to a specific version since the latest one might break some packages URLs.';
  # E.g.: version 3.3.15 breaks the Qt4 package.
  oldpath=$(pwd);
  cd /usr/local/Homebrew || exit;

  git fetch --tags --all;
  git checkout 3.3.14;
  echo 'Avoid homebrew from auto-update itself every time its installed something.';
  export HOMEBREW_NO_AUTO_UPDATE=1;
  brew --version;

  echo 'Install packages separately, so it continues regardless if some error occurs in one.';
  brew install cmake;
  brew install gcc@12; # GCC v12 is the latest version compatible with conan.
  brew install openssl@1.1;
  brew install shellcheck;
  brew install llvm;
  brew install libomp;
  brew install cpulimit;
  brew install lcov;
  brew install python3;
  brew install pyenv;
  brew install conan;
  cd "${oldpath}" || exit;

  MAJOR_MAC_VERSION=$(sw_vers | grep ProductVersion | cut -d ':' -f2 | cut -d '.' -f1 | tr -d '[:space:]');
  echo "MacOS '${MAJOR_MAC_VERSION}' detected";
  # This command needs sudo.
  # With Xcode_14.0.1.app, it throws this error on MacOS-12:
  # ld: Assertion failed: (_file->_atomsArrayCount == computedAtomCount && "more atoms allocated than expected")
  # For more information, check: https://stackoverflow.com/questions/73714336/xcode-update-to-version-2395-ld-compile-problem-occurs-computedatomcount-m
  # Recommended Xcode_13.2.app because it seems the only one compatible with MacOS-11 & MacOS-12.
  sudo xcode-select --switch /System/Volumes/Data/Applications/Xcode_13.2.app/Contents/Developer;
}

# Update Python, PIP and CMake versions if necessary.
update_python() {
  if [ -x "$(command -v choco)" ]; then
    echo 'Install Python with choco';
    choco install python --version 3.8.0;
  fi

  if [ ! -x "$(command -v apt-get)" ]; then
    echo 'Not Debian based Linux detected';
    echo 'Ensure pip is used by default';
    python3 -m ensurepip --default-pip;
  fi

  echo 'Upgrade pip';
  pip install --upgrade pip --user;
  pip3 install --upgrade pip --user;
  executeWithoutExiting python3 -m pip install --upgrade pip --user;
  pip3 install cmake --upgrade --user;

  addCommandToPath "cmake";
}
###############################################################################
###############################################################################


###############################################################################
# Install Conan package manager.
###############################################################################
install_conan() {
  # Necessary to install python 3.9 which uses six version 1.15!
  # Packages that should be used: six==1.15.0 conan==1.51.3 conan-package-tools
  echo "Installing conan";
  pip3 install six==1.15.0 conan==1.51.3 conan-package-tools --ignore-installed six;
  echo "Installed conan!";
  pip3 install clang;

  PATH=$(pip3 list -v | grep -i cmake | tr -s ' ' | cut -d ' ' -f 3 | head -1):${PATH};
  PATH=$(pip3 list -v | grep -i conan | tr -s ' ' | cut -d ' ' -f 3 | head -1):${PATH};

  addCommandToPath "conan";

  conan -v;
  checkCommand conan;
}
###############################################################################
###############################################################################


###############################################################################
# Test dependencies.
###############################################################################
test_commands() {
  echo "Checking required shell commands.";

  checkCommand vim;
  checkCommand cmake;
  checkCommand make;
  checkCommand sh;
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
echo "Host OS: $(uname -a)";
executeWithoutExiting install_dependencies;
install_conan;
test_commands;
###############################################################################
###############################################################################
