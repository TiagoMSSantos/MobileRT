#!/usr/bin/env bash

###############################################################################
# README
###############################################################################
# This script contains a mixed functions to help execute or debug MobileRT.
# At the moment it allows to:
# * Profile MobileRT (measure latencies)
# * Draw graphs with the latencies (or draw a graph with the speedup)
# * Execute MobileRT in release mode
# * Execute MobileRT in debug mode
# * Execute the C++ linter (clang-tidy) in the codebase
# * Execute the Unit Tests
###############################################################################
###############################################################################


###############################################################################
# Exit immediately if a command exits with a non-zero status.
###############################################################################
set -euo pipefail -o posix;
###############################################################################
###############################################################################


###############################################################################
# Change directory to MobileRT root.
###############################################################################
cd "$(dirname "${0}")/.." || exit;
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
# Set paths for MobileRT.
###############################################################################
setPaths() {
  PATH_TO_SEARCH="../";
  FILE_TO_SEARCH="MobileRT.jks";

  set +e;
  FIND_MOBILERT=$(find ${PATH_TO_SEARCH} -iname "${FILE_TO_SEARCH}" 2> /dev/null | head -n 1);
  MOBILERT_PATH=$(echo "${FIND_MOBILERT}" | sed 's/\/app\/.*//g');
  set -e;

  if [ -z "${MOBILERT_PATH}" ]; then
    PATH_TO_SEARCH="/";
    FIND_MOBILERT=$(find ${PATH_TO_SEARCH} -iname "MobileRT" 2> /dev/null | head -n 1);
    MOBILERT_PATH=$(echo "${FIND_MOBILERT}" | sed "s/\/app\/${FILE_TO_SEARCH}/g");
  fi

  echo "FILE_TO_SEARCH = ${FILE_TO_SEARCH}";
  echo "PATH_TO_SEARCH = ${PATH_TO_SEARCH}";
  echo "FIND_MOBILERT = ${FIND_MOBILERT}";
  echo "MOBILERT_PATH = ${MOBILERT_PATH}";

  BIN_DEBUG_PATH="${MOBILERT_PATH}/build_debug/bin";
  BIN_RELEASE_PATH="${MOBILERT_PATH}/build_release/bin";
  SCRIPTS_PATH="${MOBILERT_PATH}/scripts";
  PLOT_SCRIPTS_PATH="${SCRIPTS_PATH}/plot";
  OBJS_PATH="${MOBILERT_PATH}/WavefrontOBJs";

  set +u;
  if [ -z "${PLOT_GRAPHS}" ]; then
    PLOT_GRAPHS=${SCRIPTS_PATH}/"graphs";
  fi
  set -u;

  mkdir -p "${PLOT_GRAPHS}";

  for FOLDER in "${PLOT_GRAPHS[@]}"; do
    FILES+=("$(find "${FOLDER}" -type f)");
  done
}
###############################################################################
###############################################################################


###############################################################################
# Set paths for MobileRT sources and headers.
###############################################################################
setHeaders() {
  MOBILERT_SRCS="${MOBILERT_PATH}/app";
  COMPONENTS_SRCS="${MOBILERT_PATH}/app";
  DEPENDENT_SRCS="${MOBILERT_PATH}/app/System_dependent";
  SCENES_SRCS="${MOBILERT_PATH}/app/Scenes";

  THIRDPARTY_HEADERS="${MOBILERT_PATH}/app/third_party";
  GLM_HEADERS="${THIRDPARTY_HEADERS}/glm";
  STB_HEADERS="${THIRDPARTY_HEADERS}/stb";
  PCG_HEADERS="${THIRDPARTY_HEADERS}/pcg-cpp/include";
  BOOST_HEADERS_ROOT="${THIRDPARTY_HEADERS}/boost/libs";
  BOOST_HEADERS_ASSERT="${BOOST_HEADERS_ROOT}/assert/include";
}
###############################################################################
###############################################################################


###############################################################################
# Set paths for the scene.
###############################################################################
setScene() {
  SCN="${OBJS_PATH}/conference/conference";
  #SCN="${OBJS_PATH}/teapot/teapot";
  #SCN="${OBJS_PATH}/buddha/buddha";
  #SCN="${OBJS_PATH}/dragon/dragon";
  #SCN="${OBJS_PATH}/sponza/sponza";
  #SCN="${OBJS_PATH}/powerplant/powerplant";
  #SCN="${OBJS_PATH}/San_Miguel/san-miguel";
  #SCN="${OBJS_PATH}/San_Miguel/san-miguel-low-poly";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-CO";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-Squashed";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-White";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Glossy-Floor";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Glossy";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Mirror";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Original";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Sphere";
  #SCN="${OBJS_PATH}/CornellBox/CornellBox-Water";
  #SCN="${OBJS_PATH}/CornellBox/water";

  OBJ="${SCN}.obj";
  MTL="${SCN}.mtl";
  CAM="${SCN}.cam";
}
###############################################################################
###############################################################################


###############################################################################
# Set options for the sanitizers.
###############################################################################
setOptionsSanitizers() {
  export ASAN_OPTIONS="suppressions=sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1:detect_stack_use_after_return=1:check_initialization_order=1:strict_init_order=1:halt_on_error=0:detect_odr_violation=1";
  export LSAN_OPTIONS="suppressions=sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1";
}
###############################################################################
###############################################################################


###############################################################################
# Set arguments for MobileRT.
###############################################################################
setArguments() {
  SPP="1";
  SPL="1";
  WIDTH="900";
  HEIGHT="900";
  REP="1";
  PRINT="false";
  ASYNC="false";
  SHOWIMAGE="false";
  SEP="-";

  # Configuration for profiling.
  REPETITIONS="2";
  THREADS="1 2";
  SHADERS="1 2";
  SCENES="1 2";
  ACCELERATORS="1 2";

  THREAD=$(nproc --all);
  SHADER="1";
  SCENE="4";
  ACC="3";
  PRINT="true";
  SHOWIMAGE="true";
  ASYNC="true";
  SPP="1";
  SPL="1";

  trap "exit" INT;
}
###############################################################################
###############################################################################

execute() {
  #ASYNC="false";
  echo "";
  echo "THREAD = ${THREAD}";
  echo "SHADER = ${SHADER}";
  echo "SCENE = ${SCENE}";
  echo "ACC = ${ACC}";
  echo "ASYNC = ${ASYNC}";

  #perf script report callgrind > perf.callgrind
  #kcachegrind perf.callgrind
  #perf stat \
  #perf record -g --call-graph 'fp' -- \
  "${BIN_RELEASE_PATH}"/AppMobileRT \
    "${THREAD}" ${SHADER} ${SCENE} ${SPP} ${SPL} ${WIDTH} ${HEIGHT} ${ACC} \
    ${REP} "${OBJ}" "${MTL}" "${CAM}" ${PRINT} ${ASYNC} ${SHOWIMAGE};
  #perf report -g '' --show-nr-samples --hierarchy;
}

debug() {
  echo "";
  echo "THREAD = ${THREAD}";
  echo "SHADER = ${SHADER}";
  echo "SCENE = ${SCENE}";
  echo "ACC = ${ACC}";

  "${BIN_DEBUG_PATH}"/AppMobileRTd \
    "${THREAD}" ${SHADER} ${SCENE} ${SPP} ${SPL} ${WIDTH} ${HEIGHT} ${ACC} \
    ${REP} "${OBJ}" "${MTL}" "${CAM}" ${PRINT} ${ASYNC} ${SHOWIMAGE};
}

clangtidy() {
  GTK_HEADERS="$(pkg-config --cflags gtk+-2.0)";
  # shellcheck disable=SC2001
  GTK_HEADERS=$(echo "${GTK_HEADERS}" | sed 's/-I/-isystem/g');
  echo "GTK_HEADERS = ${GTK_HEADERS}";

  clang-tidy \
    -analyze-temporary-dtors \
    -checks='*,-*llvm-header-guard*,-fuchsia-default-arguments,-fuchsia-overloaded-operator' \
    -header-filter='.*' \
    "${MOBILERT_SRCS}"/MobileRT/*.*pp \
    "${MOBILERT_SRCS}"/MobileRT/*/*.*pp \
    "${COMPONENTS_SRCS}"/Components/*/*.*pp \
    "${DEPENDENT_SRCS}"/Native/*.*pp \
    "${SCENES_SRCS}"/*.*pp \
    -- -std=c++11 -ferror-limit=1 -stdlib=libc++ \
    -I "${MOBILERT_SRCS}" \
    -I "${COMPONENTS_SRCS}" \
    -I "${DEPENDENT_SRCS}"/Native \
    -I "${SCENES_SRCS}" \
    -isystem "${THIRDPARTY_HEADERS}" \
    -isystem "${GLM_HEADERS}" \
    -isystem "${STB_HEADERS}" \
    -isystem "${PCG_HEADERS}" \
    -isystem "${BOOST_HEADERS_ASSERT}" \
    -isystem /usr/include/c++/7 \
    -isystem /usr/include/c++/v1 \
    -isystem /usr/include/x86_64-linux-gnu/c++/7 \
    -isystem /usr/include/glib-2.0/gobject \
    -isystem /usr/include/gtk-2.0/gtk \
    -isystem /usr/lib/llvm-8/include/openmp \
    "${GTK_HEADERS}";
}

profile() {
  ASYNC=false;
  for R in $(seq 1 ${REPETITIONS}); do
    for THREAD in "${THREADS[@]}"; do
      for SHADER in "${SHADERS[@]}"; do
        for SCENE in "${SCENES[@]}"; do
          for ACC in "${ACCELERATORS[@]}"; do
            echo "";
            echo "REPETITION = ${R}";
            echo "THREAD = ${THREAD}";
            echo "SHADER = ${SHADER}";
            echo "SCENE = ${SCENE}";
            echo "ACC = ${ACC}";
            echo "ASYNC = ${ASYNC}";

            PLOT_FILE="SC${SCENE}${SEP}SH${SHADER}${SEP}A${ACC}${SEP}R${WIDTH}x${HEIGHT}";

            "${BIN_RELEASE_PATH}"/AppMobileRT \
              "${THREAD}" "${SHADER}" "${SCENE}" "${SPP}" "${SPL}" "${WIDTH}" "${HEIGHT}" "${ACC}" "${REP}" \
              "${OBJ}" "${MTL}" "${CAM}" "${PRINT}" "${ASYNC}" "${SHOWIMAGE}" |
              awk -v threads="${THREAD}" -f "${PLOT_SCRIPTS_PATH}"/parser_out.awk 2>&1 |
              tee -a "${PLOT_GRAPHS}"/"${PLOT_FILE}".dat;

          done
        done
      done
    done
    R=$(( R + 1 ));
  done
}

###############################################################################
# Parse arguments.
###############################################################################
parseArguments() {
  if [ $# -eq 0 ]; then
    execute;
  else
    for P in "${@}"; do
      case ${P} in
      "time")
        profile;
        sleep 2s;
        ;;
      "drawt")
        # shellcheck disable=SC1091
        . scripts/plot/plot.sh 0 ;;
      "draws")
        # shellcheck disable=SC1091
        . scripts/plot/plot.sh 1 ;;
      "test") awk -f "${PLOT_SCRIPTS_PATH}/parser_median.awk" "${PLOT_SCRIPTS_PATH}/test.dat" ;;
      "release") execute ;;
      "debug") debug ;;
      "tidy") clangtidy ;;
      "gtest") "${BIN_DEBUG_PATH}"/UnitTestsd ;;
      *)
        printf "\nWrong Parameter: %s\n" "${P}";
        printArguments;
        break;
        ;;
      esac
    done
  fi
}

printArguments() {
  echo "The valid parameters are:";
  echo "time - Profile application and log the measured times.";
  echo "drawt - Draw a graph of latencies with GNU Plot.";
  echo "draws - Draw a graph of speedups with GNU Plot.";
  echo "release - Execute MobileRT in release mode.";
  echo "debug - Execute MobileRT in debug mode.";
  echo "tidy - Execute C++ linter (clang-tidy) in MobileRT.";
  echo "gtest - Execute MobileRT's unit tests.";
}
###############################################################################
###############################################################################

setPaths;
setHeaders;
setScene;
setOptionsSanitizers;
setArguments;
parseArguments "${@}";
