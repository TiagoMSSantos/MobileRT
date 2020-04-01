# Recommended CMake version: 3.0+
cmake_minimum_required( VERSION 3.0...3.0 FATAL_ERROR )
cmake_policy( VERSION 3.0 )

# Add helper functions
include(${CMAKE_CURRENT_SOURCE_DIR}/CMakeLists_helper.cmake)

# Common flags for all projects
set( COMMON_FLAGS "" )
set( COMMON_FLAGS_DEBUG "" )
set( COMMON_FLAGS_RELEASE "" )

# Whether CMake will handle the Qt moc preprocessor automatically
set( CMAKE_AUTOMOC ON )

# Set the name of the operating system and the proper interface layer
if( DEFINED ANDROID_ABI )
  set( CMAKE_SYSTEM_NAME "Android" )
  set( MOBILE_DEPENDENT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Android_JNI" )
else()
  set( CMAKE_SYSTEM_NAME "Linux" )
  set( MOBILE_DEPENDENT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Linux" )
  # Tell C++ compiler that it should produce optimized code for the current CPU architecture used
  set( COMMON_FLAGS "${COMMON_FLAGS}" -march=native )

  # Add postfix when build type is debug
  string( TOLOWER "${CMAKE_BUILD_TYPE}" CMAKE_BUILD_TYPE_L )
  if( CMAKE_BUILD_TYPE_L MATCHES debug )
    set( CMAKE_DEBUG_POSTFIX "d" )
  endif()
endif()

# Add warnings and Link Time Optimization when using Clang compiler
if( CMAKE_CXX_COMPILER MATCHES "clang" OR DEFINED ANDROID_ABI )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Weverything -ferror-limit=1 )
  set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}" -flto=full )
endif()

# Add flags for code coverage analysis when using GCC compiler
if( CMAKE_CXX_COMPILER MATCHES "gcc" )
  set( COMMON_LINKER_FLAGS_TEST -lgcov )
endif()

# Remove default flags
set( ANDROID_COMPILER_FLAGS "" CACHE STRING "CUSTOM" FORCE )

# Add custom default flags for all third party projects
set( CMAKE_CXX_FLAGS "-fPIC" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_DEBUG "-D_GLIBCXX_DEBUG" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG -ffunction-sections -fdata-sections"
  CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_EXE_LINKER_FLAGS "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_RELEASE "-Wl,-gc-sections -O3 -discard-all -fno-rtti -s -flto=full"
  CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_SHARED_LINKER_FLAGS "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_RELEASE "-Wl,-gc-sections -O3 -discard-all -fno-rtti -s -flto=full"
  CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

# Turn off glm test
set( GLM_TEST_ENABLE "OFF" CACHE STRING "CUSTOM" FORCE )

# Set default lib and bin folders
set( CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib )
set( CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib )
set( CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin )

# Set common headers
set( MOBILE_RT_HEADERS "${CMAKE_SOURCE_DIR}" )
set( MOBILE_RC_HEADERS "${CMAKE_SOURCE_DIR}" )
set( SCENES_HEADERS "${CMAKE_SOURCE_DIR}/Scenes" )
set( THIRD_PARTY_HEADERS "${CMAKE_SOURCE_DIR}/third_party" )
set( GLM_HEADERS "${CMAKE_SOURCE_DIR}/third_party/glm" )
set( QT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Linux/QT/MobileRT" )
set( QT_HEADERS "${QT_HEADERS}"
  "${CMAKE_SOURCE_DIR}/System_dependent/Linux/Qt/build-${CMAKE_BUILD_TYPE}"
)

# Set common flags
set( COMMON_FLAGS "${COMMON_FLAGS}"
  -std=c++11 -pedantic-errors -pedantic -Wpedantic -Wall -Wextra -Wfatal-errors -Werror )
set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-c++98-compat-pedantic -Wno-padded )
set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-exit-time-destructors )

if( CMAKE_CXX_COMPILER MATCHES "clang" )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -fopenmp=libomp )
else()
  set( COMMON_FLAGS "${COMMON_FLAGS}" -fopenmp )
endif()

set( COMMON_FLAGS_TEST -ftest-coverage -fprofile-arcs )

set( COMMON_FLAGS_DEBUG -O0 -g3 -fno-optimize-sibling-calls -fno-omit-frame-pointer )

set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}" -O3 -DNDEBUG -ffunction-sections -fdata-sections )

# Set common linker flags
set( COMMON_LINKER_FLAGS -Wl,-gc-sections -O3 -discard-all -fno-rtti )

set( COMMON_LINKER_FLAGS_TEST "${COMMON_LINKER_FLAGS_TEST}" --coverage )

set( COMMON_LINKER_FLAGS_DEBUG "" )

set( COMMON_LINKER_FLAGS_RELEASE -s -flto=full )

# Project
project( AppMobileRT VERSION 1.0.0.0 LANGUAGES CXX )

# Add sub directories
add_subdirectory( MobileRT )
add_subdirectory( third_party )
add_subdirectory( Components )
add_subdirectory( System_dependent )
add_subdirectory( Unit_Testing )

# Specify the maximum number of lines of a function to be inlined
include( CheckCXXCompilerFlag )
check_cxx_compiler_flag( "-finline-limit=150" FLAG__FINLINE_LIMIT__SUPPORTED )
if( FLAG__FINLINE_LIMIT__SUPPORTED )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -finline-limit=150 )
endif()

# Add Scenes source files
file( GLOB_RECURSE SCENES_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/Scenes/**.cpp" )

# Add libs for the interface layer
if( DEFINED ANDROID_ABI )
  # Java Native Interface
  add_library( ${PROJECT_NAME} SHARED ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} )
  target_link_libraries( ${PROJECT_NAME} PRIVATE android jnigraphics )
else ()
  # Linux interface
  find_package( Qt4 REQUIRED QtGui QtXml QtCore )
  add_executable( ${PROJECT_NAME} ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} )

  target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE "${QT_HEADERS}" )
  target_link_libraries( ${PROJECT_NAME} PRIVATE Qt4::QtGui Qt4::QtXml Qt4::QtCore )
endif()

# Add debug postfix
set_target_properties( ${PROJECT_NAME} PROPERTIES DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )

# Add MobileRT and third party headers
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_RC_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${SCENES_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_APP_SOURCES}" )
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "${GLM_HEADERS}" )

# Add compiler flags
target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} -Wno-global-constructors
  -Wno-zero-as-null-pointer-constant -Wno-redundant-parens )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_TEST}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

if( CMAKE_CXX_COMPILER MATCHES "clang" AND CMAKE_CXX_COMPILER_VERSION VERSION_GREATER 6 )
  target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} -Wno-extra-semi-stmt )
endif()

# Link with subprojects and third party libs
target_link_libraries( ${PROJECT_NAME}
  PRIVATE glm Components tinyobjloader
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}"
  debug "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)

#print_environment()
