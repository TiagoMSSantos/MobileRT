# Unit Tests Project
project( UnitTests VERSION 1.0.0.0 LANGUAGES CXX )

# Add unit tests source files
file( GLOB_RECURSE GTEST_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/**.cpp" )

# Create executable
add_executable( ${PROJECT_NAME} ${GTEST_SOURCES} )

# Add debug postfix
set_target_properties( ${PROJECT_NAME} PROPERTIES DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )
set_target_properties( ${PROJECT_NAME} PROPERTIES BUNDLE TRUE )

# Add dependencies
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "${THIRD_PARTY_HEADERS}/googletest/googletest/include" )
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "/usr/include/libcxxabi" )

# Add compiler flags
target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} -Wno-global-constructors -Wno-used-but-marked-unused -Wno-covered-switch-default )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

# Link with tested projects
target_link_libraries( ${PROJECT_NAME}
    PRIVATE Components gtest
    general "${COMMON_LINKER_FLAGS}"
    debug "${COMMON_LINKER_FLAGS_DEBUG}"
    optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)
