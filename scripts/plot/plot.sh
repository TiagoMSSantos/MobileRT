#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# This script draw graphs with the latencies (or draw a graph with the speedup)
###############################################################################
###############################################################################


###############################################################################
# Change directory to MobileRT root
###############################################################################
cd "$(dirname "${0}")/../.." || exit;
###############################################################################
###############################################################################


###############################################################################
# Get helper functions
###############################################################################
# shellcheck disable=SC1091
. scripts/helper_functions.sh
###############################################################################
###############################################################################


SEP=',';
SPEEDUP='0';
if [ "${1}" = 'draws' ]; then
  SPEEDUP='1';
fi

setPaths() {
  PATH_TO_SEARCH='./';
  FILE_TO_SEARCH='MobileRT.jks';

  FIND_MOBILERT=$(find ${PATH_TO_SEARCH} -iname "${FILE_TO_SEARCH}" 2> /dev/null | head -n 1 || true);
  MOBILERT_PATH=$(echo "${FIND_MOBILERT}" | sed 's/\/app\/.*//g' || true);

  if [ -z "${MOBILERT_PATH}" ]; then
    PATH_TO_SEARCH='/';
    FIND_MOBILERT=$(find ${PATH_TO_SEARCH} -iname "MobileRT" 2> /dev/null | head -n 1);
    MOBILERT_PATH=$(echo "${FIND_MOBILERT}" | sed "s/\/app\/${FILE_TO_SEARCH}/g");
  fi

  SCRIPTS_PATH="${MOBILERT_PATH}/scripts";
  PLOT_SCRIPTS_PATH="${SCRIPTS_PATH}/plot";

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

prepareFilenames() {
  FILES="$(find "${PLOT_GRAPHS}" -type f)";
  FILE_NUMBERS="$(find "${PLOT_GRAPHS}" -type f | wc -l)";

  GRAPHS='';
  i=1;
  for FILE in ${FILES}; do
    GRAPHS="files[${i}]='${FILE}'; ${GRAPHS}";
    i=$(( i + 1 ));
  done

  numberFiles=0;
  FILENAMES='';
  for f in ${FILES}; do
    FILEPATH="./${f#"${PWD}"/}";
    FILENAMES="${FILEPATH}${SEP}${FILENAMES}";
    numberFiles=$(( numberFiles + 1 ));
  done

  FILENAMES="${FILENAMES%% }";
}

drawPlot() {
  echo "#FILES = '${FILES}'";
  echo "FILENAMES = '${FILENAMES}'";
  echo "SPEEDUP = '${SPEEDUP}'";
  echo "SEP = '${SEP}'";
  echo "GRAPHS = ${GRAPHS}";
  echo "SCRIPT = '${PLOT_SCRIPTS_PATH}/plot_output.gp'";

  gnuplot \
    -e "array files[${FILE_NUMBERS}]; ${GRAPHS}; filenumbers=${FILE_NUMBERS};" \
    -e "filenames='${FILENAMES}'" \
    -e "speedup='${SPEEDUP}'" \
    -e "separator='${SEP}'" \
    -c "${PLOT_SCRIPTS_PATH}/plot_output.gp"
}

setPaths;
prepareFilenames;
drawPlot;
