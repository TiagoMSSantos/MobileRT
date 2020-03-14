project( GoogleTest VERSION 1.0.0.0 LANGUAGES CXX )

file( GLOB_RECURSE GTEST_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/**.cpp" )

add_executable( ${PROJECT_NAME} ${GTEST_SOURCES} )
add_coverage( ${PROJECT_NAME} )
set_target_properties( ${PROJECT_NAME} PROPERTIES DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )

target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "${THIRD_PARTY_HEADERS}/googletest/googletest/include" )
target_include_directories( ${PROJECT_NAME} SYSTEM PUBLIC "/usr/include/libcxxabi" )

target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} -Wno-global-constructors )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

target_link_libraries( ${PROJECT_NAME}
PRIVATE Components gtest
general "${COMMON_LINKER_FLAGS}"
debug "${COMMON_LINKER_FLAGS_DEBUG}"
optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)
