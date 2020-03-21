# MobileRT: Mobile Ray Tracing engine <br/>
- A portable Ray Tracing (RT) engine for multiple devices <br/>
- Already available interfaces for Android and Linux <br/>

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/LICENSE)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FTiagoMSSantos%2FMobileRT.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FTiagoMSSantos%2FMobileRT?ref=badge_shield)
[![BCH compliance](https://bettercodehub.com/edge/badge/TiagoMSSantos/MobileRT?branch=master)](https://bettercodehub.com/results/TiagoMSSantos/MobileRT)
[![Documentation](https://codedocs.xyz/TiagoMSSantos/MobileRT.svg)](https://codedocs.xyz/TiagoMSSantos/MobileRT/)
[![Build Status](https://travis-ci.com/TiagoMSSantos/MobileRT.svg?branch=master)](https://travis-ci.com/TiagoMSSantos/MobileRT)
[![codecov](https://codecov.io/gh/TiagoMSSantos/MobileRT/branch/master/graph/badge.svg)](https://codecov.io/gh/TiagoMSSantos/MobileRT)
[![Copy Paste Status](https://tiagomssantos.github.io/MobileRT/jscpd-report/jscpd-badge.svg)](https://tiagomssantos.github.io/MobileRT/jscpd-report/jscpd-report)

<img src="Example.gif" alt="RayTracer" width="400"/>

## Run docker image
This C++ Ray Tracer is compatible with Android and Linux. <br/>

For Linux, if [docker](https://www.docker.com/) is installed, it is possible to
try this ray tracer with ease by using the following commands to get the docker
image and execute the container: <br/>
```bash
docker pull ptpuscas/mobile_rt
xhost +; docker run -v /tmp/.X11-unix:/tmp/.X11-unix -e DISPLAY=${DISPLAY} -it ptpuscas/mobile_rt
```
And a docker container should start and render the conference room model like
the image above :) <br/>

## Build docker image
For the most curious, this is the command used to build the docker image:
```bash
docker build -t ptpuscas/mobile_rt -f docker_image/Dockerfile --no-cache=false --build-arg build_type=Release .
```

## Compile Ray tracer
It is also possible to clone this repository and compile this ray tracer by
yourself.
To compile it, it is essential to install cmake and have a C++ compiler.
It is also needed the [Qt4](https://www.qt.io/) library and the
[git](https://git-scm.com/) control system to get the code from the repository.
<br/>
```bash
sudo apt-get update
sudo apt-get install cmake libqt4-dev build-essential ca-certificates git g++
```
Then, to finally compile this code, just create a build directory and compile
in it, like for example:
```bash
mkdir build_Release
cmake -DCMAKE_VERBOSE_MAKEFILE=ON -DCMAKE_CXX_COMPILER=g++ -DCMAKE_BUILD_TYPE=Release ../app/
```

## Run Ray tracer
This ray tracer comes with a script with many functionalities useful to run
static code analyzers and to benchmark the ray tracer itself.
To execute the ray tracer just use the profile.sh script available in the
Scripts directory.
For example, inside the build_Release directory (which should be inside the root
folder of this project) that contains all the object files compiled previously,
the following command should start the ray tracer: <br/>
```bash
../Scripts/profile.sh Release
```

## Android
To try this ray tracer for Android just download the
[APK](https://github.com/TiagoMSSantos/MobileRT/blob/master/app/release/app-release.apk?raw=true)
file available in the repository.

## Models Wavefront OBJ
To get some OBJ models, just download some from here:
[OBJs](https://casual-effects.com/data/).
Then, it will just be needed to add some lights in the scene geometry, by using
some modeling application like [3D Blender](https://www.blender.org/).
One thing to have it in account is to make sure the light material has the
light emission component (Ke) with some positive values in the ".mtl" file.
Finally, add a camera file with the extension ".cam" that should contain a
definition of a perspective camera, like for example:
```
t perspective #type of the camera
p 0 0 0 #position of the camera x y z
l 0 0 1 #look at of the camera x y z
u 0 1 0 #up vector of the camera x y z
f 45 45 #field of view of the camera u v
```

## TODO

### Ray tracing engine
- [x] Support load of textures
- [ ] Support for textures with different materials
- [x] Support acceleration structures
- [x] Split Material from Primitive in order to save memory
- [x] Make all acceleration structures as templates (to let compiler generate
better optimized code)
- [ ] Support ray packet intersections
- [ ] Support acceleration structures with ray packet intersections
- [ ] Support GPU ray tracing (to compare with CPU)
- [x] Split naive acceleration structure from Scene
- [ ] Support KD-Tree
- [ ] Support acceleration structures compatible with the lights
- [x] Support triangles with normals per vertex
- [ ] Parallelize build of Regular Grid
- [ ] Parallelize build of BVH

### Ray tracing shaders
- [ ] Fix refractions
- [ ] Fix Path Tracing algorithm
- [ ] Improve shaders performance
- [ ] Add Bidirectional Path Tracing
- [ ] Add Metropolis light transport
- [ ] Support shader for debug purposes (wireframe of shapes and boxes)

### Ray tracing JNI layer
- [ ] Refactor DrawView translation unit
- [ ] Improve DrawView code readability
- [ ] Fix race conditions

### Android Interface
- [x] Fix memory leak in Java UI
- [x] Fix load of obj files in Android 10
- [ ] Improve Java UI code to more Object Oriented
- [ ] Change Android icon
- [x] Remove usage of deprecated methods
- [x] Add Android instrumented unit tests
- [ ] Make Android instrumented tests run on debug and release
- [ ] Make all Android instrumented tests pass without flakiness

### Linux Interface
- [x] Support Linux's UI with Qt
- [x] Support options menu in GUI
- [x] Support about menu in GUI
- [x] Support for selection of OBJ files in GUI
- [ ] Support selection of all ray tracer options in GUI

### Building process
- [x] Support compiler warnings for clang / g++ in CMake
- [x] Support compiler warnings for Java
- [x] Support warnings for Gradle
- [x] Support rules with optimizations for proguard
- [x] Support C++ compilation with optimization flags
- [ ] Support CMake jobs to improve building time
- [x] Support CMake to clone the third party repositories
- [x] Support CMake to pull new versions of third party repositories
- [x] Split the Android application in 3 layers:
    - [x] MobileRT (ray tracing engine)
    - [x] Rendering components (cameras, lights, loaders, samplers, shaders)
    - [x] UI (Android through JNI, Linux through Qt)

### Third party frameworks / libraries used
- [x] C++ [Boost libraries](https://www.boost.org/)
- [x] C++ [OpenGL Mathematics](https://glm.g-truc.net/0.9.9/index.html)
library
- [x] C++ [tinyobjloader](https://github.com/tinyobjloader/tinyobjloader)
library
- [x] C [STB libraries](https://github.com/nothings/stb)
- [x] C++ [Google Test](https://github.com/google/googletest) framework
for unit tests
- [x] Java [Google Guava](https://github.com/google/guava) libraries
- [x] Java [streams](https://github.com/stefan-zobel/streamsupport) to
reduce complexity
- [x] Java [Apache Commons](https://commons.apache.org/) framework
- [x] Java [JUnit4](https://junit.org/junit4/) framework for
unit tests
- [x] Java [AssertJ](https://assertj.github.io/doc/) library for
unit tests assertions
- [x] Java [Mockito](https://site.mockito.org/) framework for
mocking in unit tests
- [x] Java Google [Truth](https://truth.dev/) library for
unit tests 
- [x] Java Android [Espresso](https://developer.android.com/training/testing/espresso)
library for instrumented tests

### System
- [x] Support doxygen documentation in the MobileRT
- [ ] Support doxygen documentation in the Components
- [ ] Support doxygen documentation in the JNI layer
- [x] Support javadoc in the Android UI
- [ ] Support unit tests (code coverage)
- [x] Support git hooks to check git commit messages
- [x] Support git hooks to submit Jenkins' jobs after each git push
- [ ] Support to export rendered image to a file
- [x] Support CI / CD from github (actions) for the unit tests
- [x] Support for a tool to detect duplicated code (jscpd)
- [ ] Remove MobileRT duplicated code
- [ ] Remove Components duplicated code
- [ ] Remove Android JNI duplicated code
- [ ] Remove Android UI duplicated code
- [ ] Remove Qt duplicated code
- [ ] Remove Java tests duplicated code
- [ ] Remove C++ tests duplicated code
- [x] Support compiler exceptions to let the user know gracefully that the
system does not have enough memory to render the scene
- [ ] Optimize load of scenes from files
- [x] Add message reasons to all assertions
- [x] Load lights and cameras from files
- [ ] Prepare more scene models

### Docker
- [x] Make a docker image with MobileRT
- [x] Add an example model to the docker image
- [ ] Make the ray tracer distribute the load across different engines
- [ ] Use docker compose to launch multiple containers and distribute the load

### Documentation
- [x] Support backlog in README
- [x] Support code documentation
- [x] Update gif image
- [ ] Benchmark against popular ray tracers like PBRT
- [ ] Benchmark against previous version of MobileRT

### Code Coverage
Here are the commands to generate the code coverage report:
```bash
rm -rf build_Debug/*
cd build_Debug
cmake -DCMAKE_VERBOSE_MAKEFILE=ON -DCMAKE_CXX_COMPILER=g++ -DCMAKE_BUILD_TYPE=Debug ../app/
make
cd ..
lcov -c -i -d . --no-external -o code_coverage_base.info
./build_Debug/bin/UnitTestsd
lcov -c -d . --no-external -o code_coverage_test.info
lcov -a code_coverage_base.info -a code_coverage_test.info -o code_coverage.info
lcov --remove code_coverage.info -o code_coverage.info '*third_party*' '*build*'
genhtml code_coverage.info -o code_coverage_report --no-branch-coverage -t MobileRT_code_coverage
bash <(curl -s https://codecov.io/bash) -t 717e75e2-b149-4997-adb4-a3fa1bde237f
```

### Code Duplication
Here are the commands to generate the code duplication report:
```bash
jscpd -c .jscpd.json .
```
