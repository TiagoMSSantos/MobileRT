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
ARG BUILD_TYPE=release
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
if [ -x "$(command -v apt-get)" ]; then \
  echo "Detected Debian based Linux"; \
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
    g++ \
    build-essential; \
elif [ -x "$(command -v yum)" ]; then \
  echo "Detected Red Hat based Linux"; \
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
    python36 \
    gcc-c++; \
elif [ -e "$(command -v pacman)" ]; then \
  echo "Detected Arch based Linux"; \
  \
  # Fix the following error:
  # error: failed to initialize alpm library
  # (could not find or read directory: /var/lib/pacman/)
  patched_glibc=glibc-linux4-2.33-4-x86_64.pkg.tar.zst && \
  curl -LO "https://repo.archlinuxcn.org/x86_64/${patched_glibc}" && \
  bsdtar -C / -xvf "${patched_glibc}"; \
  \
  pacman -Sy --noconfirm --needed; \
  pacman -Sy --noconfirm --needed \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    git \
    which \
    qt5-base \
    python3 \
    gcc; \
elif [ -x "$(command -v apk)" ]; then \
  echo "Detected Alpine based Linux"; \
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
    py3-pip \
    gcc; \
elif [ -x "$(command -v emerge)" ]; then \
  echo "Detected Gentoo based Linux"; \
  emerge --sync; \
  emerge sys-apps/portage; \
  emerge app-portage/layman; \
  emerge dev-libs/icu; \
  layman -a qt; \
  \
  # Fix the error when installing Qt:
  #
  # The following USE changes are necessary to proceed:
  # (see "package.use" in the portage(5) man page for more details)
  # required by dev-qt/qtgui-5.15.2-r1::gentoo[X]
  # required by dev-qt/qtwidgets-5.15.2::gentoo
  # >=x11-libs/libxkbcommon-1.0.3 X
  # required by dev-qt/qtcore-5.15.2-r2::gentoo
  # required by dev-qt/qtgui-5.15.2-r1::gentoo
  # required by dev-qt/qtwidgets-5.15.2::gentoo
  # >=dev-libs/libpcre2-10.35 pcre16
  # required by media-libs/mesa-20.2.6::gentoo
  # required by virtual/opengl-7.0-r2::gentoo
  # required by dev-qt/qtgui-5.15.2-r1::gentoo[-gles2-only]
  # required by dev-qt/qtwidgets-5.15.2::gentoo
  # >=media-libs/libglvnd-1.3.2-r2 X
  echo 'FEATURES="-sandbox -usersandbox"' >> /etc/portage/make.conf; \
  echo 'USE="dev-libs/libpcre2-10.35 pcre16 x11-libs/libxkbcommon-1.0.3 media-libs/libglvnd-1.3.2-r2 X"' >> /etc/portage/make.conf; \
  \
  emerge \
    vim \
    findutils \
    cmake \
    make \
    bash \
    ca-certificates \
    dev-vcs/git \
    which \
    sys-devel/gcc \
    dev-qt/qtcore \
    dev-qt/qtgui \
    dev-qt/qtwidgets; \
else \
  echo "Detected unknown based Linux"; \
fi
###############################################################################
###############################################################################


###############################################################################
# Configure git
###############################################################################
RUN git config --global http.postBuffer 524288000; \
    git config --global http.sslVerify "false";
###############################################################################
###############################################################################


###############################################################################
# Get MobileRT contents (required to create docker image in CI)
###############################################################################
RUN git clone https://github.com/TiagoMSSantos/MobileRT.git --branch master;

# Or manually copy MobileRT
#COPY . /MobileRT
###############################################################################
###############################################################################


###############################################################################
# Install Conan dependencies
###############################################################################
WORKDIR /MobileRT
RUN bash scripts/install_dependencies.sh;
###############################################################################
###############################################################################


###############################################################################
# Compile MobileRT
###############################################################################
RUN bash scripts/compile_native.sh ${BUILD_TYPE} g++ yes && \
    ls -laR build_${BUILD_TYPE}/bin && \
    ls -laR build_${BUILD_TYPE}/lib && \
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
