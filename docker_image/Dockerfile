# Dependency docker image
FROM ubuntu:18.04

# Install dependencies
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends \
    vim \
    libatomic1 \
    libomp-8-dev \
    libqt4-dev \
    cmake \
    build-essential \
    git \
    ca-certificates

# Get MobileRT contents
RUN git clone https://github.com/TiagoMSSantos/MobileRT.git

# Variables
ARG build_type=Release

# Prepare environment
ENV DISPLAY=":1" \
    QT_GRAPHICSSYSTEM="native" \
    LD_LIBRARY_PATH="/MobileRT/libraries:/MobileRT/build_${build_type}/lib" \
    BUILD_TYPE="${build_type}"

# Compile Ray Tracer
RUN cd MobileRT && \
    mkdir -p build_${build_type} && \
    cd build_${build_type} && \
    pwd && \
    cmake \
      -DCMAKE_VERBOSE_MAKEFILE=ON \
      -DCMAKE_CXX_COMPILER=g++ \
      -DCMAKE_BUILD_TYPE=${build_type} \
      ../app/ && \
    make && \
    find ./*/* -not -path bin -not -path lib ! -name AppMobileRT ! -name *.so -delete && \
    rm -rf ../app/ && \
    rm -rf ../docs/ && \
    rm -rf ../documentation/ && \
    rm -rf ../git-hooks/ && \
    rm -rf ../gradle/

# Create volume
VOLUME /tmp/.X11-unix

# Execute
WORKDIR /MobileRT/build_${build_type}
CMD bash ../scripts/profile.sh ${BUILD_TYPE}
