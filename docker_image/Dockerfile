# Dependency docker image
FROM ubuntu

# Variables
ARG build_type=Release
ARG build_dir=build_release

ENV DISPLAY=:1
ENV QT_ACCESSIBILITY=1
ENV QT_GRAPHICSSYSTEM="native"

RUN echo "build_dir = $build_dir"
RUN echo "build_type = $build_type"
RUN echo "DISPLAY = $DISPLAY"

# Install dependencies
RUN apt-get update -y
RUN apt-get install -y cmake git g++ vim
RUN apt-get install -y libgtk2.0-dev
RUN apt-get install -y libqt4-dev

# Copy contents
RUN git clone https://github.com/TiagoMSSantos/MobileRT.git

# Compile
WORKDIR /MobileRT
RUN mkdir $build_dir

WORKDIR /MobileRT/$build_dir
RUN cmake -DCMAKE_VERBOSE_MAKEFILE=ON -DCMAKE_CXX_COMPILER=g++ -DCMAKE_BUILD_TYPE=$build_type ../app/
RUN make

# Execute
CMD bash ../Scripts/profile.sh exec
