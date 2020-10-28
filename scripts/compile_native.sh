#!/bin/bash

###############################################################################
# Change directory to MobileRT root
###############################################################################
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit
###############################################################################
###############################################################################

###############################################################################
# Get arguments
###############################################################################
type="${1:-Release}"
compiler="${2:-g++}"
recompile="${3:-no}"
###############################################################################
###############################################################################

###############################################################################
# Get helper functions
###############################################################################
source scripts/helper_functions.sh
###############################################################################
###############################################################################

###############################################################################
# Get the proper C compiler for conan
# Possible values for clang are ['3.3', '3.4', '3.5', '3.6', '3.7', '3.8', '3.9', '4.0',
# '5.0', '6.0', '7.0', '7.1', '8', '9', '10', '11']
# Possible values for gcc (Apple clang) are ['4.1', '4.4', '4.5', '4.6', '4.7', '4.8',
# '4.9', '5', '5.1', '5.2', '5.3', '5.4', '5.5', '6', '6.1', '6.2', '6.3', '6.4',
# '6.5', '7', '7.1', '7.2', '7.3', '7.4', '7.5', '8', '8.1', '8.2', '8.3', '8.4',
# '9', '9.1', '9.2', '9.3', '10', '10.1']
###############################################################################
if [[ "${compiler}" == *"clang++"* ]]; then
  conan_compiler="clang"
  conan_compiler_version=$(clang++ -dumpversion)
  callCommand clang++ -v;
  export CC=clang
elif [[ "${compiler}" == *"g++"* ]]; then
  conan_compiler="gcc"
  conan_compiler_version=$(g++ -dumpversion)
  callCommand g++ -v;
  export CC=gcc
fi
export CXX="${compiler}";
# Fix compiler version used
if [ "${conan_compiler_version}" == "9.0.0" ]; then
  conan_compiler_version=9
fi
if [ "${conan_compiler_version}" == "12.0.0" ]; then
  conan_compiler_version=12
fi
###############################################################################
###############################################################################

###############################################################################
# Get Conan path
###############################################################################
CONAN_PATH=$(find /usr ~/ -name "conan" || true);
echo "Conan binary: ${CONAN_PATH}"
echo "Conan location: ${CONAN_PATH%/conan}"
PATH=${CONAN_PATH%/conan}:${PATH}
echo "PATH: ${PATH}"
###############################################################################
###############################################################################

###############################################################################
# Get CPU Architecture
###############################################################################
CPU_ARCHITECTURE=x86_64
if [ -x "$(command -v uname)" ]; then
  CPU_ARCHITECTURE=$(uname -m)
  if [ "${CPU_ARCHITECTURE}" == "aarch64" ]; then
    CPU_ARCHITECTURE=armv8
  fi
fi
###############################################################################
###############################################################################

###############################################################################
# Add Conan remote dependencies
###############################################################################
conan profile new default;
callCommand conan profile update settings.compiler="${conan_compiler}" default;
callCommand conan profile update settings.compiler.version="${conan_compiler_version}" default;
callCommand conan profile update settings.compiler.libcxx="libstdc++11" default;
callCommand conan profile update settings.arch="${CPU_ARCHITECTURE}" default;
callCommand conan profile update settings.os="Linux" default;
callCommand conan profile update settings.build_type="Release" default;
conan remote add bintray https://api.bintray.com/conan/bincrafters/public-conan;
###############################################################################
###############################################################################

###############################################################################
# Compile for native
###############################################################################

# Capitalize 1st letter
type="$(tr '[:lower:]' '[:upper:]' <<<"${type:0:1}")${type:1}"
echo "type: '${type}'"

# Set path to build
build_path=./build_${type}
callCommand mkdir -p "${build_path}"

if [ "${recompile}" == "yes" ]; then
  callCommand rm -rf "${build_path}"/*
fi

function build() {
  ln -s configure/config.guess /home/travis/.conan/data/libuuid/1.0.3/_/_/build/b818fa1fc0d3879f99937e93c6227da2690810fe/configure/config.guess
  ln -s configure/config.sub /home/travis/.conan/data/libuuid/1.0.3/_/_/build/b818fa1fc0d3879f99937e93c6227da2690810fe/configure/config.sub
  callCommand cd "${build_path}"
  callCommand conan install \
    -s compiler=${conan_compiler} \
    -s compiler.version="${conan_compiler_version}" \
    -s compiler.libcxx=libstdc++11 \
    -s arch="${CPU_ARCHITECTURE}" \
    -s os="Linux" \
    -s build_type=Release \
    -o bzip2:shared=True \
    --build missing \
    --profile default \
    ../app/
  echo "Calling CMake"
  callCommand cmake -DCMAKE_VERBOSE_MAKEFILE=ON \
    -DCMAKE_CXX_COMPILER="${compiler}" -DCMAKE_BUILD_TYPE="${type}" ../app/ \
    2>&1 | tee ./log_cmake_"${type}".log
  resCompile=${PIPESTATUS[0]}

  if [ "${resCompile}" -eq 0 ]; then
    callCommand ls -laR ..
    echo "Calling Make"
    callCommand cmake --build . 2>&1 | tee ./log_make_"${type}".log
    resCompile=${PIPESTATUS[0]}
  else
    echo "Compilation: cmake failed"
  fi
}
###############################################################################
###############################################################################

build

###############################################################################
# Exit code
###############################################################################
printCommandExitCode "${resCompile}" "Compilation"
###############################################################################
###############################################################################
