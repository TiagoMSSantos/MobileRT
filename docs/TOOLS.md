# Tools commands

## Code Coverage
Here are the commands to generate the code coverage report:
```bash
find build_debug/* -name *.gcda | xargs rm
cd build_debug
cmake -DCMAKE_VERBOSE_MAKEFILE=ON -DCMAKE_CXX_COMPILER=g++ -DCMAKE_BUILD_TYPE=debug ../app/
make
cd ..
lcov -c -i -d . --no-external -o code_coverage_base.info
./build_debug/bin/UnitTestsd
lcov -c -d . --no-external -o code_coverage_test.info
lcov -a code_coverage_base.info -a code_coverage_test.info -o code_coverage.info
lcov --remove code_coverage.info -o code_coverage.info '*third_party*' '*build*'
genhtml code_coverage.info -o code_coverage_report --no-branch-coverage -t MobileRT_code_coverage
bash <(curl -s https://codecov.io/bash)
./test-reporter-latest-linux-amd64 format-coverage -t lcov code_coverage.info
./test-reporter-latest-linux-amd64 upload-coverage
```

## Code Duplication
Here are the commands to generate the code duplication report:
```bash
jscpd -c .jscpd.json .
```

## Check dependency updates
Here are the commands to generate the dependency updates report in `app/report.html`:
```bash
bash gradlew dependencyUpdates -Drevision=release -DoutputFormatter=html -DoutputDir=.
```
And the commands to generate the dependency report in `app/build/reports/project/dependencies/root.app.html`:
```bash
bash gradlew htmlDependencyReport
```

## Delete Workflow runs
Here are the commands to delete the workflow runs from all branches except the master.
```bash
user=TiagoMSSantos repo=MobileRT; gh api repos/$user/$repo/actions/runs --paginate -q '.workflow_runs[] | select(.head_branch != "master") | "\(.id)"' | xargs -n1 -I % gh api repos/$user/$repo/actions/runs/% -X DELETE
```
