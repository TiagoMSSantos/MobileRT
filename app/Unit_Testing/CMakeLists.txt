###############################################################################
# Set up project
###############################################################################
message( STATUS "Setting up Unit Tests project." )
project( UnitTests VERSION 1.0.0.0 LANGUAGES CXX )

message( STATUS "Adding unit tests source files." )
file( GLOB_RECURSE GTEST_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/**.cpp" )

message( STATUS "Creating executable." )
add_executable( ${PROJECT_NAME} ${GTEST_SOURCES} ${MOBILE_DEPENDENT_SOURCES} ${SCENES_SOURCES} )
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
message( STATUS "Adding compiler flags." )
target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

# Turn off warnings because of Google Test
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
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
message( STATUS "Linking with MobileRT and Google Test." )
target_link_libraries( ${PROJECT_NAME}
  PRIVATE MobileRT Components gtest
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}" "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}" )
###############################################################################
###############################################################################
