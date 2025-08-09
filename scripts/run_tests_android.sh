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
if [ $# -ge 1 ]; then
  cd "$(dirname "${0}")/.." || return 1;
fi
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
if [ $# -ge 1 ] && command -v shellcheck > /dev/null; then
  shellcheck "${0}" || return 1;
fi
###############################################################################
###############################################################################


###############################################################################
# Set default arguments.
###############################################################################
type='release';
run_test='all';
android_api_version='14';
kill_previous='true';
cpu_architecture='"x86","x86_64"';
parallelizeBuild;

printEnvironment() {
  echo '';
  echo 'Selected arguments:';
  echo "type: ${type}";
  echo "run_test: ${run_test}";
  echo "android_api_version: ${android_api_version}";
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
  set +e;
  pid_app=$(grep -E -i "proc.puscas:*" "${reports_path}"/logcat_"${type}".log |
    grep -i "pid=" | cut -d "=" -f 2 | cut -d "u" -f 1 | tr -d ' ' | tail -1);
  grep -e "${pid_app}" -e "I DEBUG" "${reports_path}"/logcat_"${type}".log \
    > "${reports_path}"/logcat_app_"${type}".log;
  echo "Filtered logcat of the app '${pid_app}' to logcat_app_${type}.log";
  set -e;

  appLogPath="${PWD}/${reports_path}";
  appLog="logcat_app_${type}.log";
  androidLogcat="logcat_${type}.log";
  androidTestsReportPath="${PWD}/${reports_path}/androidTests/connected/${type}";
  androidTestsReport='index.html';

  checkPathExists "${appLogPath}" "${androidLogcat}";
  printf '\e]8;;file://'"%s"'\aClick here to check the whole logcat.\e]8;;\a  ' "${appLogPath}/${androidLogcat}";

  checkPathExists "${appLogPath}" "${appLog}";
  printf '\e]8;;file://'"%s"'\aClick here to check the app log.\e]8;;\a\n' "${appLogPath}/${appLog}";

  checkPathExists "${androidTestsReportPath}" "${androidTestsReport}";
  printf '\e]8;;file://'"%s"'\aClick here to check the Android tests report.\e]8;;\a\n' "${androidTestsReportPath}/${androidTestsReport}";

  if [ "${type}" != 'release' ] && ! echo "${run_test}" | grep -q "rep_"; then
    jacocoTestReportPath="${PWD}/${reports_path}/jacoco/jacocoTestReport/html";
    jacocoTestReport='index.html';
    checkPathExists "${jacocoTestReportPath}" "${jacocoTestReport}";
    printf '\e]8;;file://'"%s"'\aClick here to check the Code coverage report.\e]8;;\a\n' "${jacocoTestReportPath}/${jacocoTestReport}";
  fi
}

clear_func() {
  set +u; # Variable might not have been set if canceled too soon.
  echo "Killing pid of logcat: '${pid_logcat}'";
  kill -TERM "${pid_logcat}" 2> /dev/null || true;
  # shellcheck disable=SC2009
  pid_tee=$(ps aux | grep -i "tee" | grep -v "grep" | tr -s ' ' | cut -d ' ' -f 2);
  echo "Killing pid of tee used by logcat command: '${pid_tee}'";
  kill -TERM "${pid_tee}" 2> /dev/null || true;
  set -u;

  kill_mobilert_processes;
  kill_gradle_processes;
  kill_adb_processes;
}

catch_signal() {
  echo '';
  echo 'Caught signal';
  trap - EXIT HUP INT QUIT ILL TRAP ABRT TERM; # Disable traps first, to avoid infinite loop.

  clear_func;
  gather_logs_func;

  echo "Killing all processes from the same group process id (thus killing also descendants): '${pid}'";
  kill -TERM -"${pid}" || true;
  echo 'Caught signal finished';
}

kill_mobilert_processes() {
  pid_apps=$(adb shell ps | grep -i "puscas.mobilertapp" | tr -s ' ' | cut -d ' ' -f 2);
  for pid_app in ${pid_apps}; do
    echo "Killing pid of MobileRT: '${pid_app}'";
    set +e;
    adb shell 'kill -TERM '"${pid_app}";
    set -e;
  done
}

kill_gradle_processes() {
  set +e;
  scriptName=$(basename "${0}");
  # shellcheck disable=SC2009
  GRADLE_PROCESSES=$(ps aux | grep -i "mobilert" | grep -v "grep" | grep -v "${scriptName}");
  set -e;
  set +u; # 'GRADLE_PROCESSES' might not be set if didn't find any process(es).
  GRADLE_PROCESSES_STR="$(echo "${GRADLE_PROCESSES}" | tr '\n' ',' | sed 's/,$//')";
  echo "Killing any Gradle process, because it should be already killed: '${GRADLE_PROCESSES_STR}'";
  GRADLE_PROCESSES=$(echo "${GRADLE_PROCESSES}" | tr -s ' ' | cut -d ' ' -f 2);
  for GRADLE_PROCESS in ${GRADLE_PROCESSES}; do
    echo "Killing: '${GRADLE_PROCESS}'";
    kill -TERM "${GRADLE_PROCESS}";
  done;
  set -u;
}

kill_adb_processes() {
  # shellcheck disable=SC2009
  ADB_PROCESSES=$(ps aux | grep -i " adb " | grep -v "grep" | tr -s ' ' | cut -d ' ' -f 2);
  ADB_PROCESSES_STR="$(echo "${ADB_PROCESSES}" | tr '\n' ',' | sed 's/,$//')";
  echo "Killing the detected ADB process(es): '${ADB_PROCESSES_STR}'";
  set +eu;
  if [ -z "${CI}" ]; then
    for ADB_PROCESS in ${ADB_PROCESSES}; do
      kill -TERM "${ADB_PROCESS}";
    done;
    sleep 3;
  fi
  set -eu;
}
###############################################################################
###############################################################################


###############################################################################
# Run Android tests in emulator.
###############################################################################

unlockDevice() {
  echo 'unlockDevice called';
  callCommandUntilSuccess 5 sh gradlew --offline --parallel --daemon \
    --no-rebuild \
    -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]" \
    --info --warning-mode fail --stacktrace;

  echo 'Set adb as root, to be able to change files permissions';
  callCommandUntilSuccess 5 adb root;

  set +e;
  # shellcheck disable=SC2009
  GRADLE_DAEMON_PROCESSES=$(ps aux | grep -i "grep -i GradleDaemon" | grep -v "grep" | tr -s ' ' | cut -d ' ' -f 2);
  echo "Detected Gradle Daemon process(es): '${GRADLE_DAEMON_PROCESSES}'";
  set +u;
  if [ -z "${CI}" ]; then
    echo "Killing previous Gradle Daemon process, just in case it was stuck: '${GRADLE_DAEMON_PROCESSES}'";
    for GRADLE_DAEMON_PROCESS in ${GRADLE_DAEMON_PROCESSES}; do
      echo "Killing: '${GRADLE_DAEMON_PROCESS}'";
      kill -TERM "${GRADLE_DAEMON_PROCESS}";
    done;
  fi
  set -u;
  set -e;
  _waitForEmulatorToBoot;

  echo 'Unlock device';
  callAdbShellCommandUntilSuccess adb shell 'am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'input keyevent 82; echo ::$?::';

  androidApi=$(adb shell getprop ro.build.version.sdk | tr -d '[:space:]');
  echo "androidApi: '${androidApi}'";

  if [ "${androidApi}" -gt 15 ]; then
    callAdbShellCommandUntilSuccess adb shell 'input tap 800 400; echo ::$?::';
    callAdbShellCommandUntilSuccess adb shell 'input tap 1000 500; echo ::$?::';
  fi

  callCommandUntilSuccess 5 adb get-state;
  callCommandUntilSuccess 5 adb devices -l;
  callCommandUntilSuccess 5 adb version;

  callAdbShellCommandUntilSuccess adb shell 'input keyevent 82; echo ::$?::';
  if [ "${androidApi}" -gt 15 ]; then
    callAdbShellCommandUntilSuccess adb shell 'input tap 800 400; echo ::$?::';
  fi
}

runEmulator() {
  set +u;
  pid="$$";
  set -u;

  script_name=$(basename "${0}");
  echo "pid: ${pid}";
  echo "script name: ${script_name}";

  if [ "${type}" = 'debug' ]; then
    gradle_command='jacocoTestReport';
  else
    gradle_command='connectedAndroidTest';
  fi

  if [ "${kill_previous}" = true ]; then
    echo 'Killing previous process';
    set +e;
    # shellcheck disable=SC2009
    pidsToKill=$(ps aux | grep -v "grep" | grep -v "${pid}" | grep -i "${script_name}" | tr -s ' ' | cut -d ' ' -f 2);
    echo "Killing processes: '${pidsToKill}'";
    for pidToKill in ${pidsToKill}; do
      echo "Killing process: '${pidToKill}'";
      kill "${pidToKill}";
    done;
    set -e;
  fi

  if command -v emulator > /dev/null; then
    avd_emulators=$(emulator -list-avds);
    echo "Emulators available: '${avd_emulators}'";
    avd_emulator=$(echo "${avd_emulators}" | tail -1);
    echo "Start '${avd_emulator}'";
  else
    echo 'Command emulator is NOT installed.';
  fi
}

waitForEmulator() {
  echo 'Wait for device to be available.';

  callCommandUntilSuccess 5 adb kill-server;
  #_restartAdbProcesses;
  callCommandUntilSuccess 5 adb start-server;
  set +e;
  adb_devices_running=$(adb devices | tail -n +2);
  retry=0;
  # Truncate nohup.out log file.
  : > nohup.out;
  set +u; # 'avd_emulator' might not have been set
  while [ "${avd_emulator}" != '' ] && [ "${adb_devices_running}" = '' ] && [ ${retry} -lt 3 ]; do
    retry=$((retry + 1));
    echo "Booting a new Android emulator: '${avd_emulator}'";
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
      -no-passive-gps -no-direct-adb -no-location-ui -no-hidpi-scaling \
      -no-mouse-reposition -no-nested-warnings -verbose \
      -qemu -m size=4096M,slots=1,maxmem=8192M -machine type=pc,accel=kvm -accel kvm,thread=multi:tcg,thread=multi -smp cpus=8,maxcpus=8,cores=8,threads=1,sockets=1
    sleep 20;
    adb_devices_running=$(callCommandUntilSuccess 5 adb devices | tail -n +2);
  done
  set -eu;
  echo "Devices running: '${adb_devices_running}'";

  if (grep -iq "Process .* dead!" nohup.out); then
    echo "Android emulator didn't boot properly, please check the 'nohup.out' log file for more context.";
    exit 1;
  fi

  echo 'Finding at least 1 Android device on.';
  _waitForEmulatorToBoot;

  unlockDevice;

  adb_devices_running=$(callCommandUntilSuccess 5 adb devices | grep -v 'List of devices attached' || true);
  echo "Devices running after triggering boot: '${adb_devices_running}'";
  if [ -z "${adb_devices_running}" ]; then
    # Abort if emulator didn't start.
    echo "Android emulator didn't start ... will exit.";
    exit 1;
  fi

  echo 'Disable animations';
  # Error in API 15 & 16: /system/bin/sh: settings: not found
  if [ "${androidApi}" -gt 16 ]; then
    callAdbShellCommandUntilSuccess adb shell 'settings put global window_animation_scale 0; echo ::$?::';
    callAdbShellCommandUntilSuccess adb shell 'settings put global transition_animation_scale 0; echo ::$?::';
    callAdbShellCommandUntilSuccess adb shell 'settings put global animator_duration_scale 0; echo ::$?::';
  fi

  echo 'Activate JNI extended checking mode';
  # Command fails on Android 34.
  # callAdbShellCommandUntilSuccess adb shell 'setprop dalvik.vm.checkjni true; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'setprop debug.checkjni 1; echo ::$?::';

  unlockDevice;
}

copyResources() {
  mkdir -p ${reports_path};

  unlockDevice;
  echo 'Possible SD Card paths:';
  adb shell ls -d '/storage/*' | grep -v 'self';
  adb shell df;
  set +e;
  adb shell env | grep -i "storage";
  set -e;
  sdcard_path_android="$(adb shell ls -d '/storage/*' | grep -v '/storage/emulated' | grep -v 'self' | tail -1)";
  # Delete all special character that might be invisible!
  sdcard_path_android="$(echo "${sdcard_path_android}" | tr -d '[:space:]')";
  if [ "${sdcard_path_android}" = '' ] || [ "${sdcard_path_android}" = '/storage/emulated' ]; then
    # If there is no SD card volume mounted on /storage/ path, then use the legacy path.
    sdcard_path_android='/mnt/sdcard';
  fi
  echo "sdcard_path_android: '${sdcard_path_android}'";
  if echo "${sdcard_path_android}" | grep -q "Nosuchfileordirectory"; then
    # If there is no SD card volume mounted on /storage/ path, then use the legacy path.
    sdcard_path_android='/mnt/sdcard/MobileRT';
  else
    sdcard_path_android="${sdcard_path_android}/Android/data/puscas.mobilertapp/files/MobileRT";
  fi
  echo "sdcard_path_android: '${sdcard_path_android}'";

  echo 'Prepare copy unit tests';
  set +e;
  adb shell rm -r ${mobilert_path};
  if [ "${androidApi}" -gt 29 ]; then
    adb shell 'rm -r '"${sdcard_path_android}";
  fi
  set -e;

  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '"${mobilert_path}"'; echo ::$?::';
  callCommandUntilSuccess 5 adb shell 'ls -laR '"${mobilert_path}";
  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '"${sdcard_path_android}"'; echo ::$?::';
  callCommandUntilSuccess 5 adb shell 'ls -laR '"${sdcard_path_android}";

  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '"${mobilert_path}"'/WavefrontOBJs/CornellBox; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'mkdir -p '"${sdcard_path_android}"'/WavefrontOBJs/teapot; echo ::$?::';

  echo 'Copy tests resources';
  callCommandUntilSuccess 5 adb push -p app/src/androidTest/resources/CornellBox ${mobilert_path}/WavefrontOBJs;
  set +e;
  adb push -p app/src/androidTest/resources/teapot "${sdcard_path_android}/WavefrontOBJs";
  set -e;

  echo 'Copy File Manager';
  callCommandUntilSuccess 5 adb push -p app/src/androidTest/resources/APKs ${mobilert_path};

  echo 'Change resources permissions';
  callAdbShellCommandUntilSuccess adb shell 'chmod -R 777 '"${mobilert_path}"'; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'chmod -R 777 '"${sdcard_path_android}"'; echo ::$?::';

  echo 'Install File Manager';
  set +e;
  adb shell pm;
  set -e;
  unlockDevice;
  if [ "${androidApi}" -gt 31 ]; then
    echo "Not installing any file manager APK because the available ones are not compatible with Android API: ${androidApi}";
  elif [ "${androidApi}" -gt 30 ]; then
    set +e;
    adb shell "pm uninstall ${mobilert_path}/APKs/asus-file-manager-2-8-0-85-230220.apk;";
    set -e;
    callAdbShellCommandUntilSuccess adb shell 'pm install -r '"${mobilert_path}"'/APKs/asus-file-manager-2-8-0-85-230220.apk; echo ::$?::';
  elif [ "${androidApi}" -gt 29 ]; then
    set +e;
    adb shell "pm uninstall ${mobilert_path}/APKs/com.asus.filemanager_2.7.0.28_220608-1520700140_minAPI30_apkmirror.com.apk;";
    set -e;
    callAdbShellCommandUntilSuccess adb shell 'pm install -r '"${mobilert_path}"'/APKs/com.asus.filemanager_2.7.0.28_220608-1520700140_minAPI30_apkmirror.com.apk; echo ::$?::';
  elif [ "${androidApi}" -gt 16 ]; then
    set +e;
    adb shell "pm uninstall ${mobilert_path}/APKs/com.asus.filemanager.apk";
    set -e;
    callAdbShellCommandUntilSuccess adb shell 'pm install -r '"${mobilert_path}"'/APKs/com.asus.filemanager.apk; echo ::$?::';
  elif [ "${androidApi}" -lt 16 ]; then
    set +e;
    adb shell "pm uninstall ${mobilert_path}/APKs/com.estrongs.android.pop_4.2.1.8-10057_minAPI14.apk;";
    set -e;
    # This file manager is compatible with Android 4.0.3 (API 15) which the Asus one is not.
    callAdbShellCommandUntilSuccess adb shell 'pm install -r '"${mobilert_path}"'/APKs/com.estrongs.android.pop_4.2.1.8-10057_minAPI14.apk; echo ::$?::';
  fi
}

startCopyingLogcatToFile() {
  unlockDevice;

  echo 'Clear logcat';
  # -b all -> Unable to open log device '/dev/log/all': No such file or directory
  # -b crash -> Unable to open log device '/dev/log/crash': No such file or directory
  # TODO: Validate whether '-G' flag is supported by Android 20.
  if [ "${androidApi}" -gt 19 ]; then
    bufferSize='-G 10M';
  else
    # Android API <= 19 doesn't support the `-G` flag to change logcat buffer size.
    bufferSize='';
  fi
  callAdbShellCommandUntilSuccess adb shell 'logcat '"${bufferSize}"' -b main -b system -b radio -b events -c; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'logcat '"${bufferSize}"' -c; echo ::$?::';

  echo 'Copy realtime logcat to file';
  adb logcat -v threadtime "*":V | tee "${reports_path}"/logcat_"${type}".log 2>&1 &
  pid_logcat="$!";
  echo "pid of logcat: '${pid_logcat}'";
}

runUnitTests() {
  echo 'Copy unit tests to Android emulator.';
  echo 'Found binary types:';
  ls app/.cxx;
  if [ "${type}" = 'release' ]; then
    typeWithDebInfo='RelWithDebInfo';
  else
    typeWithDebInfo="${typeWithCapitalLetter}";
  fi
  dirUnitTests="app/.cxx/${typeWithDebInfo}";
  echo 'Found binary generated ids:';
  ls "${dirUnitTests}";
  android_cpu_architecture=$(adb shell getprop ro.product.cpu.abi | tr -d '[:space:]');
  echo "Checking generated id for: ${android_cpu_architecture}";
  # Note: flag `-t` of `ls` is to sort by date (newest first).
  # shellcheck disable=SC2012
  generatedId=$(find "${dirUnitTests}" -iname "*unittests" -exec readlink -f {} \; \
    | sort -n -r \
    | grep "${typeWithDebInfo}/" \
    | grep "${android_cpu_architecture}.*/" \
    | head -1 \
    | tr -s ' ' \
    | cut -d ' ' -f 7 \
    | sed "s/app\/.cxx\/${typeWithDebInfo}\///g" \
    | sed -e "s/\/${android_cpu_architecture}.*\/bin\/UnitTests//g" \
    | rev \
    | cut -d '/' -f 1 \
    | rev \
  );
  echo "Generated id: ${generatedId}";
  dirUnitTests="${dirUnitTests}/${generatedId}";
  echo 'Found binary CPU arch:';
  ls "${dirUnitTests}";
  dirUnitTests=$(find "${dirUnitTests}" -iname "*unittests" -exec readlink -f {} \; \
    | sort -n -r \
    | grep "${typeWithDebInfo}/" \
    | grep "${android_cpu_architecture}.*/" \
    | sed "s/\/bin\/UnitTests//g" \
    | head -1 \
    | tr -s ' ' \
    | cut -d ' ' -f 7 \
  );
  echo "Checking generated unit tests binaries: ${dirUnitTests}";
  files=$(ls "${dirUnitTests}");
  echo "Copy unit tests bin: ${files}/bin";
  echo "Copy unit tests libs: ${files}/lib";

  unlockDevice;

  callCommandUntilSuccess 5 adb push -p "${dirUnitTests}"/bin/* ${mobilert_path};
  callCommandUntilSuccess 5 adb push -p "${dirUnitTests}"/lib/* ${mobilert_path};

  echo 'Run unit tests';
  if [ "${type}" = 'debug' ]; then
    echo 'Enabling AddressSanitizer';
    adb shell setprop debug.asan.enabled true;
    adb shell setprop debug.asan.options detect_leaks=1:verbosity=1:shadow_mapping=1;
  fi
  adb shell "LD_LIBRARY_PATH=${mobilert_path} ${mobilert_path}/UnitTests; echo "'$?'" > ${mobilert_path}/unit_tests_result.log";
  adb pull "${mobilert_path}"/unit_tests_result.log .;
  resUnitTests=$(cat "unit_tests_result.log");
  if [ "${androidApi}" = '15' ]; then
    # TODO: Fix the native unit tests in Android API 15. Ignore the result for now.
    printCommandExitCode '0' "Unit tests (result: ${resUnitTests})";
  else
    printCommandExitCode "${resUnitTests}" 'Unit tests';
  fi
}

verifyResources() {
  echo 'Verify resources in SD Card';
  callCommandUntilSuccess 5 adb shell 'ls -laR '"${mobilert_path}/WavefrontOBJs";
  callCommandUntilSuccess 5 adb shell 'ls -laR '"${sdcard_path_android}";
  callCommandUntilSuccess 5 adb shell 'ls -laR '"${sdcard_path_android}/WavefrontOBJs";

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
      kill -TERM "${GRADLE_PROCESS}";
    done;
    sh gradlew --offline --parallel --stop \
      --no-rebuild \
      -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]" \
      --info --warning-mode fail --stacktrace;

    numberOfFilesOpened=$(adb shell lsof /dev/goldfish_pipe | wc -l);
    if [ "${numberOfFilesOpened}" -gt '32000' ]; then
      echo "Kill 'graphics.allocator' process since it has a bug where it
        accumulates a memory leak by continuously using more and more
        files of '/dev/goldfish_pipe' and never freeing them.";
      echo 'This might make the device restart!';
      set +e;
      pidsToKill=$(adb shell ps | grep -ine "graphics.allocator" | tr -s ' ' | cut -d ' ' -f 2);
      for pidToKill in ${pidsToKill}; do
        echo "Killing Android process: '${pidToKill}'";
        adb shell kill "${pidToKill}";
      done;
      set -e;
    fi
  fi
  set -eu;

  echo 'Searching for APK to install in Android emulator.';
  apksPath=$(find . -iname "*.apk" | grep -i "output");
  for apkPath in ${apksPath}; do
    echo "Will install APK: ${apkPath}";
    callCommandUntilSuccess 5 adb push -p "${apkPath}" "${mobilert_path}";
  done;
  callCommandUntilSuccess 5 adb shell 'ls -la '"${mobilert_path}";
  unlockDevice;
  echo 'Installing both APKs for tests and app.';
  set +e;
  adb shell "pm uninstall ${mobilert_path}/app-${type}-androidTest.apk;";
  adb shell "pm uninstall ${mobilert_path}/app-${type}.apk;";
  adb shell rm -r /data/app/puscas.mobilertapp*;
  adb shell ls -la /data/app;
  set -e;
  callAdbShellCommandUntilSuccess adb shell 'pm install -r '"${mobilert_path}"'/app-'"${type}"'.apk; echo ::$?::';
  callAdbShellCommandUntilSuccess adb shell 'pm install -r '"${mobilert_path}"'/app-'"${type}"'-androidTest.apk; echo ::$?::';
  if { [ "${androidApi}" -gt 22 ] && [ "${androidApi}" -lt 28 ]; }; then
    echo 'Granting read external SD Card to MobileRT.';
    callAdbShellCommandUntilSuccess adb shell 'pm grant puscas.mobilertapp.test android.permission.READ_EXTERNAL_STORAGE; echo ::$?::';
    callAdbShellCommandUntilSuccess adb shell 'pm grant puscas.mobilertapp android.permission.READ_EXTERNAL_STORAGE; echo ::$?::';
  fi

  # puscas.mobilertapp not found
  # adb shell pm grant puscas.mobilertapp android.permission.SET_ANIMATION_SCALE;

  echo 'List of instrumented APKs:';
  adb shell 'pm list instrumentation;';
  unlockDevice;

  if [ "${type}" = 'debug' ]; then
    echo 'Enabling AddressSanitizer';
    adb shell setprop debug.asan.enabled true;
    adb shell setprop debug.asan.options detect_leaks=1:verbosity=1:shadow_mapping=1;
  fi

  echo "Setting Gradle Wrapper to a version that is compatible with Android API: '${android_api_version}'".;
  sh gradlew --parallel wrapper -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]";

  if [ "${run_test}" = 'all' ]; then
    echo 'Running all tests';
    mkdir -p app/build/reports/jacoco/jacocoTestReport;
    # Allow to execute the tests a 2nd time in case it fails.
    # This allows for tests to pass when using Android emulator without hardware acceleration (e.g.: MacOS on Github Actions).
    callCommandUntilSuccess 1 sh gradlew ${gradle_command} -DtestType="${type}" \
      -DandroidApiVersion="${android_api_version}" \
      -Pandroid.testInstrumentationRunnerArguments.package='puscas' \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  elif echo "${run_test}" | grep -q "rep_"; then
    run_test_without_prefix=${run_test#"rep_"};
    echo "Repeatable of test: ${run_test_without_prefix}";
    callCommandUntilError sh gradlew connectedAndroidTest -DtestType="${type}" \
      -DandroidApiVersion="${android_api_version}" \
      -Pandroid.testInstrumentationRunnerArguments.class="${run_test_without_prefix}" \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  else
    echo "Running test: ${run_test}";
    sh gradlew --offline connectedAndroidTest -DtestType="${type}" \
      -DandroidApiVersion="${android_api_version}" \
      -Pandroid.testInstrumentationRunnerArguments.class="${run_test}" \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  fi
  resInstrumentationTests=${?};
  pid_instrumentation_tests="$!";
  echo 'Android test(s) executed!';
  echo "pid of instrumentation tests: '${pid_instrumentation_tests}'";
}

_restartAdbProcesses() {
  kill_adb_processes;
  set +u;
  if [ -z "${CI}" ]; then
    # Kill process(es) using same port as ADB
    killProcessesUsingPort 5037
  fi
  set -u;
}

# Waits for the Android Emulator to boot.
# By using cpulimit to 1, it can take around 3 minutes to boot.
# E.g.: Android API 33 can take more than 1 minute to boot.
_waitForEmulatorToBoot() {
  # Make sure ADB daemon started properly.
  callCommandUntilSuccess 25 adb shell 'ps > /dev/null;';
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
export GRADLE_OPTS='-Xms4G -Xmx4G -XX:ActiveProcessorCount=4';
echo 'Prepare traps';
trap 'catch_signal ${?}' EXIT HUP INT QUIT ILL TRAP ABRT TERM;
clearOldBuildFiles;
createReportsFolders;
runEmulator;
waitForEmulator;
copyResources;
verifyResources;
startCopyingLogcatToFile;
kill_gradle_processes;
kill_mobilert_processes;
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
