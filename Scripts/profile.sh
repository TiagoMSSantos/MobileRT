#!/bin/bash

FILE_TO_SEARCH="MobileRT.jks"
FIND_MOBILERT=$(find /mnt/D/Projects -iname "${FILE_TO_SEARCH}" 2> /dev/null | head -n 1)
MOBILERT_PATH="${FIND_MOBILERT//\/app\/"${FILE_TO_SEARCH}"/}"

echo "MOBILERT_PATH = ${MOBILERT_PATH}"

BIN_DEBUG_PATH="${MOBILERT_PATH}/build_debug/bin"
BIN_RELEASE_PATH="${MOBILERT_PATH}/build_release/bin"
SCRIPTS_PATH="${MOBILERT_PATH}/Scripts"
PLOT_SCRIPTS_PATH="${SCRIPTS_PATH}/Plot_Scripts"
OBJS_PATH="${MOBILERT_PATH}/../WavefrontOBJs"

MOBILERT_SRCS="${MOBILERT_PATH}/app"
COMPONENTS_SRCS="${MOBILERT_PATH}/app"
DEPENDENT_SRCS="${MOBILERT_PATH}/app/System_dependent"
SCENES_SRCS="${MOBILERT_PATH}/app/Scenes"

THIRDPARTY_HEADERS="${MOBILERT_PATH}/app/third_party"
GLM_HEADERS="${THIRDPARTY_HEADERS}/glm"
STB_HEADERS="${THIRDPARTY_HEADERS}/stb"
GTK_HEADERS="$(pkg-config --cflags gtk+-2.0)"
GTK_HEADERS="${GTK_HEADERS//-I/-isystem}"

if [ -z "${PLOT_GRAPHS}" ]; then
  PLOT_GRAPHS="Plot_Graphs"
fi
mkdir -p ${PLOT_GRAPHS}

for FOLDER in ${PLOT_GRAPHS[@]}
do
  FILES+=($(find ${FOLDER} -type f))
done

OBJ="${OBJS_PATH}/conference/conference.obj"
MTL="${OBJS_PATH}/conference/conference.mtl"
CAM="${OBJS_PATH}/conference/conference.cam"

OBJ="${OBJS_PATH}/teapot/teapot.obj"
MTL="${OBJS_PATH}/teapot/teapot.mtl"
CAM="${OBJS_PATH}/teapot/teapot.cam"

OBJ="${OBJS_PATH}/sponza/sponza.obj"
MTL="${OBJS_PATH}/sponza/sponza.mtl"
CAM="${OBJS_PATH}/sponza/sponza.cam"

OBJ="${OBJS_PATH}/powerplant/powerplant.obj"
MTL="${OBJS_PATH}/powerplant/powerplant.mtl"
CAM="${OBJS_PATH}/powerplant/powerplant.cam"

OBJ="${OBJS_PATH}/../San_Miguel/san-miguel.obj"
MTL="${OBJS_PATH}/../San_Miguel/san-miguel.mtl"
CAM="${OBJS_PATH}/../San_Miguel/san-miguel.cam"

OBJ="${OBJS_PATH}/../San_Miguel/san-miguel-low-poly.obj"
MTL="${OBJS_PATH}/../San_Miguel/san-miguel-low-poly.mtl"
CAM="${OBJS_PATH}/../San_Miguel/san-miguel-low-poly.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Empty-CO.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Empty-CO.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Empty-CO.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Empty-RG.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Empty-RG.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Empty-CO.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Empty-Squashed.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Empty-Squashed.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Empty-Squashed.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Empty-White.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Empty-White.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Empty-White.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Glossy-Floor.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Glossy-Floor.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Glossy-Floor.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Glossy.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Glossy.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Glossy.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Mirror.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Mirror.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Mirror.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Original.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Original.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Original.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Sphere.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Sphere.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Sphere.cam"

OBJ="${OBJS_PATH}/CornellBox/CornellBox-Water.obj"
MTL="${OBJS_PATH}/CornellBox/CornellBox-Water.mtl"
CAM="${OBJS_PATH}/CornellBox/CornellBox-Water.cam"

OBJ="${OBJS_PATH}/CornellBox/water.obj"
MTL="${OBJS_PATH}/CornellBox/water.mtl"
CAM="${OBJS_PATH}/CornellBox/water.cam"

OBJ="${OBJS_PATH}/../San_Miguel/san-miguel.obj"
MTL="${OBJS_PATH}/../San_Miguel/san-miguel.mtl"
CAM="${OBJS_PATH}/../San_Miguel/san-miguel.cam"

export ASAN_OPTIONS="suppressions=sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1:detect_stack_use_after_return=1:check_initialization_order=1:strict_init_order=1:halt_on_error=0:detect_odr_violation=1"

export LSAN_OPTIONS="suppressions=sanitizer_ignore.suppr:verbosity=1:strict_string_checks=1"

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
REPETITIONS="10"

SHADERS="1 2"
SCENES="2"
ACCELERATORS="1 2"

function execute {
  THREAD="4"
  SHADER="1"
  SCENE="4"
  ACC="2"
  PRINT="true"
  SHOWIMAGE="true"
  ASYNC="true"
  SPP="1"
  SPL="1"

  echo ""
  echo "THREAD = "${THREAD}
  echo "SHADER = "${SHADER}
  echo "SCENE = "${SCENE}
  echo "ACC = "${ACC}

  #perf script report callgrind > perf.callgrind
  #kcachegrind perf.callgrind
  #perf stat \
  #perf record -g --call-graph 'fp' -- \
  #perf record -g --call-graph 'fp' --  \
  ${BIN_RELEASE_PATH}/AppInterface \
            ${THREAD} ${SHADER} ${SCENE} ${SPP} ${SPL} ${WIDTH} ${HEIGHT} ${ACC} ${REP} \
            ${OBJ} ${MTL} ${CAM} ${PRINT} ${ASYNC} ${SHOWIMAGE}
  #perf report -g '' --show-nr-samples --hierarchy
}



function clangtidy {
  clang-tidy \
	-analyze-temporary-dtors \
	-checks='*,-*llvm-header-guard*,-fuchsia-default-arguments,-fuchsia-overloaded-operator' \
	-header-filter='.*' \
  ${MOBILERT_SRCS}/MobileRT/*.*pp \
  ${MOBILERT_SRCS}/MobileRT/*/*.*pp \
  ${COMPONENTS_SRCS}/Components/*/*.*pp \
	${DEPENDENT_SRCS}/Linux/*.*pp \
  ${SCENES_SRCS}/*.*pp \
	-- -std=c++17 -ferror-limit=1 -stdlib=libc++ \
  -I ${MOBILERT_SRCS} \
  -I ${COMPONENTS_SRCS} \
  -I ${DEPENDENT_SRCS}/Linux \
  -I ${SCENES_SRCS} \
  -isystem ${THIRDPARTY_HEADERS} \
  -isystem ${GLM_HEADERS} \
  -isystem ${STB_HEADERS} \
  -isystem /usr/include/c++/7 \
  -isystem /usr/include/c++/v1 \
  -isystem /usr/include/x86_64-linux-gnu/c++/7 \
  -isystem /usr/include/glib-2.0/gobject \
  -isystem /usr/include/gtk-2.0/gtk \
	${GTK_HEADERS} \
	2>&1 | tee ${SCRIPTS_PATH}/tidy.out
}

function profile {
  trap "exit" INT
  for R in `seq 1 ${REPETITIONS}`;
  do
    for THREAD in ${THREADS[@]}
    do
      for SHADER in ${SHADERS[@]}
      do
        for SCENE in ${SCENES[@]}
        do
          for ACC in ${ACCELERATORS[@]}
          do
            echo ""
            echo "REPETITION = ${R}"
            echo "THREAD = "${THREAD}
            echo "SHADER = "${SHADER}
            echo "SCENE = "${SCENE}
            echo "ACC = "${ACC}

            PLOT_FILE="SC${SCENE}${SEP}SH${SHADER}${SEP}A${ACC}${SEP}R${WIDTH}x${HEIGHT}"

            ${BIN_DEBUG_PATH}/AppInterfaced \
            ${THREAD} ${SHADER} ${SCENE} ${SPP} ${SPL} ${WIDTH} ${HEIGHT} ${ACC} ${REP} \
            ${OBJ} ${MTL} ${CAM} ${PRINT} ${ASYNC} ${SHOWIMAGE} \
            | awk -v threads="${THREAD}" -f ${PLOT_SCRIPTS_PATH}/parser_out.awk 2>&1 \
            | tee -a ${PLOT_GRAPHS}/${PLOT_FILE}.dat

          done
        done
      done
    done
    ((R++))
  done
}

PARAM1="time"
PARAM2="drawt"
PARAM3="draws"
PARAM4="test"
PARAM5="exec"
PARAM6="tidy"
PARAM7="gtest"

for P in ${@}
do
  case ${P} in
    ${PARAM1}) profile; sleep 2s ;;
    ${PARAM2}) . ${PLOT_SCRIPTS_PATH}/plot.sh 0;;
    ${PARAM3}) . ${PLOT_SCRIPTS_PATH}/plot.sh 1;;
    ${PARAM4}) awk -f "${PLOT_SCRIPTS_PATH}/parser_median.awk" "${PLOT_SCRIPTS_PATH}/test.dat"  ;;
    ${PARAM5}) execute ;;
    ${PARAM6}) clangtidy ;;
    ${PARAM7}) ${BIN_DEBUG_PATH}/GoogleTestd ;;
    *) echo ""
       echo "Wrong Parameter: ${P}"
       echo "The valid parameters are:"
       echo "${PARAM1} - Profile application and log the measured times."
       echo "${PARAM2} - Draw a graph with GNU Plot."
       break
       ;;
  esac
done
