# Tools commands

## Code Coverage
Here are the commands to generate the code coverage report:
```bash
find build_Debug_gcc/* -name *.gcda | xargs rm
cd build_Debug_gcc
cmake -DCMAKE_VERBOSE_MAKEFILE=ON -DCMAKE_CXX_COMPILER=g++ -DCMAKE_BUILD_TYPE=Debug ../app/
make
cd ..
lcov -c -i -d . --no-external -o code_coverage_base.info
./build_Debug_gcc/bin/UnitTestsd
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
