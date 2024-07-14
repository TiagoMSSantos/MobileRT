###############################################################################
# Set up project
###############################################################################
message( STATUS "Setting up MobileRT project." )
project( MobileRT VERSION 1.0.0.0 LANGUAGES CXX )

message( STATUS "Adding MobileRT source files." )
file( GLOB_RECURSE MOBILE_RT_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/**.cpp" )

message( STATUS "Setting up as library." )
if( DEFINED ANDROID_ABI )
  add_library( ${PROJECT_NAME} SHARED ${MOBILE_RT_SOURCES} )
else()
  if( CMAKE_HOST_SYSTEM MATCHES "Linux" )
    add_library( ${PROJECT_NAME} SHARED ${MOBILE_RT_SOURCES} )
  else()
    add_library( ${PROJECT_NAME} STATIC ${MOBILE_RT_SOURCES} )
  endif()
endif()
###############################################################################
###############################################################################


###############################################################################
# Set headers
###############################################################################
message( STATUS "Adding MobileRT and third party headers." )
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_RT_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_DEPENDENT_HEADERS}" )
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "${GLM_HEADERS}" )
###############################################################################
###############################################################################


###############################################################################
# Set compiler flags
###############################################################################
message( STATUS "Adding compiler flags." )
if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
  # -Wno-date-time because of PCG
  target_compile_options( ${PROJECT_NAME} PUBLIC -Wno-date-time )
endif()
target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:DEBUG>:${COMMON_FLAGS_TEST}> )
target_compile_options( ${PROJECT_NAME} PRIVATE
  $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )
###############################################################################
###############################################################################

if( DEFINED ANDROID_ABI )
  target_link_libraries( ${PROJECT_NAME} PUBLIC log )
else()
  if( CMAKE_HOST_SYSTEM MATCHES "Linux" )
    message( STATUS "Detected Linux." )
  endif()
  message( STATUS "Native OS: Linking with pthread." )

  if( NOT "${CMAKE_GENERATOR}" MATCHES "Visual Studio" )
    target_link_libraries( ${PROJECT_NAME} PUBLIC pthread )
  endif()
endif()

###############################################################################
# Link project
###############################################################################
message( STATUS "Linking with third party libs." )
target_link_libraries( ${PROJECT_NAME}
  PRIVATE 
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}" "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}" )
###############################################################################
###############################################################################
