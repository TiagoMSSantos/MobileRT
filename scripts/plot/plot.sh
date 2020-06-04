#!/bin/bash

SEP=" "

i=1
for FILE in ${FILES[@]}
do
  GRAPH[${i}]="file${i}='"${FILE}"'"
  ((i++))
done

i=1
for f in ${GRAPH[@]}
do
  GRAPHS+=" -e ${GRAPH[${i}]}"
  ((i++))
done

i=0
for f in ${FILES[@]}
do
  FILENAMES+="${FILES[${i}]}${SEP}"
  ((i++))
done

SPEEDUP=${1}
gnuplot \
  -e "files='${#FILES[@]}'" \
  -e "filenames='${FILENAMES}'" \
  -e "speedup='${SPEEDUP}'" \
  -e "separator='${SEP}'" ${GRAPHS} \
  -c ${PLOT_SCRIPTS}/plot_output.gp