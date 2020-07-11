#!/bin/bash

###############################################################################
# Change directory to MobileRT root
###############################################################################
cd "$( dirname "${BASH_SOURCE[0]}" )/../.." || exit
###############################################################################
###############################################################################


###############################################################################
# Get helper functions
###############################################################################
source scripts/helper_functions.sh;
###############################################################################
###############################################################################

SEP=" "

i=1;
for FILE in ${FILES[@]}
do
  GRAPH[${i}]="file${i}='${FILE}'";
  ((i++));
done

i=1;
for f in ${GRAPH[@]}
do
  GRAPHS+=" -e ${f}";
  ((i++));
done

i=0;
for f in ${FILES[@]}
do
  FILEPATH=./${f#${PWD}/};
  FILENAMES+="${FILEPATH}${SEP}";
  ((i++));
done
FILENAMES="${FILENAMES%% }"

SPEEDUP=${1};

echo "#FILES = '${#FILES[@]}'"
echo "FILENAMES = '${FILENAMES}'"
echo "SPEEDUP = '${SPEEDUP}'"
echo "SEP = '${SEP}'"
echo "SCRIPT = '${PLOT_SCRIPTS_PATH}/plot_output.gp'"

callCommand gnuplot \
  -e "files='${#FILES[@]}'" \
  -e "filenames='${FILENAMES}'" \
  -e "speedup='${SPEEDUP}'" \
  -e "separator='${SEP}'"${GRAPHS} \
  -c "${PLOT_SCRIPTS_PATH}/plot_output.gp";
