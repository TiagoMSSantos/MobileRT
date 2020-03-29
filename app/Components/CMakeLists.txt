# Rendering components Project
project( Components VERSION 1.0.0.0 LANGUAGES CXX )

# Add Components source files
file( GLOB_RECURSE MOBILE_RC_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/**.cpp" )

# Set as library
add_library( ${PROJECT_NAME} SHARED ${MOBILE_RC_SOURCES} )

# Add debug postfix
set_target_properties( ${PROJECT_NAME} PROPERTIES DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )

# Add MobileRT and third party headers
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "${THIRD_PARTY_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_RT_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_RC_HEADERS}" )

# Add compiler flags
target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_TEST}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

# Link with MobileRT and third party libs
target_link_libraries( ${PROJECT_NAME}
PUBLIC tinyobjloader MobileRT PRIVATE
    general "${COMMON_LINKER_FLAGS}"
    debug "${COMMON_LINKER_FLAGS_DEBUG}"
    debug "${COMMON_LINKER_FLAGS_TEST}"
    optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)
