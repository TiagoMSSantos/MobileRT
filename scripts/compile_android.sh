#!/bin/bash

###############################################################################
# Change directory to MobileRT root
###############################################################################
cd "$( dirname "${BASH_SOURCE[0]}" )/.." || exit
###############################################################################
###############################################################################


###############################################################################
# Get arguments
###############################################################################
type="${1:-Release}";
ndk_version="${2:-21.3.6528147}";
cmake_version="${3:-3.10.2}";
###############################################################################
###############################################################################


###############################################################################
# Get helper functions
###############################################################################
source scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Compile for Android
###############################################################################

# Capitalize 1st letter
type="$(tr '[:lower:]' '[:upper:]' <<< "${type:0:1}")${type:1}";
echo "type: '${type}'";

# Set path to reports
reports_path=./app/build/reports;
callCommand mkdir -p ${reports_path};

rm -rf ./app/.cxx/;
rm -rf ./app/build/;

files_being_used=$(find . -name "*.fuse_hidden*" | grep -i ".fuse_hidden");
echo "files_being_used: '${files_being_used}'";

if [ "${files_being_used}" != "" ]; then
  while IFS= read -r file; do
    processes_using_file=$(lsof "${file}" | tail -n +2 | tr -s ' ');
    echo "processes_using_file: '${processes_using_file}'";
    processes_id_using_file=$(echo "${processes_using_file}" | cut -d ' ' -f 2 | head -1);
    echo "Going to kill this process: '${processes_id_using_file}'";
    kill "${processes_id_using_file}";
    while [ -f "${file}" ]; do
      echo "sleeping 1 sec";
      sleep 1;
    done
  done <<< "${files_being_used}";
fi

callCommand rm -rf ./app/.cxx/;
callCommand rm -rf ./app/build/;

callCommand ./gradlew clean assemble"${type}" --profile --parallel \
  -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
  --console plain \
  | tee ${reports_path}/log_build_"${type}".log 2>&1;
resCompile=${PIPESTATUS[0]};
###############################################################################
###############################################################################


###############################################################################
# Exit code
###############################################################################
echo "########################################################################"
echo "Results:"
if [ "${resCompile}" -eq 0 ]; then
  echo "Compilation: success";
else
  echo "Compilation: failed";
  exit "${resCompile}";
fi
###############################################################################
###############################################################################
