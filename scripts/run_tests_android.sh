#!/bin/bash

###############################################################################
# README
###############################################################################
# This script runs the Android Instrumentation Tests of MobileRT on an
# Android emulator.
###############################################################################
###############################################################################


###############################################################################
# Change directory to MobileRT root.
###############################################################################
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit;
###############################################################################
###############################################################################


###############################################################################
# Exit immediately if a command exits with a non-zero status.
###############################################################################
set -euo pipefail;
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
source scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Set default arguments.
###############################################################################
type="release";
run_test="all";
ndk_version="21.3.6528147";
cmake_version="3.10.2";
kill_previous="true";
parallelizeBuild;

function printEnvironment() {
  echo "";
  echo "Selected arguments:";
  echo "type: ${type}";
  echo "run_test: ${run_test}";
  echo "ndk_version: ${ndk_version}";
  echo "cmake_version: ${cmake_version}";
  echo "kill_previous: ${kill_previous}";
}
###############################################################################
###############################################################################


###############################################################################
# Set paths.
###############################################################################
echo "Set path to reports";
reports_path="./app/build/reports";

echo "Set path to instrumentation tests resources";
mobilert_path="/data/local/tmp/MobileRT";
sdcard_path="/mnt/sdcard/MobileRT";
sdcard_path_android_11_emulator="/storage/1CE6-261B/MobileRT";
###############################################################################
###############################################################################


###############################################################################
# Parse arguments.
###############################################################################
parseArgumentsToTestAndroid "$@";
printEnvironment;
###############################################################################
###############################################################################


###############################################################################
# Helper functions.
###############################################################################
function gather_logs_func() {
  echo "";
  echo "";
  echo "Gathering logs";

  echo "Finding at least 1 Android device on. (2)";
  callCommandUntilSuccess adb shell ps > /dev/null;

  echo "Copy logcat to file"
  adb logcat -v threadtime -d "*":V \
    > "${reports_path}"/logcat_"${type}".log 2>&1;
  echo "Copied logcat to logcat_${type}.log";

  set +e;
  local pid_app;
  pid_app=$(grep -E -i "proc.puscas:*" "${reports_path}"/logcat_"${type}".log |
    grep -i "pid=" | cut -d "=" -f 2 | cut -d "u" -f 1 | tr -d ' ' | tail -1);
  echo "Filter logcat of the app: ${pid_app}";
  cat "${reports_path}"/logcat_"${type}".log | grep -e "${pid_app}" -e "I DEBUG" \
      > "${reports_path}"/logcat_app_"${type}".log;

  echo "Filter realtime logcat of the app";
  cat "${reports_path}"/logcat_current_"${type}".log |
    grep -E -i "$(grep -E -i "proc.*:puscas" \
      "${reports_path}"/logcat_current_"${type}".log |
      cut -d ":" -f 4 | cut -d ' ' -f 4)" \
      > "${reports_path}"/logcat_current_app_"${type}".log;
  echo "Filtered realtime logcat of the app";
  set -e;

  echo -e '\e]8;;file:///'"${PWD}"'/'"${reports_path}"'/androidTests/connected/index.html\aClick here to check the Android tests report.\e]8;;\a';
  echo -e '\e]8;;file:///'"${PWD}"'/'"${reports_path}"'/coverage/'"${type}"'/index.html\aClick here to check the Code coverage report.\e]8;;\a';
  echo -e '\e]8;;file:///'"${PWD}"'/'"${reports_path}"'/logcat_app_'"${type}"'.log\aClick here to check the app log.\e]8;;\a';
  echo "";
  echo "";
}

function clear_func() {
  echo "Killing pid of logcat: '${pid_logcat}'";
  set +e;
  kill -SIGTERM "${pid_logcat}" 2> /dev/null;
  set -e;

  local pid_app;
  echo "Will kill MobileRT process";
  pid_app=$(adb shell ps | grep -i puscas.mobilertapp | tr -s ' ' | cut -d ' ' -f 2);
  echo "Killing pid of MobileRT: '${pid_app}'";
  set +e;
  adb shell kill -SIGTERM "${pid_app}" 2> /dev/null;
  set -e;

  # Kill all processes in the whole process group, thus killing also descendants.
  echo "All processes will be killed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
  set +e;
  kill -SIGKILL -- -$$;
  set -e;
}

function catch_signal() {
  echo "";
  echo "";
  echo "Caught signal";

  gather_logs_func;
  # clear_func;

  echo "";
  echo "";
}
###############################################################################
###############################################################################


###############################################################################
# Run Android tests in emulator.
###############################################################################

function unlockDevice() {
  echo "Unlock device";
  callCommandUntilSuccess adb shell input tap 800 400;
  callCommandUntilSuccess adb shell input keyevent 82;
  callCommandUntilSuccess adb shell input tap 800 600;
  callCommandUntilSuccess adb shell input keyevent 82;
  callCommandUntilSuccess adb shell input tap 800 200;
  callCommandUntilSuccess adb shell input keyevent 82;
}

function runEmulator() {
  set +u;
  pid=${BASHPID};
  set -u;

  script_name=$(basename "${0}");
  echo "pid: ${pid}";
  echo "script name: ${script_name}";

  if [ "${type}" == "debug" ]; then
    code_coverage="createDebugCoverageReport";
  fi

  if [ "${kill_previous}" == true ]; then
    echo "Killing previous process";
    set +e;
    ps aux |
      grep -v "grep" |
      grep -v "${pid}" |
      grep -i "${script_name}" |
      tr -s ' ' |
      cut -d ' ' -f 2 |
      xargs kill;
    set -e;
  fi

  if [ -x "$(command -v emulator)" ]; then
    avd_emulators=$(emulator -list-avds);
    echo "Emulators available: '${avd_emulators}'";
    avd_emulator=$(echo "${avd_emulators}" | head -1);
    echo "Start '${avd_emulator}'";
  else
    echo "Command emulator is NOT installed.";
  fi
}

function waitForEmulator() {
  echo "Wait for device to be available.";
  # Don't make the Android emulator belong in the process group, so it will not be killed at the end.
  set -m;

  echo "Killing previous ADB process, just in case it was stuck.";
  local ADB_PROCESS;
  set +e;
  ADB_PROCESS=$(ps aux | grep -i "adb" | grep -v "grep" | tr -s ' ' | cut -d ' ' -f 2 | head -1);
  echo "Will kill process: '${ADB_PROCESS}'";
  kill -SIGKILL "${ADB_PROCESS}";
  local adb_devices_running;
  adb_devices_running=$(adb devices | tail -n +2);
  set -e;
  echo "Devices running: '${adb_devices_running}'";
  if [ -z "${adb_devices_running}" ]; then
    echo "Booting a new Android emulator";
    callCommandUntilSuccess cpulimit -- emulator -avd "${avd_emulator}" -writable-system &
  else
    echo "Android emulator '${adb_devices_running}' already running.";
  fi

  echo "Finding at least 1 Android device on. (1)";
  callCommandUntilSuccess adb shell ps > /dev/null;

  echo "Prepare traps";
  trap 'catch_signal ${?} ${LINENO}' EXIT SIGHUP SIGINT SIGQUIT SIGILL SIGTRAP SIGABRT SIGTERM;

  # Make the all other processes belong in the process group, so that will be killed at the end.
  set +m;

  echo "Wait for device to be ready to unlock.";
  adb kill-server;
  callCommandUntilSuccess adb start-server;
  callCommandUntilSuccess adb wait-for-device;
  # adb shell needs ' instead of ", so 'getprop' works properly
  callCommandUntilSuccess adb shell 'while [[ $(getprop service.bootanim.exit) -ne 1 ]]; do sleep 1; done;';
  callCommandUntilSuccess adb shell whoami;

  echo "Set adb as root, to be able to change files permissions";
  callCommandUntilSuccess adb root;

  unlockDevice;

  local adb_devices_running;
  set +e;
  adb_devices_running=$(callCommandUntilSuccess adb devices | grep -v "List of devices attached");
  set -e;
  echo "Devices running: '${adb_devices_running}'";
  if [ -z "${adb_devices_running}" ]; then
    # Abort if emulator didn't start.
    echo "Android emulator didn't start ... will exit.";
    exit 1;
  fi
  # Make sure ADB daemon started properly.
  callCommandUntilSuccess adb start-server;
  callCommandUntilSuccess adb wait-for-device;
}

function copyResources() {
  mkdir -p ${reports_path};

  echo "Checking files in root";
  callCommandUntilSuccess adb shell ls -la /;
  echo "Checking files in /mnt";
  callCommandUntilSuccess adb shell ls -la /mnt;
  echo "Prepare copy unit tests";
  set +e;
  adb shell mount -o remount,rw /mnt/sdcard;
  adb shell mount -o remount,rw /mnt/media_rw/1CE6-261B;
  set -e;
  callCommandUntilSuccess adb shell mkdir -p ${mobilert_path};
  callCommandUntilSuccess adb shell mkdir -p ${sdcard_path};
  callCommandUntilSuccess adb shell mkdir -p ${sdcard_path_android_11_emulator};
  callCommandUntilSuccess adb shell rm -r ${mobilert_path};
  callCommandUntilSuccess adb shell rm -r ${sdcard_path};
  callCommandUntilSuccess adb shell rm -r ${sdcard_path_android_11_emulator};
  callCommandUntilSuccess adb shell mkdir -p ${mobilert_path};
  callCommandUntilSuccess adb shell mkdir -p ${sdcard_path};
  callCommandUntilSuccess adb shell mkdir -p ${sdcard_path_android_11_emulator};

  echo "Copy tests resources";
  callCommandUntilSuccess adb push app/src/androidTest/resources/teapot ${mobilert_path}/WavefrontOBJs/teapot;
  callCommandUntilSuccess adb push app/src/androidTest/resources/CornellBox ${sdcard_path}/WavefrontOBJs/CornellBox;
  set +e;
  adb push app/src/androidTest/resources/CornellBox ${sdcard_path_android_11_emulator}/WavefrontOBJs/CornellBox;
  set -e;

  echo "Copy File Manager";
  callCommandUntilSuccess adb push app/src/androidTest/resources/APKs ${mobilert_path}/;

  echo "Change resources permissions";
  callCommandUntilSuccess adb shell chmod -R 777 ${mobilert_path};
  callCommandUntilSuccess adb shell chmod -R 777 ${sdcard_path};
  callCommandUntilSuccess adb shell chmod -R 777 ${sdcard_path_android_11_emulator};

  echo "Install File Manager";
  callCommandUntilSuccess adb shell pm install -t -r "${mobilert_path}/APKs/com.asus.filemanager.apk";
}

function startCopyingLogcatToFile() {
  # Make sure ADB daemon started properly.
  callCommandUntilSuccess adb start-server;
  callCommandUntilSuccess adb wait-for-device;

  # echo "Disable animations";
  # puscas.mobilertapp not found
  # adb shell pm grant puscas.mobilertapp android.permission.SET_ANIMATION_SCALE;

  # /system/bin/sh: settings: not found
  # adb shell settings put global window_animation_scale 0.0;
  # adb shell settings put global transition_animation_scale 0.0;
  # adb shell settings put global animator_duration_scale 0.0;

  echo "Activate JNI extended checking mode";
  callCommandUntilSuccess adb shell setprop dalvik.vm.checkjni true;
  callCommandUntilSuccess adb shell setprop debug.checkjni 1;

  echo "Clear logcat";
  callCommandUntilSuccess adb root;
  callCommandUntilSuccess adb shell logcat -b all -b main -b system -b radio -b events -b crash -c;

  echo "Copy realtime logcat to file";
  adb logcat -v threadtime "*":V \
    2>&1 | tee ${reports_path}/logcat_current_"${type}".log &
  pid_logcat="$!";
  echo "pid of logcat: '${pid_logcat}'";
}

function runUnitTests() {
  local files;
  files=$(ls app/build/intermediates/cmake/"${type}"/obj/x86);
  echo "Copy unit tests: ${files}";
  adb push app/build/intermediates/cmake/"${type}"/obj/x86/* ${mobilert_path}/;

  echo "Run unit tests";
  if [ "${type}" == "debug" ]; then
    # Ignore unit tests that should crash the system because of a failing assert.
    adb shell LD_LIBRARY_PATH=${mobilert_path} \
      ${mobilert_path}/UnitTests \
      --gtest_filter=-*.TestInvalid* \
      2>&1 | tee ${reports_path}/log_unit_tests_"${type}".log;
  else
    adb shell LD_LIBRARY_PATH=${mobilert_path} \
      ${mobilert_path}/UnitTests \
      2>&1 | tee ${reports_path}/log_unit_tests_"${type}".log;
  fi
  resUnitTests=${PIPESTATUS[0]};
}

function verifyResources() {
  echo "Verify resources in SD Card";
  adb shell ls -Rla ${mobilert_path}/WavefrontOBJs;
  adb shell ls -Rla ${sdcard_path}/WavefrontOBJs;
  adb shell ls -Rla ${sdcard_path_android_11_emulator}/WavefrontOBJs;
#  adb shell cat ${sdcard_path}/WavefrontOBJs/CornellBox/CornellBox-Water.obj;
#  adb shell cat ${sdcard_path}/WavefrontOBJs/CornellBox/CornellBox-Water.mtl;
#  adb shell cat ${sdcard_path}/WavefrontOBJs/CornellBox/CornellBox-Water.cam;

  echo "Verify memory available on host:";
  if [ -x "$(command -v free)" ]; then
    free -h;
  else
    vm_stat;
  fi

  echo "Verify memory available on Android emulator:";
  adb shell free -h;
  set +e;
  adb shell cat /proc/meminfo;
  echo "Verified memory available on Android emulator.";

  grep -r "hw.ramSize" ~/.android 2> /dev/null;
  set -e;
}

function runInstrumentationTests() {
  echo "Run instrumentation tests";
  set +e;
  jps | grep -i gradle | tr -s ' ' | cut -d ' ' -f 1 | head -1 | xargs kill -SIGKILL;
  set -e;
  ./gradlew --stop;

  echo "Wait for device to be ready to unlock.";
  adb kill-server;
  # Make sure ADB daemon started properly.
  callCommandUntilSuccess adb start-server;
  callCommandUntilSuccess adb wait-for-device;
  # adb shell needs ' instead of ", so 'getprop' works properly.
  callCommandUntilSuccess adb shell 'while [[ $(getprop service.bootanim.exit) -ne 1 ]]; do sleep 1; done;';
  callCommandUntilSuccess adb shell whoami;

  local numberOfFilesOpened;
  numberOfFilesOpened=$(adb shell lsof /dev/goldfish_pipe | wc -l);
  if [ "${numberOfFilesOpened}" -gt "32000" ]; then
    echo "Kill 'graphics.allocator' process since it has a bug where it
      accumulates a memory leak by continuously using more and more
      files of '/dev/goldfish_pipe' and never freeing them.";
    echo "This might make the device restart!";
    set +e;
    adb shell ps | grep -ine "graphics.allocator" | tr -s ' ' | cut -d ' ' -f 2 | xargs adb shell kill;
    set -e;
  fi

  callCommandUntilSuccess ./gradlew --daemon;

  unlockDevice;

  if [ "${run_test}" == "all" ]; then
    echo "Running all tests";
    set +u; # Because 'code_coverage' is only set when debug
    ./gradlew connected"${type}"AndroidTest -DtestType="${type}" \
      -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
      ${code_coverage} --console plain --parallel \
      2>&1 | tee ${reports_path}/log_tests_"${type}".log;
    set -u;
  elif [[ ${run_test} == rep_* ]]; then
    run_test_without_prefix=${run_test#"rep_"};
    echo "Repeatable of test: ${run_test_without_prefix}";
    callCommandUntilError ./gradlew connected"${type}"AndroidTest -DtestType="${type}" \
      -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
      -Pandroid.testInstrumentationRunnerArguments.class="${run_test_without_prefix}" \
      --console plain --parallel \
      2>&1 | tee ${reports_path}/log_tests_"${type}".log;
  else
    echo "Running test: ${run_test}";
    ./gradlew connected"${type}"AndroidTest -DtestType="${type}" \
      -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
      -Pandroid.testInstrumentationRunnerArguments.class="${run_test}" \
      --console plain --parallel \
      2>&1 | tee ${reports_path}/log_tests_"${type}".log;
  fi
  resInstrumentationTests=${PIPESTATUS[0]};
  pid_instrumentation_tests="$!";

  mkdir -p ${reports_path};
  set +e;
  adb logcat -v threadtime -d "*":V > "${reports_path}"/logcat_tests_"${type}".log 2>&1;
  set -e;
  echo "pid of instrumentation tests: '${pid_instrumentation_tests}'";
}
###############################################################################
###############################################################################

runEmulator;
waitForEmulator;
copyResources;
startCopyingLogcatToFile;
verifyResources;
runInstrumentationTests;
runUnitTests;
checkLastModifiedFiles;

###############################################################################
# Exit code
###############################################################################
printCommandExitCode "${resUnitTests}" "Unit tests";
printCommandExitCode "${resInstrumentationTests}" "Instrumentation tests";
###############################################################################
###############################################################################
