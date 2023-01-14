# MobileRT: Mobile Ray Tracing engine
- A portable Ray Tracing (RT) engine for multiple devices.
- Already available interfaces for:
  - Android (through **Java** + **JNI** and **C**)
  - Linux, MacOS X, Windows (through **Qt** 4 or 5)
- Compatible with C++ compilers:
  - GNU C++ Compiler (g++)
  - Clang++
  - MinGW (g++)
  - Microsoft Visual C++ (MSVC)

[comment]: # (Licenses and documentation)
[![GitHub](https://img.shields.io/github/license/TiagoMSSantos/MobileRT)](https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/LICENSE)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FTiagoMSSantos%2FMobileRT.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FTiagoMSSantos%2FMobileRT?ref=badge_shield)
[![Known Vulnerabilities](https://snyk.io/test/github/TiagoMSSantos/MobileRT/badge.svg?targetFile=app/build.gradle)](https://snyk.io/test/github/TiagoMSSantos/MobileRT?targetFile=app/build.gradle)
[![Documentation](https://codedocs.xyz/TiagoMSSantos/MobileRT.svg)](https://codedocs.xyz/TiagoMSSantos/MobileRT/)
[![Gitbook documentation](https://badges.aleen42.com/src/gitbook_2.svg)](https://tiago-s.gitbook.io/mobilert/docs)

[comment]: # (Continuous Integration & Code coverage)
[![Unit Tests C++](https://github.com/TiagoMSSantos/MobileRT/workflows/Native%20(Qt)/badge.svg)](https://github.com/TiagoMSSantos/MobileRT/actions)
[![Tests Android](https://github.com/TiagoMSSantos/MobileRT/workflows/Android/badge.svg)](https://github.com/TiagoMSSantos/MobileRT/actions)
[![codecov](https://codecov.io/gh/TiagoMSSantos/MobileRT/branch/master/graph/badge.svg)](https://codecov.io/gh/TiagoMSSantos/MobileRT)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=TiagoMSSantos_MobileRT&metric=alert_status)](https://sonarcloud.io/dashboard?id=TiagoMSSantos_MobileRT)

<!-- 
[![Test Coverage](https://api.codeclimate.com/v1/badges/6a80c282c888f405d779/test_coverage)](https://codeclimate.com/github/TiagoMSSantos/MobileRT/test_coverage)
-->

[comment]: # (Static analysis)
[![Maintainability](https://api.codeclimate.com/v1/badges/6a80c282c888f405d779/maintainability)](https://codeclimate.com/github/TiagoMSSantos/MobileRT/maintainability)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/c4f4fb76bb934fe6980b969e7c8aac0e)](https://www.codacy.com/gh/TiagoMSSantos/MobileRT/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TiagoMSSantos/MobileRT&amp;utm_campaign=Badge_Grade)
[![Copy Paste Status](https://tiagomssantos.github.io/MobileRT/jscpd-report/jscpd-badge.svg)](https://tiagomssantos.github.io/MobileRT/jscpd-report/jscpd-report)
[![Docker Pulls](https://img.shields.io/docker/pulls/ptpuscas/mobile_rt)](https://hub.docker.com/r/ptpuscas/mobile_rt)

<!-- Code beat server fails sometimes analysing the pull requests.
[![codebeat badge](https://codebeat.co/assets/svg/badges/B-66bd63-dcc2e015f60fd0645631f8e7891440fb04fff8acc238aa755faf7de8c0ff7e2b.svg)](https://codebeat.co/projects/github-com-tiagomssantos-mobilert-master)
-->

<img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/Example_Android.gif" alt="MobileRT: Android" width="400"/>
<img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/Example_Linux.gif" alt="MobileRT: Linux" height="400"/>


## Run docker image
This C++ Ray Tracer is compatible with Android and Linux. <br/>

For Linux, if [docker](https://www.docker.com/) is installed, it is possible to
try this ray tracer with ease by using the following commands to get the docker
image and execute the container: <br/>
```
docker pull ptpuscas/mobile_rt
xhost +; docker run -it -v /tmp/.X11-unix:/tmp/.X11-unix -e DISPLAY=${DISPLAY} ptpuscas/mobile_rt
```
or
```
xhost +; docker-compose -f docker_image/docker-compose.yml up MobileRT
```
And a docker container should start and render the conference room model like
the image above :) <br/>

## Build docker image
For the most curious, this is the command used to build the docker image:
```
docker build -t ptpuscas/mobile_rt -f docker_image/Dockerfile --no-cache=false --build-arg BUILD_TYPE=release --build-arg BASE_IMAGE=ubuntu:20.04 .
```

The docker image is in docker hub:
[https://hub.docker.com/r/ptpuscas/mobile_rt](https://hub.docker.com/r/ptpuscas/mobile_rt).

## Compile Ray tracer
It is also possible to clone this repository and compile this ray tracer by
yourself.
To compile it, it is essential to install cmake and have a C++11 compiler.
It is also needed the [Qt4 or Qt5](https://www.qt.io/) library and the
[git](https://git-scm.com/) control system to get the code from the repository.
<br/>
```
sh scripts/install_dependencies.sh
```
Then, to finally compile this code, just create a build directory and compile
in it, like for example:
```
mkdir -p build_Release
cmake -DCMAKE_VERBOSE_MAKEFILE=ON -DCMAKE_CXX_COMPILER=g++ -DCMAKE_BUILD_TYPE=release ../app/
```

## Run Ray tracer
This ray tracer comes with a script with many functionalities useful to run
static code analyzers and to benchmark the ray tracer itself.
To execute the ray tracer just use the profile.sh script available in the
`scripts` directory.
For example, inside the build_Release directory (which should be inside the root
folder of this project) that contains all the object files compiled previously,
the following command should start the ray tracer: <br/>
```
../scripts/profile.sh Release
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


## Third party frameworks / libraries used
- [x] C++ [Boost libraries](https://www.boost.org/)
for the assertions
- [x] C++ [OpenGL Mathematics](https://glm.g-truc.net/0.9.9/)
library to help in the vector math
- [x] C++ [tinyobjloader](https://github.com/tinyobjloader/tinyobjloader)
library to load Wavefront OBJ model files
- [x] C++ [Qt4 or Qt5](https://www.qt.io/)
framework for Linux interface
- [x] C++ [Google Test](https://github.com/google/googletest)
framework for unit tests
- [x] C [STB libraries](https://github.com/nothings/stb)
librart to load the textures
- [x] Java [Streams](https://github.com/stefan-zobel/streamsupport)
to reduce complexity
- [x] Java [Google Guava](https://github.com/google/guava)
libraries to reduce complexity
- [x] Java [Apache Commons](https://commons.apache.org/)
framework to reduce complexity
- [x] Java [Project Lombok](https://projectlombok.org/)
library to use annotations
- [x] Java [JUnit4](https://junit.org/junit4/)
framework for unit tests
- [x] Java [AssertJ](https://assertj.github.io/doc/)
library for unit tests assertions
- [x] Java [Android Espresso](https://developer.android.com/training/testing/espresso)
library for instrumented tests

<!--
- [x] Java [Mockito](https://site.mockito.org/)
framework for mocking in unit tests
- [x] Java [Google Truth](https://truth.dev/)
library for assertions unit tests
-->

## Supported Operating Systems
<div ><!-- The extra space before closing the tag is necessary for the GitHub Flavored Markdown parser to detect HTML -->
  <table><!-- Note: Doxygen does not support class="tg" -->
    <!-- Note: Doxygen does not support thead nor tbody tags! -->
    <tr>
      <th colspan="1" style="text-align:center">OS</th>
      <th colspan="6" style="text-align:center">Versions</th>
    </tr>
    <tr>
      <td style="text-align:center"><b>Android</b></td>
      <td style="text-align:center">4.0 (API 14) <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">4.0.3 (API 15) <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
      <td style="text-align:center">4.1 (API 16) <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25"></td>
      <td style="text-align:center">4.4 (API 19) <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
      <td style="text-align:center">7.0 (API 24) <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
      <td style="text-align:center">10 (API 29) <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>MacOS</b></td>
      <td style="text-align:center">10.12 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">10.13 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">10.14 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">10.15 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25"></td>
      <td style="text-align:center">11.0 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
      <td style="text-align:center">12 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Windows</b></td>
      <td style="text-align:center">Server 2019 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
      <td style="text-align:center">Server 2022 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
      <td style="text-align:center">7<br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">8<br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">10<br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">11<br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Ubuntu</b></td>
      <td style="text-align:center">12.04 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">14.04 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">16.04 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25"></td>
      <td style="text-align:center">18.04 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25"></td>
      <td style="text-align:center">20.04 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
      <td style="text-align:center">22.04 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>CentOS</b></td>
      <td style="text-align:center">4 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">5 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">6 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">7 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">8 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25"></td>
      <td style="text-align:center">stream <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Alpine</b></td>
      <td style="text-align:center">3.12 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">3.13 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">3.14 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25"></td>
      <td style="text-align:center">3.15 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25"></td>
      <td style="text-align:center">3.16 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25"></td>
      <td style="text-align:center">3.17 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Arch Linux</b></td>
      <td style="text-align:center" colspan="6" align="center">base-devel <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Gentoo</b></td>
      <td style="text-align:center" colspan="6" align="center">stage3:x86 <br> <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25"></td>
    </tr>
  </table>
  Table: <br/>
  <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="15" height="15" style="text-align:center" align="left"> -> actively tested <br/>
  <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="15" height="15" align="left"> -> tested <br/>
  <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="15" height="15" align="left"> -> not tested yet <br/>
</div>

## Requirements
It's necessary the following SDKs in order to compile this project for Android:
- [Android SDK](https://developer.android.com/studio) which should also bring the [Android NDK](https://developer.android.com/ndk) in order to compile the native code.
  - It's recommended to use the Android Studio `2022.1.1` which is compatible with Gradle [7.4.0](https://github.com/TiagoMSSantos/MobileRT/blob/master/build.gradle#L38?) used by this project.

For native Linux and Mac support, the `install_dependencies.sh` script should download and install the necessary dependencies, by just calling:
```
  sh scripts/install_dependencies.sh
```
Note that the script already supports multiple Linux distributions like:
- Debian
- Red Hat
- Arch
- Alpine
- Gentoo

If the distribution you use is not supported, you can always open an issue or even a pull request :)

For native Windows support:
- [Microsoft Visual C++ (MSVC)](https://visualstudio.microsoft.com/)
- [Qt4 or Qt5](https://www.qt.io/)

For Linux in a [Docker](https://www.docker.com/) container:
  - [Docker Engine](https://docs.docker.com/engine/install/)

## Documentation
This project started as a [Masters' dissertation](https://repositorium.sdum.uminho.pt/handle/1822/66577). <br/>
Click [here](https://github.com/TiagoMSSantos/MobileRT/blob/master/docs/TODO.md?) to check the TODO list. <br/>
Click [here](https://github.com/TiagoMSSantos/MobileRT/blob/master/docs/TOOLS.md?) to check the code coverage and code duplication commands. <br/>
Click [here](https://codedocs.xyz/TiagoMSSantos/MobileRT/) to check the Doxygen codebase documentation. <br/>
