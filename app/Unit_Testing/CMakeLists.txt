###############################################################################
# Include auxiliary functions
###############################################################################
message( STATUS "Adding helper functions." )
include( ${CMAKE_SOURCE_DIR}/CMakeLists_helper.cmake )
include( CheckCXXCompilerFlag )
print_environment()
###############################################################################
###############################################################################


###############################################################################
# Add compiler flag to auto generated MOC
###############################################################################
if( CMAKE_CXX_COMPILER MATCHES "clang" )
  # Turn off redundant parentheses warning because of Qt moc generated code
  set( CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-redundant-parens" CACHE STRING "CUSTOM" FORCE )
endif()
###############################################################################
###############################################################################


###############################################################################
# Set up project
###############################################################################
message( STATUS "Setting up Unit Tests project." )
project( UnitTests VERSION 1.0.0.0 LANGUAGES CXX )

if( ANDROID_PLATFORM STREQUAL "android-15" OR ANDROID_PLATFORM STREQUAL "android-16")
  message( STATUS "Disabling PIE-style executable for Unit Tests, so it also works on Android API 15" )
  add_link_options( -no-pie )
endif()

message( STATUS "Adding unit tests source files." )
file( GLOB GTEST_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/*.cpp" )

if( NOT DEFINED ANDROID_ABI )
  message( STATUS "Adding native Ray Tracing engine tests if it's not for Android, so the code coverage is more accurate." )
  message( STATUS "Also, since these engine tests use the native 'System_Dependent' dependency, it won't work in Android." )
  file( GLOB GTEST_SOURCES_RAY_TRACING "${CMAKE_CURRENT_SOURCE_DIR}/engine/*.cpp" )
  message( STATUS "Adding the following test classes: ${GTEST_SOURCES_RAY_TRACING}" )
  list( APPEND GTEST_SOURCES ${GTEST_SOURCES_RAY_TRACING} )
endif()

message( STATUS "Creating unit test executable with the following test classes: ${GTEST_SOURCES}" )
add_executable( ${PROJECT_NAME} ${GTEST_SOURCES} ${MOBILE_DEPENDENT_SOURCES} ${SCENES_SOURCES} )

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
# Set target properties
###############################################################################
message( STATUS "Adding debug postfix." )
set_target_properties( ${PROJECT_NAME} PROPERTIES
  DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )
###############################################################################
###############################################################################


###############################################################################
# Set compiler flags
###############################################################################
# Add dependencies headers installed from conan package manager.
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC ${CMAKE_INCLUDE_PATH} )
target_link_libraries( ${PROJECT_NAME} PRIVATE ${gtest_LIBRARIES_TARGETS} )

message( STATUS "Adding compiler flags." )
target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:DEBUG>:${COMMON_FLAGS_TEST}> )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE_WINDOWS_2022}> )

# Turn off warnings because of Google Test
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-global-constructors )
  target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-used-but-marked-unused )
  target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-covered-switch-default )
  # Necessary because in MacOS it gives: error: redundant move in return statement
  target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-redundant-move )
endif()
###############################################################################
###############################################################################


###############################################################################
# Link project
###############################################################################
if( DEFINED ANDROID_ABI )
  message( STATUS "Adding Android JNI layer." )
  target_link_libraries( ${PROJECT_NAME} PRIVATE android jnigraphics )
endif()

message( STATUS "Linking with MobileRT and Google Test." )
target_link_libraries( ${PROJECT_NAME} PRIVATE -L/usr/local/lib )
target_link_libraries( ${PROJECT_NAME}
  PRIVATE MobileRT Components gtest
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}" "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}" )
###############################################################################
###############################################################################
