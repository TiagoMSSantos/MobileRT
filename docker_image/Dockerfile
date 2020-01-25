# Dependency docker image
FROM ubuntu:18.04

# Install dependencies
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends \
    cmake \
    ca-certificates \
    git \
    g++ \
    vim \
    libgtk2.0-dev \
    libqt4-dev

# Copy contents
COPY . ./MobileRT
#RUN git clone https://github.com/TiagoMSSantos/MobileRT.git

# Variables
ARG build_type=Release

# Prepare environment
ENV DISPLAY=:1 \
    QT_ACCESSIBILITY=1 \
    QT_GRAPHICSSYSTEM="native" \
    BUILD_TYPE=${build_type}

# Compile Ray Tracer
RUN cd MobileRT && \
    mkdir -p build_${build_type} && \
    cd build_${build_type} && \
    cmake \
      -DCMAKE_VERBOSE_MAKEFILE=ON \
      -DCMAKE_CXX_COMPILER=g++ \
      -DCMAKE_BUILD_TYPE=${build_type} \
      ../app/ && \
    make && \
    find ./*/* -not -path bin -not -path lib ! -name AppInterface ! -name *.so -delete

# Execute
WORKDIR /MobileRT/build_${build_type}
CMD bash ../Scripts/profile.sh ${BUILD_TYPE}
