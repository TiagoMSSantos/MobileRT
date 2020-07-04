#!/bin/bash

###############################################################################
# Get arguments
###############################################################################
type="${1:-release}";
run_test="${2:-all}";
ndk_version="${3:-21.3.6528147}";
cmake_version="${4:-3.10.2}";
kill_previous="${5:-true}";
###############################################################################
###############################################################################


###############################################################################
# Get helper functions
###############################################################################
source scripts/helper_functions.sh;
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
    > ${reports_path}/logcat_${type}.log 2>&1;

  # Filter logcat of the app
  callCommand cat ${reports_path}/logcat_${type}.log \
    | egrep -i `cat ${reports_path}/logcat_${type}.log \
    | egrep -i "proc.*:puscas" | cut -d ":" -f 4 | cut -d ' ' -f 4` \
    > ${reports_path}/logcat_app_${type}.log;

  callCommand cat ${reports_path}/logcat_current_${type}.log \
    | egrep -i `cat ${reports_path}/logcat_current_${type}.log \
    | egrep -i "proc.*:puscas" | cut -d ":" -f 4 | cut -d ' ' -f 4` \
    > ${reports_path}/logcat_current_app_${type}.log;

  echo "";
  echo "";
}

function clear_func() {
  echo "Killing pid of logcat: '${pid_logcat}'";
  kill -s SIGTERM ${pid_logcat} 2> /dev/null;

  local pid_app=`adb shell ps | grep puscas.mobilertapp | tr -s ' ' | cut -d ' ' -f 2`;
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
trap catch_signal EXIT SIGHUP SIGINT SIGQUIT SIGILL SIGTRAP SIGABRT SIGTERM

pid=${BASHPID};
script_name=$(basename $0);
echo "pid: ${pid}";
echo "script name: ${script_name}";

if [ ${type} == "debug" ]; then
  code_coverage="createDebugCoverageReport";
fi

if [ ${kill_previous} == true ]; then
  callCommand ps aux \
    | grep -v grep \
    | grep -v ${pid} \
    | grep -i ${script_name} \
    | tr -s ' ' \
    | cut -d ' ' -f 2 \
    | xargs kill;
fi

clear_func;

avd_emulators=`emulator -list-avds`;
echo "Emulators available: '${avd_emulators}'";

avd_emulator=`echo ${avd_emulators} | head -1`;
echo "Start '${avd_emulator}'";

adb shell whoami;
started_emulator=${PIPESTATUS[0]};

echo "Wait for device to be available.";
emulator -avd ${avd_emulator} -writable-system 2> /dev/null &

callCommand sleep 1;
callCommand adb wait-for-device;
callCommand adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 3; done; input keyevent 82';

echo "Set adb as root, to be able to change files permissions";
adb root;

if [ ${started_emulator} -ne 0 ]; then
  callCommand sleep 7;
fi

callCommand adb shell input tap 800 900;
callCommand adb root;

echo "Set path to reports";
reports_path="./app/build/reports";
callCommand mkdir -p ${reports_path};

echo "Set path to instrumentation tests resources";
mobilert_path="/data/MobileRT";

echo "Copy unit tests";
callCommand adb shell mkdir -p ${mobilert_path};
callCommand adb shell rm -r ${mobilert_path}/*;
callCommand adb push app/build/intermediates/cmake/${type}/obj/x86/* ${mobilert_path}/;

echo "Copy tests resources";
callCommand adb push app/src/androidTest/resources/teapot ${mobilert_path}/WavefrontOBJs/teapot;
callCommand adb shell ls -la ${mobilert_path}/WavefrontOBJs;

echo "Change resources permissions";
callCommand adb shell chmod -R 777 ${mobilert_path};

echo "Disable animations";
callCommand adb shell settings put global window_animation_scale 0.0;
callCommand adb shell settings put global transition_animation_scale 0.0;
callCommand adb shell settings put global animator_duration_scale 0.0;

echo "Activate JNI extended checking mode";
callCommand adb shell setprop dalvik.vm.checkjni true;
callCommand adb shell setprop debug.checkjni 1;

echo "Clear logcat";
callCommand adb logcat -c;

echo "Copy logcat to file";
callCommand adb logcat -v threadtime *:V \
  | tee ${reports_path}/logcat_current_${type}.log 2>&1 &
pid_logcat="$!";
echo "pid of logcat: '${pid_logcat}'";

echo "Run instrumentation tests";
if [ ${run_test} == "all" ]; then
  callCommand ./gradlew connected${type}AndroidTest -DtestType="${type}" \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    ${code_coverage} --console plain \
    | tee ${reports_path}/log_tests_${type}.log 2>&1;
  resInstrumentationTests=${PIPESTATUS[0]};
  pid_instrumentation_tests="$!";
elif [[  ${run_test} == rep_* ]]; then
  run_test_without_prefix=${run_test#"rep_"};
  echo "Repeatable of test: " ${run_test_without_prefix};
  callCommandUntilError ./gradlew connected${type}AndroidTest -DtestType="${type}" \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    -Pandroid.testInstrumentationRunnerArguments.class=${run_test_without_prefix} \
    --console plain \
    | tee ${reports_path}/log_tests_${type}.log 2>&1;
  resInstrumentationTests=${PIPESTATUS[0]};
  pid_instrumentation_tests="$!";
else
  echo "Test: " + ${run_test};
  callCommand ./gradlew connected${type}AndroidTest -DtestType="${type}" \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
    -Pandroid.testInstrumentationRunnerArguments.class=${run_test} \
    --console plain \
    | tee ${reports_path}/log_tests_${type}.log 2>&1;
  resInstrumentationTests=${PIPESTATUS[0]};
  pid_instrumentation_tests="$!";
fi
echo "pid of instrumentation tests: '${pid_instrumentation_tests}'";

echo "Run unit tests";
if [ ${type} == "debug" ]; then
  # Ignore unit tests that should crash the system because of a failing assert
  callCommand adb shell LD_LIBRARY_PATH=${mobilert_path} \
    ${mobilert_path}/UnitTests \
    --gtest_filter=-*.TestInvalid* \
    | tee ${reports_path}/log_unit_tests_${type}.log 2>&1;
else
  callCommand adb shell LD_LIBRARY_PATH=${mobilert_path} \
    ${mobilert_path}/UnitTests \
    | tee ${reports_path}/log_unit_tests_${type}.log 2>&1;
fi
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
