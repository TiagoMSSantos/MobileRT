# MobileRT: Mobile Ray Tracing engine

- A portable Ray Tracing (RT) engine for multiple devices.
- Already available interfaces for:
  - Android (through **Java** + **JNI** and **C**)
  - Linux, MacOS, Windows (through **Qt** 4 or 5)
- Compatible with C++ compilers:
  - GNU C++ Compiler (g++)
  - Clang++
  - MinGW (g++)
  - Microsoft Visual C++ (MSVC)
  - Intel® oneAPI DPC++/C++ Compiler (ICX)

[//]: # (Licenses and documentation)
[![GitHub](https://img.shields.io/github/license/TiagoMSSantos/MobileRT)](https://github.com/TiagoMSSantos/MobileRT/blob/master/LICENSE.md)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FTiagoMSSantos%2FMobileRT.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FTiagoMSSantos%2FMobileRT?ref=badge_shield)
[![Known Vulnerabilities](https://snyk.io/test/github/TiagoMSSantos/MobileRT/badge.svg?targetFile=app/build.gradle)](https://snyk.io/test/github/TiagoMSSantos/MobileRT?targetFile=app/build.gradle)
[![Documentation](https://codedocs.xyz/TiagoMSSantos/MobileRT.svg)](https://codedocs.xyz/TiagoMSSantos/MobileRT/)
[![Gitbook documentation](https://badges.aleen42.com/src/gitbook_2.svg)](https://tiago-s.gitbook.io/mobilert/docs)

[//]: # (Continuous Integration & Code coverage)
[![Unit Tests C++](https://github.com/TiagoMSSantos/MobileRT/actions/workflows/native.yml/badge.svg?branch=master)](https://github.com/TiagoMSSantos/MobileRT/actions)
[![Tests Android](https://github.com/TiagoMSSantos/MobileRT/actions/workflows/android.yml/badge.svg?branch=master)](https://github.com/TiagoMSSantos/MobileRT/actions)
[![codecov](https://codecov.io/gh/TiagoMSSantos/MobileRT/branch/master/graph/badge.svg)](https://app.codecov.io/gh/TiagoMSSantos/MobileRT/tree/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=TiagoMSSantos_MobileRT&metric=alert_status)](https://sonarcloud.io/dashboard?id=TiagoMSSantos_MobileRT)

[//]: # (Static analysis)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/c4f4fb76bb934fe6980b969e7c8aac0e)](https://app.codacy.com/gh/TiagoMSSantos/MobileRT/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Copy Paste Status](https://tiagomssantos.github.io/MobileRT/jscpd-report/jscpd-badge.svg)](https://tiagomssantos.github.io/MobileRT/jscpd-report/jscpd-report)
[![Docker Pulls](https://img.shields.io/docker/pulls/ptpuscas/mobile_rt)](https://hub.docker.com/r/ptpuscas/mobile_rt)

<!-- The images have the full path to the repository so they also appear in Docker hub. -->
<img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/Example_Android.gif" alt="MobileRT: Android" width="400"/>
<img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/Example_Linux.gif" alt="MobileRT: Linux" height="400"/>

## Run docker image

This C++ Ray Tracer is compatible with Android and Linux.

For Linux, if [docker](https://www.docker.com/) is installed, it is possible to
try this ray tracer with ease by using the following commands to get the docker
image and execute the container:

```shell
docker pull ptpuscas/mobile_rt:ubuntu-24.04;
xhost +; docker run -it -v /tmp/.X11-unix:/tmp/.X11-unix -e DISPLAY=${DISPLAY} ptpuscas/mobile_rt:ubuntu-24.04;
```

or (still WIP)

```shell
xhost +; docker-compose -f deploy/docker-compose.yml up MobileRT;
```

And a docker container should start and render the conference room model like
the image above :)

## Build docker image

For the most curious, this is the command used to build the docker image:

```shell
docker build -t ptpuscas/mobile_rt:ubuntu-24.04 -f deploy/Dockerfile.unix --no-cache=false --build-arg BUILD_TYPE=release --build-arg BASE_IMAGE=ubuntu:24.04 .;
```

The docker image is in docker hub:
[https://hub.docker.com/r/ptpuscas/mobile_rt](https://hub.docker.com/r/ptpuscas/mobile_rt).

## Compile Ray tracer

It is also possible to clone this repository and compile this ray tracer by
yourself.
To compile it, it is essential to install cmake and have a C++20 compiler.
It is also needed the [Qt4 or Qt5](https://www.qt.io/) library and the
[git](https://git-scm.com/) control system to get the code from the repository.

```shell
sh scripts/install_dependencies.sh;
```

Then, to finally compile this code for the native machine, just execute the following command:

```shell
sh scripts/compile_native.sh -c g++ -t release -r yes;
```

## Run Ray tracer

This ray tracer comes with a script with many functionalities useful to run
static code analyzers and to benchmark the ray tracer itself.
To execute the ray tracer just use the `profile.sh` shell script available in the
`scripts` directory.
The following command should start the ray tracer when executed in the root directory:

```shell
sh scripts/profile.sh release;
```

## Android

To try this ray tracer for Android just download the
[APK](https://github.com/TiagoMSSantos/MobileRT/blob/master/app/release/app-release.apk?raw=true)
file available in the repository.

Or you can compile yourself for Android as well.
First, install all the necessary dependencies:

```shell
sh scripts/install_dependencies.sh;
sh gradlew build -DandroidApiVersion='<android_api>' -DabiFilters='["<cpu_architecture>"]' --dry-run -Dorg.gradle.configuration-cache=true --parallel --info --warning-mode all --stacktrace;
```

Then, to finally compile this code for Android, execute the following command:

```shell
sh scripts/compile_android.sh -c g++ -t release -r yes -a <android_api> -f \"<cpu_architecture>\";
```

## Models Wavefront OBJ

To get some OBJ models, just download some from here:
[OBJs](https://casual-effects.com/data/).
Then, it will just be needed to add some lights in the scene geometry, by using
some modeling application like [3D Blender](https://www.blender.org/).
One thing to have it in account is to make sure the light material has the
light emission component (Ke) with some positive values in the ".mtl" file.
Finally, add a camera file with the extension ".cam" that should contain a
definition of a perspective camera, like for example:

```text
t perspective #type of the camera
p 0 0 0 #position of the camera x y z
l 0 0 1 #look at of the camera x y z
u 0 1 0 #up vector of the camera x y z
f 45 45 #field of view of the camera u v
```

## Third party frameworks / libraries used

- [x] C++ [Boost libraries](https://www.boost.org/)
for the assertions
- [x] C++ [OpenGL Mathematics](https://github.com/g-truc/glm)
library to help in the vector math
- [x] C++ [tinyobjloader](https://github.com/tinyobjloader/tinyobjloader)
library to load Wavefront OBJ model files
- [x] C++ [Qt4 or Qt5](https://www.qt.io/)
framework for Linux interface
- [x] C++ [Google Test](https://github.com/google/googletest)
framework for unit tests
- [x] C [STB libraries](https://github.com/nothings/stb)
library to load the textures
- [x] Java [Streams](https://github.com/stefan-zobel/streamsupport)
to reduce complexity
- [x] Java [Google Guava](https://github.com/google/guava)
framework to reduce complexity
- [x] Java [JUnit4](https://junit.org/junit4/)
framework for unit tests
- [x] Java [AssertJ](https://assertj.github.io/doc/)
library for unit tests assertions
- [x] Java [Android Espresso](https://developer.android.com/training/testing/espresso)
library for instrumented tests
- [x] Java [EasyMock](https://easymock.org/)
framework for mocking in unit tests
- [x] Java [PowerMock for EasyMock](https://github.com/powermock/powermock/wiki/EasyMock)
  framework to improve mocking in unit tests since it allows mocking `final` classes, `native` and `static` methods.

## Supported Operating Systems

<!-- The images have the full path to the repository so they also appear in Docker hub. -->
<div ><!-- The extra space before closing the tag is needed for the GitHub Flavored Markdown parser to detect HTML -->
  <table><!-- Note: Doxygen does not support class="tg" -->
    <!-- Note: Doxygen does not support thead nor tbody tags! -->
    <tr>
      <th colspan="1" style="text-align:center">OS</th>
      <th colspan="5" style="text-align:center">Versions</th>
    </tr>
    <tr>
      <td style="text-align:center"><b>Android</b></td>
      <td style="text-align:center">4.0.3<br>(API 15)<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
      <td style="text-align:center">4.4W<br>(API 20)<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25" alt="red"></td>
      <td style="text-align:center">5.0<br>(API 21)<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
      <td style="text-align:center">10<br>(API 29)<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
      <td style="text-align:center">15<br>(API 35)<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>MacOS</b></td>
      <td style="text-align:center">11<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">12<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">13<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
      <td style="text-align:center">14<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
      <td style="text-align:center">15<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Windows</b></td>
      <td style="text-align:center">Server 2019<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">Server 2022<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
      <td style="text-align:center">Server 2025<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
      <td style="text-align:center">10<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25" alt="red"></td>
      <td style="text-align:center">11<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Ubuntu</b></td>
      <td style="text-align:center">16.04<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">18.04<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">20.04<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">22.04<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
      <td style="text-align:center">24.04<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>CentOS</b></td>
      <td style="text-align:center">5<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25" alt="red"></td>
      <td style="text-align:center">6<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25" alt="red"></td>
      <td style="text-align:center">7<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="25" height="25" alt="red"></td>
      <td style="text-align:center">8<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">stream<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Alpine</b></td>
      <td style="text-align:center">3.18<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">3.19<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">3.20<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">3.21<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="25" height="25" alt="gray"></td>
      <td style="text-align:center">3.22<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Arch Linux</b></td>
      <td style="text-align:center" colspan="5" align="center">base-devel<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
    </tr>
    <tr>
      <td style="text-align:center"><b>Gentoo</b></td>
      <td style="text-align:center" colspan="5" align="center">stage3:x86<br><img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="25" height="25" alt="green"></td>
    </tr>
  </table>
  Table: <br/>
  <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_green.png" width="15" height="15" style="text-align:center" align="left" alt="green"> -> actively tested <br/>
  <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/checkmark_gray.png" width="15" height="15" align="left" alt="gray"> -> tested <br/>
  <img src="https://raw.githubusercontent.com/TiagoMSSantos/MobileRT/master/docs/cross_red.png" width="15" height="15" align="left" alt="red"> -> not tested <br/>
</div>

## Requirements

It's necessary the following SDKs in order to compile this project for Android:

- [Android SDK](https://developer.android.com/studio) which should also bring the [Android NDK](https://developer.android.com/ndk) in order to compile the native code.

For native **Linux**, **MacOS** and **Windows** support, the `install_dependencies.sh` script should download and install the necessary dependencies, by just calling:

```shell
sh scripts/install_dependencies.sh;
```

Note that the script already supports multiple **Linux** distributions like:

- Debian (using apt)
- Red Hat (using yum)
- Arch (using pacman)
- Alpine (using apk)
- Gentoo (using emerge)

It also supports installing dependencies in the following Operating Systems:

- MacOS (using Homebrew)
- Windows (using Chocolatey)

Note that these scripts are tested in Github actions pipeline only, so it might assume that some tools are already installed in the system.
If the OS you use is not supported, or it's missing the installation of some tool, you can always open an issue or even a pull request.

## Documentation

<!-- The documentation have the full path to the repository so the URL also work in Docker hub. -->
Click [here](https://github.com/TiagoMSSantos/MobileRT/blob/master/docs/FEATURES.md) to check the features list that are supported.  
Click [here](https://github.com/TiagoMSSantos/MobileRT/blob/master/docs/TOOLS.md) to check the code coverage and code duplication commands.  
Click [here](https://github.com/TiagoMSSantos/MobileRT/blob/master/docs/BUILD_DOCS.md) to build and serve the documentation locally.  
Click [here](https://github.com/TiagoMSSantos/MobileRT/blob/master/docs/BLENDER.md) for some basic Blender tips.  
Click [here](https://codedocs.xyz/TiagoMSSantos/MobileRT/) to check the Doxygen codebase documentation.  
