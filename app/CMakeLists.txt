# Recommended CMake version: 3.0+
cmake_minimum_required( VERSION 3.0...3.0 FATAL_ERROR )
cmake_policy( VERSION 3.0 )

# Add helper functions
include( ${CMAKE_CURRENT_SOURCE_DIR}/CMakeLists_helper.cmake )

# Common flags for all projects
set( COMMON_FLAGS "" )
set( COMMON_FLAGS_DEBUG "" )
set( COMMON_FLAGS_RELEASE "" )

# Whether CMake will handle the Qt moc preprocessor automatically
set( CMAKE_AUTOMOC ON )
set( CMAKE_AUTOUIC ON )
set( CMAKE_AUTORCC ON )
set( CMAKE_INCLUDE_CURRENT_DIR ON )
set( CMAKE_MACOSX_RPATH OFF )
set( CMAKE_INSTALL_RPATH_USE_LINK_PATH ON )

# Set the name of the operating system and the proper interface layer
if( DEFINED ANDROID_ABI )
  set( CMAKE_SYSTEM_NAME "Android" )
  set( MOBILE_DEPENDENT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Android_JNI" )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -stdlib=libc++ )
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

if( DEFINED ANDROID_ABI )
  set( CMAKE_CXX98_EXTENSION_COMPILE_OPTION "-std=c++98" CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_CXX11_EXTENSION_COMPILE_OPTION "-std=c++11" CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_CXX14_EXTENSION_COMPILE_OPTION "-std=c++14" CACHE STRING "CUSTOM" FORCE )
  set( CMAKE_CXX17_EXTENSION_COMPILE_OPTION "-std=c++17" CACHE STRING "CUSTOM" FORCE )
endif()

set( CMAKE_CXX_FLAGS "-std=c++11 -fPIC" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_DEBUG "-D_GLIBCXX_DEBUG" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG -ffunction-sections -fdata-sections"
  CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_EXE_LINKER_FLAGS "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_RELEASE "-O3 -discard-all -fno-rtti -flto=full"
  CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_SHARED_LINKER_FLAGS "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_RELEASE "-O3 -discard-all -fno-rtti -flto=full"
  CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

message( STATUS "CMAKE_HOST_SYSTEM_NAME = ${CMAKE_HOST_SYSTEM_NAME}" )
if( NOT DEFINED ANDROID_ABI AND NOT CMAKE_HOST_SYSTEM_NAME MATCHES "Linux" )
  set( CMAKE_SHARED_LIBRARY_SONAME_CXX_FLAG "-Wl,-install_name," CACHE STRING "CUSTOM" FORCE )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-reserved-id-macro )
endif()

if( NOT DEFINED ANDROID_ABI AND CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-deprecated )
  if( CMAKE_CXX_COMPILER MATCHES "clang" )
    set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-signed-enum-bitfield -Wno-sign-conversion )
  endif()
endif()

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
set( QT_HEADERS_RT "${CMAKE_SOURCE_DIR}/System_dependent/Linux/QT/MobileRT" )
set( QT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Linux/Qt/build-${CMAKE_BUILD_TYPE}" )

# Set common flags
set( COMMON_FLAGS "${COMMON_FLAGS}"
  -std=c++11 -pedantic-errors -pedantic -Wpedantic -Wall -Wextra -Wfatal-errors -Werror )
set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-c++98-compat-pedantic -Wno-padded )
set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-exit-time-destructors )

if( DEFINED ANDROID_ABI OR CMAKE_CXX_COMPILER MATCHES "clang" AND CMAKE_HOST_SYSTEM MATCHES "Linux" )
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Xpreprocessor -fopenmp=libomp )
else()
  set( COMMON_FLAGS "${COMMON_FLAGS}" -Xpreprocessor -fopenmp )
endif()

set( COMMON_FLAGS_TEST -ftest-coverage -fprofile-arcs )

set( COMMON_FLAGS_DEBUG -O0 -g3 -fno-optimize-sibling-calls -fno-omit-frame-pointer )

set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}" -O3 -DNDEBUG -ffunction-sections -fdata-sections )

set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}" -Wno-unused-command-line-argument )

# Set common linker flags
set( COMMON_LINKER_FLAGS ${COMMON_LINKER_FLAGS} -O3 -discard-all -fno-rtti )

set( COMMON_LINKER_FLAGS_TEST ${COMMON_LINKER_FLAGS_TEST} --coverage )

set( COMMON_LINKER_FLAGS_DEBUG ${COMMON_LINKER_FLAGS_DEBUG}  )

set( COMMON_LINKER_FLAGS_RELEASE ${COMMON_LINKER_FLAGS_RELEASE} -flto=full )

# Project
project( AppMobileRT VERSION 1.0.0.0 LANGUAGES CXX )

# Specify the maximum number of lines of a function to be inlined
#include( CheckCXXCompilerFlag )
#check_cxx_compiler_flag( "-finline-limit=150" FLAG__FINLINE_LIMIT__SUPPORTED )
#if( FLAG__FINLINE_LIMIT__SUPPORTED )
#  set( COMMON_FLAGS "${COMMON_FLAGS}" -finline-limit=150 )
#endif()

# Add Scenes source files
file( GLOB_RECURSE SCENES_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/Scenes/**.cpp" )

# Add libs for the interface layer
if( DEFINED ANDROID_ABI )
  # Java Native Interface
  add_library( ${PROJECT_NAME} SHARED ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} )
  target_link_libraries( ${PROJECT_NAME} PRIVATE android jnigraphics )

  # Add sub directories
  add_subdirectory( MobileRT )
  add_subdirectory( third_party )
  add_subdirectory( Components )
  add_subdirectory( System_dependent )
  add_subdirectory( Unit_Testing )
else()
  # Linux interface
  find_package( Qt4 REQUIRED COMPONENTS QtCore QtGui QtXml )

  # Add sub directories
  add_subdirectory( MobileRT )
  add_subdirectory( third_party )
  add_subdirectory( Components )
  add_subdirectory( System_dependent )
  add_subdirectory( Unit_Testing )

  add_executable( ${PROJECT_NAME} ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} )

  message( STATUS "QT_INCLUDES = ${QT_INCLUDES}" )
  message( STATUS "QT_QTCORE_LIBRARIES = ${QT_QTCORE_LIBRARIES}" )
  message( STATUS "QT_QTGUI_LIBRARIES = ${QT_QTGUI_LIBRARIES}" )
  message( STATUS "QT_QTXML_LIBRARIES = ${QT_QTXML_LIBRARIES}" )
  target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE ${QT_HEADERS} ${QT_INCLUDES} PRIVATE ${QT_HEADERS_RT} )

  if( CMAKE_HOST_SYSTEM_NAME MATCHES "Darwin" )
    target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE /usr/include/qt4 )
    target_include_directories( ${PROJECT_NAME} SYSTEM PRIVATE /usr/local/include/ )
    target_link_libraries( ${PROJECT_NAME} PRIVATE /usr/local/Cellar/qt@4/4.8.7_6/lib/QtGui.framework/Versions/4/Headers/ )
    target_link_libraries( ${PROJECT_NAME} PRIVATE /usr/local/lib/QtGui.framework/Headers/ )
    target_link_libraries( ${PROJECT_NAME} PRIVATE -F/usr/local/lib )

#    target_link_libraries( ${PROJECT_NAME} PRIVATE -F${QT_QTCORE_LIBRARIES} )
#    target_link_libraries( ${PROJECT_NAME} PRIVATE -F${QT_QTGUI_LIBRARIES} )
#    target_link_libraries( ${PROJECT_NAME} PRIVATE -F${QT_QTXML_LIBRARIES} )
    target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtCore" )
    target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtGui" )
    target_link_libraries( ${PROJECT_NAME} PRIVATE "-framework QtXml" )

#    target_link_libraries( ${PROJECT_NAME} PRIVATE -framework Qt4::QtCore )
#    target_link_libraries( ${PROJECT_NAME} PRIVATE -framework Qt4::QtGui )
#    target_link_libraries( ${PROJECT_NAME} PRIVATE -framework Qt4::QtXml )
#    target_link_libraries( ${PROJECT_NAME} PRIVATE -framework ${QT_QTCORE_LIBRARIES} )
#    target_link_libraries( ${PROJECT_NAME} PRIVATE -framework ${QT_QTGUI_LIBRARIES} )
#    target_link_libraries( ${PROJECT_NAME} PRIVATE -framework ${QT_QTXML_LIBRARIES} )
  else()
    target_link_libraries( ${PROJECT_NAME} PRIVATE Qt4::QtCore Qt4::QtGui Qt4::QtXml )
    target_link_libraries( ${PROJECT_NAME} PRIVATE ${QT_QTCORE_LIBRARIES} ${QT_QTGUI_LIBRARIES} ${QT_QTXML_LIBRARIES} )
  endif()
endif()

# Add debug postfix
set_target_properties( ${PROJECT_NAME} PROPERTIES DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )
set_target_properties( ${PROJECT_NAME} PROPERTIES BUNDLE TRUE )

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

# Flags for Clang to disable some warnings
if( CMAKE_CXX_COMPILER MATCHES "clang" )
  if( CMAKE_CXX_COMPILER_VERSION VERSION_GREATER 8 )
    target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} -Wno-extra-semi-stmt )
  endif()
  if( CMAKE_CXX_COMPILER_VERSION VERSION_LESS 8 )
    target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} -Wno-unused-command-line-argument )
  endif()
else()
  target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} -Wno-redundant-move )
endif()

# Link with subprojects and third party libs
target_link_libraries( ${PROJECT_NAME}
  PRIVATE glm Components tinyobjloader
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}"
  debug "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)

print_environment()
