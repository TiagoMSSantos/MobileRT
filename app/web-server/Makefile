.PHONY: build clean run test

BOOST_INCLUDE = ../third_party/boost/libs
INCLUDE_PATHS = -I../ \
                -I../System_dependent/Native \
                -I../Scenes \
                -I../third_party \
                -I../third_party/glm \
                -I../third_party/pcg-cpp/include \
                -I$(BOOST_INCLUDE)/assert/include \
                -I$(BOOST_INCLUDE)/sort/include \
                -I$(BOOST_INCLUDE)/type_traits/include \
                -I$(BOOST_INCLUDE)/config/include \
                -I$(BOOST_INCLUDE)/static_assert/include \
                -I$(BOOST_INCLUDE)/core/include \
                -I$(BOOST_INCLUDE)/range/include \
                -I$(BOOST_INCLUDE)/preprocessor/include \
                -I$(BOOST_INCLUDE)/mpl/include \
                -I$(BOOST_INCLUDE)/iterator/include \
                -I$(BOOST_INCLUDE)/stacktrace/include \
                -I$(BOOST_INCLUDE)/predef/include \
                -I$(BOOST_INCLUDE)/container_hash/include
CLEAN_FILES = target/*web_server* *.o *.so
LD_PATH := ../../build_release/lib

clean:
	rm -rf $(CLEAN_FILES) || true

build: clean libcpp.so
	cargo build -j8

libcpp.so: src/lib.cpp ../Scenes/Scenes.cpp ../System_dependent/Native/*.cpp
	g++ $(INCLUDE_PATHS) -fPIC -c $^
	g++ -shared -o $@ *.o

run:
	LD_LIBRARY_PATH=.:$(LD_PATH):${LD_LIBRARY_PATH} RUST_BACKTRACE=1 cargo run

test:
	LD_LIBRARY_PATH=.:$(LD_PATH):${LD_LIBRARY_PATH} cargo test -- --nocapture
