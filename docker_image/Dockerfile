# Dependency docker image
FROM ubuntu:18.04

# Install dependencies
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends \
    vim \
    libatomic1 \
    libqt4-dev
#    build-essential \
#    cmake \
#    ca-certificates \
#    git \
#    g++

# Copy contents
COPY . ./MobileRT

# Variables
ARG build_type=Release

# Prepare environment
ENV DISPLAY=":1" \
    QT_GRAPHICSSYSTEM="native" \
    LD_LIBRARY_PATH="/MobileRT/libraries:/MobileRT/build_${build_type}/lib" \
    BUILD_TYPE="${build_type}"

# Compile Ray Tracer
#RUN cd MobileRT && \
#    mv WavefrontOBJs ../
#    mkdir -p build_${build_type} && \
#    cd build_${build_type} && \
#    cmake \
#      -DCMAKE_VERBOSE_MAKEFILE=ON \
#      -DCMAKE_CXX_COMPILER=clang++ \
#      -DCMAKE_BUILD_TYPE=${build_type} \
#      ../app/ && \
#    make && \
#    find ./*/* -not -path bin -not -path lib ! -name AppMobileRT ! -name *.so -delete

# Execute
WORKDIR /MobileRT/build_${build_type}
CMD bash ../Scripts/profile.sh ${BUILD_TYPE}
