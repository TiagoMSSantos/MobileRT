###############################################################################
# Turn on verbose
###############################################################################
set( CMAKE_VERBOSE_MAKEFILE ON )
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
message( STATUS "Recommended CMake version: 3.0+." )

cmake_minimum_required( VERSION 3.0...3.0 FATAL_ERROR )

message( STATUS
  "Setting policies for compatibility with CMake v3.0.")
cmake_policy( VERSION 3.0 )
###############################################################################
###############################################################################


###############################################################################
# Setting default flags for all modules (including third party)
###############################################################################
message( STATUS "Adding custom default flags for all third party projects." )

message( STATUS "Setting default compiler flags." )
set( ANDROID_COMPILER_FLAGS "" CACHE STRING "CUSTOM" FORCE )

if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
    # PIC: Position Independent Code
    set( CMAKE_CXX_FLAGS "-std=c++11 -fPIC -Bsymbolic -Bsymbolic-functions"
      CACHE STRING "CUSTOM" FORCE )
endif()
# _GLIBCXX_DEBUG: activate debug mode and all the assertions
set( CMAKE_CXX_FLAGS_DEBUG "-D_GLIBCXX_DEBUG" CACHE STRING "CUSTOM" FORCE )
# NDEBUG: Deactivate all the assertions
# ffunction-sections: generates a separate ELF section for each function in
# the source file. The unused section elimination feature of the linker can
# then remove unused functions at link time.
# fdata-sections: enables the generation of one ELF section for each variable
# in the source file.
set( CMAKE_CXX_FLAGS_RELEASE "-DNDEBUG -ffunction-sections -fdata-sections"
  CACHE STRING "CUSTOM" FORCE )
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3" CACHE STRING "CUSTOM" FORCE )
endif()
set( CMAKE_CXX_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

message( STATUS "Setting default linker flags." )
# -Bsymbolic-functions: this tells the linker to use direct local jumps to
# symbols within your library instead of trying to resolve them by the usual
# means. The effect is that every function call within your library will be
# initially faster since there's no lookup required.
# This leads to faster load times.
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( CMAKE_EXE_LINKER_FLAGS "-Bsymbolic -Bsymbolic-functions"
    CACHE STRING "CUSTOM" FORCE )
endif()
if( CMAKE_HOST_WIN32 MATCHES "1" )
  set( CMAKE_EXE_LINKER_FLAGS  "-FORCE" CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_GENERATOR_PLATFORM  "x64" CACHE STRING "CUSTOM" FORCE )
endif()
set( CMAKE_EXE_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
# discard-all: delete all local symbols.
# fno-rtti: disable generation of information about every class with virtual
# functions for use by the C++ runtime type identification features
# (`dynamic_cast' and `typeid'). If you don't use those parts of the language,
# you can save some space by using this flag.
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( CMAKE_EXE_LINKER_FLAGS_RELEASE "-O3 -discard-all -fno-rtti"
    CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_SHARED_LINKER_FLAGS "-Bsymbolic -Bsymbolic-functions"
    CACHE STRING "CUSTOM" FORCE )
endif()
set( CMAKE_EXE_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_SHARED_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( CMAKE_SHARED_LINKER_FLAGS_RELEASE "-O3 -discard-all -fno-rtti"
    CACHE STRING "CUSTOM" FORCE )
endif()

if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  message( STATUS "Detected not Windows OS." )

  if( DEFINED ANDROID_ABI AND NOT CMAKE_HOST_WIN32 MATCHES "1" )
    # flto: Link Time Optimization
    set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -flto=full"
      CACHE STRING "CUSTOM" FORCE )
  endif()

  set( CMAKE_EXE_LINKER_FLAGS_RELEASE
    "${CMAKE_EXE_LINKER_FLAGS_RELEASE} -flto=full"
    CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_SHARED_LINKER_FLAGS_RELEASE
    "${CMAKE_SHARED_LINKER_FLAGS_RELEASE} -flto=full"
    CACHE STRING "CUSTOM" FORCE )
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
#    set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -march=native"
#      CACHE STRING "CUSTOM" FORCE )
  endif()
  if( CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
    message( STATUS "Detected native MacOS." )

    # Add linker flag to link with Qt framework in MacOS
    set( CMAKE_SHARED_LIBRARY_SONAME_CXX_FLAG "-Wl,-install_name,"
      CACHE STRING "CUSTOM" FORCE )
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
    include( ${CMAKE_CURRENT_SOURCE_DIR}/../conanbuildinfo.cmake )
  else()
    include( ${CMAKE_BINARY_DIR}/conanbuildinfo.cmake )
  endif()
  conan_basic_setup( TARGETS )
endif()
###############################################################################
###############################################################################


###############################################################################
# Setting default flags
###############################################################################
message( STATUS "Setting up default lib and bin directories." )
set( CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib )
set( CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib )
set( CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin )

message( STATUS "CMake will handle Qt moc preprocessor automatically." )
set( CMAKE_AUTOMOC ON )

message( STATUS "Setting up Android Clang with static stdlib." )
set( ANDROID_TOOLCHAIN clang )
set( ANDROID_STL c++_static )
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
###############################################################################
###############################################################################

message( STATUS
  "Setting up the name of the OS and the proper interface layer." )
message( STATUS "Host system: ${CMAKE_HOST_SYSTEM_NAME}." )
# Use to make MacOS use the right OpenMP headers
#set( CMAKE_SYSTEM_NAME "Linux" )
if( DEFINED ANDROID_ABI )
  message( STATUS "Detected Android OS." )

  set( MOBILE_DEPENDENT_HEADERS
    "${CMAKE_SOURCE_DIR}/System_dependent/Android_JNI" )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -stdlib=libc++ )
else()
  message( STATUS "Detected Native OS." )

  set( MOBILE_DEPENDENT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Native" )

  if( NOT CMAKE_HOST_WIN32 MATCHES "1"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "powerpc"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "aarch64"
    AND NOT CMAKE_CXX_LIBRARY_ARCHITECTURE MATCHES "s390x" )
    # We can't enable `-march=native` flag in CI since the docker image will only work in CPUs of the same
    # architecture as it was used during compilation.
#    message( STATUS
#    "Add flag to generate instructions for the machine type native." )
#    set( COMMON_FLAGS "${COMMON_FLAGS}" -march=native )
  endif()

  if( CMAKE_CXX_COMPILER MATCHES "clang" )
    # Necessary to disable this warning flag to avoid this error in clang:
    # fatal error: disabled expansion of recursive macro [-Wdisabled-macro-expansion]
    # /usr/include/x86_64-linux-gnu/bits/confname.h:255:26: note: expanded from macro '_SC_PHYS_PAGES'
    # #define _SC_PHYS_PAGES                  _SC_PHYS_PAGES
    set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-disabled-macro-expansion )

    # Necessary to avoid the following error:
    # build_release/MobileRT/MobileRT_autogen/mocs_compilation.cpp:3:8: fatal error:
    # 'cmake_automoc_silence_linker_warning' has no out-of-line virtual method definitions;
    # its vtable will be emitted in every translation unit [-Wweak-vtables]
    set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-weak-vtables )
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
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Weverything -ferror-limit=1 )
endif()

if( DEFINED ANDROID_ABI AND NOT CMAKE_HOST_WIN32 MATCHES "1" )
  message( STATUS "Adding Link Time Optimization flag." )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}" -flto=full )
endif()

message( STATUS "CMAKE_HOST_SYSTEM_NAME = ${CMAKE_HOST_SYSTEM_NAME}" )
if( NOT DEFINED ANDROID_ABI AND CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
  message( STATUS "Detected native MacOS." )

  # Turn off the warning flag because of OpenMP in MacOS
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-reserved-id-macro )
endif()

if( NOT DEFINED ANDROID_ABI AND CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
  message( STATUS "Detected native MacOS." )

  set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-deprecated )
  if( CMAKE_CXX_COMPILER MATCHES "clang" )
    message( STATUS "Detected Clang compiler." )

    set( COMMON_FLAGS "${COMMON_FLAGS}"
      -Wno-signed-enum-bitfield
      -Wno-sign-conversion )
  endif()
endif()

message( STATUS "Setting up common headers." )
set( MOBILE_RT_HEADERS "${CMAKE_SOURCE_DIR}" )
set( MOBILE_RC_HEADERS "${CMAKE_SOURCE_DIR}" )
set( SCENES_HEADERS "${CMAKE_SOURCE_DIR}/Scenes" )
set( THIRD_PARTY_HEADERS "${CMAKE_SOURCE_DIR}/third_party" )
set( GLM_HEADERS "${CMAKE_SOURCE_DIR}/third_party/glm" )
set( QT_HEADERS_RT "${CMAKE_SOURCE_DIR}/System_dependent/Native/QT/MobileRT" )
set( QT_HEADERS
  "${CMAKE_SOURCE_DIR}/System_dependent/Native/Qt/build-${CMAKE_BUILD_TYPE}" )

message( STATUS "Setting up common flags." )
set( COMMON_FLAGS "${COMMON_FLAGS}" -Wall )
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -pedantic-errors -pedantic )
endif()

if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Wpedantic -Wextra -Wfatal-errors -Werror )
endif()

if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  if( DEFINED ANDROID_ABI OR CMAKE_CXX_COMPILER MATCHES "clang"
    OR (CMAKE_CXX_COMPILER MATCHES "g[++]" AND NOT CMAKE_CXX_COMPILER MATCHES "clang")
  )
    set( COMMON_FLAGS "${COMMON_FLAGS}" -std=c++11 )
  endif()
  # Turn off C++98 pedantic warning so it can use C++11 features
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-c++98-compat-pedantic )
  # Turn off padded warning because some classes still need padding
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-padded )

  # Allow '/usr/local/include' in PATH unsafe for cross-compilation
  if( NOT DEFINED ANDROID_ABI AND CMAKE_CXX_COMPILER MATCHES "clang"
    AND CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin"
  )
    CHECK_CXX_COMPILER_FLAG( "-Wno-poison-system-directories" COMPILER_SUPPORTS_FLAG )
    if( COMPILER_SUPPORTS_FLAG )
      set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-poison-system-directories )
    endif()
  endif()
endif()

if( DEFINED ANDROID_ABI OR CMAKE_CXX_COMPILER MATCHES "clang" )
  # Turn off exit time destructors warning because of the usage of
  # static variables in namespaces
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-exit-time-destructors )
endif()

if( DEFINED ANDROID_ABI
    OR CMAKE_CXX_COMPILER MATCHES "clang" AND CMAKE_HOST_SYSTEM MATCHES "Linux"
)
  message( STATUS "Detected Android OS or Clang compiler in Linux." )
  message( STATUS "Adding OpenMP." )

  # Xpreprocessor: it passes an option to the preprocessor. It is useful to
  # pass options that GCC doesn't recognize.
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Xpreprocessor -fopenmp )
  if( NOT DEFINED ANDROID_ABI )
    set( COMMON_FLAGS "${COMMON_FLAGS}" -openmp )
  endif()
else()
  message( STATUS "Detected Mac or Windows OS or GCC compiler." )

  if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
    message( STATUS "Adding OpenMP." )
    set( COMMON_FLAGS "${COMMON_FLAGS}" -Xpreprocessor -fopenmp )
  endif()
endif()

if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( COMMON_FLAGS_DEBUG -O0 -g3 -fno-optimize-sibling-calls
    -fno-omit-frame-pointer -fstack-protector-all )
endif()

set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}" -DNDEBUG -ffunction-sections -fdata-sections )
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}" -O3 )
endif()

set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}"
  -Wno-unused-command-line-argument )

message( STATUS "Setting common linker flags." )
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  set( COMMON_LINKER_FLAGS "${COMMON_LINKER_FLAGS}" -O3 -discard-all -fno-rtti )
endif()

set( COMMON_LINKER_FLAGS_DEBUG "${COMMON_LINKER_FLAGS_DEBUG}"  )

if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  message( STATUS "Detected not Windows OS." )

  set( COMMON_LINKER_FLAGS_RELEASE "${COMMON_LINKER_FLAGS_RELEASE}" -flto=full )
endif()

if( NOT CMAKE_HOST_WIN32 MATCHES "1" AND NOT CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
  message( STATUS "Detected not Windows OS." )

  if( CMAKE_CXX_COMPILER MATCHES "g[++]"
    AND NOT CMAKE_CXX_COMPILER MATCHES "clang"
  )
    message( STATUS "Add code coverage analysis flags for GCC." )

    set( COMMON_FLAGS_TEST -ftest-coverage -fprofile-arcs )
    set( COMMON_LINKER_FLAGS_TEST --coverage -lgcov )
  endif()
endif()


message( STATUS "Adding scenes source files." )
file( GLOB_RECURSE SCENES_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/Scenes/**.cpp" )
set( SCENES_SOURCES "${SCENES_SOURCES}" CACHE STRING "CUSTOM" FORCE )

if( NOT DEFINED ANDROID_ABI )
  message( STATUS "Finding Qt5." )
  find_package( Qt5 OPTIONAL_COMPONENTS Core Gui Widgets )
  if( NOT Qt5_FOUND )
    message( STATUS "Did not find Qt5. Finding Qt4." )
    find_package( Qt4 OPTIONAL_COMPONENTS QtCore QtGui )
  endif()
endif()


###############################################################################
#find_package( glm )

include_directories( SYSTEM ${CONAN_INCLUDE_DIRS_GLM} )
include_directories( SYSTEM ${CONAN_INCLUDE_DIRS_STB} )
include_directories( SYSTEM ${CONAN_INCLUDE_DIRS_TINYOBJLOADER} )
include_directories( SYSTEM ${CONAN_INCLUDE_DIRS_QT} )
#link_libraries( ${CONAN_LIB_DIRS_GLM} )
#link_libraries( ${CONAN_LIB_DIRS_STB} )
#link_libraries( ${CONAN_LIB_DIRS_QT} )
###############################################################################


###############################################################################
# Add dependencies
###############################################################################
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

  add_library( ${PROJECT_NAME} SHARED
    ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} )
  target_link_libraries( ${PROJECT_NAME} PRIVATE android jnigraphics )
else()
  message( STATUS "Adding Linux native Qt layer." )

  add_executable( ${PROJECT_NAME} ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} ${MOBILE_DEPENDENT_SOURCES_QT} )

  message( STATUS "QT_INCLUDES = ${QT_INCLUDES}" )
  message( STATUS "QT_QTCORE_LIBRARIES = ${QT_QTCORE_LIBRARIES}" )
  message( STATUS "QT_QTGUI_LIBRARIES = ${QT_QTGUI_LIBRARIES}" )

  message( STATUS "Qt5Core_INCLUDE_DIRS = " ${Qt5Core_INCLUDE_DIRS} )
  message( STATUS "Qt5Gui_INCLUDE_DIRS = " ${Qt5Gui_INCLUDE_DIRS} )
  message( STATUS "Qt5Widgets_INCLUDE_DIRS = " ${Qt5Widgets_INCLUDE_DIRS} )

  # Include Qt headers
  target_include_directories( ${PROJECT_NAME}
    SYSTEM PRIVATE ${QT_HEADERS}
    PRIVATE ${QT_HEADERS_RT} )

  if( Qt5_FOUND )
    message( STATUS "Including headers from Qt5." )
    target_include_directories( ${PROJECT_NAME}
      SYSTEM PRIVATE ${Qt5Core_INCLUDE_DIRS} ${Qt5Gui_INCLUDE_DIRS} ${Qt5Widgets_INCLUDE_DIRS} )
  elseif( Qt4_FOUND )
    message( STATUS "Including headers from Qt4." )
    target_include_directories( ${PROJECT_NAME}
      SYSTEM PRIVATE ${QT_INCLUDES} )
  endif()

  if( CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
    message( STATUS "Linking Qt for MacOS." )

    set( CMAKE_MODULE_PATH "${CMAKE_MODULE_PATH}" /usr/local/lib/python3.9/site-packages/cmake/data/CMake.app/Contents/share/cmake-3.22/Modules/ CACHE STRING "CUSTOM" FORCE )
    message( STATUS "CMAKE_MODULE_PATH = ${CMAKE_MODULE_PATH}" )

    if( NOT Qt5_FOUND )
      message( STATUS "Did not find Qt5. Finding Qt4." )
      find_package( Qt4 OPTIONAL_COMPONENTS QtCore QtGui )
    endif()

    target_link_libraries( ${PROJECT_NAME} PRIVATE -F/usr/local/lib )
    # If github action jurplel/install-qt-action was used, then Qt should be at root of project.
    target_link_libraries( ${PROJECT_NAME} PRIVATE -F${CMAKE_SOURCE_DIR}/../Qt/5.12.9/clang_64/lib )

    target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtCore" )
    target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtGui" )
    if( Qt5_FOUND )
      message( STATUS "Linking with QtWidgets from Qt5 (which Qt4 doesn't have)." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtWidgets" )
    endif()
  else()
    message( STATUS "Linking Qt." )

    if( Qt5_FOUND )
      message( STATUS "Linking with Qt5." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE Qt5::Core Qt5::Gui Qt5::Widgets )
    elseif( Qt4_FOUND )
      message( STATUS "Linking with Qt4." )
      target_link_libraries( ${PROJECT_NAME} PRIVATE Qt4::QtCore Qt4::QtGui Qt4::Widgets )
    endif()

  endif()
endif()
###############################################################################
###############################################################################


###############################################################################
# Set target properties
###############################################################################
message( STATUS "Adding debug postfix." )
set_target_properties( ${PROJECT_NAME} PROPERTIES
  DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )
###############################################################################
###############################################################################


message( STATUS "Adding MobileRT and third party headers." )
target_include_directories( ${PROJECT_NAME} PRIVATE "${MOBILE_RC_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PRIVATE "${SCENES_HEADERS}" )
target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE "${GLM_HEADERS}" )

message( STATUS "Adding compiler flags." )
target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} )
# Turn off global constructors warnings because of scenes
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-global-constructors )
endif()
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
  message( STATUS "Detected not Windows OS." )

  unset( COMPILER_SUPPORTS_FLAG )
  CHECK_CXX_COMPILER_FLAG( "-Wno-zero-as-null-pointer-constant" COMPILER_SUPPORTS_FLAG )
  if( COMPILER_SUPPORTS_FLAG )
    # Turn off zero as null pointer warning because of Qt generated code
    target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-zero-as-null-pointer-constant )
  endif()
  unset( COMPILER_SUPPORTS_FLAG )
  CHECK_CXX_COMPILER_FLAG( "-Wno-redundant-parens" COMPILER_SUPPORTS_FLAG )
  if( COMPILER_SUPPORTS_FLAG )
    # Turn off redundant parentheses warning because of Qt moc generated code
    target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-redundant-parens )
  endif()
endif()

target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:DEBUG>:${COMMON_FLAGS_TEST}> )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

if( CMAKE_CXX_COMPILER MATCHES "clang" )
  message( STATUS "Detected Clang compiler." )
  message( STATUS "Setting up flags for Clang to disable some warnings." )

  if( CMAKE_CXX_COMPILER_VERSION VERSION_LESS 8 )
    message( STATUS "Detected Clang version < 8." )

    target_compile_options( ${PROJECT_NAME} PRIVATE
      -Wno-unused-command-line-argument )
  else()
    message( STATUS "Detected Clang version >= 8." )

    CHECK_CXX_COMPILER_FLAG( "-Wno-extra-semi-stmt" COMPILER_SUPPORTS_FLAG )
    if( COMPILER_SUPPORTS_FLAG )
      target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-extra-semi-stmt )
    endif()
  endif()
else()
  message( STATUS "Detected GCC compiler." )

  if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
    target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-redundant-move )
  endif()
endif()

###############################################################################
# Link project
###############################################################################
message( STATUS "Linking with sub modules and third party libs." )
target_link_libraries( ${PROJECT_NAME}
  PRIVATE MobileRT Components
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}" "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)
###############################################################################
###############################################################################


#print_environment()
