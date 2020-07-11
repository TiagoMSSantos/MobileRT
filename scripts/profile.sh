#!/bin/bash

###############################################################################
# Change directory to MobileRT root
###############################################################################
cd "$( dirname "${BASH_SOURCE[0]}" )/.."
###############################################################################
###############################################################################


###############################################################################
# Set paths for MobileRT
###############################################################################
PATH_TO_SEARCH="/mnt/D/Projects"
FILE_TO_SEARCH="MobileRT.jks"

FIND_MOBILERT=$(find ${PATH_TO_SEARCH} -iname "${FILE_TO_SEARCH}" 2> /dev/null | head -n 1)
MOBILERT_PATH="${FIND_MOBILERT//\/app\/"${FILE_TO_SEARCH}"/}"

if [ -z "${MOBILERT_PATH}" ]
then
    PATH_TO_SEARCH="/"
    FIND_MOBILERT=$(find ${PATH_TO_SEARCH} -iname "MobileRT" 2> /dev/null | head -n 1)
    MOBILERT_PATH="${FIND_MOBILERT//\/app\/"${FILE_TO_SEARCH}"/}"
fi

echo "FILE_TO_SEARCH = ${FILE_TO_SEARCH}"
echo "PATH_TO_SEARCH = ${PATH_TO_SEARCH}"
echo "FIND_MOBILERT = ${FIND_MOBILERT}"
echo "MOBILERT_PATH = ${MOBILERT_PATH}"

BIN_DEBUG_PATH="${MOBILERT_PATH}/build_Debug/bin"
BIN_RELEASE_PATH="${MOBILERT_PATH}/build_Release/bin"
SCRIPTS_PATH="${MOBILERT_PATH}/scripts"
PLOT_SCRIPTS_PATH="${SCRIPTS_PATH}/plot"
OBJS_PATH="${MOBILERT_PATH}/WavefrontOBJs"
###############################################################################
###############################################################################


###############################################################################
# Set paths for MobileRT sources and headers
###############################################################################
MOBILERT_SRCS="${MOBILERT_PATH}/app"
COMPONENTS_SRCS="${MOBILERT_PATH}/app"
DEPENDENT_SRCS="${MOBILERT_PATH}/app/System_dependent"
SCENES_SRCS="${MOBILERT_PATH}/app/Scenes"

THIRDPARTY_HEADERS="-isystem ${MOBILERT_PATH}/app/third_party"
GLM_HEADERS="-isystem ${THIRDPARTY_HEADERS}/glm"
STB_HEADERS="-isystem ${THIRDPARTY_HEADERS}/stb"
BOOST_HEADERS_ROOT="-isystem ${THIRDPARTY_HEADERS}/boost/libs/"
BOOST_HEADERS="-isystem  ${BOOST_HEADERS_ROOT}/assert/include"
BOOST_HEADERS="${BOOST_HEADERS} -isystem  ${BOOST_HEADERS_ROOT}/assert/include/boost"
###############################################################################
###############################################################################


if [ -z "${PLOT_GRAPHS}" ]; then
  PLOT_GRAPHS="Plot_Graphs"
fi
mkdir -p ${PLOT_GRAPHS}

for FOLDER in "${PLOT_GRAPHS[@]}"
do
  FILES+=("$(find ${FOLDER} -type f)")
done


###############################################################################
# Set paths for the scene
###############################################################################
SCN="${OBJS_PATH}/conference/conference"
SCN="${OBJS_PATH}/teapot/teapot"
SCN="${OBJS_PATH}/buddha/buddha"
SCN="${OBJS_PATH}/dragon/dragon"
SCN="${OBJS_PATH}/sponza/sponza"
SCN="${OBJS_PATH}/powerplant/powerplant"
SCN="${OBJS_PATH}/San_Miguel/san-miguel"
SCN="${OBJS_PATH}/San_Miguel/san-miguel-low-poly"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-CO"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-Squashed"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Empty-White"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Glossy-Floor"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Glossy"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Mirror"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Original"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Sphere"
SCN="${OBJS_PATH}/CornellBox/CornellBox-Water"
SCN="${OBJS_PATH}/CornellBox/water"

SCN="${OBJS_PATH}/conference/conference"
OBJ="${SCN}.obj"
MTL="${SCN}.mtl"
CAM="${SCN}.cam"
###############################################################################
###############################################################################


###############################################################################
# Set options for the sanitizers
###############################################################################
export ASAN_OPTIONS="suppressions=sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1:detect_stack_use_after_return=1:check_initialization_order=1:strict_init_order=1:halt_on_error=0:detect_odr_violation=1"

export LSAN_OPTIONS="suppressions=sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1"
###############################################################################
###############################################################################


###############################################################################
# Set arguments for MobileRT
###############################################################################
SPP="1"
SPL="1"
WIDTH="900"
HEIGHT="900"
REP="1"
PRINT="false"
ASYNC="false"
SHOWIMAGE="false"
SEP="-"

THREADS="1"
REPETITIONS="1"

SHADERS="1 2"
SCENES="2"
ACCELERATORS="2"

THREAD=$(nproc --all)
SHADER="1"
SCENE="4"
ACC="3"
PRINT="true"
SHOWIMAGE="true"
ASYNC="true"
SPP="1"
SPL="1"
###############################################################################
###############################################################################

function execute {
  echo ""
  echo "THREAD = ${THREAD}"
  echo "SHADER = ${SHADER}"
  echo "SCENE = ${SCENE}"
  echo "ACC = ${ACC}"

  #perf script report callgrind > perf.callgrind
  #kcachegrind perf.callgrind
  #perf stat \
  #perf record -g --call-graph 'fp' -- \
  "${BIN_RELEASE_PATH}"/AppMobileRT \
    "${THREAD}" ${SHADER} ${SCENE} ${SPP} ${SPL} ${WIDTH} ${HEIGHT} ${ACC}
    ${REP} "${OBJ}" "${MTL}" "${CAM}" ${PRINT} ${ASYNC} ${SHOWIMAGE}
  #perf report -g '' --show-nr-samples --hierarchy
}

function debug {
  echo ""
  echo "THREAD = ${THREAD}"
  echo "SHADER = ${SHADER}"
  echo "SCENE = ${SCENE}"
  echo "ACC = ${ACC}"

  "${BIN_DEBUG_PATH}"/AppMobileRTd \
    "${THREAD}" ${SHADER} ${SCENE} ${SPP} ${SPL} ${WIDTH} ${HEIGHT} ${ACC}
    ${REP} "${OBJ}" "${MTL}" "${CAM}" ${PRINT} ${ASYNC} ${SHOWIMAGE}
}


function clangtidy {
  GTK_HEADERS="$(pkg-config --cflags gtk+-2.0)"
  GTK_HEADERS="${GTK_HEADERS//-I/-isystem}"

  clang-tidy \
	-analyze-temporary-dtors \
	-checks='*,-*llvm-header-guard*,-fuchsia-default-arguments,-fuchsia-overloaded-operator' \
	-header-filter='.*' \
	"${MOBILERT_SRCS}"/MobileRT/*.*pp \
	"${MOBILERT_SRCS}"/MobileRT/*/*.*pp \
	"${COMPONENTS_SRCS}"/Components/*/*.*pp \
	"${DEPENDENT_SRCS}"/Linux/*.*pp \
	"${SCENES_SRCS}"/*.*pp \
	-- -std=c++11 -ferror-limit=1 -stdlib=libc++ \
	-I "${MOBILERT_SRCS}" \
	-I "${COMPONENTS_SRCS}" \
	-I "${DEPENDENT_SRCS}"/Linux \
	-I "${SCENES_SRCS}" \
	-isystem "${THIRDPARTY_HEADERS}" \
	-isystem "${GLM_HEADERS}" \
	-isystem "${STB_HEADERS}" \
	"${BOOST_HEADERS}" \
	-isystem /usr/include/c++/7 \
	-isystem /usr/include/c++/v1 \
	-isystem /usr/include/x86_64-linux-gnu/c++/7 \
	-isystem /usr/include/glib-2.0/gobject \
	-isystem /usr/include/gtk-2.0/gtk \
	"${GTK_HEADERS}" \
	2>&1 | tee "${SCRIPTS_PATH}"/tidy.out
}

function profile {
  ASYNC=false
  trap "exit" INT
  for R in $(seq 1 ${REPETITIONS});
  do
    for THREAD in "${THREADS[@]}"
    do
      for SHADER in "${SHADERS[@]}"
      do
        for SCENE in "${SCENES[@]}"
        do
          for ACC in "${ACCELERATORS[@]}"
          do
            echo ""
            echo "REPETITION = ${R}"
            echo "THREAD = ${THREAD}"
            echo "SHADER = ${SHADER}"
            echo "SCENE = ${SCENE}"
            echo "ACC = ${ACC}"
			      echo "ASYNC = ${ASYNC}"

            PLOT_FILE="SC${SCENE}${SEP}SH${SHADER}${SEP}A${ACC}${SEP}R${WIDTH}x${HEIGHT}"

            "${BIN_RELEASE_PATH}"/AppMobileRT \
            ${THREAD} "${SHADER}" ${SCENE} ${SPP} ${SPL} ${WIDTH} ${HEIGHT} "${ACC}" ${REP} \
            "${OBJ}" "${MTL}" "${CAM}" ${PRINT} ${ASYNC} ${SHOWIMAGE} \
            | awk -v threads="${THREAD}" -f "${PLOT_SCRIPTS_PATH}"/parser_out.awk 2>&1 \
            | tee -a ${PLOT_GRAPHS}/"${PLOT_FILE}".dat

          done
        done
      done
    done
    ((R++))
  done
}


###############################################################################
# Parse arguments
###############################################################################
PARAM1="time"
PARAM2="drawt"
PARAM3="draws"
PARAM4="test"
PARAM5="Release"
PARAM6="tidy"
PARAM7="gtest"
PARAM8="Debug"

if [ $# -eq 0 ]; then
  execute
else
  for P in "${@}"
  do
    case ${P} in
      ${PARAM1}) profile; sleep 2s ;;
      ${PARAM2}) . ./scripts/plot/plot.sh 0;;
      ${PARAM3}) . ./scripts/plot/plot.sh 1;;
      ${PARAM4}) awk -f "${PLOT_SCRIPTS_PATH}/parser_median.awk" "${PLOT_SCRIPTS_PATH}/test.dat"  ;;
      ${PARAM5}) execute ;;
      ${PARAM6}) clangtidy ;;
      ${PARAM7}) "${BIN_DEBUG_PATH}"/UnitTestsd ;;
      ${PARAM8}) debug ;;
      *) echo ""
         echo "Wrong Parameter: ${P}"
         echo "The valid parameters are:"
         echo "${PARAM1} - Profile application and log the measured times."
         echo "${PARAM2} - Draw a graph with GNU Plot."
         break
         ;;
    esac
  done
fi
###############################################################################
###############################################################################
