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
# * MacOS (using Homebrew)
# * Windows (using Chocolatey)
#
# And it is compatible with the following Linux distributions:
# * Debian (using apt)
# * Red Hat (using yum)
# * Arch (using pacman)
# * Alpine (using apk)
# * Gentoo (using emerge)
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
if [ $# -ge 1 ]; then
  cd "$(dirname "${0}")/.." || return 1;
fi
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
if [ $# -ge 1 ] && command -v shellcheck > /dev/null; then
  shellcheck "${0}" --exclude=SC1017 || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Install dependencies.
###############################################################################
install_dependencies() {
  if uname -a | grep -iq 'linux' && command -v apt-get > /dev/null; then
    echo 'Detected Debian based Linux';
    install_dependencies_debian;
    checkCommand lcov;
    checkCommand readelf;
  elif uname -a | grep -iq 'linux' && command -v yum > /dev/null; then
    echo 'Detected Red Hat based Linux';
    install_dependencies_red_hat;
  elif uname -a | grep -iq 'linux' && command -v pacman > /dev/null; then
    echo 'Detected Arch based Linux';
    install_dependencies_arch;
  elif uname -a | grep -iq 'linux' && command -v apk > /dev/null; then
    echo 'Detected Alpine based Linux';
    install_dependencies_alpine;
  elif uname -a | grep -iq 'linux' && command -v emerge > /dev/null; then
    echo 'Detected Gentoo based Linux';
    install_dependencies_gentoo;
  elif uname -a | grep -iq 'darwin' && command -v brew > /dev/null; then
    echo 'Detected MacOS';
    install_dependencies_macos;
    checkCommand lcov;
    checkCommand readelf;
  elif uname -a | grep -iq 'msys' && command -v choco > /dev/null; then
    echo 'Detected Windows';
    # Requires running chocolatey in an elevated command shell.
    install_dependencies_windows;
    checkCommand lcov;
  else
    echo 'Detected unknown Operating System';
  fi
  # update_python;
}

# Run any apt-get subcommand with timeout and retry, recovering from dpkg lock/interrupted
# state between attempts (timeout kills apt-get but may leave the dpkg child holding the lock).
# Usage: apt_get_with_retry <subcommand> [args...]
#   e.g. apt_get_with_retry update -y
#        apt_get_with_retry install --no-install-recommends -y vim
apt_get_with_retry() {
  _max_retries=3;
  _retry=0;
  while [ "${_retry}" -lt "${_max_retries}" ]; do
    _retry=$((_retry + 1));
    # Wait briefly for any orphaned dpkg process (left by a timed-out apt-get) to finish
    # and release its lock, then fix any interrupted dpkg state before the next attempt.
    sudo dpkg --configure -a 2>&1 || true;
    timeout 180 sudo apt-get -o DPkg::Lock::Timeout=30 "$@" && return 0;
    echo "apt-get '$*' failed or timed out (attempt ${_retry} of ${_max_retries})";
    sleep 5;
  done;
  return 1;
}

install_dependencies_debian() {
  if ! command -v sudo > /dev/null; then
    echo 'Installing sudo.';
    apt_get_with_retry install --no-install-recommends -y sudo;
  fi
  sudo rm /etc/apt/sources.list.d/microsoft-prod.list || true;

  echo 'Updating APT repositories.';
  apt_get_with_retry update -y || true;

  if ! command -v lcov > /dev/null; then
    echo 'Installing APT coverage dependencies (lcov).';
    apt_get_with_retry install --no-install-recommends -y lcov;
  fi
  if ! command -v g++ > /dev/null; then
    echo 'Installing APT host build tools (g++).';
    apt_get_with_retry install --no-install-recommends -y g++;
  fi
  if ! dpkg -s build-essential > /dev/null 2>&1; then
    echo 'Installing APT host build tools (build-essential).';
    apt_get_with_retry install --no-install-recommends -y build-essential;
  fi
  if ! command -v cmake > /dev/null; then
    echo 'Installing APT host build tools (cmake).';
    apt_get_with_retry install --no-install-recommends -y cmake;
  fi
  if ! command -v make > /dev/null; then
    echo 'Installing APT host build tools (make).';
    apt_get_with_retry install --no-install-recommends -y make;
  fi
  if ! command -v vim > /dev/null; then
    echo 'Installing vim.';
    apt_get_with_retry install --no-install-recommends -y vim;
  fi
  if ! command -v git > /dev/null; then
    echo 'Installing git.';
    apt_get_with_retry install --no-install-recommends -y git;
  fi
  if ! command -v git-lfs > /dev/null; then
    echo 'Installing git-lfs.';
    apt_get_with_retry install --no-install-recommends -y git-lfs;
  fi
  if ! command -v shellcheck > /dev/null; then
    echo 'Installing shellcheck.';
    apt_get_with_retry install --no-install-recommends -y shellcheck;
  fi
  if ! command -v python3 > /dev/null; then
    echo 'Installing python3.';
    apt_get_with_retry install --no-install-recommends -y python3;
  fi
  if ! dpkg -s python3-dev > /dev/null 2>&1; then
    echo 'Installing python3-dev.';
    apt_get_with_retry install --no-install-recommends -y python3-dev;
  fi
  if ! dpkg -s python3-setuptools > /dev/null 2>&1; then
    echo 'Installing python3-setuptools.';
    apt_get_with_retry install --no-install-recommends -y python3-setuptools;
  fi
  if ! command -v pip3 > /dev/null; then
    echo 'Installing pip3.';
    apt_get_with_retry install --no-install-recommends -y python3-pip;
  fi
  if ! command -v cpulimit > /dev/null; then
    echo 'Installing cpulimit.';
    apt_get_with_retry install --no-install-recommends -y cpulimit;
  fi
  if ! command -v lsof > /dev/null; then
    echo 'Installing lsof.';
    apt_get_with_retry install --no-install-recommends -y lsof;
  fi
  if ! command -v zip > /dev/null; then
    echo 'Installing zip.';
    apt_get_with_retry install --no-install-recommends -y zip;
  fi
  if ! command -v unzip > /dev/null; then
    echo 'Installing unzip.';
    apt_get_with_retry install --no-install-recommends -y unzip;
  fi
  if ! command -v clang++ > /dev/null; then
    echo 'Installing dependencies that conan might use.';
    apt_get_with_retry install --no-install-recommends -y clang;
  fi
  if ! dpkg -s libc++-dev > /dev/null 2>&1; then
    echo 'Installing libc++-dev.';
    apt_get_with_retry install --no-install-recommends -y libc++-dev;
  fi
  if ! dpkg -s libc++abi-dev > /dev/null 2>&1; then
    echo 'Installing libc++abi-dev.';
    apt_get_with_retry install --no-install-recommends -y libc++abi-dev;
  fi
  if ! command -v readelf > /dev/null; then
    echo 'Installing readelf.';
    apt_get_with_retry install --no-install-recommends -y binutils;
  fi
  if ! dpkg -s qtbase5-dev > /dev/null 2>&1; then
    echo 'Installing Qt';
    apt_get_with_retry install --no-install-recommends -y qtbase5-dev;
  fi
}

install_dependencies_red_hat() {
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False update -y;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y epel-release;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False update -y;
  dnf install -y python3-pip;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y ShellCheck;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y vim;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y findutils;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y gcc-c++;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y cmake;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y make;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y git;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y git-lfs;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y ca-certificates;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y which;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y qt5-qtbase-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y procps; # Install 'ps'
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y lsof; # Install 'lsof'

  echo 'Installing dependencies that conan might use.';
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y mesa-libGL-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXaw-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXcomposite-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXcursor-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXtst-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXinerama-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXrandr-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXScrnSaver-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXdamage-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXv-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libuuid-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y xkeyboard-config-devel;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libfontenc;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXdmcp;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libxkbfile;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXres;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y xcb-util-wm;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y xcb-util-image;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y xcb-util-keysyms;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y xcb-util-renderutil;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y libXxf86vm;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y xcb-util;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y zip;
  yum --setopt=skip_missing_names_on_install=False --setopt=skip_missing_names_on_update=False install -y unzip;
}

install_dependencies_arch() {
  echo 'Installing dependencies';
  pacman -Sy --noconfirm --needed --noscriptlet icu qt5-base gcc;

  if ! command -v cmake > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet cmake;
  fi
  if ! command -v vim > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet vim;
  fi
  if ! command -v make > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet make;
  fi
  if ! command -v shellcheck > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet ghc-libs shellcheck;
  fi
  if ! command -v git > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet ca-certificates git;
  fi
  if ! command -v git-lfs > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet git-lfs;
  fi
  if ! command -v which > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet which;
  fi
  if ! command -v python3 > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet python3;
  fi
  if ! command -v lsof > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet lsof;
  fi
  if ! command -v zip > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet zip;
  fi
  if ! command -v unzip > /dev/null; then
    pacman -S --noconfirm --needed --noscriptlet unzip;
  fi
  echo 'Installed dependencies';
}

install_dependencies_alpine() {
  apk update;
  apk add \
    vim \
    findutils \
    cmake make ncurses \
    shellcheck \
    git git-lfs ca-certificates \
    qt5-qtbase-dev \
    which \
    g++ gcc \
    py3-pip \
    zip unzip;
  echo 'Installing dependencies that conan might use.';
  apk add libfontenc-dev libxaw-dev libxcomposite-dev libxcursor-dev libxi-dev \
    libxinerama-dev libxkbfile-dev libxrandr-dev libxres-dev libxscrnsaver-dev \
    libxtst-dev libxv-dev libxvmc-dev xcb-util-wm-dev;
}

install_dependencies_gentoo() {
  echo 'FEATURES="-sandbox -usersandbox -ipc-sandbox -network-sandbox -pid-sandbox"' >> /etc/portage/make.conf;
  echo 'USE="dev-libs/libpcre2-10.35 pcre16 x11-libs/libxkbcommon-1.0.3 media-libs/libglvnd-1.3.2-r2 X"' >> /etc/portage/make.conf;
  echo 'Emerge sync';
  emerge --sync || true;
  echo 'Emerge sys-apps/portage';
  emerge --changed-use sys-apps/portage;
  echo 'Emerge dev-libs/icu';
  emerge --changed-use dev-libs/icu;
  echo 'Emerge app-editors/vim';
  emerge --changed-use app-editors/vim;
  echo 'Emerge findutils';
  emerge --changed-use findutils;
  echo 'Emerge cmake';
  emerge --changed-use cmake;
  echo 'Emerge make';
  emerge --changed-use make;
  echo 'Emerge dev-vcs/git';
  emerge --changed-use dev-vcs/git;
  echo 'Emerge dev-vcs/git-lfs';
  emerge --changed-use dev-vcs/git-lfs;
  echo 'Emerge ca-certificates';
  emerge --changed-use ca-certificates;
  echo 'Emerge which';
  emerge --changed-use which;
  echo 'Emerge xcb';
  emerge --changed-use xcb;
  echo 'Emerge dev-qt/qtcore';
  emerge --changed-use dev-qt/qtcore;
  echo 'Emerge dev-qt/qtgui';
  emerge --changed-use dev-qt/qtgui;
  echo 'Emerge dev-qt/qtwidgets';
  emerge --changed-use dev-qt/qtwidgets;
  echo 'Emerge dev-lang/python';
  emerge --changed-use dev-lang/python;
  echo 'Emerge shellcheck';
  emerge --changed-use dev-util/shellcheck-bin;
  echo 'Emerge lsof';
  emerge --changed-use sys-process/lsof;
  echo 'Emerge zip';
  emerge --changed-use app-arch/zip;
  echo 'Emerge unzip';
  emerge --changed-use app-arch/unzip;
}

install_dependencies_macos() {
  echo 'Update homebrew (to use the new repository).';
  brew --version;
  brew update || true;

  echo 'Avoid homebrew from auto-update itself every time its installed something.';
  export HOMEBREW_NO_AUTO_UPDATE=1;

  # `port selfupdate` (~1-2 min) and the unconditional `port install git git-lfs`
  # are the slowest steps of this function. Skip them when git and git-lfs are
  # already present (the common case on GitHub-hosted macOS runners). If Qt
  # later needs a macports install, that step will do its own work with whatever
  # package definitions are cached locally; in practice the cached definitions
  # on a fresh runner are recent enough, and a stale macports falls back to brew.
  if ! command -v git > /dev/null || ! command -v git-lfs > /dev/null; then
    echo 'Install packages separately, so it continues regardless if some error occurs in one.';
    set +e;
    sudo port help;
    echo 'Updating MacPorts.';
    sudo port -bn selfupdate;
    echo 'Installing git via MacPorts.';
    sudo port -bn install git git-lfs;
    installedGit="$?";
    if [ "${installedGit}" != '0' ]; then
      echo 'Installing git via MacPorts failed. Installing git via Homebrew.';
      brew list git > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install git;
      brew list git-lfs > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install git-lfs;
    fi
    set -e;
  fi

  if ! command -v timeout > /dev/null; then
    echo 'Installing coreutils.';
    brew list coreutils > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install coreutils || true; # To install 'timeout' command.
  fi
  if ! command -v zip > /dev/null; then
    echo 'Installing zip.';
    brew list zip > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install zip;
  fi
  if ! command -v unzip > /dev/null; then
    echo 'Installing unzip.';
    brew list unzip > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install unzip;
  fi
  if ! command -v lcov > /dev/null; then
    echo 'Installing lcov.';
    brew list lcov > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install lcov;
  fi
  if ! command -v shellcheck > /dev/null; then
    echo 'Installing shellcheck.';
    brew list shellcheck > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install shellcheck;
  fi
  if ! command -v readelf > /dev/null; then
    echo 'Installing readelf.';
    brew list binutils > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install binutils;
    echo 'export PATH="/usr/local/opt/binutils/bin:${PATH}"' >> ~/.bash_profile;
    echo 'export PATH="/opt/homebrew/opt/binutils/bin:${PATH}"' >> ~/.bash_profile;
    . ~/.bash_profile;
  fi

  set +e;
  test -d Qt;
  # shellcheck disable=SC2319
  qtAlreadyInstalled="$?";
  if [ "${qtAlreadyInstalled}" = '0' ]; then
    set -e;
    echo 'Detected Qt folder in MobileRT root dir. Assuming Qt is already installed there. Not installing Qt via package manager.';
    # If github action jurplel/install-qt-action was used, then Qt should be at root of project. E.g. paths:
    # * Qt/5.15.2/msvc2019_64/include/QtWidgets/QDialog
    # * Qt/6.9.0/msvc2022_64/include/QtWidgets/QDialog
  else
    echo 'Installing Qt via MacPorts.';
    sudo port -bn install qt5;
    installedLibQt="$?";
    set -e;
    if [ "${installedLibQt}" != '0' ]; then
      echo 'Installing Qt via MacPorts failed. Installing Qt via Homebrew.';
      brew unlink openssl;
      brew link --overwrite openssl;
      brew list qt@5 > /dev/null 2>&1 || brew install --skip-cask-deps --skip-post-install qt@5;
    fi
  fi

  MAJOR_MAC_VERSION=$(sw_vers | grep ProductVersion | cut -d ':' -f2 | cut -d '.' -f1 | tr -d '[:space:]');
  echo "MacOS '${MAJOR_MAC_VERSION}' detected";
  # This command needs sudo.
  # With Xcode_14.0.1.app, it throws this error on MacOS-12:
  # ld: Assertion failed: (_file->_atomsArrayCount == computedAtomCount && "more atoms allocated than expected")
  # For more information, check: https://stackoverflow.com/questions/73714336/xcode-update-to-version-2395-ld-compile-problem-occurs-computedatomcount-m
  if [ "${MAJOR_MAC_VERSION}" = 12 ]; then
    # Recommended Xcode_13.2.app because it seems the only one compatible with MacOS-12:
    sudo xcode-select --switch /System/Volumes/Data/Applications/Xcode_13.2.1.app/Contents/Developer;
    ls -lahp /System/Volumes/Data/Applications/Xcode_13.2.1.app/Contents/Developer;
  fi
  if [ "${MAJOR_MAC_VERSION}" = 13 ]; then
    # To be compatible with MacOS-13:
    sudo xcode-select --switch /System/Volumes/Data/Applications/Xcode_14.1.app/Contents/Developer;
    ls -lahp /System/Volumes/Data/Applications/Xcode_14.1.app/Contents/Developer;
  fi
}

install_dependencies_windows() {
  if ! command -v git-lfs > /dev/null; then
    # Install git-lfs: https://community.chocolatey.org/packages/git-lfs
    callCommandUntilSuccess 2 choco install -y git-lfs;
  fi

  if ! command -v cmake > /dev/null; then
    # Install cmake: https://community.chocolatey.org/packages/cmake
    callCommandUntilSuccess 2 choco install -y cmake --installargs 'ADD_CMAKE_TO_PATH=System';
  fi

  if ! command -v make > /dev/null; then
    # Install make: https://community.chocolatey.org/packages/make
    callCommandUntilSuccess 2 choco install -y make;
  fi

  if ! command -v shellcheck > /dev/null; then
    # Install shellcheck: https://community.chocolatey.org/packages/shellcheck
    callCommandUntilSuccess 2 choco install -y shellcheck;
  fi

  if ! command -v lcov > /dev/null; then
    # Install lcov: https://community.chocolatey.org/packages/lcov
    callCommandUntilSuccess 2 choco install -y lcov;
    export PATH="${PATH}:/c/ProgramData/chocolatey/lib/lcov/tools/bin:../ProgramData/chocolatey/lib/lcov/tools/bin";
  fi

  set +e;
  test -d Qt;
  qtAlreadyInstalled="$?";
  set -e;
  if [ "${qtAlreadyInstalled}" = '0' ]; then
    echo 'Detected Qt folder in MobileRT root dir. Assuming Qt is already installed there. Not installing Qt via package manager.';
    # If github action jurplel/install-qt-action was used, then Qt should be at root of project. E.g. paths:
    # * Qt/5.15.2/msvc2019_64/include/QtWidgets/QDialog
    # * Qt/6.9.0/msvc2022_64/include/QtWidgets/QDialog
  else
    echo 'Installing Qt via Chocolatey.';
    # Install Qt: https://community.chocolatey.org/packages/qt5-default
    callCommandUntilSuccess 2 choco install -y qt5-default;
  fi
}

# Update Python, PIP and CMake versions if necessary.
update_python() {
  if ! command -v apt-get > /dev/null; then
    echo 'Not Debian based Linux detected';
    echo 'Ensure pip is used by default';
    python3 -m ensurepip --default-pip || true;
  fi

  echo 'Upgrade pip';
  python -m pip install --upgrade pip --user || true;
  python3 -m pip install --upgrade pip --user || true;
  echo 'Upgrade CMake from pip';
  python -m pip install cmake --upgrade --user || true;
  python3 -m pip install cmake --upgrade --user || true;

  addCommandToPath 'cmake';
}
###############################################################################
###############################################################################


###############################################################################
# Install Conan package manager.
###############################################################################
install_conan() {
  # Necessary to install python 3.9 which uses six version 1.15!
  # Packages that should be used: six==1.15.0 conan==1.51.3 conan-package-tools
  echo 'Installing conan';
  pip3 install --ignore-installed six==1.15.0 conan==1.51.3 conan-package-tools;
  echo 'Installed conan!';
  pip3 install clang;

  PATH=${PATH}:$(pip3 list -v | grep -i cmake | tr -s ' ' | cut -d ' ' -f 3 | head -1);
  PATH=${PATH}:$(pip3 list -v | grep -i conan | tr -s ' ' | cut -d ' ' -f 3 | head -1);

  addCommandToPath 'conan';

  conan -v;
  checkCommand conan;
}
###############################################################################
###############################################################################


###############################################################################
# Test dependencies.
###############################################################################
test_commands() {
  echo 'Checking required shell commands.';

  set +u;
  if [ "${MAJOR_MAC_VERSION}" != 14 ]; then
    checkCommand timeout;
  fi
  set -u;

  checkCommand vim;
  checkCommand cmake;
  checkCommand make;
  checkCommand sh;
  checkCommand git;
  checkCommand git-lfs;
  checkCommand g++;
  checkCommand gcc;
  checkCommand set;
  checkCommand cd;
  checkCommand dirname;
  checkCommand .;
  checkCommand echo;
  checkCommand rm;
  checkCommand find;
  checkCommand env;
  checkCommand export;
  checkCommand command;
  checkCommand if;
  checkCommand else;
  checkCommand elif;
  checkCommand mkdir;
  checkCommand pwd;
  checkCommand exit;
  checkCommand du;
  checkCommand ls;
  checkCommand uname;
  checkCommand trap;
  checkCommand grep;
  checkCommand tr;
  checkCommand xargs;
  checkCommand cut;
  checkCommand kill;
  checkCommand sleep;
  checkCommand printf;
  checkCommand ln;
  checkCommand return;
  checkCommand chmod;
  checkCommand for;
  checkCommand while;
  checkCommand in;
  checkCommand case;
  checkCommand break;
  checkCommand sed;
  checkCommand ps;
  checkCommand true;
  checkCommand exec;
  checkCommand awk;
  checkCommand head;
  checkCommand tail;
  checkCommand getopts;
  checkCommand read;
  checkCommand basename;
  checkCommand wc;
  checkCommand nohup;
  checkCommand eval;
  checkCommand cat;
  checkCommand tput;
  checkCommand tee;
  checkCommand unzip;
  checkCommand ranlib;
  checkCommand ar;
  checkCommand shellcheck;

  if uname -a | grep -iq 'darwin'; then
    # Only available in MacOS.
    checkCommand vm_stat;
    checkCommand sw_vers;
    checkCommand curl;
  fi

  if ! uname -a | grep -iq 'msys' && ! uname -a | grep -iq 'darwin'; then
    # Not available in MacOS & Windows.
    checkCommand free;
    checkCommand setsid;
  fi

  if ! uname -a | grep -iq 'msys'; then
    # Not available in Windows.
    checkCommand sudo;
    checkCommand pkg-config;
    checkCommand lsof;
    checkCommand zip;
  fi

  # Linux Gentoo doesn't allow to use 'pip' directly.
  # checkCommand pip;
  # checkCommand pip3;

  # Can't install in docker container:
  # checkCommand clang++;
  # checkCommand cpulimit;
}
###############################################################################
###############################################################################


###############################################################################
# Execute script.
###############################################################################
echo "Detected Host OS: $(uname -a)";
install_dependencies;
# TODO: Add back the installation of conan package manager for all Linux distributions.
# install_conan;
test_commands;
###############################################################################
###############################################################################
