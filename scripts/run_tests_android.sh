#!/usr/bin/env sh

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
cd "$(dirname "${0}")/.." || exit;
###############################################################################
###############################################################################


###############################################################################
# Exit immediately if a command exits with a non-zero status.
###############################################################################
set -eu;

# Make the all other processes belong in the process group, so that will be killed at the end.
set +m;
pid="$$";
###############################################################################
###############################################################################


###############################################################################
# Get helper functions.
###############################################################################
# shellcheck disable=SC1091
. scripts/helper_functions.sh;
###############################################################################
###############################################################################


###############################################################################
# Execute Shellcheck on this script.
###############################################################################
if command -v shellcheck > /dev/null; then
  shellcheck "${0}" || exit
fi
###############################################################################
###############################################################################


###############################################################################
# Set default arguments.
###############################################################################
type='release';
run_test='all';
ndk_version='23.2.8568313';
cmake_version='3.22.1';
kill_previous='true';
cpu_architecture='"x86","x86_64"';
parallelizeBuild;

printEnvironment() {
  echo '';
  echo 'Selected arguments:';
  echo "type: ${type}";
  echo "run_test: ${run_test}";
  echo "ndk_version: ${ndk_version}";
  echo "cmake_version: ${cmake_version}";
  echo "kill_previous: ${kill_previous}";
  echo "cpu_architecture: ${cpu_architecture}";
}
###############################################################################
###############################################################################


###############################################################################
# Set paths.
###############################################################################
echo 'Set path to reports';
reports_path='app/build/reports';

echo 'Set path to instrumentation tests resources';
mobilert_path='/data/local/tmp/MobileRT';
sdcard_path='/mnt/sdcard/MobileRT';
###############################################################################
###############################################################################


###############################################################################
# Parse arguments.
###############################################################################
parseArgumentsToTestAndroid "$@";
printEnvironment;
typeWithCapitalLetter=$(capitalizeFirstletter "${type}");
###############################################################################
###############################################################################


###############################################################################
# Helper functions.
###############################################################################
gather_logs_func() {
  adb_devices_running=$(adb devices | tail -n +2);
  if [ "${adb_devices_running}" != '' ]; then
    callCommandUntilSuccess adb shell 'ps > /dev/null;';
    adb logcat -v threadtime -d "*":V \
      > "${reports_path}"/logcat_"${type}".log 2>&1;
    echo "Copied logcat to logcat_${type}.log";
  else
    echo 'Logs not gathered because Android emulator is down.';
  fi

  set +e;
  pid_app=$(grep -E -i "proc.puscas:*" "${reports_path}"/logcat_"${type}".log |
    grep -i "pid=" | cut -d "=" -f 2 | cut -d "u" -f 1 | tr -d ' ' | tail -1);
  grep -e "${pid_app}" -e "I DEBUG" "${reports_path}"/logcat_"${type}".log \
    > "${reports_path}"/logcat_app_"${type}".log;
  echo "Filtered logcat of the app '${pid_app}' to logcat_app_${type}.log";
  set -e;

  printf '\e]8;;file://'"%s"'/'"%s"'/tests/test'"%s"'UnitTest/index.html\aClick here to check the Unit tests report.\e]8;;\a\n' "${PWD}" "${reports_path}" "${typeWithCapitalLetter}";
  printf '\e]8;;file://'"%s"'/'"%s"'/androidTests/connected/index.html\aClick here to check the Android tests report.\e]8;;\a\n' "${PWD}" "${reports_path}";
  printf '\e]8;;file://'"%s"'/'"%s"'/jacoco/jacocoTestReport/html/index.html\aClick here to check the Code coverage report.\e]8;;\a\n' "${PWD}" "${reports_path}";
  printf '\e]8;;file://'"%s"'/'"%s"'/logcat_app_'"%s"'.log\aClick here to check the app log.\e]8;;\a  ' "${PWD}" "${reports_path}" "${type}";
  printf '\e]8;;file://'"%s"'/'"%s"'/logcat_'"%s"'.log\aClick here to check the whole logcat.\e]8;;\a\n' "${PWD}" "${reports_path}" "${type}";
}

clear_func() {
  set +u; # Variable might not have been set if canceled too soon.
  echo "Killing pid of logcat: '${pid_logcat}'";
  kill -TERM "${pid_logcat}" 2> /dev/null || true;
  set -u;

  pid_app=$(adb shell ps | grep -i puscas.mobilertapp | tr -s ' ' | cut -d ' ' -f 2);
  echo "Killing pid of MobileRT: '${pid_app}'";
  set +e;
  adb shell 'kill -TERM '"${pid_app}"'; echo ::$?::';
  set -e;

  # Kill all processes in the whole process group, thus killing also descendants.
  trap - EXIT HUP INT QUIT ILL TRAP ABRT TERM; # Disable traps first, to avoid infinite loop.
  echo "Killing all processes from the same group process id: '${pid}'";
  kill -TERM -"${pid}" || true;
}

catch_signal() {
  echo '';
  echo 'Caught signal';

  gather_logs_func;
  clear_func;

  echo '';
}
###############################################################################
###############################################################################


###############################################################################
# Run Android tests in emulator.
###############################################################################

unlockDevice() {
  callCommandUntilSuccess sh gradlew --daemon \
    --no-rebuild \
    -DabiFilters="[${cpu_architecture}]" \
    -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" --info --warning-mode fail --stacktrace;

  echo 'Set adb as root, to be able to change files permissions';
  callCommandUntilSuccess adb root;

  set +e;
  # shellcheck disable=SC2009
  GRADLE_DAEMON_PROCESSES=$(ps aux | grep -i "grep -i GradleDaemon" | grep -v "grep" | tr -s ' ' | cut -d ' ' -f 2);
  echo "Detected Gradle Daemon process(es): '${GRADLE_DAEMON_PROCESSES}'";
  set +u;
  if [ -z "${CI}" ]; then
    echo "Killing previous Gradle Daemon process, just in case it was stuck: '${GRADLE_DAEMON_PROCESSES}'";
    for GRADLE_DAEMON_PROCESS in ${GRADLE_DAEMON_PROCESSES}; do
      echo "Killing: '${GRADLE_DAEMON_PROCESS}'";
      kill -KILL "${GRADLE_DAEMON_PROCESS}";
    done;
  fi
  set -u;
  set -e;
  _waitForEmulatorToBoot;

  echo 'Unlock device';
  callAdbShellCommandUntilSuccess adb shell 'input keyevent 82; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'input tap 800 400; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'input tap 1000 500; echo ::$?::';

  callCommandUntilSuccess adb get-state;
  callCommandUntilSuccess adb devices -l;
  callCommandUntilSuccess adb version;

  androidApi=$(adb shell getprop ro.build.version.sdk | tr -d '[:space:]');
  echo "androidApi: '${androidApi}'";
}

runEmulator() {
  set +u;
  pid="$$";
  set -u;

  script_name=$(basename "${0}");
  echo "pid: ${pid}";
  echo "script name: ${script_name}";

  if [ "${type}" = 'debug' ]; then
    gradle_command='createDebugCoverageReport jacocoTestReport'
  else
    gradle_command='connectedAndroidTest';
  fi

  if [ "${kill_previous}" = true ]; then
    echo 'Killing previous process';
    set +e;
    # shellcheck disable=SC2009
    ps aux |
      grep -v "grep" |
      grep -v "${pid}" |
      grep -i "${script_name}" |
      tr -s ' ' |
      cut -d ' ' -f 2 |
      xargs kill;
    set -e;
  fi

  if command -v emulator > /dev/null; then
    avd_emulators=$(emulator -list-avds);
    echo "Emulators available: '${avd_emulators}'";
    avd_emulator=$(echo "${avd_emulators}" | head -1);
    echo "Start '${avd_emulator}'";
  else
    echo 'Command emulator is NOT installed.';
  fi
}

waitForEmulator() {
  echo 'Wait for device to be available.';

  callCommandUntilSuccess adb kill-server;
  #_restartAdbProcesses;
  callCommandUntilSuccess adb start-server;
  set +e;
  adb_devices_running=$(adb devices | tail -n +2);
  retry=0;
  # Truncate nohup.out log file.
  : > nohup.out;
  set +u; # 'avd_emulator' might not have been set
  while [ "${avd_emulator}" != '' ] && [ "${adb_devices_running}" = '' ] && [ ${retry} -lt 3 ]; do
    retry=$((retry + 1));
    echo 'Booting a new Android emulator.';
    # Possible CPU accelerators locally (Intel CPU + Linux OS based) [qemu-system-i386 -accel ?]:
    # kvm, tcg
    # Possible CPU accelerators:
    # hvf is the MacOS Hypervisor.framework accelerator;
    # hax is the cross-platform Intel HAXM accelerator;
    # whp is the Windows Hypervisor Platform accelerator;
    # xen is a type-1 hypervisor, providing services that allow multiple computer operating systems to execute on the same computer hardware concurrently;
    # kvm is the Linux Kernel-based Virtual Machine accelerator;
    # tcg is a JIT compiler that dynamically translates target instruction set architecture (ISA) to host ISA;
    # Possible machines locally:
    # q35
    # pc
    # setsid -> Run the Android emulator in a new session.
    # nohup -> Disconnect the process from the terminal, redirects its output to nohup.out and shields it from SIGHUP.
    # Both `setsid` and `nohup` are used to make sure the Android emulator continues to work after this script is completed.
    # Note that the 'memory', 'cache-size' and 'partition-size' might make Android emulator to boot slower.
    # Using 'cache-size' and 'partition-size' below 256 and above 1024 seems to be slower.
    # Also, using 8GB+ as memory seems to allow for Android emulator to boot faster.
    setsid nohup cpulimit --cpu 8 --limit 800 -- \
      emulator -avd "${avd_emulator}" -cores 8 -memory 4096 -cache-size 512 -partition-size 800 \
      -ranchu -fixed-scale -skip-adb-auth -gpu swiftshader_indirect -no-audio \
      -no-snapshot -no-snapstorage -no-snapshot-update-time -no-snapshot-save -no-snapshot-load \
      -no-boot-anim -camera-back none -camera-front none -netfast -wipe-data -no-sim \
      -no-passive-gps -read-only -no-direct-adb -no-location-ui -no-hidpi-scaling \
      -no-mouse-reposition -no-nested-warnings -verbose \
      -qemu -m size=4096M,slots=1,maxmem=8192M -machine type=pc,accel=kvm -accel kvm,thread=multi:tcg,thread=multi -smp cpus=8,maxcpus=8,cores=8,threads=1,sockets=1
    sleep 20;
    adb_devices_running=$(callCommandUntilSuccess adb devices | tail -n +2);
  done
  set -eu;
  echo "Devices running: '${adb_devices_running}'";

  if (grep -iq "Process .* dead!" nohup.out); then
    echo "Android emulator didn't boot properly, please check the 'nohup.out' log file for more context.";
    exit 1;
  fi

  echo 'Finding at least 1 Android device on.';
  _waitForEmulatorToBoot;

  echo 'Prepare traps';
  trap 'catch_signal ${?}' EXIT HUP INT QUIT ILL TRAP ABRT TERM;

  unlockDevice;

  adb_devices_running=$(callCommandUntilSuccess adb devices | grep -v 'List of devices attached' || true);
  echo "Devices running after triggering boot: '${adb_devices_running}'";
  if [ -z "${adb_devices_running}" ]; then
    # Abort if emulator didn't start.
    echo "Android emulator didn't start ... will exit.";
    exit 1;
  fi
  unlockDevice;
}

copyResources() {
  mkdir -p ${reports_path};

  unlockDevice;
  sdcard_path_android="$(adb shell ls -d '/storage/*' | grep -v 'emulated' | grep -v 'self' | tail -1)";
  # Delete all special character that might be invisible!
  sdcard_path_android="$(echo "${sdcard_path_android}" | tr -d '[:space:]')";
  if echo "${sdcard_path_android}" | grep -q "Nosuchfileordirectory"; then
    # If there is no SD card volume mounted on /storage/ path, then use the legacy path.
    sdcard_path_android=${sdcard_path};
  else
    sdcard_path_android="${sdcard_path_android}/MobileRT";
  fi
  echo "sdcard_path_android: '${sdcard_path_android}'";

  echo 'Prepare copy unit tests';
  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '${mobilert_path}'; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '${sdcard_path}'; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '${sdcard_path_android}'; echo ::$?::';

  callAdbShellCommandUntilSuccess adb shell 'rm -r '${mobilert_path}'; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'rm -r '${sdcard_path}'; echo ::$?::';
  if [ "${androidApi}" -gt 29 ]; then
    callAdbShellCommandUntilSuccess adb shell 'rm -r '${sdcard_path_android}'; echo ::$?::';
  fi

  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '${mobilert_path}'/WavefrontOBJs/CornellBox; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '${sdcard_path}'/WavefrontOBJs/teapot; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '${sdcard_path_android}'/WavefrontOBJs/teapot; echo ::$?::';

  echo 'Copy tests resources';
  callCommandUntilSuccess adb push -p app/src/androidTest/resources/teapot ${sdcard_path}/WavefrontOBJs;
  callCommandUntilSuccess adb push -p app/src/androidTest/resources/CornellBox ${mobilert_path}/WavefrontOBJs;
  set +e;
  # Push to SD Card in `/storage/` if possible (necessary for Android 5+).
  adb push -p app/src/androidTest/resources/teapot ${sdcard_path_android}/WavefrontOBJs;
  set -e;

  echo 'Copy File Manager';
  callCommandUntilSuccess adb push -p app/src/androidTest/resources/APKs ${mobilert_path};

  echo 'Change resources permissions';
  callAdbShellCommandUntilSuccess adb shell 'chmod -R 777 '${mobilert_path}'; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'chmod -R 777 '${sdcard_path}'; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'chmod -R 777 '${sdcard_path_android}'; echo ::$?::';

  echo 'Install File Manager';
  if [ "${androidApi}" -gt 31 ]; then
    echo "Not installing any file manager APK because the available ones are not compatible with Android API: ${androidApi}";
  elif [ "${androidApi}" -gt 30 ]; then
    callAdbShellCommandUntilSuccess adb shell 'pm install -t -r '${mobilert_path}'/APKs/asus-file-manager-2-8-0-85-230220.apk echo ::$?::';
  elif [ "${androidApi}" -gt 29 ]; then
    callAdbShellCommandUntilSuccess adb shell 'pm install -t -r '${mobilert_path}'/APKs/com.asus.filemanager_2.7.0.28_220608-1520700140_minAPI30_apkmirror.com.apk; echo ::$?::';
  elif [ "${androidApi}" -gt 16 ]; then
    callAdbShellCommandUntilSuccess adb shell 'pm install -t -r '${mobilert_path}'/APKs/com.asus.filemanager.apk; echo ::$?::';
  elif [ "${androidApi}" -lt 16 ]; then
    # This file manager is compatible with Android 4.0.3 (API 15) which the Asus one is not.
    callAdbShellCommandUntilSuccess adb shell 'pm install -t -r '${mobilert_path}'/APKs/com.estrongs.android.pop_4.2.1.8-10057_minAPI14.apk; echo ::$?::';
  fi
}

startCopyingLogcatToFile() {
  unlockDevice;

  # echo 'Disable animations';
  # puscas.mobilertapp not found
  # adb shell pm grant puscas.mobilertapp android.permission.SET_ANIMATION_SCALE;

  # /system/bin/sh: settings: not found
  # adb shell settings put global window_animation_scale 0.0;
  # adb shell settings put global transition_animation_scale 0.0;
  # adb shell settings put global animator_duration_scale 0.0;

  echo 'Activate JNI extended checking mode';
  callAdbShellCommandUntilSuccess adb shell 'setprop dalvik.vm.checkjni true; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'setprop debug.checkjni 1; echo ::$?::';

  echo 'Clear logcat';
  # -b all -> Unable to open log device '/dev/log/all': No such file or directory
  # -b crash -> Unable to open log device '/dev/log/crash': No such file or directory
  callAdbShellCommandUntilSuccess adb shell 'logcat -b main -b system -b radio -b events -c; echo ::$?::';

  echo 'Copy realtime logcat to file';
  adb logcat -v threadtime "*":V &
  pid_logcat="$!";
  echo "pid of logcat: '${pid_logcat}'";
}

runUnitTests() {
  echo 'Copy unit tests to Android emulator.';
  ls app/.cxx;
  if [ "${type}" = 'release' ]; then
    typeWithDebInfo='RelWithDebInfo';
  else
    typeWithDebInfo="${typeWithCapitalLetter}";
  fi
  dirUnitTests="app/.cxx/${typeWithDebInfo}";
  echo 'Checking generated id.';
  # Note: flag `-t` of `ls` is to sort by date (newest first).
  # shellcheck disable=SC2012
  generatedId=$(ls -t "${dirUnitTests}" | head -1);
  dirUnitTests="${dirUnitTests}/${generatedId}/x86";
  find . -iname "*unittests*" -exec readlink -f {} \;
  echo 'Checking generated unit tests binaries.';
  files=$(ls "${dirUnitTests}");
  echo "Copy unit tests bin: ${files}/bin";
  echo "Copy unit tests libs: ${files}/lib";

  unlockDevice;

  callCommandUntilSuccess adb push -p "${dirUnitTests}"/bin/* ${mobilert_path}/;
  callCommandUntilSuccess adb push -p "${dirUnitTests}"/lib/* ${mobilert_path}/;

  echo 'Run unit tests';
  if [ "${type}" = 'debug' ]; then
    # Ignore unit tests that should crash the system because of a failing assert.
    adb shell "LD_LIBRARY_PATH=${mobilert_path} ${mobilert_path}/UnitTests --gtest_filter=-*.TestInvalid*; echo "'$?'" > ${mobilert_path}/unit_tests_result.log";
  else
    adb shell "LD_LIBRARY_PATH=${mobilert_path} ${mobilert_path}/UnitTests; echo "'$?'" > ${mobilert_path}/unit_tests_result.log";
  fi
  adb pull "${mobilert_path}"/unit_tests_result.log .;
  resUnitTests=$(cat "unit_tests_result.log");
}

verifyResources() {
  echo 'Verify resources in SD Card';
  callCommandUntilSuccess adb shell 'ls -laR '${mobilert_path}/WavefrontOBJs;
  callCommandUntilSuccess adb shell 'ls -laR '${sdcard_path}/WavefrontOBJs;
  callCommandUntilSuccess adb shell 'ls -laR '${sdcard_path_android}/WavefrontOBJs;
#  adb shell cat ${sdcard_path}/WavefrontOBJs/CornellBox/CornellBox-Water.obj;
#  adb shell cat ${sdcard_path}/WavefrontOBJs/CornellBox/CornellBox-Water.mtl;
#  adb shell cat ${sdcard_path}/WavefrontOBJs/CornellBox/CornellBox-Water.cam;

  echo 'Verify memory available on host:';
  if command -v free > /dev/null; then
    free -h;
  else
    vm_stat;
  fi

  echo 'Verify memory available on Android emulator:';
  set +e;
  callAdbShellCommandUntilSuccess adb shell 'cat /proc/meminfo; echo ::$?::';
  set -e;
  echo 'Verified memory available on Android emulator.';

  grep -r "hw.ramSize" ~/.android 2> /dev/null || true;
}

runInstrumentationTests() {
  echo 'Run instrumentation tests';
  set +eu;
  if [ -z "${CI}" ]; then
    GRADLE_PROCESSES="$(jps | grep -i "gradle" | tr -s ' ' | cut -d ' ' -f 1)";
    for GRADLE_PROCESS in ${GRADLE_PROCESSES}; do
      kill -KILL "${GRADLE_PROCESS}";
    done;
    sh gradlew --stop \
      --no-rebuild \
      -DabiFilters="[${cpu_architecture}]" \
      -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" --info --warning-mode fail --stacktrace;

    numberOfFilesOpened=$(adb shell lsof /dev/goldfish_pipe | wc -l);
    if [ "${numberOfFilesOpened}" -gt '32000' ]; then
      echo "Kill 'graphics.allocator' process since it has a bug where it
        accumulates a memory leak by continuously using more and more
        files of '/dev/goldfish_pipe' and never freeing them.";
      echo 'This might make the device restart!';
      set +e;
      adb shell ps | grep -ine "graphics.allocator" | tr -s ' ' | cut -d ' ' -f 2 | xargs adb shell kill;
      set -e;
    fi
  fi
  set -eu;

  echo 'Searching for APK to install in Android emulator.';
  find . -iname "*.apk" | grep -i "output";
  apkPath=$(find . -iname "*.apk" | grep -i "output" | grep -i "test" | grep -i "${type}");
  echo "Will install APK: ${apkPath}";
  callCommandUntilSuccess adb push -p "${apkPath}" "${mobilert_path}";
  callCommandUntilSuccess adb shell 'ls -la '${mobilert_path};
  callAdbShellCommandUntilSuccess adb shell 'pm install -t -r '${mobilert_path}'/app-'${type}'-androidTest.apk; echo ::$?::';
  echo 'List of instrumented APKs:';
  adb shell 'pm list instrumentation;';
  unlockDevice;

  if [ "${run_test}" = 'all' ]; then
    echo 'Running all tests';
    mkdir -p app/build/reports/jacoco/jacocoTestReport/;
    sh gradlew ${gradle_command} -DtestType="${type}" \
      -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
      -Pandroid.testInstrumentationRunnerArguments.package='puscas' \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  elif echo "${run_test}" | grep -q "rep_"; then
    run_test_without_prefix=${run_test#"rep_"};
    echo "Repeatable of test: ${run_test_without_prefix}";
    callCommandUntilError sh gradlew connectedAndroidTest -DtestType="${type}" \
      -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
      -Pandroid.testInstrumentationRunnerArguments.class="${run_test_without_prefix}" \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  else
    echo "Running test: ${run_test}";
    sh gradlew connectedAndroidTest -DtestType="${type}" \
      -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}" \
      -Pandroid.testInstrumentationRunnerArguments.class="${run_test}" \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  fi
  resInstrumentationTests=${?};
  pid_instrumentation_tests="$!";
  echo 'Android test(s) executed!';

  mkdir -p ${reports_path};
  set +e;
  adb logcat -v threadtime -d "*":V > "${reports_path}"/logcat_tests_"${type}".log 2>&1;
  set -e;
  echo "pid of instrumentation tests: '${pid_instrumentation_tests}'";
}

_restartAdbProcesses() {
  set +eu;
  # shellcheck disable=SC2009
  ADB_PROCESSES=$(ps aux | grep -i "adb" | grep -v "grep" | tr -s ' ' | cut -d ' ' -f 2);
  echo "Detected ADB process(es): '${ADB_PROCESSES}'";
  if [ -z "${CI}" ]; then
    echo "Killing previous ADB process(es), just in case it was stuck: '${ADB_PROCESSES}'";
    for ADB_PROCESS in ${ADB_PROCESSES}; do
      echo "Killing: '${ADB_PROCESS}'";
      kill -KILL "${ADB_PROCESS}";
    done;
    sleep 3;
    # Kill process(es) using same port as ADB
    killProcessesUsingPort 5037
  fi
  set -eu;
}

# Waits for the Android Emulator to boot.
# By using cpulimit to 1, it can take around 3 minutes to boot.
_waitForEmulatorToBoot() {
  # Make sure ADB daemon started properly.
  callCommandUntilSuccess adb shell 'ps > /dev/null;';
  # adb shell needs ' instead of ", so 'getprop' works properly.
  # shellcheck disable=SC2016
  callAdbShellCommandUntilSuccess adb shell 'echo -n ::$(($(getprop sys.boot_completed)-1))::';
  # shellcheck disable=SC2016
  callAdbShellCommandUntilSuccess adb shell 'echo -n ::$(($(getprop dev.bootcomplete)-1))::';
  # Property 'service.bootanim.exit' is not available in Android with API < 16.
  # shellcheck disable=SC2016
  adb shell 'getprop service.bootanim.exit';
}
###############################################################################
###############################################################################

# Increase memory for heap.
export GRADLE_OPTS="-Xmx4G -Xms4G -XX:ActiveProcessorCount=3";
clearOldBuildFiles;
createReportsFolders;
runEmulator;
waitForEmulator;
copyResources;
startCopyingLogcatToFile;
verifyResources;
runUnitTests;
runInstrumentationTests;
# checkLastModifiedFiles;

###############################################################################
# Exit code
###############################################################################
if [ "${androidApi}" = '15' ]; then
  # TODO: Fix the native unit tests in Android API 15. Ignore the result for now.
  printCommandExitCode '0' "Unit tests (result: ${resUnitTests})";
else
  printCommandExitCode "${resUnitTests}" 'Unit tests';
fi
printCommandExitCode "${resInstrumentationTests}" 'Instrumentation tests';
###############################################################################
###############################################################################
