###############################################################################
# Set up project
###############################################################################
message( STATUS "Setting up Rendering Components project." )
project( Components VERSION 1.0.0.0 LANGUAGES CXX )

message( STATUS "Adding Components source files." )
file( GLOB_RECURSE MOBILE_RC_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/**.cpp" )

message( STATUS "Setting up as library." )
if( DEFINED ANDROID_ABI )
  add_library( ${PROJECT_NAME} SHARED ${MOBILE_RC_SOURCES} )
else()
  add_library( ${PROJECT_NAME} STATIC ${MOBILE_RC_SOURCES} )
endif()

###############################################################################
###############################################################################


###############################################################################
# Set headers
###############################################################################
message( STATUS "Adding MobileRT and third party headers." )
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC
  "${THIRD_PARTY_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PRIVATE "${MOBILE_RT_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_RC_HEADERS}" )
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
  $<$<CONFIG:DEBUG>:${COMMON_FLAGS_TEST}> )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )
###############################################################################
###############################################################################


###############################################################################
# Link project
###############################################################################
message( STATUS "Linking with MobileRT and third party libs." )
target_link_libraries( ${PROJECT_NAME}
  PRIVATE MobileRT
  PUBLIC tinyobjloader
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}" "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}" )
###############################################################################
###############################################################################
