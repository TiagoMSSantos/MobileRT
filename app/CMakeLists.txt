###############################################################################
# Turn on verbose
###############################################################################
set( CMAKE_VERBOSE_MAKEFILE "ON" )
###############################################################################
###############################################################################


###############################################################################
# Include auxiliary functions
###############################################################################
message( STATUS "Adding helper functions." )
include( ${CMAKE_CURRENT_SOURCE_DIR}/CMakeLists_helper.cmake )
include( CheckCXXCompilerFlag )
print_environment()
###############################################################################
###############################################################################


###############################################################################
# Check CMake version
###############################################################################
set( CMAKE_MIN_VERSION "3.10" )
message( STATUS "Recommended CMake version: ${CMAKE_MIN_VERSION}+." )

cmake_minimum_required( VERSION ${CMAKE_MIN_VERSION}...${CMAKE_MIN_VERSION} FATAL_ERROR )
message( STATUS "Setting policies for compatibility with CMake v${CMAKE_MIN_VERSION}.")
cmake_policy( VERSION ${CMAKE_MIN_VERSION} )
###############################################################################
###############################################################################


###############################################################################
# Setting default flags for all modules (including third party)
###############################################################################
message( STATUS "Adding custom default flags for all third party projects." )

message( STATUS "Setting default compiler flags." )
set( ANDROID_COMPILER_FLAGS "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_STANDARD "11" CACHE STRING "CUSTOM" FORCE )

if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
    # PIC: Position Independent Code
    set( CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC -Bsymbolic -Bsymbolic-functions" CACHE STRING "CUSTOM" FORCE )
endif()
# _GLIBCXX_DEBUG: activate debug mode and all the assertions
set( CMAKE_CXX_FLAGS_DEBUG "-D_GLIBCXX_DEBUG -D_GLIBCXX_SANITIZE_VECTOR" CACHE STRING "CUSTOM" FORCE )
# NDEBUG: Deactivate all the assertions
set( CMAKE_CXX_FLAGS_RELEASE "-DNDEBUG" CACHE STRING "CUSTOM" FORCE )
if( CMAKE_BUILD_TYPE MATCHES "Release" OR CMAKE_BUILD_TYPE MATCHES "RelWithDebInfo" )
  add_definitions( -DNDEBUG )
endif()
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3" CACHE STRING "CUSTOM" FORCE )
  # ffunction-sections: generates a separate ELF section for each function in
  # the source file. The unused section elimination feature of the linker can
  # then remove unused functions at link time.
  # fdata-sections: enables the generation of one ELF section for each variable
  # in the source file.
  set( CMAKE_CXX_FLAGS_RELEASE "-DNDEBUG -ffunction-sections -fdata-sections" CACHE STRING "CUSTOM" FORCE )
endif()
if( "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} /O2 /Ot" CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} /Od /DEBUG" CACHE STRING "CUSTOM" FORCE )
endif()

set( CMAKE_CXX_FLAGS_RELWITHDEBINFO "-DNDEBUG" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

message( STATUS "Setting default linker flags." )
# -Bsymbolic-functions: this tells the linker to use direct local jumps to
# symbols within your library instead of trying to resolve them by the usual
# means. The effect is that every function call within your library will be
# initially faster since there's no lookup required.
# This leads to faster load times.
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( CMAKE_EXE_LINKER_FLAGS "-Bsymbolic -Bsymbolic-functions" CACHE STRING "CUSTOM" FORCE )
endif()
if( "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( CMAKE_EXE_LINKER_FLAGS  "-FORCE" CACHE STRING "CUSTOM" FORCE )
endif()
set( CMAKE_EXE_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
# discard-all: delete all local symbols.
# fno-rtti: disable generation of information about every class with virtual
# functions for use by the C++ runtime type identification features
# (`dynamic_cast' and `typeid'). If you don't use those parts of the language,
# you can save some space by using this flag.
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( CMAKE_EXE_LINKER_FLAGS_RELEASE "-O3 -discard-all" CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_SHARED_LINKER_FLAGS "-Bsymbolic -Bsymbolic-functions" CACHE STRING "CUSTOM" FORCE )
endif()
set( CMAKE_EXE_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_SHARED_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( CMAKE_SHARED_LINKER_FLAGS_RELEASE "-O3 -discard-all" CACHE STRING "CUSTOM" FORCE )
endif()

if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  message( STATUS "Detected not using MSVC compiler." )

  if( DEFINED ANDROID_ABI AND NOT CMAKE_HOST_WIN32 MATCHES "1" )
    # flto: Link Time Optimization
    set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -flto=full" CACHE STRING "CUSTOM" FORCE )
  endif()

  set( CMAKE_EXE_LINKER_FLAGS_RELEASE "${CMAKE_EXE_LINKER_FLAGS_RELEASE} -flto=full" CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_SHARED_LINKER_FLAGS_RELEASE "${CMAKE_SHARED_LINKER_FLAGS_RELEASE} -flto=full" CACHE STRING "CUSTOM" FORCE )
endif()

set( CMAKE_SHARED_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

if( NOT DEFINED ANDROID_ABI )
  message( STATUS "Detected native OS." )
  if( NOT CMAKE_HOST_WIN32 MATCHES "1"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "powerpc"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "aarch64"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "s390x" )
    # We can't enable `-march=native` flag in CI since the docker image will only work in CPUs of the same
    # architecture as it was used during compilation.
    # set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -march=native" CACHE STRING "CUSTOM" FORCE )
  endif()
  if( CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
    message( STATUS "Detected native MacOS." )

    message( STATUS "Add linker flag to link with Qt framework in MacOS." )
    set( CMAKE_SHARED_LIBRARY_SONAME_CXX_FLAG "-Wl,-install_name," CACHE STRING "CUSTOM" FORCE )
  endif()
endif()

set( CMAKE_BUILD_TYPE_INIT "${CMAKE_BUILD_TYPE}" CACHE STRING "CUSTOM" FORCE )

message( STATUS "Turning off GLM test." )
set( GLM_TEST_ENABLE "OFF" CACHE STRING "CUSTOM" FORCE )
###############################################################################
###############################################################################


###############################################################################
# set up project
###############################################################################
message( status "Setting up AppMobileRT project." )
project( AppMobileRT VERSION 1.0.0.0 LANGUAGES CXX )
print_environment()
if( DEFINED CONAN )
  if( DEFINED ANDROID_ABI )
    include( ${CMAKE_SOURCE_DIR}/../conanbuildinfo.cmake )
  else()
    include( ${CMAKE_SOURCE_DIR}/../conanbuildinfo.cmake )
  endif()
  conan_basic_setup( TARGETS )
endif()
###############################################################################
###############################################################################


###############################################################################
# Setting default flags
###############################################################################
message( STATUS "Setting up default lib and bin directories." )
set( CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib" )
set( CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib" )
set( CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin" )

if( NOT DEFINED ANDROID_ABI )
  message( STATUS "CMake will handle Qt moc preprocessor automatically." )
  set( CMAKE_AUTOMOC "ON" CACHE STRING "CUSTOM" FORCE )
else()
  set( CMAKE_AUTOMOC "OFF" CACHE STRING "CUSTOM" FORCE )
endif()

message( STATUS "Setting up Android Clang with static stdlib." )
set( ANDROID_TOOLCHAIN "clang" )
set( ANDROID_STL "c++_static" )
###############################################################################
###############################################################################


###############################################################################
# Setting common flags for all modules
###############################################################################
message( STATUS "Setting up common flags for all modules." )

set( COMMON_FLAGS "" )
set( COMMON_FLAGS_DEBUG "" )
set( COMMON_FLAGS_RELEASE "" )
set( COMMON_LINKER_FLAGS "" )
set( COMMON_LINKER_FLAGS_DEBUG "" )
set( COMMON_LINKER_FLAGS_RELEASE "" )
if( NOT CMAKE_GENERATOR MATCHES "Visual Studio" )
  set( COMMON_FLAGS "${COMMON_FLAGS} -Wall -Wpedantic -Wextra -Wfatal-errors -Werror" )
  set( COMMON_FLAGS_DEBUG "${COMMON_FLAGS_DEBUG} -O0 -g3" )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE} -O3" )
  set( COMMON_LINKER_FLAGS "${COMMON_LINKER_FLAGS} -Wpedantic -Wextra -Wfatal-errors -Werror" )
  set( COMMON_LINKER_FLAGS_DEBUG "${COMMON_LINKER_FLAGS_DEBUG} -O0 -g3" )
  set( COMMON_LINKER_FLAGS_RELEASE "${COMMON_LINKER_FLAGS_RELEASE} -O3" )
else()
  set( COMMON_FLAGS "${COMMON_FLAGS} /std:c11 /fastfail /sdl /Wall" )
  set( COMMON_FLAGS_DEBUG "${COMMON_FLAGS_DEBUG} /Od /DEBUG" )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE} /O2 /Ot" )
  set( COMMON_LINKER_FLAGS "${COMMON_LINKER_FLAGS}" )
  set( COMMON_LINKER_FLAGS_DEBUG "${COMMON_LINKER_FLAGS_DEBUG}" )
  set( COMMON_LINKER_FLAGS_RELEASE "${COMMON_LINKER_FLAGS_RELEASE}" )
endif()

if( NOT CMAKE_HOST_WIN32 MATCHES "1" AND NOT DEFINED ANDROID_ABI )
  set( COMMON_FLAGS_DEBUG "${COMMON_FLAGS_DEBUG} -fsanitize=address" )
  set( COMMON_LINKER_FLAGS_DEBUG "${COMMON_LINKER_FLAGS_DEBUG} -fsanitize=address" )
  set( CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -fsanitize=address" CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_EXE_LINKER_FLAGS_DEBUG "${CMAKE_EXE_LINKER_FLAGS_DEBUG} -fsanitize=address" CACHE STRING "CUSTOM" FORCE )
endif()
###############################################################################
###############################################################################

message( STATUS "Use C++11 in all variants." )
if( CMAKE_HOST_WIN32 MATCHES "1" AND ( CMAKE_CXX_COMPILER MATCHES "clang" OR CMAKE_CXX_COMPILER MATCHES "g[++]") AND CMAKE_BUILD_TOOL MATCHES "MSBuild" )
  set( COMMON_FLAGS "${COMMON_FLAGS} /std=c++11" )
elseif( "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE} /O2 /Ot" )
  set( COMMON_FLAGS_DEBUG "${COMMON_FLAGS_DEBUG} /Od /DEBUG" )
else()
  set( COMMON_FLAGS "${COMMON_FLAGS} -std=c++11" )
  set( COMMON_LINKER_FLAGS "${COMMON_LINKER_FLAGS} -std=c++11" )
endif()

message( STATUS "Setting up the name of the OS and the proper interface layer." )
message( STATUS "Host system: ${CMAKE_HOST_SYSTEM_NAME}." )

unset( COMPILER_SUPPORTS_FLAG CACHE )
CHECK_CXX_COMPILER_FLAG( "-Wno-extra-semi-stmt" COMPILER_SUPPORTS_FLAG )
# if( COMPILER_SUPPORTS_FLAG )
#   set( CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -Wno-extra-semi-stmt" CACHE STRING "CUSTOM" FORCE )
# endif()

if( DEFINED ANDROID_ABI )
  message( STATUS "Detected Android OS." )

  set( MOBILE_DEPENDENT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Android_JNI" )
  set( COMMON_FLAGS "${COMMON_FLAGS} -stdlib=libc++" )
  unset( COMPILER_SUPPORTS_FLAG CACHE )
  CHECK_CXX_COMPILER_FLAG( "-Wno-unsafe-buffer-usage" COMPILER_SUPPORTS_FLAG )
  if( COMPILER_SUPPORTS_FLAG )
    set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-unsafe-buffer-usage" )
  endif()
else()
  message( STATUS "Detected Native OS." )

  set( MOBILE_DEPENDENT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Native" )

  if( NOT CMAKE_HOST_WIN32 MATCHES "1"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "powerpc"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "aarch64"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "s390x" )
    # We can't enable `-march=native` flag in CI since the docker image will only work in CPUs of the same
    # architecture as it was used during compilation.
    # message( STATUS "Add flag to generate instructions for the machine type native." )
    # set( COMMON_FLAGS "${COMMON_FLAGS} -march=native" )
  endif()

  if( CMAKE_CXX_COMPILER MATCHES "clang" )
    # Necessary to disable this warning flag to avoid this error in clang:
    # fatal error: disabled expansion of recursive macro [-Wdisabled-macro-expansion]
    # /usr/include/x86_64-linux-gnu/bits/confname.h:255:26: note: expanded from macro '_SC_PHYS_PAGES'
    # #define _SC_PHYS_PAGES                  _SC_PHYS_PAGES
    set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-disabled-macro-expansion" )

    # Necessary to avoid the following error:
    # build_release/MobileRT/MobileRT_autogen/mocs_compilation.cpp:3:8: fatal error:
    # 'cmake_automoc_silence_linker_warning' has no out-of-line virtual method definitions;
    # its vtable will be emitted in every translation unit [-Wweak-vtables]
    set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-weak-vtables" )

    # Necessary to avoid the following error:
    # build_release/MobileRT/MobileRT_autogen/mocs_compilation.cpp:3:6: fatal error:
    # no previous prototype for function 'cmake_automoc_silence_linker_warning588b7c9ac6b8d237cd75c53c7a2339f15629b09ee2655a8f9b16255d443cea18' [-Wmissing-prototypes]
    # due to the usage of `CMAKE_AUTOMOC`.
    set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-missing-prototypes" )

    unset( COMPILER_SUPPORTS_FLAG CACHE )
    CHECK_CXX_COMPILER_FLAG( "-Wno-unsafe-buffer-usage" COMPILER_SUPPORTS_FLAG )
    if( COMPILER_SUPPORTS_FLAG )
      set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-unsafe-buffer-usage" )
    endif()
  endif()

  string( TOLOWER "${CMAKE_BUILD_TYPE}" CMAKE_BUILD_TYPE_L )
  if( CMAKE_BUILD_TYPE_L MATCHES debug )
    message( STATUS "Adding postfix for debug type." )

    set( CMAKE_DEBUG_POSTFIX "d" )
  endif()
endif()

if( CMAKE_CXX_COMPILER MATCHES "clang" OR DEFINED ANDROID_ABI )
  message( STATUS "Detected Clang compiler or Android ABI." )

  message( STATUS "Adding warning flags." )
  set( COMMON_FLAGS "${COMMON_FLAGS} -Weverything -ferror-limit=1" -Wno-switch-default )
endif()

if( DEFINED ANDROID_ABI AND NOT CMAKE_HOST_WIN32 MATCHES "1" )
  message( STATUS "Adding Link Time Optimization flag." )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE} -flto=full" )
endif()

message( STATUS "CMAKE_HOST_SYSTEM_NAME = ${CMAKE_HOST_SYSTEM_NAME}" )

if( NOT DEFINED ANDROID_ABI AND CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
  message( STATUS "Detected native MacOS." )

  set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-deprecated" )
  if( CMAKE_CXX_COMPILER MATCHES "clang" )
    message( STATUS "Detected Clang compiler." )

    set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-signed-enum-bitfield -Wno-sign-conversion" )
  endif()
endif()

if ( CMAKE_CXX_COMPILER_ID STREQUAL "IntelLLVM" )
  message( STATUS "Detected IntelLLVM compiler." )
  set( COMMON_FLAGS "${COMMON_FLAGS} -ffp-model=precise" )
else()
  message( STATUS "Not using IntelLLVM compiler." )
endif()

message( STATUS "Setting up common headers." )
set( MOBILE_RT_HEADERS "${CMAKE_SOURCE_DIR}" )
set( MOBILE_RC_HEADERS "${CMAKE_SOURCE_DIR}" )
set( SCENES_HEADERS "${CMAKE_SOURCE_DIR}/Scenes" )
set( THIRD_PARTY_HEADERS "${CMAKE_SOURCE_DIR}/third_party" )
set( GLM_HEADERS "${CMAKE_SOURCE_DIR}/third_party/glm" )
set( QT_HEADERS_RT "${CMAKE_SOURCE_DIR}/System_dependent/Native/QT/MobileRT" )
set( QT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Native/Qt/build-${CMAKE_BUILD_TYPE}" )

message( STATUS "Setting up common flags." )
set( COMMON_FLAGS "${COMMON_FLAGS} -Wall" )
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( COMMON_FLAGS "${COMMON_FLAGS} -pedantic-errors -pedantic" )
endif()

if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( COMMON_FLAGS "${COMMON_FLAGS} -Wpedantic -Wextra -Wfatal-errors -Werror" )
endif()

if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  message( STATUS "Not using MSVC compiler." )
  if( DEFINED ANDROID_ABI OR CMAKE_CXX_COMPILER MATCHES "clang"
    OR (CMAKE_CXX_COMPILER MATCHES "g[++]" AND NOT CMAKE_CXX_COMPILER MATCHES "clang")
  )
    message( STATUS "Detected Android or Clang compiler. or GCC compiler." )
  endif()
  # Turn off C++98 pedantic warning so it can use C++11 features
  set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-c++98-compat-pedantic" )
  # Turn off padded warning because some classes still need padding
  set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-padded" )

  # Allow '/usr/local/include' in PATH unsafe for cross-compilation
  if( NOT DEFINED ANDROID_ABI AND CMAKE_CXX_COMPILER MATCHES "clang"
    AND CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin"
  )
    unset( COMPILER_SUPPORTS_FLAG CACHE )
    CHECK_CXX_COMPILER_FLAG( "-Wno-poison-system-directories" COMPILER_SUPPORTS_FLAG )
    if( COMPILER_SUPPORTS_FLAG )
      set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-poison-system-directories" )
    endif()
  endif()
endif()

if( CMAKE_CXX_COMPILER MATCHES "clang" )
  unset( COMPILER_SUPPORTS_FLAG CACHE )
  CHECK_CXX_COMPILER_FLAG( "-Wno-redundant-parens" COMPILER_SUPPORTS_FLAG )
  if( COMPILER_SUPPORTS_FLAG )
    # Turn off redundant parentheses warning because of Qt moc generated code
    set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-redundant-parens" )
  endif()
endif()

if( DEFINED ANDROID_ABI OR CMAKE_CXX_COMPILER MATCHES "clang" )
  # Turn off exit time destructors warning because of the usage of
  # static variables in namespaces
  set( COMMON_FLAGS "${COMMON_FLAGS} -Wno-exit-time-destructors" )
endif()

if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" AND NOT ( CMAKE_CXX_COMPILER MATCHES "g[++]" AND NOT CMAKE_CXX_COMPILER MATCHES "clang" ) )
  set( COMMON_FLAGS_DEBUG "${COMMON_FLAGS_DEBUG} -O0 -g3 -fno-optimize-sibling-calls -fno-omit-frame-pointer -fstack-check" )
  set( COMMON_LINKER_FLAGS_DEBUG "${COMMON_LINKER_FLAGS_DEBUG} -fstack-check -fstack-protector-all" )
endif()

set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE} -DNDEBUG" )
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE} -ffunction-sections -fdata-sections" )
endif()

if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE} -O3" )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE} -Wno-unused-command-line-argument" )
endif()

message( STATUS "Setting common linker flags." )
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( COMMON_LINKER_FLAGS "${COMMON_LINKER_FLAGS} -O3 -discard-all" )
endif()

if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  set( COMMON_LINKER_FLAGS_DEBUG "${COMMON_LINKER_FLAGS_DEBUG} -O0 -g3" )
endif()

if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( COMMON_LINKER_FLAGS_DEBUG "${COMMON_LINKER_FLAGS_DEBUG} -rdynamic" )
endif()

if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  message( STATUS "Detected not using MVSC compiler." )

  set( COMMON_LINKER_FLAGS_RELEASE "${COMMON_LINKER_FLAGS_RELEASE} -flto=full" )
endif()

if( NOT CMAKE_HOST_WIN32 MATCHES "1" AND NOT CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
  message( STATUS "Detected not Windows OS." )

  if( CMAKE_CXX_COMPILER MATCHES "g[++]" AND NOT CMAKE_CXX_COMPILER MATCHES "clang" )
    message( STATUS "Add code coverage analysis flags for GCC." )

    set( COMMON_FLAGS_TEST "-ftest-coverage -fprofile-arcs" )
    set( COMMON_LINKER_FLAGS_TEST "--coverage -fprofile-update=atomic -lgcov" )
  endif()
endif()


message( STATUS "Adding scenes source files." )
file( GLOB_RECURSE SCENES_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/Scenes/**.cpp" )
set( SCENES_SOURCES "${SCENES_SOURCES}" CACHE STRING "CUSTOM" FORCE )

if( NOT DEFINED ANDROID_ABI )
  message( STATUS "Finding Qt6." )
  find_package( Qt6 OPTIONAL_COMPONENTS Core Gui Widgets )
  if( NOT Qt6_FOUND )
    message( STATUS "Did not find Qt6. Finding Qt5." )
    find_package( Qt5 OPTIONAL_COMPONENTS Core Gui Widgets )
    if( NOT Qt5_FOUND )
      message( STATUS "Did not find Qt5. Finding Qt4." )
      find_package( Qt4 OPTIONAL_COMPONENTS QtCore QtGui )
    else()
      message( STATUS "Found Qt5." )
    endif()
  else()
    message( STATUS "Found Qt6." )
  endif()
endif()


###############################################################################
# Add dependencies
###############################################################################
separate_arguments( COMMON_FLAGS )
separate_arguments( COMMON_FLAGS_TEST )
separate_arguments( COMMON_FLAGS_DEBUG )
separate_arguments( COMMON_FLAGS_RELEASE )
separate_arguments( COMMON_LINKER_FLAGS )
separate_arguments( COMMON_LINKER_FLAGS_TEST )
separate_arguments( COMMON_LINKER_FLAGS_DEBUG )
separate_arguments( COMMON_LINKER_FLAGS_RELEASE )

message( STATUS "Adding sub directories." )
add_subdirectory( MobileRT )
add_subdirectory( Components )
add_subdirectory( third_party )
add_subdirectory( System_dependent )
add_subdirectory( Unit_Testing )
###############################################################################
###############################################################################


###############################################################################
# Add interface layer
###############################################################################
message( STATUS "Adding libs for the interface layer." )
if( DEFINED ANDROID_ABI )
  message( STATUS "Adding Android JNI layer." )

  add_library( ${PROJECT_NAME} SHARED ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} )
  target_link_libraries( ${PROJECT_NAME} PRIVATE android jnigraphics )
else()
  message( STATUS "Adding Linux native Qt layer." )

  add_executable( ${PROJECT_NAME} ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} ${MOBILE_DEPENDENT_SOURCES_QT} )

  message( STATUS "Qt6Core_INCLUDE_DIRS = " ${Qt6Core_INCLUDE_DIRS} )
  message( STATUS "Qt6Gui_INCLUDE_DIRS = " ${Qt6Gui_INCLUDE_DIRS} )
  message( STATUS "Qt6Widgets_INCLUDE_DIRS = " ${Qt6Widgets_INCLUDE_DIRS} )

  message( STATUS "Qt5Core_INCLUDE_DIRS = " ${Qt5Core_INCLUDE_DIRS} )
  message( STATUS "Qt5Gui_INCLUDE_DIRS = " ${Qt5Gui_INCLUDE_DIRS} )
  message( STATUS "Qt5Widgets_INCLUDE_DIRS = " ${Qt5Widgets_INCLUDE_DIRS} )

  message( STATUS "QT_INCLUDES = ${QT_INCLUDES}" )
  message( STATUS "QT_QTCORE_LIBRARIES = ${QT_QTCORE_LIBRARIES}" )
  message( STATUS "QT_QTGUI_LIBRARIES = ${QT_QTGUI_LIBRARIES}" )

  # Include Qt headers
  target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE ${QT_HEADERS} PRIVATE ${QT_HEADERS_RT} )

  if( Qt6_FOUND )
    message( STATUS "Including headers from Qt6." )
    target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE ${Qt6Core_INCLUDE_DIRS} ${Qt6Gui_INCLUDE_DIRS} ${Qt6Widgets_INCLUDE_DIRS} )
  elseif( Qt5_FOUND )
    message( STATUS "Including headers from Qt5." )
    target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE ${Qt5Core_INCLUDE_DIRS} ${Qt5Gui_INCLUDE_DIRS} ${Qt5Widgets_INCLUDE_DIRS} )
  elseif( Qt4_FOUND )
    message( STATUS "Including headers from Qt4." )
    target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE ${QT_INCLUDES} )
  endif()

  if( CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
    message( STATUS "Linking Qt for MacOS." )

    set( CMAKE_MODULE_PATH "${CMAKE_MODULE_PATH} /usr/local/lib/python3.9/site-packages/cmake/data/CMake.app/Contents/share/cmake-3.22/Modules/" CACHE STRING "CUSTOM" FORCE )
    message( STATUS "CMAKE_MODULE_PATH = ${CMAKE_MODULE_PATH}" )

    if( NOT Qt6_FOUND )
      if( NOT Qt5_FOUND )
        message( STATUS "Did not find Qt6 nor Qt5. Finding Qt4." )
        find_package( Qt4 OPTIONAL_COMPONENTS QtCore QtGui )
      endif()
    endif()

    message( STATUS "Linking with default user local path." )
    target_link_libraries( ${PROJECT_NAME} PRIVATE -F/usr/local/lib )
    # If github action jurplel/install-qt-action was used, then Qt should be at root of project. E.g. paths:
    # * Qt/5.15.2/msvc2019_64/include/QtWidgets/QDialog
    # * Qt/6.9.0/msvc2022_64/include/QtWidgets/QDialog
    message( STATUS "Finding lib files from Qt6 or Qt5." )
    file( GLOB QT_LIBS "${CMAKE_SOURCE_DIR}/../Qt/*/*/lib/" )
    message( STATUS "Linking lib files from Qt6 or Qt5." )
    target_link_libraries( ${PROJECT_NAME} PRIVATE -F${QT_LIBS} )

    if( Qt6_FOUND )
      message( STATUS "Linking with Qt6." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE Qt6::Core Qt6::Gui Qt6::Widgets )

      message( STATUS "Linking with QtWidgets from Qt6 (which Qt4 doesn't have)." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtWidgets" )
    elseif( Qt5_FOUND )
      message( STATUS "Linking with Qt5." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE Qt5::Core Qt5::Gui Qt5::Widgets )

      message( STATUS "Linking with QtWidgets from Qt5 (which Qt4 doesn't have)." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtWidgets" )
    else()
      message( STATUS "Linking with Qt4." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtCore" )
      target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtGui" )
    endif()
  else()
    message( STATUS "Linking Qt." )

    if( Qt6_FOUND )
      message( STATUS "Linking with Qt6." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE Qt6::Core Qt6::Gui Qt6::Widgets )
    elseif( Qt5_FOUND )
      message( STATUS "Linking with Qt5." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE Qt5::Core Qt5::Gui Qt5::Widgets )
    elseif( Qt4_FOUND )
      message( STATUS "Linking with Qt4." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE Qt4::QtCore Qt4::QtGui Qt4::Widgets )
    else()
      message( STATUS "Did not find any Qt in the system." )
    endif()

  endif()
endif()
###############################################################################
###############################################################################


###############################################################################
# Set target properties
###############################################################################
message( STATUS "Adding debug postfix." )
set_target_properties( ${PROJECT_NAME} PROPERTIES DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )
###############################################################################
###############################################################################


message( STATUS "Adding MobileRT and third party headers." )
target_include_directories( ${PROJECT_NAME} PRIVATE "${MOBILE_RC_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PRIVATE "${SCENES_HEADERS}" )
target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE "${GLM_HEADERS}" )

message( STATUS "Adding compiler flags." )
target_compile_options( ${PROJECT_NAME} PRIVATE "${COMMON_FLAGS}" )
# Turn off global constructors warnings because of scenes
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-global-constructors )
endif()
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  message( STATUS "Detected not using MSVC compiler." )

  unset( COMPILER_SUPPORTS_FLAG CACHE )
  CHECK_CXX_COMPILER_FLAG( "-Wno-zero-as-null-pointer-constant" COMPILER_SUPPORTS_FLAG )
  if( COMPILER_SUPPORTS_FLAG )
    # Turn off zero as null pointer warning because of Qt generated code
    target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-zero-as-null-pointer-constant )
  endif()
endif()

target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_TEST}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

if( CMAKE_CXX_COMPILER MATCHES "clang" )
  message( STATUS "Detected Clang compiler." )
  message( STATUS "Setting up flags for Clang to disable some warnings." )

  if( CMAKE_CXX_COMPILER_VERSION VERSION_LESS 8 )
    message( STATUS "Detected Clang version < 8." )
    target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-unused-command-line-argument )
  endif()
else()
  message( STATUS "Detected GCC compiler." )

  if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
    target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-redundant-move )
  endif()
endif()

###############################################################################
# Link project
###############################################################################
message( STATUS "Linking with sub modules and third party libs." )
target_link_libraries( ${PROJECT_NAME}
  PUBLIC MobileRT Components
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}" "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)
###############################################################################
###############################################################################


#print_environment()
