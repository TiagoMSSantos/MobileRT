# Tools commands

## Check code coverage
Here are the commands to generate the code coverage report:
```bash
find build_debug/* -name *.gcda | xargs rm;
pushd build_debug;
cmake -DCMAKE_VERBOSE_MAKEFILE=ON -DCMAKE_CXX_COMPILER=g++ -DCMAKE_BUILD_TYPE=debug ../app/;
cmake --build .;
popd;
lcov -c -i -d . --no-external -o code_coverage_base.info;
./build_debug/bin/UnitTestsd;
lcov -c -d . --no-external -o code_coverage_test.info;
lcov -a code_coverage_base.info -a code_coverage_test.info -o code_coverage.info;
lcov --remove code_coverage.info -o code_coverage.info '*third_party*' '*build*';
genhtml code_coverage.info -o code_coverage_report --no-branch-coverage -t MobileRT_code_coverage;
bash <(curl -s https://codecov.io/bash);
./test-reporter-latest-linux-amd64 format-coverage -t lcov code_coverage.info;
./test-reporter-latest-linux-amd64 upload-coverage;
```

## Check code duplication
Here are the commands to generate the code duplication report:
```bash
jscpd -c .jscpd.json .;
```

## Check dependency updates
Here are the commands to generate the dependency updates report in `app/report.html`:
```bash
bash gradlew dependencyUpdates -Drevision=release -DoutputFormatter=html -DoutputDir=.;
```
And the commands to generate the dependency report in `app/build/reports/project/dependencies/root.app.html`:
```bash
bash gradlew htmlDependencyReport;
```

## Delete Workflow runs
Here are the commands to delete the workflow runs from all branches except the master.
```bash
user=TiagoMSSantos repo=MobileRT; gh api repos/${user}/${repo}/actions/runs --paginate -q '.workflow_runs[] | select(.head_branch != "master") | "\(.id)"' | xargs -n1 -I % gh api repos/${user}/${repo}/actions/runs/% -X DELETE;
```

## Delete local branches which are not in remote
Here are the commands to delete the git local branches that are not on the remote server.
```
git branch -vv | grep ': gone]' | awk '{print $1}' | xargs git branch -D
```

## Remove all docker containers and volumes
Here are the commands to remove all docker containers and mounted volumes.
```
docker system df;
docker rm -f $(docker ps -a | grep -v "portainer" | awk 'NR>1 {print $1}');
docker system prune --volumes --force;
docker builder prune --all --force;
docker buildx prune --all --force --verbose;
docker network prune --force;
docker volume prune --force;
docker system df;
```

## Delete cache from conan package manager
Here are the commands to delete all the cache from the conan package manager.
```
conan remove "*" --builds --src --system-reqs --force;
```
