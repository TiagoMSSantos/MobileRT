#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# This script compiles MobileRT (in debug or release mode) for a native
# Operating System by using the CMake to generate the Makefiles, making it
# compatible with:
# * Linux
# * MacOS
# * Windows
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
env | grep -i path;
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
  shellcheck "${0}" --exclude=SC1017,SC2215 || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Set default arguments.
###############################################################################
type='release';
compiler='g++';
recompile='no';
parallelizeBuild;

printEnvironment() {
  echo '';
  echo 'Selected arguments:';
  echo "type: ${type}";
  echo "compiler: ${compiler}";
  echo "recompile: ${recompile}";
}
###############################################################################
###############################################################################


###############################################################################
# Parse arguments.
###############################################################################
parseArgumentsToCompile "$@";
printEnvironment;
###############################################################################
###############################################################################


###############################################################################
# Fix llvm clang OpenMP library for MacOS.
###############################################################################
addOpenMpPath() {
  OPENMP_INCLUDE_PATH="$(find /usr/local/Cellar/libomp /opt/homebrew/opt/libomp/include -iname "omp.h" | head -1 2> /dev/null || true)";
  OPENMP_LIB_PATH="$(find /usr/local/Cellar/libomp /opt/homebrew/opt/libomp/lib -iname "libomp.dylib" | head -1 2> /dev/null || true)";

  echo "OPENMP_INCLUDE_PATH = ${OPENMP_INCLUDE_PATH}";
  echo "OPENMP_LIB_PATH = ${OPENMP_LIB_PATH}";
  set +u;
  export CPLUS_INCLUDE_PATH="${OPENMP_INCLUDE_PATH%/omp.h}:${CPLUS_INCLUDE_PATH}";
  export LIBRARY_PATH="${OPENMP_LIB_PATH%/libomp.dylib}:${LIBRARY_PATH}";
  set -u;
}
addOpenMpPath;
###############################################################################
###############################################################################


###############################################################################
# Get the proper C compiler for conan.
#
# For more info, check:
# https://docs.conan.io/en/1.51/reference/config_files/settings.yml.html
###############################################################################
addCompilerPathForConan() {
  if echo "${compiler}" | grep -q "clang++.*"; then
    # Possible values for clang are ['3.3', '3.4', '3.5', '3.6', '3.7', '3.8', '3.9',
    # '4.0', '5.0', '6.0', '7.0', '7.1', '8', '9', '10', '11', '12', '13', '14', '15']
    conan_compiler='clang';
    echo 'Compiler version:';
    ${compiler} --version;
    echo 'Compiler version 2:';
    ${compiler} --version | grep -i version;
    echo 'Compiler version 3:';
    ${compiler} --version | grep -i version | sed 's/[ A-Za-z]//g' | awk -F '[^0-9]*' '{print $1}';
    conan_compiler_version=$(${compiler} --version | grep -i version | sed 's/[ A-Za-z]//g' | awk -F '[^0-9]*' '{print $1}' | head -1);
    export CXX="${conan_compiler}++";
    export CC="${conan_compiler}";
    export CFLAGS='-stdlib=libc++';
    export CXXFLAGS='-stdlib=libc++';
    conan_libcxx='libc++';
  elif echo "${compiler}" | grep -q "g++.*"; then
    export CXX='g++';
    export CC='gcc';
    #  Possible compiler values are ['Visual Studio', 'apple-clang', 'clang',
    # 'gcc', 'intel', 'intel-cc', 'mcst-lcc', 'msvc', 'qcc', 'sun-cc']
    if (uname -a | grep -iq 'darwin'); then
      # Possible values for Apple clang are ['5.0', '5.1', '6.0', '6.1', '7.0', '7.3',
      # '8.0', '8.1', '9.0', '9.1', '10.0', '11.0', '12.0', '13', '13.0', '13.1']
      echo 'Detected MacOS, so the C++ compiler should be apple-clang instead of old gcc.';
      conan_compiler='apple-clang';
      export CFLAGS='-stdlib=libc++';
      export CXXFLAGS='-stdlib=libc++';
      conan_libcxx='libc++';
    else
      # Possible values for gcc (Apple clang) are ['4.1', '4.4', '4.5', '4.6', '4.7',
      # '4.8', '4.9', '5', '5.1', '5.2', '5.3', '5.4', '5.5', '6', '6.1', '6.2', '6.3',
      # '6.4', '6.5', '7', '7.1', '7.2', '7.3', '7.4', '7.5', '8', '8.1', '8.2', '8.3',
      # '8.4', '9', '9.1', '9.2', '9.3', '9.4', '10', '10.1', '10.2', '10.3', '11',
      # '11.1', '11.2', '12']
      echo "Didn't detect MacOS, so the C++ compiler should be gcc.";
      conan_compiler='gcc';
      conan_libcxx='libstdc++';
    fi
    echo 'Compiler version:';
    ${compiler} -dumpversion;
    echo 'Compiler version 2:';
    ${compiler} -dumpversion | sed 's/[ A-Za-z]//g' | awk -F '[^0-9]*' '{print $1}';
    conan_compiler_version=$(${compiler} -dumpversion | sed 's/[ A-Za-z]//g' | awk -F '[^0-9]*' '{print $1}' | head -1);
  fi

  set +u; # Because of Windows OS doesn't have clang++ nor g++.
  # Fix compiler version used.
  if [ "${conan_compiler}" = 'apple-clang' ] && [ "${conan_compiler_version}" = '12' ]; then
    conan_compiler_version='12.0';
  fi
  if [ "${conan_compiler}" = 'clang' ] && [ "${conan_compiler_version}" = '6' ]; then
    conan_compiler_version='6.0';
  fi

  echo "Detected '${conan_compiler}' '${conan_compiler_version}' '(${conan_libcxx})' compiler.";
  set -u;
}

addCompilerPathForConan;
###############################################################################
###############################################################################


###############################################################################
# Get CPU Architecture.
###############################################################################
setCpuArchitecture() {
  CPU_ARCHITECTURE=x86_64;
  if command -v uname > /dev/null; then
    CPU_ARCHITECTURE=$(uname -m);
    if [ "${CPU_ARCHITECTURE}" = 'aarch64' ]; then
      CPU_ARCHITECTURE=armv8;
    fi
  fi
}

setCpuArchitecture;
###############################################################################
###############################################################################


###############################################################################
# Add Conan remote dependencies.
###############################################################################
# Install C++ Conan dependencies.
install_conan_dependencies() {
#  ln -s configure/config.guess /home/travis/.conan/data/libuuid/1.0.3/_/_/build/b818fa1fc0d3879f99937e93c6227da2690810fe/configure/config.guess;
#  ln -s configure/config.sub /home/travis/.conan/data/libuuid/1.0.3/_/_/build/b818fa1fc0d3879f99937e93c6227da2690810fe/configure/config.sub;

  addCommandToPath 'conan';
  echo 'Checking if conan is available.';
  if command -v conan > /dev/null; then
    echo 'Setting up conan.';
    conan profile new mobilert || true;
    conan profile update settings.compiler="${conan_compiler}" mobilert;
    conan profile update settings.compiler.version="${conan_compiler_version}" mobilert;
    conan profile update settings.compiler.libcxx="${conan_libcxx}" mobilert;
    conan profile update settings.compiler.cppstd=17 mobilert;
    # Possible values for compiler.libcxx for gcc are ['libstdc++', 'libstdc++11'].
    conan profile update settings.arch="${CPU_ARCHITECTURE}" mobilert;
    conan profile update settings.os='Linux' mobilert;
    conan profile update settings.build_type='Release' mobilert;
    conan remote add conancenter http://conan.io/center/ || true;

    echo 'Installing dependencies with conan.';
    conan_os='Linux';
    if uname -a | grep -iq 'darwin'; then
      conan_os='Macos';
    fi
    conan install \
      -s compiler="${conan_compiler}" \
      -s compiler.version="${conan_compiler_version}" \
      -s compiler.libcxx="${conan_libcxx}" \
      -s compiler.cppstd=17 \
      -s arch="${CPU_ARCHITECTURE}" \
      -s os="${conan_os}" \
      -s build_type=Release \
      -o bzip2:shared=True \
      -c tools.system.package_manager:mode=install \
      -c tools.system.package_manager:sudo=True \
      --build missing \
      --profile mobilert \
      --install-folder build_conan-native \
      ./app/third_party/conan/Native;

    export CONAN='TRUE';
    echo 'Done!';
  fi
}
###############################################################################
###############################################################################


###############################################################################
# Compile for native.
###############################################################################
create_build_folder() {
  # Set path to build.
  build_path=build_${type};

  typeWithCapitalLetter=$(capitalizeFirstletter "${type}");
  echo "type: '${typeWithCapitalLetter}'";

  if [ "${recompile}" = 'yes' ]; then
    rm -rf "${build_path:?Missing build path}"/*;
  fi
  mkdir -p "${build_path}";
}

build() {
  create_build_folder;
  oldpath=$(pwd);

  cd "${build_path}" || exit 1;
  addCommandToPath 'cmake';

  conanToolchainFile='../build_conan-native/conan_toolchain.cmake';
  addConanToolchain='';
  if [ -f "${conanToolchainFile}" ]; then
    addConanToolchain="-DCMAKE_TOOLCHAIN_FILE=${conanToolchainFile}";
  fi

  echo 'Calling CMake';
  cmake --version;
  if [ "${compiler}" = 'clang++' ]; then
    c_compiler='clang';
  else
    c_compiler='gcc';
  fi

  cmake -DCMAKE_VERBOSE_MAKEFILE=ON \
    -DCMAKE_CXX_COMPILER="${compiler}" \
    -DCMAKE_C_COMPILER="${c_compiler}" \
    -DCMAKE_C_SOURCE_FILE_EXTENSIONS="c" \
    -DCMAKE_BUILD_TYPE="${typeWithCapitalLetter}" \
     "${addConanToolchain}" \
    ../app;
  resCompile=${?};
  echo 'Called CMake';

  # The compiler might redirect the output to stderr, so we also have to redirect it to the variable.
  compiler_version=$(${compiler} -v 2>&1 || true);
  echo "Compiler version: ${compiler_version}";
  if echo "${compiler_version}" | grep -q "\.exe.*"; then
    echo 'Detected C++ compiler for Windows!';
    JOBS_FLAG='';
  else
    echo "Didn't detect C++ compiler for Windows!";
    JOBS_FLAG="-- ${MAKEFLAGS}";
  fi

  if [ "${resCompile}" -eq 0 ]; then
    set +u;
    echo "Calling Make with parameters: '${JOBS_FLAG}'";
    # shellcheck disable=SC2086
    cmake --build . ${JOBS_FLAG};
    set -u;
    resCompile=${?};
  else
    echo 'Compilation: cmake failed';
  fi
  cd "${oldpath}" || exit 1;
}
###############################################################################
###############################################################################

echo "Detected Host OS: $(uname -a)";
#install_conan_dependencies;
createReportsFolders;
build;
validateNativeLibCompiled;
du -h -d 1 build_"${type}"/bin build_"${type}"/lib;

###############################################################################
# Exit code.
###############################################################################
printCommandExitCode "${resCompile}" 'Compilation';
###############################################################################
###############################################################################
