###############################################################################
# Get arguments
###############################################################################
variant="${1:-release}"
ndk_version="${2:-21.0.6113669}"
cmake_version="${3:-3.6.0}"
###############################################################################
###############################################################################


###############################################################################
# Run tests
###############################################################################
adb logcat -c;
output="$(./gradlew connectedAndroidTest -DtestType="${variant}" \
  -DndkVersion="${ndk_version}" -DcmakeVersion="${cmake_version}")";
resInstrumentationTests=$?
adb logcat -v threadtime -d *:V > ./app/build/reports/logcat_${variant}.txt;
cat ./app/build/reports/logcat_${variant}.txt \
  | egrep -i `cat ./app/build/reports/logcat_${variant}.txt \
  | egrep -i "proc.*:puscas" | cut -d ":" -f 4 | cut -d ' ' -f 4` \
  > ./app/build/reports/logcat_app_${variant}.txt;
adb push app/build/intermediates/cmake/${variant}/obj/x86/ /data/RT_Unit_Tests;
adb shell ls -la /data/RT_Unit_Tests;
adb shell chmod 777 /data/RT_Unit_Tests/UnitTests;
adb shell ls -la /data/RT_Unit_Tests;
adb shell LD_LIBRARY_PATH=/data/RT_Unit_Tests/ /data/RT_Unit_Tests/UnitTests \
  2>&1 | tee ./app/build/reports/log_unit_tests_${variant}.txt;
resUnitTests=$?
echo -e "output = ${output}" > ./app/build/reports/log_tests_${variant}.txt
###############################################################################
###############################################################################


###############################################################################
# Exit code
###############################################################################
echo "########################################################################"
echo "Results:"
if [ $resUnitTests -eq 0 ]; then
  echo "Unit tests: success"
else
  echo "Unit tests: failed"
  exit $resUnitTests
fi

if [ $resInstrumentationTests -eq 0 ]; then
  echo "Instrumentation tests: success"
else
  echo "Instrumentation tests: failed"
  exit $resInstrumentationTests
fi
###############################################################################
###############################################################################
