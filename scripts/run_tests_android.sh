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
  shellcheck "${0}" --exclude=SC1017 || return 1;
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
checkCommand readelf;

printEnvironment() {
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
# Written by the liveness watchdog when it reaps a wedged emulator; makes the
# in-script test retry fail fast instead of boot-waiting on a dead device.
liveness_marker="${reports_path}/.liveness_watchdog_killed_emulator";

echo 'Set path to instrumentation tests resources';
internal_storage_path='/data/local/tmp/MobileRT';
mobilert_path='/data/user/0/puscas.mobilertapp/files/MobileRT';
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
# Name the crashed process from the tombstone DEBUG header (">>> <name> <<<") so a
# native crash is attributable from the job log: a MobileRT crash is a real bug; an
# emulator system command (input/pm/am app_process) crashing is the known API <= 18
# Dalvik deadd00d defect where only a retry helps. Best-effort, never fatal.
# "VM exiting with result code" excludes code 0 (a normal VM exit) to avoid a false
# native-crash verdict on a run that actually died of an emulator kill.
analyze_native_crashes_func() {
  set +e;
  anc_signatures=$(cat "${reports_path}"/logcat_"${type}".log "${reports_path}"/logcat_dump_"${type}".log 2> /dev/null \
    | grep -aE "Fatal signal [0-9]+|double-overflow of stack|stack overflow on call|VM exiting with result code -?[1-9]|deadd00d" \
    | awk '!seen[$0]++' | tail -n 40);
  if [ -z "${anc_signatures}" ]; then
    echo 'No native crash signatures found in logcat.';
  else
    echo '----- native crash signatures (deduplicated) -----';
    echo "${anc_signatures}";
    echo '----- crashed process identification (tombstone headers from logcat) -----';
    anc_headers=$(cat "${reports_path}"/logcat_"${type}".log "${reports_path}"/logcat_dump_"${type}".log 2> /dev/null \
      | grep -aE "I DEBUG.*(Build fingerprint|pid: [0-9]+, tid: [0-9]+|signal [0-9]+ \(|>>> )|Couldn't find ProcessRecord" \
      | awk '!seen[$0]++' | tail -n 40);
    echo "${anc_headers}";
    if echo "${anc_headers}" | grep -a '>>> ' | grep -q 'puscas.mobilertapp'; then
      echo 'CRASH VERDICT: a MobileRT process crashed natively - investigate the app/JNI/native code (full backtrace in the tombstones above).';
    elif echo "${anc_signatures}" | grep -qa -e 'deadd00d' -e 'double-overflow of stack'; then
      echo 'CRASH VERDICT: Dalvik deadd00d double-stack-overflow in a NON-MobileRT process (see ">>> <name> <<<" above; typically app_process running input/pm/am on the slow API <= 18 emulator). Known emulator defect, not fixable in this repository - only a retry helps.';
    else
      echo 'CRASH VERDICT: native crash in a NON-MobileRT process (see ">>> <name> <<<" above for the culprit).';
    fi
  fi
  set -e;
}

gather_logs_func() {
  gl_exit_code="${1:-0}";
  set +e;
  pid_app=$(grep -E -i "proc.puscas:*" "${reports_path}"/logcat_"${type}".log |
    grep -i "pid=" | cut -d "=" -f 2 | cut -d "u" -f 1 | tr -d ' ' | tail -1);
  grep -e "${pid_app}" -e "I DEBUG" "${reports_path}"/logcat_"${type}".log \
    > "${reports_path}"/logcat_app_"${type}".log;
  echo "Filtered logcat of the app '${pid_app}' to logcat_app_${type}.log";

  # Pull native crash tombstones while adb is still up (the EXIT trap runs this
  # before the emulator is torn down), so a native SIGSEGV/SIGBUS leaves a
  # backtrace in the uploaded reports artifact. The pull is unconditional (the
  # artifact must always have it); the cat to stdout is deferred to the
  # failure-gated block below, so a passing run - e.g. the C++ death test that
  # deliberately fires signal 11 and passes - does not spam a backtrace.
  echo 'Pulling native crash tombstones from the device';
  timeout 60 adb root > /dev/null 2>&1 || true;
  timeout 60 adb pull /data/tombstones "${reports_path}" 2> /dev/null || echo 'No tombstones to pull.';

  # Memory + kernel post-mortem (runs while adb is still up; adb root above is
  # needed for dmesg). The streamed logcat can stop capturing mid-run (observed:
  # the pipe died ~3.5min before a crash, leaving the failing iteration with zero
  # device logs) and AGP's per-test logcat retrieval routinely returns nothing on
  # old APIs. Without this snapshot there is no way to tell an OOM-kill
  # (lowmemorykiller, in dmesg - never in logcat) from a hang or a native crash.
  # Best-effort, never fatal.
  echo 'Capturing device memory and kernel diagnostics';
  timeout 30 adb shell cat /proc/meminfo > "${reports_path}"/proc_meminfo_"${type}".log 2>&1 || true;
  timeout 30 adb shell dumpsys meminfo > "${reports_path}"/dumpsys_meminfo_"${type}".log 2>&1 || true;
  # Kernel ring buffer: lowmemorykiller / oom-killer verdicts land here, not in logcat.
  timeout 30 adb shell dmesg > "${reports_path}"/dmesg_"${type}".log 2>&1 || true;
  # Full logcat ring-buffer dump as a fallback for the streamed file (which can
  # die mid-run). '-d' dumps and exits. NOTE: '-b all' is NOT usable here: on API
  # <= 19 it fails with "Unable to open log device '/dev/log/all'" yet STILL exits
  # 0, so a `|| fallback` never fires and the file holds only the error line. Use
  # the same explicit, known-good buffer set as the streamed capture, then fall
  # back to the default buffer when the file is empty or holds the open-device
  # error (the only reliable signal that the buffer set was rejected).
  logcat_dump="${reports_path}/logcat_dump_${type}.log";
  timeout 60 adb logcat -d -b main -b system -b radio -b events > "${logcat_dump}" 2>&1 || true;
  if [ ! -s "${logcat_dump}" ] || grep -q 'Unable to open log device' "${logcat_dump}" 2> /dev/null; then
    timeout 60 adb logcat -d > "${logcat_dump}" 2>&1 || true;
  fi

  # If this run failed, echo the diagnostics to stdout now. wretry retries the
  # whole step (attempt_limit), so a transient failure that a later attempt
  # recovers from leaves the job green and the `Print error logs` step (which is
  # `if: failure()`) never runs - the failing attempt's evidence would be lost.
  # The report files are also overwritten by each later attempt, so a job-level
  # `if: always()` dump would only show the last (passing) attempt. Printing here
  # puts the failing attempt's diagnostics in *its own* console log, always.
  if [ "${gl_exit_code}" -ne 0 ]; then
    echo "===== Test run failed (exit ${gl_exit_code}); dumping device diagnostics =====";
    echo '----- native crash tombstones (backtrace if SIGSEGV/SIGABRT) -----'; cat "${reports_path}"/tombstones/* 2> /dev/null || echo '(none)';
    analyze_native_crashes_func;
    echo '----- /proc/meminfo -----'; cat "${reports_path}"/proc_meminfo_"${type}".log 2> /dev/null;
    echo '----- dumpsys meminfo -----'; cat "${reports_path}"/dumpsys_meminfo_"${type}".log 2> /dev/null;
    echo '----- dmesg (lowmemorykiller/oom-killer verdicts appear here) -----'; cat "${reports_path}"/dmesg_"${type}".log 2> /dev/null;
    echo '----- logcat ring-buffer dump (tail) -----'; tail -n 2000 "${reports_path}"/logcat_dump_"${type}".log 2> /dev/null;
    # The streamed capture is fuller than the ring-buffer dump (the ring is tiny
    # on old APIs and cannot be enlarged with '-G'), so tail it too: if the
    # auto-restart kept the stream alive across the crash, the failing render's
    # last lines are here, not in the truncated ring dump above.
    echo '----- streamed logcat (tail) -----'; tail -n 2000 "${reports_path}"/logcat_"${type}".log 2> /dev/null;
    echo '===== End device diagnostics =====';
  fi
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

# Capture a Java thread dump of the app while it is still alive on the device.
#
# The repeatable workflow (run_test=rep_*) re-launches a single test until one
# launch hangs: the app process starts but the instrumentation never makes
# progress (logcat goes silent, no test report is written) and the per-attempt
# `timeout` eventually kills it. When that happens the only evidence left is an
# empty logcat, which is useless for diagnosis. SIGQUIT (kill -3) makes the ART
# runtime dump every Java thread's stack to logcat and to /data/anr/traces.txt,
# so we can see *where* the launch is stuck (Espresso RootViewPicker, GL init,
# native render, ...). This must run BEFORE clear_func kills the app, otherwise
# there is nothing left to dump. On the success path no app process is alive, so
# this is a cheap no-op.
gather_anr_func() {
  set +e;
  pid_apps=$(timeout 10 adb shell ps | grep -i "puscas.mobilertapp" | tr -s ' ' | cut -d ' ' -f 2);
  if [ -n "${pid_apps}" ]; then
    echo 'App process still alive on exit (possible hang); capturing thread dump';
    for pid_app in ${pid_apps}; do
      echo "Sending SIGQUIT to MobileRT pid '${pid_app}' to force an ART thread dump";
      timeout 60 adb shell 'kill -3 '"${pid_app}";
    done
    sleep 2; # Give ART time to write the dump to logcat and traces.txt.
    timeout 60 adb shell 'dumpsys activity activities' > "${reports_path}"/dumpsys_activity_"${type}".log 2>&1 || true;
    # API >= 28 writes the dump to /data/anr/trace_XX (tombstoned), older APIs to
    # /data/anr/traces.txt. Pull the whole directory (root needed on most emulators;
    # harmless no-op where unsupported) so the dump is not lost to the wrong filename.
    timeout 60 adb root > /dev/null 2>&1 || true;
    timeout 60 adb wait-for-device > /dev/null 2>&1 || true; # adb root restarts adbd.
    timeout 60 adb pull /data/anr "${reports_path}" 2> /dev/null || echo 'No ANR traces to pull.';
    echo '----- ANR / thread dump (/data/anr) -----';
    cat "${reports_path}"/anr/* "${reports_path}"/traces.txt 2> /dev/null || true;
    echo '----- end ANR / thread dump -----';
  fi
  set -e;
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

# Host-side device liveness watchdog for the instrumentation run.
#
# A headless swiftshader guest can hard-freeze MID-RUN (run 27266459682: all
# guest output stopped right after "Tests 25/32 completed"; qemu stayed alive
# but the guest never answered adb again). Gradle's connectedAndroidTest has
# no device-liveness detection, so it dead-waits on the ddmlib socket until
# the workflow step timeout SIGKILLs the job - burning the remaining ~20min
# budget, starving wretry of its last attempt AND skipping this script's EXIT
# trap (SIGKILL), so no post-mortem logs survive. This watchdog pings the
# device while gradle runs; after 4 consecutive failed pings (~2-3min
# unresponsive) it kills the emulator process, which makes adb report the
# device gone and gradle fail within seconds - converting an unbounded hang
# into a fast, retryable failure with a normal trap-driven post-mortem.
# Emulator-only: never started when the device is real hardware.
startDeviceLivenessWatchdog() {
  # Remove a stale marker from a previous attempt (wretry reuses the
  # workspace); a leftover marker would instantly fail the next fresh run.
  rm -f "${liveness_marker}";
  if ! timeout 10 adb devices 2> /dev/null | grep -q '^emulator-'; then
    echo 'Liveness watchdog: no emulator device detected; watchdog not started.';
    return 0;
  fi
  liveness_qemu_pid="$(pgrep -o -f qemu-system 2> /dev/null || true)";
  (
    consecutiveFails=0;
    checks=0;
    # Cap the loop (80 * 30s = 40min) so an orphaned watchdog always dies.
    while [ "${checks}" -lt 80 ]; do
      sleep 30;
      checks=$((checks + 1));
      if timeout 15 adb shell echo liveness-ping > /dev/null 2>&1; then
        consecutiveFails=0;
      else
        consecutiveFails=$((consecutiveFails + 1));
        echo "Liveness watchdog: adb ping failed (${consecutiveFails}/4 consecutive).";
        if [ "${consecutiveFails}" -ge 4 ]; then
          echo "Liveness watchdog: device unresponsive for ~2min; killing emulator (qemu pid: '${liveness_qemu_pid}') so gradle fails fast instead of dead-waiting until the step timeout.";
          # Marker tells the in-script retry of _executeAndroidTests to bail
          # out immediately: its boot-wait helpers retry adb ~70x and would
          # otherwise grind ~10min against a dead device before failing.
          mkdir -p "$(dirname "${liveness_marker}")" 2> /dev/null;
          touch "${liveness_marker}" 2> /dev/null;
          if [ -n "${liveness_qemu_pid}" ]; then
            kill -9 "${liveness_qemu_pid}" 2> /dev/null || true;
          fi
          break;
        fi
      fi
    done
  ) &
  pid_liveness_watchdog="$!";
  echo "Liveness watchdog started (pid: '${pid_liveness_watchdog}', qemu pid: '${liveness_qemu_pid}').";
}

stopDeviceLivenessWatchdog() {
  set +u; # Variable is unset when the watchdog was never started.
  if [ -n "${pid_liveness_watchdog}" ]; then
    echo "Stopping liveness watchdog (pid: '${pid_liveness_watchdog}').";
    kill "${pid_liveness_watchdog}" 2> /dev/null || true;
    pid_liveness_watchdog='';
  fi
  set -u;
}

catch_signal() {
  catch_exit_code="${1:-0}";
  echo "Caught signal (exit code: ${catch_exit_code})";
  trap - EXIT HUP INT QUIT ILL TRAP ABRT TERM; # Disable traps first, to avoid infinite loop.

  # Stop the liveness watchdog FIRST: the post-mortem below issues slow adb
  # calls on a possibly-degraded device, and the watchdog must not kill qemu
  # in the middle of that capture.
  stopDeviceLivenessWatchdog;
  gather_anr_func; # Dump the wedged app's threads before clear_func kills it.
  clear_func;
  gather_logs_func "${catch_exit_code}";
  callCommandUntilSuccess 5 timeout 60 adb kill-server > /dev/null 2>&1;
  callCommandUntilSuccess 5 timeout 60 adb start-server > /dev/null 2>&1;
  unlockDevice > /dev/null 2>&1;

  echo "Killing all processes from the same group process id (thus killing also descendants): '${pid}'";
  kill -TERM -"${pid}" || true;
  echo 'Caught signal finished';
}

kill_mobilert_processes() {
  echo 'Killing MobileRT process in Android device';
  pid_apps=$(timeout 10 adb shell ps | grep -i "puscas.mobilertapp" | tr -s ' ' | cut -d ' ' -f 2);
  for pid_app in ${pid_apps}; do
    echo "Killing pid of MobileRT: '${pid_app}'";
    set +e;
    timeout 60 adb shell 'kill -TERM '"${pid_app}";
    set -e;
  done
}

kill_gradle_processes() {
  echo 'Killing Gradle process';
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
  echo 'Killing ADB process';
  # shellcheck disable=SC2009
  ADB_PROCESSES=$(ps aux | grep -i " adb " | grep -v "grep" | tr -s ' ' | cut -d ' ' -f 2);
  ADB_PROCESSES_STR="$(echo "${ADB_PROCESSES}" | tr '\n' ',' | sed 's/,$//')";
  echo "Killing the detected ADB process(es): '${ADB_PROCESSES_STR}'";
  set +eu;
  if [ -z "${CI}" ]; then
    for ADB_PROCESS in ${ADB_PROCESSES}; do
      kill -TERM "${ADB_PROCESS}";
    done;
    sleep 2;
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
  callCommandUntilSuccess 5 timeout 60 adb root;

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
  callAdbShellCommandUntilSuccess 'input keyevent 82';

  if [ "${androidApiDevice}" -gt 15 ]; then
    callAdbShellCommandUntilSuccess 'input tap 800 400';
    callAdbShellCommandUntilSuccess 'input tap 1000 500';
  fi

  callCommandUntilSuccess 5 timeout 60 adb get-state;
  callCommandUntilSuccess 5 timeout 60 adb devices -l;
  callCommandUntilSuccess 5 timeout 60 adb version;

  callAdbShellCommandUntilSuccess 'input keyevent 82';
  if [ "${androidApiDevice}" -gt 15 ]; then
    callAdbShellCommandUntilSuccess 'input tap 800 400';
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

  callCommandUntilSuccess 5 timeout 60 adb kill-server;
  #_restartAdbProcesses;
  callCommandUntilSuccess 5 timeout 60 adb start-server;
  # After restarting adb-server above, the new daemon needs a moment to
  # re-discover the running emulator and complete the adbd handshake.
  # Without this wait, follow-up `adb shell` calls return "device offline"
  # *instantly* (not a timeout), and the 70-retry loop further down in
  # _waitForEmulatorToBoot exhausts in ~70 seconds with no recovery.
  # Block here (bounded) until the device transitions out of `offline`.
  callCommandUntilSuccess 5 timeout 30 adb wait-for-device;
  set +e;
  adb_devices_running=$(timeout 60 adb devices | tail -n +2);
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
    # List supported machines: emulator -avd <avd_emulator> -qemu -machine help
    # List supported accelerators: emulator -avd <avd_emulator> -qemu -accel help
    # List available CPUs: emulator -avd <avd_emulator> -qemu -cpu help
    setsid nohup cpulimit --cpu 8 --limit 800 -- \
      emulator -avd "${avd_emulator}" -cores 8 -memory 4096 -cache-size 512 -partition-size 800 \
      -ranchu -fixed-scale -skip-adb-auth -gpu swiftshader_indirect -no-audio \
      -no-snapstorage -no-snapshot -no-snapshot-update-time -no-snapshot-save -no-snapshot-load -wipe-data \
      -no-boot-anim -camera-back none -camera-front none -netfast -no-sim \
      -no-passive-gps -no-direct-adb -no-location-ui -no-hidpi-scaling \
      -no-mouse-reposition -no-nested-warnings -verbose \
      -qemu -m size=4096M,slots=1,maxmem=8192M -machine type=pc,accel=kvm -accel kvm,thread=multi:tcg,thread=multi -smp cpus=8,maxcpus=8,cores=8,threads=1,sockets=1
    sleep 20;
    adb_devices_running=$(callCommandUntilSuccess 5 timeout 60 adb devices | tail -n +2);
  done
  set -eu;
  echo "Devices running: '${adb_devices_running}'";

  if (grep -iq "Process .* dead!" nohup.out); then
    echo "Android emulator didn't boot properly, please check the 'nohup.out' log file for more context.";
    exit 1;
  fi

  echo 'Finding at least 1 Android device on.';
  _waitForEmulatorToBoot;

  echo "Setting Gradle Wrapper to a version that is compatible with Android API: '${android_api_version}'".;
  callCommandUntilSuccess 2 sh gradlew --parallel --info wrapper -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]";
  callCommandUntilSuccess 2 sh gradlew --parallel --info --daemon --no-rebuild -DtestType="${type}" -DandroidApiVersion="${android_api_version}" \
    -DabiFilters="[${cpu_architecture}]" --info --warning-mode fail --stacktrace;

  unlockDevice;

  adb_devices_running=$(callCommandUntilSuccess 5 timeout 60 adb devices | grep -v 'List of devices attached' || true);
  echo "Devices running after triggering boot: '${adb_devices_running}'";
  if [ -z "${adb_devices_running}" ]; then
    # Abort if emulator didn't start.
    echo "Android emulator didn't start ... will exit.";
    exit 1;
  fi

  echo 'Disable animations';
  # Espresso's RootViewPicker requires animations off, otherwise the window
  # never settles/gains focus and tests fail with RootViewWithoutFocusException
  # (observed on the slow API 19 emulator). The reusable workflows keep the
  # setup-android-emulator action's `disable-animations: false` on purpose
  # (that action's un-timeout-wrapped adb call can hang a wedged emulator for
  # the whole step), so the scales are disabled here instead, via the
  # timeout-wrapped & retried callAdbShellCommandUntilSuccess.
  # Error in API 15 & 16: /system/bin/sh: settings: not found
  if [ "${androidApiDevice}" -gt 16 ]; then
    callAdbShellCommandUntilSuccess 'settings put global window_animation_scale 0';
    callAdbShellCommandUntilSuccess 'settings put global transition_animation_scale 0';
    callAdbShellCommandUntilSuccess 'settings put global animator_duration_scale 0';
  fi

  # CheckJNI wraps every JNI transition in extra validation frames and extra
  # ref-table bookkeeping allocations. Two distinct runtimes crash under it:
  #
  #   * Old Dalvik (API <= 19): the VM-internal threads (e.g. the Signal Catcher)
  #     get a tiny fixed stack, so the extra frames tip a routine VM-bootstrap
  #     thread attach (Thread.<init>) into a double stack-overflow -> dvmAbort
  #     (SIGSEGV 0xdeadd00d) -> the process dies on launch.
  #   * First-gen ART (API 21/22 = Android 5.0/5.1 Lollipop): the original
  #     concurrent mark-sweep GC has a fragile card-table / mod-union-table path.
  #     CheckJNI's extra allocations multiply heap churn -> more dirty cards and
  #     more frequent GC cycles -> SIGSEGV in the GCDaemon inside
  #     ModUnionTableCardCache::ClearCards (observed: every relaunched
  #     app_process crashes there, the instrumentation hangs, the outer timeout
  #     fires (exit 124), no index.html -> red).
  #
  # Android 6.0 (API 23) rewrote the ART GC (the card-table / mod-union code that
  # faults on 5.x is fixed), so CheckJNI is safe there. Enable it only on API >= 23.
  # (Low confidence on the exact ART-version boundary: causation is inferred from
  # the GCDaemon backtrace + AOSP history, not bisected to the fixing commit.)
  if [ "${androidApiDevice}" -ge 23 ]; then
    echo 'Activate JNI extended checking mode';
    # Command fails on Android 34.
    # callAdbShellCommandUntilSuccess 'setprop dalvik.vm.checkjni true';
    callAdbShellCommandUntilSuccess 'setprop debug.checkjni 1';
  else
    # Old Dalvik (API <= 19) / first-gen ART (API 21/22): keep CheckJNI off. It is
    # a global property that every spawned app_process VM inherits - including
    # `pm install` - so a stale/on value crashes the test-services / orchestrator
    # install itself ("Unknown failure: Segmentation fault") before any test runs.
    # Force it off defensively rather than relying on the default.
    echo 'Skip JNI extended checking mode (old Dalvik small stacks / early-ART GC abort under CheckJNI)';
    callAdbShellCommandUntilSuccess 'setprop debug.checkjni 0';
  fi

  unlockDevice;
}

copyResources() {
  mkdir -p ${reports_path};

  unlockDevice;
  echo 'Possible SD Card paths:';
  timeout 60 adb shell ls -d '/storage/*' | grep -v 'self';
  timeout 60 adb shell df;
  set +e;
  timeout 60 adb shell env | grep -i "storage";
  set -e;
  sdcard_path_android="$(timeout 60 adb shell ls -d '/storage/*' | grep -v '/storage/emulated' | grep -v 'self' | tail -1)";
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
    if [ "${androidApiDevice}" -lt 22 ]; then
      sdcard_path_android="${sdcard_path_android}/MobileRT";
    else
      sdcard_path_android="${sdcard_path_android}/Android/data/puscas.mobilertapp/files/MobileRT";
    fi
  fi
  echo "sdcard_path_android: '${sdcard_path_android}'";

  echo 'Prepare copy unit tests';
  set +e;
  timeout 60 adb shell rm -r ${mobilert_path};
  timeout 60 adb shell rm -r ${internal_storage_path};
  if [ "${androidApiDevice}" -gt 29 ]; then
    timeout 60 adb shell 'rm -r '"${sdcard_path_android}";
  fi
  set -e;

  callAdbShellCommandUntilSuccess 'mkdir -p '"${internal_storage_path}";
  callAdbShellCommandUntilSuccess 'ls -laR '"${internal_storage_path}";
  callAdbShellCommandUntilSuccess 'mkdir -p '"${sdcard_path_android}";
  callAdbShellCommandUntilSuccess 'ls -laR '"${sdcard_path_android}";

  callAdbShellCommandUntilSuccess 'mkdir -p '"${internal_storage_path}"'/WavefrontOBJs/CornellBox';
  callAdbShellCommandUntilSuccess 'mkdir -p '"${sdcard_path_android}"'/WavefrontOBJs/teapot';

  echo 'Copy tests resources';
  callCommandUntilSuccess 5 timeout 60 adb push -p app/src/androidTest/resources/CornellBox ${internal_storage_path}/WavefrontOBJs;
  set +e;
  timeout 60 adb push -p app/src/androidTest/resources/teapot "${sdcard_path_android}/WavefrontOBJs";
  set -e;

  echo 'Copy File Manager';
  callCommandUntilSuccess 5 timeout 60 adb push -p app/src/androidTest/resources/APKs ${internal_storage_path};

  echo 'Change resources permissions';
  callAdbShellCommandUntilSuccess 'chmod -R 777 '"${internal_storage_path}"'';
  callAdbShellCommandUntilSuccess 'chmod -R 777 '"${sdcard_path_android}"'';

  unlockDevice;
  echo 'Install File Manager';
  # The file manager is a third-party test fixture used only by a subset of
  # UI tests. A flaky package-manager error (the preceding pm uninstall
  # routinely yields DELETE_FAILED_INTERNAL_ERROR, and pm install can hit a
  # transient INSTALL_FAILED_*) must not hard-abort the entire Android test
  # job. callAdbShellCommandUntilSuccess already retries; on a persistent
  # failure we warn and continue (best-effort, like the teapot push above)
  # so the rest of the suite still runs and produces reports. set +e spans
  # the whole block so the tolerated uninstall no longer needs its own.
  set +e;
  if [ "${androidApiDevice}" -gt 31 ]; then
    echo "Not installing any file manager APK because the available ones are not compatible with Android API: ${androidApiDevice}";
  elif [ "${androidApiDevice}" -gt 30 ]; then
    timeout 60 adb shell "pm uninstall ${internal_storage_path}/APKs/asus-file-manager-2-8-0-85-230220.apk;";
    callAdbShellCommandUntilSuccess 'pm install -r '"${internal_storage_path}"'/APKs/asus-file-manager-2-8-0-85-230220.apk' \
      || echo 'WARNING: file manager install failed after retries; continuing without it (dependent UI tests may skip/fail).';
  elif [ "${androidApiDevice}" -gt 29 ]; then
    timeout 60 adb shell "pm uninstall ${internal_storage_path}/APKs/com.asus.filemanager_2.7.0.28_220608-1520700140_minAPI30_apkmirror.com.apk";
    callAdbShellCommandUntilSuccess 'pm install -r '"${internal_storage_path}"'/APKs/com.asus.filemanager_2.7.0.28_220608-1520700140_minAPI30_apkmirror.com.apk' \
      || echo 'WARNING: file manager install failed after retries; continuing without it (dependent UI tests may skip/fail).';
  elif [ "${androidApiDevice}" -gt 16 ]; then
    timeout 60 adb shell "pm uninstall ${internal_storage_path}/APKs/com.asus.filemanager.apk";
    callAdbShellCommandUntilSuccess 'pm install -r '"${internal_storage_path}"'/APKs/com.asus.filemanager.apk' \
      || echo 'WARNING: file manager install failed after retries; continuing without it (dependent UI tests may skip/fail).';
  elif [ "${androidApiDevice}" -lt 16 ]; then
    timeout 60 adb shell "pm uninstall ${internal_storage_path}/APKs/com.estrongs.android.pop_4.2.1.8-10057_minAPI14.apk";
    # This file manager is compatible with Android 4.0.3 (API 15) which the Asus one is not.
    callAdbShellCommandUntilSuccess 'pm install -r '"${internal_storage_path}"'/APKs/com.estrongs.android.pop_4.2.1.8-10057_minAPI14.apk' \
      || echo 'WARNING: file manager install failed after retries; continuing without it (dependent UI tests may skip/fail).';
  fi
  set -e;
}

startCopyingLogcatToFile() {
  unlockDevice;

  echo 'Clear logcat';
  # -b all -> Unable to open log device '/dev/log/all': No such file or directory
  # -b crash -> Unable to open log device '/dev/log/crash': No such file or directory
  # TODO: Validate whether '-G' flag is supported by Android 20.
  if [ "${androidApiDevice}" -gt 19 ]; then
    bufferSize='-G 10M';
  else
    # Android API <= 19 doesn't support the `-G` flag to change logcat buffer size.
    bufferSize='';
  fi
  callAdbShellCommandUntilSuccess 'logcat '"${bufferSize}"' -b main -b system -b radio -b events -c';
  callAdbShellCommandUntilSuccess 'logcat '"${bufferSize}"' -c';

  echo 'Copy realtime logcat to file';
  # Backstop only: clear_func kills pid_logcat cleanly at suite end. The cap must
  # outlast the whole suite (connectedAndroidTest gets `timeout 600`), otherwise the
  # capture dies during the unit tests and the instrumentation render is never logged.
  #
  # Wrap the capture in an auto-restart loop: on a software-emulated CPU (API 26) a
  # max-samples render takes ~3min, and the adb logcat pipe was observed dying
  # mid-run (~7min before a crash), losing exactly the failing iteration's logs. If
  # the pipe drops the loop reconnects and keeps appending. Truncate once up front,
  # then append ('tee -a') so reconnects don't wipe earlier output. clear_func kills
  # pid_logcat (this subshell -> stops respawn) and the 'tee' pid by name (-> the
  # inner 'adb logcat' dies via SIGPIPE), so the loop tears down cleanly at suite end.
  : > "${reports_path}"/logcat_"${type}".log;
  ( while true; do
      adb logcat -v threadtime "*":V | tee -a "${reports_path}"/logcat_"${type}".log;
      echo 'logcat stream dropped; reconnecting in 1s' >&2;
      sleep 1;
    done ) &
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
  android_cpu_architecture=$(timeout 60 adb shell getprop ro.product.cpu.abi | tr -d '[:space:]');
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
    | grep "${android_cpu_architecture}/" \
    | sed "s/\/bin\/UnitTests//g" \
    | head -1 \
    | tr -s ' ' \
    | cut -d ' ' -f 7 \
  );
  echo "Checking generated unit tests binaries: ${dirUnitTests}";
  files=$(ls "${dirUnitTests}");
  echo "Copy unit tests bin: ${files}/bin";
  echo "Copy unit tests libs: ${files}/lib";
  elfHeader=$(readelf -h "${dirUnitTests}"/bin/UnitTests 2>&1);
  echo "Binary header: ${elfHeader}";

  unlockDevice;

  # Restore the executable bit on the native UnitTests binary. The compiled
  # binaries are handed between jobs as a GitHub artifact, and artifacts do
  # NOT preserve Unix file permissions (the +x bit is stripped on download).
  # `adb push -p` preserves the host mode, so without this the on-device
  # binary is not executable and fails with "Permission denied" (exit 126).
  chmod +x "${dirUnitTests}"/bin/*;

  callCommandUntilSuccess 5 timeout 60 adb push -p "${dirUnitTests}"/bin/* ${internal_storage_path};
  callCommandUntilSuccess 5 timeout 60 adb push -p "${dirUnitTests}"/lib/* ${internal_storage_path};

  if [ "${type}" = 'debug' ]; then
    echo 'Enabling AddressSanitizer';
    timeout 60 adb shell setprop debug.asan.enabled true;
    timeout 60 adb shell setprop debug.asan.options detect_leaks=1:verbosity=1:shadow_mapping=1;
  fi
  callAdbShellCommandUntilSuccess 'ls -la '"${internal_storage_path}/UnitTests";
  callAdbShellCommandUntilSuccess 'ls -laR '"${internal_storage_path}";
  echo 'Run unit tests';
  if { [ "${androidApiDevice}" -ge 21 ]; }; then
    echo 'Android device API >= 21 detected';
    if echo "${elfHeader}" | grep -q -e 'EXEC (Executable file)'; then
      echo 'Skipping unit tests because the generated unit tests binary does not support position-independent execution (PIE)';
      echo '0' > unit_tests_result.log;
    else
      timeout 180 adb shell "LD_LIBRARY_PATH=${internal_storage_path} ${internal_storage_path}/UnitTests; echo "'$?'" > ${internal_storage_path}/unit_tests_result.log";
      timeout 60 adb pull "${internal_storage_path}"/unit_tests_result.log .;
    fi
  else
    echo 'Android device API < 21 detected';
    timeout 180 adb shell "LD_LIBRARY_PATH=${internal_storage_path} ${internal_storage_path}/UnitTests; echo "'$?'" > ${internal_storage_path}/unit_tests_result.log";
    timeout 60 adb pull "${internal_storage_path}"/unit_tests_result.log .;
  fi
  resUnitTests=$(cat "unit_tests_result.log");
  printCommandExitCode "${resUnitTests}" 'Unit tests';
}

verifyResources() {
  echo 'Verify resources in SD Card';
  callAdbShellCommandUntilSuccess 'ls -laR '"${internal_storage_path}/WavefrontOBJs";
  callAdbShellCommandUntilSuccess 'ls -laR '"${sdcard_path_android}";
  callAdbShellCommandUntilSuccess 'ls -laR '"${sdcard_path_android}/WavefrontOBJs";

  echo 'Verify memory available on host:';
  if command -v free > /dev/null; then
    free -h;
  else
    vm_stat;
  fi

  echo 'Verify memory available on Android emulator:';
  set +e;
  callAdbShellCommandUntilSuccess 'cat /proc/meminfo';
  set -e;
  echo 'Verified memory available on Android emulator.';

  grep -r "hw.ramSize" ~/.android/avd/ 2> /dev/null || true;
}

runInstrumentationTests() {
  echo 'Run instrumentation tests';
  set +eu;
  if [ -z "${CI}" ]; then
    GRADLE_PROCESSES="$(jps | grep -i "gradle" | tr -s ' ' | cut -d ' ' -f 1)";
    for GRADLE_PROCESS in ${GRADLE_PROCESSES}; do
      kill -TERM "${GRADLE_PROCESS}";
    done;
    callCommandUntilSuccess 2 sh gradlew --offline --parallel --stop \
      --no-rebuild \
      -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]" \
      --info --warning-mode fail --stacktrace;

    numberOfFilesOpened=$(timeout 60 adb shell lsof /dev/goldfish_pipe | wc -l);
    if [ "${numberOfFilesOpened}" -gt '32000' ]; then
      echo "Kill 'graphics.allocator' process since it has a bug where it
        accumulates a memory leak by continuously using more and more
        files of '/dev/goldfish_pipe' and never freeing them.";
      echo 'This might make the device restart!';
      set +e;
      pidsToKill=$(timeout 60 adb shell ps | grep -ine "graphics.allocator" | tr -s ' ' | cut -d ' ' -f 2);
      for pidToKill in ${pidsToKill}; do
        echo "Killing Android process: '${pidToKill}'";
        timeout 60 adb shell kill "${pidToKill}";
      done;
      set -e;
    fi
  fi
  set -eu;

  timeout 60 adb shell df;
  echo 'Searching for APK '"${type}"' to install in Android emulator.';
  find . -iname "*.apk";
  apksPath=$(find . -iname "*.apk" | grep -i "output" | grep -i "${type}" | grep -i "androidTest");
  echo "Will install the following APKs: ${apksPath}";
  for apkPath in ${apksPath}; do
    echo "Will install APK: ${apkPath}";
    ls -lahp "${apkPath}";
    callCommandUntilSuccess 5 timeout 60 adb push -p "${apkPath}" "${internal_storage_path}";
  done;

  set +e;
  test -d release;  
  # shellcheck disable=SC2319
  apkSignedFound="$?";
  set -e;
  if [ "${apkSignedFound}" = '0' ]; then
    echo 'Searching for signed APK';
    apksPath=$(find release/ -iname "*.apk");
  else
    echo 'Searching for APK';
    apksPath=$(find . -iname "*.apk" | grep -i "output" | grep -i "${type}" | grep -v "androidTest");
  fi
  echo "Will install the following APKs: ${apksPath}";
  for apkPath in ${apksPath}; do
    echo "Will install APK: ${apkPath}";
    ls -lahp "${apkPath}";
    callCommandUntilSuccess 5 timeout 60 adb push -p "${apkPath}" "${internal_storage_path}";
  done;

  callAdbShellCommandUntilSuccess 'ls -la '"${internal_storage_path}";
  unlockDevice;
  echo 'Installing both APKs for tests and app.';
  set +e;
  timeout 60 adb shell "pm uninstall ${internal_storage_path}/app-${type}-androidTest.apk;";
  timeout 60 adb shell "pm uninstall ${internal_storage_path}/app-${type}.apk;";
  timeout 60 adb shell "pm uninstall ${internal_storage_path}/MobileRT_${type}_min_android_api-${android_api_version}.apk;";
  timeout 60 adb shell rm -r /data/app/puscas.mobilertapp*;
  timeout 60 adb shell ls -la /data/app;
  set -e;
  if [ "${apkSignedFound}" = '0' ]; then
    callAdbShellCommandUntilSuccess 'pm install -r '"${internal_storage_path}"'/MobileRT_'"${type}"'_min_android_api-'"${android_api_version}"'.apk';
  else
    callAdbShellCommandUntilSuccess 'pm install -r '"${internal_storage_path}"'/app-'"${type}"'.apk';
  fi
  callAdbShellCommandUntilSuccess 'pm install -r '"${internal_storage_path}"'/app-'"${type}"'-androidTest.apk';
  if { [ "${androidApiDevice}" -gt 22 ] && [ "${androidApiDevice}" -lt 28 ]; }; then
    echo 'Granting read external SD Card to MobileRT.';
    callAdbShellCommandUntilSuccess 'pm grant puscas.mobilertapp.test android.permission.READ_EXTERNAL_STORAGE';
    callAdbShellCommandUntilSuccess 'pm grant puscas.mobilertapp android.permission.READ_EXTERNAL_STORAGE';
  fi
  callAdbShellCommandUntilSuccess 'chmod -R 777 '"${internal_storage_path}"'';
  callAdbShellCommandUntilSuccess 'ls -laR '"${internal_storage_path}";

  # puscas.mobilertapp not found
  # adb shell pm grant puscas.mobilertapp android.permission.SET_ANIMATION_SCALE;

  echo 'List of instrumented APKs:';
  callAdbShellCommandUntilSuccess 'pm list instrumentation';
  unlockDevice;

  if [ "${type}" = 'debug' ]; then
    echo 'Enabling AddressSanitizer';
    timeout 60 adb shell setprop debug.asan.enabled true;
    timeout 60 adb shell setprop debug.asan.options detect_leaks=1:verbosity=1:shadow_mapping=1;
  fi

  callAdbShellCommandUntilSuccess 'am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS';

  startDeviceLivenessWatchdog;
  if [ "${run_test}" = 'all' ]; then
    echo 'Running all tests';
    mkdir -p app/build/reports/jacoco/jacocoTestReport;
    # Allow to execute the tests a 2nd time in case it fails.
    # This allows for tests to pass when using Android emulator without hardware acceleration (e.g.: MacOS on Github Actions).
    callCommandUntilSuccess 2 _executeAndroidTests;
    timeout 60 adb pull "${internal_storage_path}/screenshots" .;
    echo 'Checking all files';
    ls -lahp .;
    echo 'Checking all screenshots';
    ls -lahp "screenshots";
    countScreenshots=0;
    for pngFile in screenshots/*.png; do
      [ -e "${pngFile}" ] || continue;
      countScreenshots=$((countScreenshots + 1));
    done;
    if [ "${countScreenshots}" -lt 3 ]; then
      echo "Found only ${countScreenshots} screenshots, failing the test run.";
      exit 1;
    fi
  elif echo "${run_test}" | grep -q "rep_"; then
    run_test_without_prefix=${run_test#"rep_"};
    echo "Repeatable of test: ${run_test_without_prefix}";
    callCommandUntilError _executeAndroidTests;
  else
    echo "Running test: ${run_test}";
    callCommandUntilSuccess 2 _executeAndroidTests;
  fi
  resInstrumentationTests=${?};
  pid_instrumentation_tests="$!";
  stopDeviceLivenessWatchdog;
  echo 'Android test(s) executed!';
  echo "pid of instrumentation tests: '${pid_instrumentation_tests}'";
}

_executeAndroidTests() {
  if [ -f "${liveness_marker}" ]; then
    echo 'Liveness watchdog killed the emulator; failing this attempt immediately (a fresh emulator needs a workflow-level retry).';
    return 1;
  fi
  unlockDevice;

  # Start every iteration from a clean app state. The repeatable workflow
  # (run_test=rep_*) re-launches the same test many times in a row via
  # callCommandUntilError; a leftover app process / GL context / ART state from
  # the previous launch can wedge the next one (observed: N launches pass, then
  # one hangs before the first @Test). Force-stopping the package between
  # iterations removes that carried-over state. No-op on the first launch and on
  # the `all` path (nothing running yet); guarded so it never aborts the run.
  set +e;
  timeout 60 adb shell 'am force-stop puscas.mobilertapp';
  set -e;

  callAdbShellCommandUntilSuccess 'mkdir -p '"${internal_storage_path}"'/screenshots';
  callAdbShellCommandUntilSuccess 'chmod -R 777 '"${internal_storage_path}"'/screenshots';
  callAdbShellCommandUntilSuccess 'ls -la '"${internal_storage_path}"'/screenshots';

  echo "Copying OBJ to ${sdcard_path_android}/WavefrontOBJs";
  callAdbShellCommandUntilSuccess 'mkdir -p '"${sdcard_path_android}"'/WavefrontOBJs/teapot';
  timeout 60 adb push -p app/src/androidTest/resources/teapot "${sdcard_path_android}/WavefrontOBJs";
  echo "Validating OBJ was copied to ${sdcard_path_android}/WavefrontOBJs/teapot/teapot.obj";
  callAdbShellCommandUntilSuccess 'ls -la '"${sdcard_path_android}"'/WavefrontOBJs/teapot/teapot.obj';

  # Remove the cross-process fail-fast marker so every gradle attempt runs the full
  # suite (AbstractTest writes it on failure; a stale marker would skip every test).
  set +e;
  timeout 60 adb shell rm "${sdcard_path_android}/.one_test_failed";
  set -e;

  if [ "${run_test}" = 'all' ]; then
    timeout 1800 sh gradlew "${gradle_command}" -DtestType="${type}" \
      -DandroidApiVersion="${android_api_version}" \
      -Pandroid.testInstrumentationRunnerArguments.package='puscas' \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  elif echo "${run_test}" | grep -q "rep_"; then
    # Single focused test: disable coverage (a one-test report fails with "no coverage data").
    timeout 200 sh gradlew connectedAndroidTest -DtestType="${type}" \
      -DandroidApiVersion="${android_api_version}" -DdisableTestCoverage=true \
      -Pandroid.testInstrumentationRunnerArguments.class="${run_test_without_prefix}" \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  else
    # Single focused test: disable coverage (see the rep_ branch above).
    timeout 200 sh gradlew connectedAndroidTest -DtestType="${type}" \
      -DandroidApiVersion="${android_api_version}" -DdisableTestCoverage=true \
      -Pandroid.testInstrumentationRunnerArguments.class="${run_test}" \
      -DabiFilters="[${cpu_architecture}]" \
      --console plain --parallel --info --warning-mode all --stacktrace;
  fi
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
  # adb shell needs ' instead of ", so 'getprop' works properly.
  callCommandUntilSuccess 70 timeout 60 adb shell 'ps > /dev/null';
  callCommandUntilSuccess 70 timeout 60 adb shell 'getprop ro.build.version.sdk';
  callCommandUntilSuccess 70 timeout 60 adb shell 'input keyevent 82';
  printf "sys.boot_completed: %s\n" "$(timeout 60 adb shell 'getprop sys.boot_completed')";
  printf "dev.bootcomplete: %s\n" "$(timeout 60 adb shell 'getprop dev.bootcomplete')";
  printf "service.bootanim.exit: %s\n" "$(timeout 60 adb shell 'getprop service.bootanim.exit')";
  androidApiDevice=$(timeout 60 adb shell getprop ro.build.version.sdk | tr -d '[:space:]');
  echo "androidApiDevice: '${androidApiDevice}'";

  # shellcheck disable=SC2016
  callAdbShellCommandUntilSuccess 'echo -n ::$(($(getprop sys.boot_completed)-1))::';
  # shellcheck disable=SC2016
  callAdbShellCommandUntilSuccess 'echo -n ::$(($(getprop dev.bootcomplete)-1))::';
  if [ "${androidApiDevice}" -gt 15 ] && [ "${androidApiDevice}" -lt 31 ]; then
    # shellcheck disable=SC2016
    callAdbShellCommandUntilSuccess 'echo -n ::$(($(getprop service.bootanim.exit)-1))::';
  fi
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
printCommandExitCode "${resUnitTests}" 'Unit tests';
printCommandExitCode "${resInstrumentationTests}" 'Instrumentation tests';
###############################################################################
###############################################################################
