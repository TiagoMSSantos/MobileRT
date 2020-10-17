###############################################################################
# Set base docker image
###############################################################################
ARG BASE_IMAGE=ubuntu:20.04
FROM ${BASE_IMAGE}
###############################################################################
###############################################################################


###############################################################################
# Setup arguments
###############################################################################
ARG BUILD_TYPE=Release
###############################################################################
###############################################################################


###############################################################################
# Change user to root
###############################################################################
USER root
###############################################################################
###############################################################################


###############################################################################
# Prepare environment
###############################################################################
ENV \
    # Create an environment variable to use it in the docker container when boot
    ENV_BUILD_TYPE=${BUILD_TYPE} \
    # Allow execution of Qt in docker
    DISPLAY=":1" \
    QT_GRAPHICSSYSTEM="native" \
    # Set lib path
    LD_LIBRARY_PATH="/MobileRT/libraries:/MobileRT/build_${BUILD_TYPE}/lib" \
    # Set apt-get as non interactive mode
    DEBIAN_FRONTEND=noninteractive
###############################################################################
###############################################################################


###############################################################################
# Print environment
###############################################################################
RUN echo "DISPLAY = ${DISPLAY}"; \
    echo "QT_GRAPHICSSYSTEM = ${QT_GRAPHICSSYSTEM}"; \
    echo "LD_LIBRARY_PATH = ${LD_LIBRARY_PATH}"; \
    echo "DEBIAN_FRONTEND = ${DEBIAN_FRONTEND}"; \
    echo "BUILD_TYPE = ${BUILD_TYPE}";
###############################################################################
###############################################################################


###############################################################################
# Install dependencies
###############################################################################
RUN \
# if Linux distribution is Debian based \
if [ -x "$(command -v apt-get)" ]; then \
  apt-get update -y; \
  apt-get install --no-install-recommends -y \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    git \
    libatomic1 \
    libomp-8-dev \
    qt5-default \
    build-essential; \
# if Linux distribution is Red Hat based \
elif [ -x "$(command -v yum)" ]; then \
  yum update -y; \
  yum install -y \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    git \
    which \
    qt5-qtbase-devel \
    gcc-c++; \
# if Linux distribution is Arch Linux based \
elif [ -x "$(command -v pacman)" ]; then \
  pacman -Sy --noconfirm; \
  pacman -S --noconfirm \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    git \
    which \
    qt5-base \
    gcc; \
# if Linux distribution is Alpine based \
elif [ -x "$(command -v apk)" ]; then \
  apk update; \
  apk add \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    git \
    qt5-qtbase-dev \
    which \
    g++ \
    gcc; \
fi
###############################################################################
###############################################################################


###############################################################################
# Get MobileRT contents (required to create docker image in CI)
###############################################################################
RUN git clone https://github.com/TiagoMSSantos/MobileRT.git
###############################################################################
###############################################################################


###############################################################################
# Compile MobileRT
###############################################################################
RUN cd MobileRT && \
    mkdir -p build_${BUILD_TYPE} && \
    cd build_${BUILD_TYPE} && \
    pwd && \
    cmake \
      -DCMAKE_VERBOSE_MAKEFILE=ON \
      -DCMAKE_CXX_COMPILER=g++ \
      -DCMAKE_BUILD_TYPE=${BUILD_TYPE} \
      ../app/ && \
    cmake --build . && \
    ls -laR bin && \
    ls -laR lib && \
    rm -rf ../app/ && \
    rm -rf ../docs/ && \
    rm -rf ../documentation/ && \
    rm -rf ../git-hooks/ && \
    rm -rf ../gradle/
###############################################################################
###############################################################################


###############################################################################
# Create volume (for Qt)
###############################################################################
VOLUME /tmp/.X11-unix
###############################################################################
###############################################################################


###############################################################################
# Execute MobileRT by default
###############################################################################
WORKDIR /MobileRT/build_${BUILD_TYPE}
CMD bash ../scripts/profile.sh ${ENV_BUILD_TYPE}
###############################################################################
###############################################################################
