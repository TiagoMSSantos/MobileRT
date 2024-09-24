#!/usr/bin/env sh

###############################################################################
# README
###############################################################################
# This script runs the Unit Tests of MobileRT in the native Operating System.
###############################################################################
###############################################################################


###############################################################################
# Exit immediately if a command exits with a non-zero status.
###############################################################################
set -eu;
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
android_api_version='14';
cpu_architecture='"x86","x86_64"';
parallelizeBuild;

printEnvironment() {
  echo '';
  echo 'Selected arguments:';
  echo "type: ${type}";
  echo "android_api_version: ${android_api_version}";
  echo "cpu_architecture: ${cpu_architecture}";
}
###############################################################################
###############################################################################


###############################################################################
# Parse arguments.
###############################################################################
parseArgumentsToCompileAndroid "$@";
printEnvironment;
###############################################################################
###############################################################################


###############################################################################
# Run unit tests natively.
###############################################################################

# Set path to reports.
reports_path=app/build/reports;
mkdir -p ${reports_path};

typeWithCapitalLetter=$(capitalizeFirstletter "${type}");
echo "type: '${type}'";

runUnitTests() {
  echo 'Calling Gradle test';
  echo 'Increasing ADB timeout to 10 minutes';
  export ADB_INSTALL_TIMEOUT=60000;
  sh gradlew --offline --parallel \
    -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]" \
    -no-rebuild --stop --info --warning-mode fail --stacktrace;
  sh gradlew --offline test"${type}"UnitTest --profile --parallel \
    -DtestType="${type}" -DandroidApiVersion="${android_api_version}" -DabiFilters="[${cpu_architecture}]" \
    --no-rebuild \
    --console plain --info --warning-mode all --stacktrace;
  resUnitTests=${?};
}
###############################################################################
###############################################################################

# Increase memory for heap.
export GRADLE_OPTS='-Xms4G -Xmx4G -XX:ActiveProcessorCount=4';
createReportsFolders;
runUnitTests;

unitTestsReportPath="${PWD}/${reports_path}/tests/test${typeWithCapitalLetter}UnitTest";
unitTestsReport='index.html';
checkPathExists "${unitTestsReportPath}" "${unitTestsReport}";

echo '';
printf '\e]8;;file://'"%s"'\aClick here to check the Unit tests report.\e]8;;\a\n' "${unitTestsReportPath}/${unitTestsReport}";
echo '';
echo '';

###############################################################################
# Exit code.
###############################################################################
printCommandExitCode "${resUnitTests}" 'Unit tests';
###############################################################################
###############################################################################
