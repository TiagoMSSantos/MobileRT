.SUFFIXES:
.PHONY: all clean tidy gtest
.PRECIOUS: %.s %.llvmIR
default: all

EXE := raytracer
CXX := g++-5
CXX := clang++-5.0
LD := $(CXX)
MAKEFLAGS := -j1
INCLUDES := -I./MobileRT/src/main/cpp
INCLUDES += -I./Components/src/main/cpp
INCLUDES += -isystem./third-party
INCLUDES += -isystem./third-party/googletest/googletest/include
INCLUDES_GTK += $(shell pkg-config --cflags gtk+-2.0)
INCLUDES += $(subst -I,-isystem,$(INCLUDES_GTK))
LIBRARIES_APP := $(shell pkg-config --libs gtk+-2.0) -pthread
LIBRARIES_MOBILERT := $(shell find ./third-party/**/build/* \
-regextype gnu-awk -iname "*.a" -printf '-L./%h\n' | uniq)

CXXFLAGS := -std=c++14 -fPIC $(INCLUDES)
CXXFLAGS += -Wall -Wpedantic -Wextra -MMD -pedantic -Wfatal-errors
CXXFLAGS += -Wno-padded -Wno-float-equal
CXXFLAGS += -fno-exceptions -fno-asynchronous-unwind-tables -fno-rtti

ifeq ($(S),0)
	CXXFLAGS += -O3 -march=native
	LDFLAGS += -s -flto=full
endif

ifeq ($(S),1)
	CXXFLAGS += -O3 -march=native -fno-omit-frame-pointer
	LDFLAGS += -s -flto=full
endif

ifeq ($(S),2)
	CXXFLAGS += -O0 -g3 -fno-optimize-sibling-calls -fno-omit-frame-pointer
endif

ifeq ($(S),3)
	CXXFLAGS += -O3 -march=native
	LDFLAGS += -s -flto=full

	#SANITIZER := -fsanitize=leak #LeakSanitizer, a run-time memory leak detector.
	#SANITIZER := -fsanitize=memory #MemorySanitizer, a detector of uninitialized reads.
	#SANITIZER := -fsanitize=address #AddressSanitizer, a memory error detector.
	#SANITIZER := -fsanitize=thread #ThreadSanitizer, a data race detector.
	#SANITIZER := -fsanitize=undefined #UndefinedBehaviorSanitizer
	#SANITIZER := -fsanitize=dataflow #DataFlowSanitizer, a general data flow analysis.
	#SANITIZER := -fsanitize=cfi #control flow integrity checks. Requires -flto.
	#SANITIZER := -fsanitize=safe-stack #safe stack protection

	SANITIZER_CXX := -fsanitize-blacklist=sanitizer_ignore.suppr
	SANITIZER_CXX += -fsanitize-recover=address
	#SANITIZER_CXX += -fsanitize-memory-track-origins=2 -fsanitize-memory-use-after-dtor
	#SANITIZER_CXX += -static-libasan

  SANITIZER_LD := -fsanitize-blacklist=sanitizer_ignore.suppr
	SANITIZER_LD += -fsanitize-address-use-after-scope
	#SANITIZER_LD += -ltsan -lasan

	CXXFLAGS += $(SANITIZER) $(SANITIZER_CXX)
	LDFLAGS += $(SANITIZER) $(SANITIZER_LD)
endif

ifneq (,$(findstring clang,$(CXX)))
	ifneq (,$(findstring flto,$(LDFLAGS)))
		CXXFLAGS += -flto=full
	endif
  CXXFLAGS += -isystem /usr/include/c++/v1
  CXXFLAGS += -Weverything -ferror-limit=1
	CXXFLAGS += -Wno-c++98-compat-pedantic
	LDFLAGS += -lc++
endif

###############################################################################

SOURCES_APP := $(shell find \
./RayTracerMain.cpp \
! -name "DrawView.cpp" \
-regextype gnu-awk -iname "*.cpp" -printf '%h/%f\n' | sort -nr)
OBJS_APP := $(addsuffix .o,$(basename $(SOURCES_APP)))
DEPS_APP := $(addsuffix .d,$(basename $(SOURCES_APP)))
ASSM_APP := $(addsuffix .s,$(basename $(SOURCES_APP)))
LLVM_APP := $(addsuffix .llvmIR,$(basename $(SOURCES_APP)))

SOURCES_WRAPPER := $(shell find \
./Scenes.cpp \
./c_wrapper.cpp \
! -name "DrawView.cpp" \
-regextype gnu-awk -iname "*.cpp" -printf '%h/%f\n' | sort -nr)
OBJS_WRAPPER := $(addsuffix .o,$(basename $(SOURCES_WRAPPER)))
DEPS_WRAPPER := $(addsuffix .d,$(basename $(SOURCES_WRAPPER)))
ASSM_WRAPPER := $(addsuffix .s,$(basename $(SOURCES_WRAPPER)))
LLVM_WRAPPER := $(addsuffix .llvmIR,$(basename $(SOURCES_WRAPPER)))

SOURCES_MOBILERT := $(shell find \
./Components/src \
./MobileRT/src \
-regextype gnu-awk -iname "*.cpp" -printf '%h/%f\n' | sort -nr)
OBJS_MOBILERT := $(addsuffix .o,$(basename $(SOURCES_MOBILERT)))
DEPS_MOBILERT := $(addsuffix .d,$(basename $(SOURCES_MOBILERT)))
ASSM_MOBILERT := $(addsuffix .s,$(basename $(SOURCES_MOBILERT)))
LLVM_MOBILERT := $(addsuffix .llvmIR,$(basename $(SOURCES_MOBILERT)))

SOURCES_THIRD_PARTY := ./third-party/tinyobjloader/tiny_obj_loader.cc
OBJS_THIRD_PARTY := $(addsuffix .o,$(basename $(SOURCES_THIRD_PARTY)))
DEPS_THIRD_PARTY := $(addsuffix .d,$(basename $(SOURCES_THIRD_PARTY)))
ASSM_THIRD_PARTY := $(addsuffix .s,$(basename $(SOURCES_THIRD_PARTY)))
LLVM_THIRD_PARTY := $(addsuffix .llvmIR,$(basename $(SOURCES_THIRD_PARTY)))

GARB := $(shell find \
./third-party/tinyobjloader \
./Components/src \
./MobileRT/src \
./RayTracerMain.* \
./c_wrapper.* \
./Scenes.* \
./GoogleUnitTest \
-regextype gnu-awk -iname "*.d" -printf '%h/%f\n' \
-o -regextype gnu-awk -iname "*.s" -printf '%h/%f\n' \
-o -regextype gnu-awk -iname "*.o" -printf '%h/%f\n' \
-o -regextype gnu-awk -iname "*.out" -printf '%h/%f\n' \
-o -regextype gnu-awk -iname "*.gto" -printf '%h/%f\n' \
-o -regextype gnu-awk -iname "*.dwo" -printf '%h/%f\n' \
)

###############################################################################

%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $(basename $<).cpp -o $@
#	objdump --source $@ > $<

#%.s: %.llvmIR
#	$(CXX) $(CXXFLAGS) -S $(basename $<).cpp -o $@

#%.llvmIR: %.cpp
#	$(CXX) $(CXXFLAGS) -S -emit-llvm $< -o $@

all: $(EXE)

$(EXE): C_Wrapper MobileRT $(OBJS_APP)
	$(LD) $(LDFLAGS) -o $@ $(OBJS_APP) $(LIBRARIES_MOBILERT) $(LIBRARIES_APP) -lC_Wrapper -lMobileRT
	@echo "Compilation App done\n"

C_Wrapper: MobileRT $(OBJS_WRAPPER)
	$(LD) -shared $(LDFLAGS) -o lib$@.so $(OBJS_WRAPPER) $(OBJS_MOBILERT) $(OBJS_THIRD_PARTY)
	@echo "Compilation C_Wrapper done\n"

MobileRT: third_party $(OBJS_MOBILERT)
	$(LD) -shared $(LDFLAGS) -o lib$@.so $(OBJS_MOBILERT) $(OBJS_THIRD_PARTY) $(LIBRARIES_MOBILERT)
	@echo "Compilation MobileRT done\n"

third_party:
	$(CXX) $(CXXFLAGS) -w -c $(SOURCES_THIRD_PARTY) -o $(OBJS_THIRD_PARTY)
	@echo "Compilation third-party done\n"

clean:
	@rm -f $(OBJS_MOBILERT) $(DEPS_MOBILERT) $(ASSM_MOBILERT) $(LLVM_MOBILERT)
	@rm -f $(OBJS_WRAPPER) $(DEPS_WRAPPER) $(ASSM_WRAPPER) $(LLVM_WRAPPER)
	@rm -f $(OBJS_APP) $(DEPS_APP) $(ASSM_APP) $(LLVM_APP)
	@rm -f $(GARB) $(EXE) gtest libMobileRT.so libC_Wrapper.so
	@echo "Clean done\n"

GTESTSRCS := $(shell find ./GoogleUnitTest -regextype gnu-awk -iname "*.cpp" -printf '%h/%f\n')
GTESTOBJS := $(addsuffix .gto,$(basename $(GTESTSRCS)))
%.gto: %.cpp
	$(CXX) $(CXXFLAGS) -c $(basename $<).cpp -o $@

gtest: $(OBJS_MOBILERT) $(GTESTOBJS)
	$(LD) $(GTESTOBJS) $(OBJS_MOBILERT) -o $@ $(LDFLAGS) $(LIBRARIES_MOBILERT) -lgtest_main -lgtest -lgmock -lgmock_main -lpthread
	@echo "Compilation Test Units done\n"


tidy:
	clang-tidy-5.0 \
	-analyze-temporary-dtors \
	-checks='*,-*llvm-header-guard*' \
	-header-filter='.*' \
	-p . \
	./*.*pp \
	./MobileRT/src/main/cpp/*/*.*pp \
	./MobileRT/src/main/cpp/*/*/*.*pp \
	./Components/src/main/cpp/*/*/*.*pp \
	-- -std=c++14 -ferror-limit=1 \
	-I./MobileRT/src/main/cpp \
	-I./Components/src/main/cpp \
	-isystem./third-party \
	-isystem/usr/include/c++/v1 \
	-isystem/usr/lib/gcc/x86_64-linux-gnu/5/include \
	-isystem/usr/include/glib-2.0/gobject \
	-isystem/usr/include/gtk-2.0/gtk \
	-pthread -isystem/usr/include/gtk-2.0 -isystem/usr/lib/x86_64-linux-gnu/gtk-2.0/include -isystem/usr/include/gio-unix-2.0/ -isystem/usr/include/atk-1.0 -isystem/usr/include/cairo -isystem/usr/include/pixman-1 -isystem/usr/include/libpng12 -isystem/usr/include/gdk-pixbuf-2.0 -isystem/usr/include/harfbuzz -isystem/usr/include/pango-1.0 -isystem/usr/include/glib-2.0 -isystem/usr/lib/x86_64-linux-gnu/glib-2.0/include -isystem/usr/include/freetype2 \
	2>&1 | tee tidy.out

#valgrind --tool=memcheck --leak-check=full --show-leak-kinds=all --track-origins=yes --show-reachable=yes --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=cachegrind --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=callgrind --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=helgrind  --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=drd --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=massif --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=exp-dhat --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=exp-sgcheck --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=exp-bbv --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#valgrind --tool=lackey --suppressions=valgrind.suppr --num-callers=500 --trace-children=yes --error-limit=no ./raytracer 5 3 3 0 1 1

#llvm-profdata-5.0 merge -o testcov.profdata pgo/default_4412358617431096720_0.profraw

#llvm-cov-5.0 show ./testcov -instr-profile=testcov.profdata testcov.c
