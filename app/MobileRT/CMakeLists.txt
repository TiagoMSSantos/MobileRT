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
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_DEPENDENT_HEADERS}"
)
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "${GLM_HEADERS}" )
###############################################################################
###############################################################################


###############################################################################
# Set compiler flags
###############################################################################
message( STATUS "Adding compiler flags." )
if( NOT CMAKE_HOST_WIN32 MATCHES "1" )
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
  message( STATUS "Android OS: Linking with JNI log and static OpenMP." )

  target_link_libraries( ${PROJECT_NAME} PRIVATE libomp.a )
  target_link_libraries( ${PROJECT_NAME} PUBLIC log )
else()
  if( CMAKE_HOST_SYSTEM MATCHES "Linux" )
    if( CMAKE_CXX_COMPILER MATCHES "clang" )
      message( STATUS "Linux OS: Linking Clang with dynamic OpenMP." )

      target_link_libraries( ${PROJECT_NAME} PRIVATE -fopenmp=libomp )
    else()
      message( STATUS "Linux OS: Linking GCC with dynamic OpenMP." )

      target_link_libraries( ${PROJECT_NAME} PRIVATE -fopenmp )
    endif()
  else()
    if( CMAKE_CXX_COMPILER_ID MATCHES "[Cc]lang" )
      message( STATUS "Native OS: Linking Clang with dynamic OpenMP." )

      target_link_libraries( ${PROJECT_NAME} PRIVATE omp )
    else()
      message( STATUS "Native OS: Linking GCC with dynamic OpenMP." )

      if( NOT CMAKE_HOST_WIN32 MATCHES "1" AND NOT CMAKE_HOST_SYSTEM MATCHES "Darwin" )
        target_link_libraries( ${PROJECT_NAME} PRIVATE gomp )
      else()
        target_link_libraries( ${PROJECT_NAME} PRIVATE -fopenmp )
      endif()
    endif()
  endif()
  message( STATUS "Native OS: Linking with pthread." )
  if( CMAKE_HOST_WIN32 MATCHES "1" )
    find_package( Threads REQUIRED )
    find_package( OpenMP REQUIRED )
    target_link_libraries( ${PROJECT_NAME} PRIVATE OpenMP::OpenMP_CXX )
  else()
    target_link_libraries( ${PROJECT_NAME} PUBLIC pthread )
  endif()
endif()

###############################################################################
# Link project
###############################################################################
message( STATUS "Linking with third party libs." )
target_link_libraries( ${PROJECT_NAME}
  PRIVATE glm
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}" "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}" )
###############################################################################
###############################################################################
