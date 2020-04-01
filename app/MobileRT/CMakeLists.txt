# MobileRT Project
project( MobileRT VERSION 1.0.0.0 LANGUAGES CXX )

# Add MobileRT source files
file( GLOB_RECURSE MOBILE_RT_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/**.cpp" )

# Set as library
add_library( ${PROJECT_NAME} SHARED ${MOBILE_RT_SOURCES} )

# Add debug postfix
set_target_properties( ${PROJECT_NAME} PROPERTIES DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )

# Add MobileRT and third party headers
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_RT_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_DEPENDENT_HEADERS}" )
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "${GLM_HEADERS}" )

# Add compiler flags
target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_TEST}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

if( DEFINED ANDROID_ABI )
  # Link with JNI log and static OpenMP for Android
  target_link_libraries( ${PROJECT_NAME} PUBLIC log libomp.a )
else()
  # Link with pthread and dynamic OpenMP for Linux
  target_link_libraries( ${PROJECT_NAME} PUBLIC pthread )
  if( CMAKE_CXX_COMPILER MATCHES "clang" )
    target_link_libraries( ${PROJECT_NAME} PUBLIC -fopenmp=libomp )
  else()
    target_link_libraries( ${PROJECT_NAME} PUBLIC -fopenmp )
  endif()
endif()

# Link with third party libs
target_link_libraries( ${PROJECT_NAME}
  PRIVATE glm atomic
  general "${COMMON_LINKER_FLAGS}"
  debug "${COMMON_LINKER_FLAGS_DEBUG}"
  debug "${COMMON_LINKER_FLAGS_TEST}"
  optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)
