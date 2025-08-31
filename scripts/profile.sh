#!/usr/bin/env sh

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
# Execute Shellcheck on this script.
###############################################################################
if [ $# -ge 1 ] && command -v shellcheck > /dev/null; then
  shellcheck "${0}" --exclude=SC1017,SC2215,SC2211 || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Set paths for MobileRT.
###############################################################################
setPaths() {
  PATH_TO_SEARCH='./';
  FILE_TO_SEARCH='MobileRT.jks';

  FIND_MOBILERT=$(find ${PATH_TO_SEARCH} -iname "${FILE_TO_SEARCH}" 2> /dev/null | head -n 1 || true);
  MOBILERT_PATH=$(echo "${FIND_MOBILERT}" | sed 's/\/app\/.*//g' || true);
  MOBILERT_PATH=$(dirname "${MOBILERT_PATH}");

  if [ -z "${MOBILERT_PATH}" ]; then
    PATH_TO_SEARCH='/';
    FIND_MOBILERT=$(find ${PATH_TO_SEARCH} -iname "MobileRT" 2> /dev/null | head -n 1);
    MOBILERT_PATH=$(echo "${FIND_MOBILERT}" | sed "s/\/app\/${FILE_TO_SEARCH}/g");
    MOBILERT_PATH=$(dirname "${MOBILERT_PATH}");
  fi

  echo "FILE_TO_SEARCH = ${FILE_TO_SEARCH}";
  echo "PATH_TO_SEARCH = ${PATH_TO_SEARCH}";
  echo "MOBILERT_PATH = ${MOBILERT_PATH}";

  if uname -a | grep -iq 'mingw'; then
    ls -lahp "${MOBILERT_PATH}/build_debug/bin/" || true;
    ls -lahp "${MOBILERT_PATH}/build_release/bin/" || true;
    # shellcheck disable=SC2010
    BIN_DEBUG_PATH="${MOBILERT_PATH}/build_debug/bin/"$(ls "${MOBILERT_PATH}"/build_debug/bin | grep Debug | tr -d '[:space:]' || true);
    # shellcheck disable=SC2010
    BIN_RELEASE_PATH="${MOBILERT_PATH}/build_release/bin/"$(ls "${MOBILERT_PATH}"/build_release/bin | grep Release | tr -d '[:space:]' || true);
    BIN_DEBUG_EXE="${BIN_DEBUG_PATH}"/AppMobileRTd
    BIN_RELEASE_EXE="${BIN_RELEASE_PATH}"/AppMobileRT
  else
    BIN_DEBUG_PATH="${MOBILERT_PATH}/build_debug/bin";
    BIN_RELEASE_PATH="${MOBILERT_PATH}/build_release/bin";
    BIN_DEBUG_EXE="${BIN_DEBUG_PATH}"/AppMobileRTd
    BIN_RELEASE_EXE="${BIN_RELEASE_PATH}"/AppMobileRT
  fi
  SCRIPTS_PATH="${MOBILERT_PATH}/scripts";
  PLOT_SCRIPTS_PATH="${SCRIPTS_PATH}/plot";
  OBJS_PATH="${MOBILERT_PATH}/WavefrontOBJs";
  echo "OBJS_PATH = ${OBJS_PATH}";

  set +u;
  if [ -z "${PLOT_GRAPHS}" ]; then
    PLOT_GRAPHS=${SCRIPTS_PATH}/"graphs";
  fi
  set -u;

  mkdir -p "${PLOT_GRAPHS}";
  set +u;
  if [ -z "${PLOT_GRAPHS}" ]; then
    PLOT_GRAPHS=${SCRIPTS_PATH}/"graphs";
  fi
  set -u;
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
  set +u; 
  if [ -z "${1}" ]; then
    SCN="${OBJS_PATH}/conference/conference";
    # SCN="${OBJS_PATH}/teapot/teapot";
    # SCN="${OBJS_PATH}/buddha/buddha";
    # SCN="${OBJS_PATH}/dragon/dragon";
    # SCN="${OBJS_PATH}/sponza/sponza";
    # SCN="${OBJS_PATH}/powerplant/powerplant";
    # SCN="${OBJS_PATH}/san_miguel_mobilert/san_miguel_mobilert";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-CO";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-Squashed";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-White";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Glossy-Floor";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Glossy";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Mirror";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Original";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Sphere";
    # SCN="${OBJS_PATH}/CornellBox/CornellBox-Water";
    # SCN="${OBJS_PATH}/CornellBox/water";
  else
    SCN="${OBJS_PATH}/${1}/${1}";
  fi
  set -u;
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
  pwd;
  ls -lahp scripts/sanitizer_ignore.suppr;
  if uname -a | grep -iq 'linux' > /dev/null; then
    export ASAN_OPTIONS='suppressions=scripts/sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1:detect_stack_use_after_return=1:check_initialization_order=1:strict_init_order=1:halt_on_error=0:detect_odr_violation=1:detect_leaks=1:detect_container_overflow=1:max_uar_stack_size_log=32';
  else
    export ASAN_OPTIONS='suppressions=scripts/sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1:detect_stack_use_after_return=1:check_initialization_order=1:strict_init_order=1:halt_on_error=0:detect_odr_violation=1:detect_container_overflow=1';
  fi
  export LSAN_OPTIONS='suppressions=scripts/sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1';
}
###############################################################################
###############################################################################


###############################################################################
# Set arguments for MobileRT.
###############################################################################
setArguments() {
  SPP='1';
  SPL='1';
  WIDTH='900';
  HEIGHT='900';
  REP='1';
  ASYNC='false';
  SHOWIMAGE='false';
  SEP='-';

  # Configuration for profiling.
  REPETITIONS='2';
  THREADS='1 2';
  SHADERS='1 2';
  SCENES='1 2';
  ACCELERATORS='1 2';

  THREAD=$(nproc --all);
  SHADER='1';
  SCENE='4';
  ACC='3';
  SHOWIMAGE='true';
  ASYNC='true';
  SPP='1';
  SPL='1';

  trap 'exit' INT;
}
###############################################################################
###############################################################################

# Params:
# * width and height
# * asynchronous - If false, MobileRT will not show rendered image and will exit when the scene is rendered.
execute() {
  set +u;
  if [ -z "${1}" ]; then
    WIDTH='900';
    HEIGHT='900';
  else
    WIDTH="${1}";
    HEIGHT="${1}";
  fi
  if [ -z "${1}" ]; then
    ASYNC='true';
  else
    ASYNC="${2}";
  fi
  set -u;
  echo '';
  echo "THREAD = ${THREAD}";
  echo "SHADER = ${SHADER}";
  echo "SCENE = ${SCENE}";
  echo "ACC = ${ACC}";
  echo "ASYNC = ${ASYNC}";
  echo "WIDTH = ${WIDTH}";
  echo "HEIGHT = ${HEIGHT}";
  echo "OBJ = ${OBJ}";

  #perf script report callgrind > perf.callgrind
  #kcachegrind perf.callgrind
  #perf stat \
  #perf record -g --call-graph 'fp' -- \
  "${BIN_RELEASE_EXE}" \
    "${THREAD}" "${SHADER}" "${SCENE}" "${SPP}" "${SPL}" "${WIDTH}" "${HEIGHT}" "${ACC}" \
    "${REP}" "${OBJ}" "${MTL}" "${CAM}" "${ASYNC}" "${SHOWIMAGE}";
  #perf report -g '' --show-nr-samples --hierarchy;
}

# Params:
# * width and height
# * asynchronous - If false, MobileRT will not show rendered image and will exit when the scene is rendered.
debug() {
  set +u;
  if [ -z "${1}" ]; then
    WIDTH='900';
    HEIGHT='900';
  else
    WIDTH="${1}";
    HEIGHT="${1}";
  fi
  if [ -z "${1}" ]; then
    ASYNC='true';
  else
    ASYNC="${2}";
  fi
  set -u;

  echo '';
  echo "THREAD = ${THREAD}";
  echo "SHADER = ${SHADER}";
  echo "SCENE = ${SCENE}";
  echo "ACC = ${ACC}";

  # gdb --args \
  "${BIN_DEBUG_EXE}" \
    "${THREAD}" "${SHADER}" "${SCENE}" "${SPP}" "${SPL}" "${WIDTH}" "${HEIGHT}" "${ACC}" \
    "${REP}" "${OBJ}" "${MTL}" "${CAM}" "${ASYNC}" "${SHOWIMAGE}";
}

# Params:
# * mode
# * width and height
# * timeout in seconds
executeTimeout() {
  set +u;
  if [ -z "${2}" ]; then
    WIDTH='900';
    HEIGHT='900';
  else
    WIDTH="${2}";
    HEIGHT="${2}";
  fi
  set -u;

  set +u;
  if [ "${1}" = 'release' ]; then
    echo 'Executing in release mode.';
    ls -lahp "${MOBILERT_PATH}/build_release/bin/" || true;
    ls -lahp "${MOBILERT_PATH}/build_release/bin/Release" || true;
    BIN_PATH_EXE="${BIN_RELEASE_EXE}";
  elif [ "${1}" = 'debug' ]; then
    echo 'Executing in debug mode.';
    ls -lahp "${MOBILERT_PATH}/build_debug/bin/" || true;
    ls -lahp "${MOBILERT_PATH}/build_debug/bin/Debug" || true;
    BIN_PATH_EXE="${BIN_DEBUG_EXE}";
  fi
  set -u;

  echo '';
  echo "BIN_DEBUG_PATH = ${BIN_DEBUG_PATH}";
  echo "BIN_RELEASE_PATH = ${BIN_RELEASE_PATH}";
  echo "BIN_DEBUG_EXE = ${BIN_DEBUG_EXE}";
  echo "BIN_RELEASE_EXE = ${BIN_RELEASE_EXE}";
  echo "BIN_PATH_EXE = ${BIN_PATH_EXE}";
  echo "THREAD = ${THREAD}";
  echo "SHADER = ${SHADER}";
  echo "SCENE = ${SCENE}";
  echo "SPP = ${SPP}";
  echo "SPL = ${SPL}";
  echo "WIDTH = ${WIDTH}";
  echo "HEIGHT = ${HEIGHT}";
  echo "ACC = ${ACC}";
  echo "REP = ${REP}";
  echo "OBJ = ${OBJ}";
  echo "MTL = ${MTL}";
  echo "CAM = ${CAM}";
  echo "ASYNC = ${ASYNC}";
  echo "SHOWIMAGE = ${SHOWIMAGE}";
  ls -lahp "${BIN_PATH_EXE}";
  ls -lahp "${OBJ}";
  ls -lahp "${MTL}";
  ls -lahp "${CAM}";
  QT_QPA_PLATFORM='offscreen' timeout "${3}" "${BIN_PATH_EXE}" \
    "${THREAD}" "${SHADER}" "${SCENE}" "${SPP}" "${SPL}" "${WIDTH}" "${HEIGHT}" "${ACC}" \
    "${REP}" "${OBJ}" "${MTL}" "${CAM}" "${ASYNC}" "${SHOWIMAGE}";
  returnValue="$?";
  return "${returnValue}";
}

clangtidy() {
  GTK_HEADERS="$(pkg-config --cflags gtk+-2.0)";
  GTK_HEADERS=$(echo "${GTK_HEADERS}" | sed 's/-I/-isystem/g');
  echo "GTK_HEADERS = ${GTK_HEADERS}";

  clang-tidy \
    -checks='*,-*llvm-header-guard*,-fuchsia-default-arguments,-fuchsia-overloaded-operator' \
    -header-filter='.*' \
    "${MOBILERT_SRCS}"/MobileRT/*.*pp \
    "${MOBILERT_SRCS}"/MobileRT/*/*.*pp \
    "${COMPONENTS_SRCS}"/Components/*/*.*pp \
    "${DEPENDENT_SRCS}"/Native/*.*pp \
    "${SCENES_SRCS}"/*.*pp \
    -- -std=c++20 -ferror-limit=1 -stdlib=libc++ \
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
    "${GTK_HEADERS}";
}

profile() {
  ASYNC=false;
  for R in $(seq 1 ${REPETITIONS}); do
    for THREAD in ${THREADS}; do
      for SHADER in ${SHADERS}; do
        for SCENE in ${SCENES}; do
          for ACC in ${ACCELERATORS}; do
            echo '';
            echo "REPETITION = ${R}";
            echo "THREAD = ${THREAD}";
            echo "SHADER = ${SHADER}";
            echo "SCENE = ${SCENE}";
            echo "ACC = ${ACC}";
            echo "ASYNC = ${ASYNC}";

            PLOT_FILE="SC${SCENE}${SEP}SH${SHADER}${SEP}A${ACC}${SEP}R${WIDTH}x${HEIGHT}";

            "${BIN_RELEASE_PATH}"/AppMobileRT \
              "${THREAD}" "${SHADER}" "${SCENE}" "${SPP}" "${SPL}" "${WIDTH}" "${HEIGHT}" "${ACC}" "${REP}" \
              "${OBJ}" "${MTL}" "${CAM}" "${ASYNC}" "${SHOWIMAGE}" |
              awk -v threads="${THREAD}" -f "${PLOT_SCRIPTS_PATH}"/parser_out.awk 2>&1 |
              tee -a "${PLOT_GRAPHS}"/"${PLOT_FILE}".dat;

          done
        done
      done
    done
    R=$(( R + 1 ));
  done
}

# Params:
# * mode
# * width and height
executePerf() {
  ASYNC="false";

  set +u;
  if [ -z "${2}" ]; then
    WIDTH='900';
    HEIGHT='900';
  else
    WIDTH="${2}";
    HEIGHT="${2}";
  fi
  set -u;

  echo '';
  echo "SHOWIMAGE = ${SHOWIMAGE}";
  echo "THREAD = ${THREAD}";
  echo "SHADER = ${SHADER}";
  echo "SCENE = ${SCENE}";
  echo "ACC = ${ACC}";
  echo "ASYNC = ${ASYNC}";
  echo "WIDTH = ${WIDTH}";
  echo "HEIGHT = ${HEIGHT}";

  set +u;
  if [ "${1}" = 'release' ]; then
    echo 'Executing in release mode.';
    BIN_PATH_EXE="${BIN_RELEASE_EXE}";
  elif [ "${1}" = 'debug' ]; then
    echo 'Executing in debug mode.';
    BIN_PATH_EXE="${BIN_DEBUG_EXE}";
  fi
  set -u;

  echo 'All events:';
  perf list | tee perf_events.log;

  echo 'Hardware events:';
  perf list hw | tee perf_events_hw.log;

  # perf script report callgrind > perf.callgrind
  # kcachegrind perf.callgrind
  # perf record -g --call-graph 'fp' --freq=3250 --sample-cpu --period
  QT_QPA_PLATFORM='offscreen' perf stat --detailed --detailed --detailed --verbose --output perf.log -- \
    "${BIN_PATH_EXE}" \
    "${THREAD}" "${SHADER}" "${SCENE}" "${SPP}" "${SPL}" "${WIDTH}" "${HEIGHT}" "${ACC}" \
    "${REP}" "${OBJ}" "${MTL}" "${CAM}" "${ASYNC}" "${SHOWIMAGE}";
  # perf report -i 'perf.data' -g '' --show-nr-samples --hierarchy --header > perf.log;
  echo 'Perf results:';
  cat perf.log;
}

###############################################################################
# Parse arguments.
###############################################################################
parseArguments() {
  if [ $# -eq 0 ]; then
    printArguments;
  else
    for P in "${@}"; do
      case ${P} in
      'perf')
        executePerf "${2}" "${3}";
        ;;
      'time')
        profile;
        sleep 2s;
        ;;
      'timeout')
        executeTimeout "${2}" "${3}" "${4}";
        ;;
      'drawt')
        sh scripts/plot/plot.sh 'drawt' ;;
      'draws')
        sh scripts/plot/plot.sh 'draws' ;;
      'test') awk -f "${PLOT_SCRIPTS_PATH}/parser_median.awk" "${PLOT_SCRIPTS_PATH}/test.dat" ;;
      'debug' | 'release')
        if [ "$#" -lt 2 ]; then
          echo 'Executing using default resolution & scene.';
          if [ "${1}" = 'release' ]; then execute; else debug; fi
        else
          if [ "$#" -gt 2 ]; then
            echo "Executing using resolution '${2}' & scene '${3}'.";
            setScene "${3}";
          fi
          if [ "$#" -gt 3 ]; then
            if [ "${1}" = 'release' ]; then execute "${2}" "${4}"; else debug "${2}" "${4}"; fi
          else
            if [ "${1}" = 'release' ]; then execute "${2}"; else debug "${2}"; fi
          fi
        fi
        ;;
      'tidy') clangtidy ;;
      'gtest') "${BIN_DEBUG_PATH}"/UnitTestsd ;;
      *)
        printf '\nWrong Parameter: %s\n' "${P}";
        printArguments;
        break;
        ;;
      esac
    return 0;
    done
  fi
}

printArguments() {
  echo 'The valid parameters are:';
  echo 'time - Profile application and log the measured times.';
  echo 'drawt - Draw a graph of latencies with GNU Plot.';
  echo 'draws - Draw a graph of speedups with GNU Plot.';
  echo 'release - Execute MobileRT in release mode.';
  echo 'debug - Execute MobileRT in debug mode.';
  echo 'perf - Execute perf on MobileRT.';
  echo 'timeout - Execute MobileRT for a given timeout.';
  echo 'tidy - Execute C++ linter (clang-tidy) in MobileRT.';
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
