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
# * Windows
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
  shellcheck "${0}" || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Install dependencies.
###############################################################################
install_dependencies() {
  if command -v apt-get > /dev/null; then
    echo 'Detected Debian based Linux';
    install_dependencies_debian;
  elif command -v yum > /dev/null; then
    echo 'Detected Red Hat based Linux';
    install_dependencies_red_hat;
  elif command -v pacman > /dev/null; then
    echo 'Detected Arch based Linux';
    install_dependencies_arch;
  elif command -v apk > /dev/null; then
    echo 'Detected Alpine based Linux';
    install_dependencies_alpine;
  elif command -v emerge > /dev/null; then
    echo 'Detected Gentoo based Linux';
    install_dependencies_gentoo;
  elif command -v brew > /dev/null; then
    echo 'Detected MacOS';
    install_dependencies_macos;
  elif command -v choco > /dev/null; then
    echo 'Detected Windows';
    install_dependencies_windows;
  else
    echo 'Detected unknown Operating System';
  fi
  update_python;
}

install_dependencies_debian() {
  sudo apt-get update -y;
  sudo apt-get install --no-install-recommends -y \
    xorg-dev \
    x11-xserver-utils \
    libx11-xcb-dev libxcb-xinerama0 \
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
    cpulimit lsof zip unzip;
  echo 'Installing dependencies that conan might use.';
  sudo apt-get install --no-install-recommends -y clang libc++-dev libc++abi-dev;
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
  # https://wiki.archlinux.org/title/Pacman/Package_signing#Upgrade_system_regularly
  echo 'Removing packages from cache';
  rm -rf /var/cache/pacman/pkg;
  pacman -Sy archlinux-keyring --noconfirm --needed;
  echo 'Resetting all the keys';
  pacman-key --init;
  pacman-key --populate;
  echo 'Upgrade system';
  pacman -Syu --noconfirm --needed;
  pacman -Sy --noconfirm --needed glibc;
  pacman -Sy --noconfirm --needed lib32-glibc;
  pacman -Sy --noconfirm --needed vim;
  pacman -Sy --noconfirm --needed findutils;
  pacman -Sy --noconfirm --needed cmake;
  pacman -Sy --noconfirm --needed make;
  pacman -Sy --noconfirm --needed shellcheck;
  pacman -Sy --noconfirm --needed git;
  pacman -Sy --noconfirm --needed ca-certificates;
  pacman -Sy --noconfirm --needed which;
  pacman -Sy --noconfirm --needed qt5-base;
  pacman -Sy --noconfirm --needed python3;
  pacman -Sy --noconfirm --needed gcc;
  pacman -Sy --noconfirm --needed lsof;
  pacman -Sy --noconfirm --needed zip;
  pacman -Sy --noconfirm --needed unzip;
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
  set +e; # To avoid error: "Bash must not run in POSIX mode. Please unset POSIXLY_CORRECT and try again."
  sh -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/uninstall.sh)";
  sh -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh)";
  set -e;

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
  cd /usr/local/Homebrew || exit 1;

  git fetch --tags --all;
  git checkout 3.3.14;
  echo 'Avoid homebrew from auto-update itself every time its installed something.';
  export HOMEBREW_NO_AUTO_UPDATE=1;
  brew --version;

  echo 'Install packages separately, so it continues regardless if some error occurs in one.';
  brew update;
  brew install coreutils; # To install 'timeout' command.
  brew install util-linux; # To install 'setsid' command.
  brew install zip;
  brew install unzip;
  addCommandToPath 'setsid';
  cd "${oldpath}" || exit 1;

  MAJOR_MAC_VERSION=$(sw_vers | grep ProductVersion | cut -d ':' -f2 | cut -d '.' -f1 | tr -d '[:space:]');
  echo "MacOS '${MAJOR_MAC_VERSION}' detected";
  # This command needs sudo.
  # With Xcode_14.0.1.app, it throws this error on MacOS-12:
  # ld: Assertion failed: (_file->_atomsArrayCount == computedAtomCount && "more atoms allocated than expected")
  # For more information, check: https://stackoverflow.com/questions/73714336/xcode-update-to-version-2395-ld-compile-problem-occurs-computedatomcount-m
  # Recommended Xcode_13.2.app because it seems the only one compatible with MacOS-11 & MacOS-12.
  sudo xcode-select --switch /System/Volumes/Data/Applications/Xcode_13.2.app/Contents/Developer;
}

install_dependencies_windows() {
  # Install cmake: https://community.chocolatey.org/packages/cmake
  choco install cmake -y --version=3.13.0;
  # Install make: https://community.chocolatey.org/packages/make
  choco install make -y;
  # Install shellcheck: https://community.chocolatey.org/packages/shellcheck
  choco install shellcheck -y;
  # Install Qt: https://community.chocolatey.org/packages/qt5-default
  # To avoid error: mingw (exited 404)
  choco install qt5-default -y || true;

  # Splitted installation of dependencies to avoid error: 
  # Running ["VC_redist.x86.exe"] was not successful. Exit code was '1618'. Exit code indicates the following: Another installation currently in progress.
  # Install Visual C++ Build Tools: https://community.chocolatey.org/packages/visualcpp-build-tools
  choco install visualcpp-build-tools -y;
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

  PATH=$(pip3 list -v | grep -i cmake | tr -s ' ' | cut -d ' ' -f 3 | head -1):${PATH};
  PATH=$(pip3 list -v | grep -i conan | tr -s ' ' | cut -d ' ' -f 3 | head -1):${PATH};

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

  checkCommand vim;
  checkCommand cmake;
  checkCommand make;
  checkCommand sh;
  checkCommand git;
  checkCommand g++;
  checkCommand gcc;
  checkCommand timeout;
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

  if command -v brew > /dev/null; then
    # Only available in MacOS.
    checkCommand vm_stat;
    checkCommand sw_vers;
    checkCommand curl;
  else
    # Not available in MacOS.
    checkCommand shellcheck;
  fi

  if ! command -v brew > /dev/null && ! command -v choco > /dev/null; then
    # Not available in MacOS & Windows.
    checkCommand free;
  fi

  if ! command -v choco > /dev/null; then
    # Not available in Windows.
    checkCommand sudo;
    checkCommand pkg-config;
    checkCommand setsid;
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
