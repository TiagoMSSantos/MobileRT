#!/bin/bash

###############################################################################
# Get arguments
###############################################################################
variant="${1:-release}";
ndk_version="${2:-21.0.6113669}";
cmake_version="${3:-3.6.0}";
###############################################################################
###############################################################################


###############################################################################
# Get helper functions
###############################################################################
source Scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Helper functions
###############################################################################
function gather_logs_func() {
  echo "";
  echo "";
  echo "Gathering logs";

  # Copy logcat to file
  callCommand adb logcat -v threadtime -d *:V \
    > ${reports_path}/logcat_${variant}.log 2>&1;

  echo "Gathering logs 1";

  # Filter logcat of the app
  callCommand cat ${reports_path}/logcat_${variant}.log \
    | egrep -i `cat ${reports_path}/logcat_${variant}.log \
    | egrep -i "proc.*:puscas" | cut -d ":" -f 4 | cut -d ' ' -f 4` \
    > ${reports_path}/logcat_app_${variant}.log;

  echo "Gathering logs 2";

  callCommand cat ${reports_path}/logcat_current_${variant}.log \
    | egrep -i `cat ${reports_path}/logcat_current_${variant}.log \
    | egrep -i "proc.*:puscas" | cut -d ":" -f 4 | cut -d ' ' -f 4` \
    > ${reports_path}/logcat_current_app_${variant}.log;

  echo "Gathering logs 3";

  echo "";
  echo "";
}

function clear_func() {
  echo "Killing pid of logcat: '${pid_logcat}'";
  kill -s SIGTERM ${pid_logcat} 2> /dev/null;

  pid_app=`adb shell pidof puscas.mobilertapp`;
  echo "Killing pid of MobileRT: '${pid_app}'";
  adb shell kill -s SIGTERM ${pid_app} 2> /dev/null;
}

function catch_signal() {
  echo "";
  echo "";
  echo "Caught signal";

  gather_logs_func;
  clear_func;

  echo "";
  echo "";
}
###############################################################################
###############################################################################


###############################################################################
# Run Android tests in emulator
###############################################################################

set -m;

echo "Prepare traps";
trap catch_signal EXIT

echo "Set adb as root, to be able to change files permissions";
callCommand adb root;

echo "Set path to reports";
reports_path="./app/build/reports";
callCommand mkdir -p ${reports_path};

echo "Set path to instrumentation tests resources";
mobilert_path="/data/MobileRT";

echo "Copy unit tests";
callCommand adb shell rm -rf ${mobilert_path};
callCommand adb push app/build/intermediates/cmake/${variant}/obj/x86/ ${mobilert_path};

#echo "Copy tests resources";
#callCommand adb push app/src/androidTest/resources/teapot ${mobilert_path}/WavefrontOBJs/teapot;
#callCommand adb shell ls -la ${mobilert_path}/WavefrontOBJs;

echo "Change resources permissions";
callCommand adb shell chmod -R 777 ${mobilert_path};

echo "Disable animations";
callCommand adb shell settings put global window_animation_scale 0.0;
callCommand adb shell settings put global transition_animation_scale 0.0;
callCommand adb shell settings put global animator_duration_scale 0.0;

echo "Clear logcat";
callCommand adb logcat -c;

echo "Copy logcat to file";
callCommand nohup adb logcat -v threadtime *:V \
  | tee ${reports_path}/logcat_current_${variant}.log 2>&1 &
pid_logcat="$!";
echo "pid of logcat: '${pid_logcat}'";

echo "Run instrumentation tests";
callCommand ./gradlew connectedAndroidTest -DtestType="${variant}" \
  -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
  | tee ${reports_path}/log_tests_${variant}.log 2>&1;
resInstrumentationTests=${PIPESTATUS[0]};
pid_instrumentation_tests="$!";
echo "pid of instrumentation tests: '${pid_instrumentation_tests}'";

echo "Run unit tests";
callCommand nohup adb shell LD_LIBRARY_PATH=${mobilert_path} ${mobilert_path}/UnitTests \
  | tee ${reports_path}/log_unit_tests_${variant}.log 2>&1;
resUnitTests=${PIPESTATUS[0]};
###############################################################################
###############################################################################


###############################################################################
# Exit code
###############################################################################
echo "########################################################################"
echo "Results:"
res=0
if [ ${resUnitTests} -eq 0 ]; then
  echo "Unit tests: success (${resUnitTests})";
else
  echo "Unit tests: failed (${resUnitTests})";
  res=${resUnitTests}
fi

if [ ${resInstrumentationTests} -eq 0 ]; then
  echo "Instrumentation tests: success (${resInstrumentationTests})";
else
  echo "Instrumentation tests: failed (${resInstrumentationTests})";
  res=${resInstrumentationTests}
fi
echo "";
echo "";
exit ${res}
###############################################################################
###############################################################################
