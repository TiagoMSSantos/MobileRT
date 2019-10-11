cmake_minimum_required( VERSION 3.6.0 )

# Set CMAKE_SYSTEM_NAME
if( DEFINED ANDROID_ABI )
	set( CMAKE_SYSTEM_NAME "Android" )
	set( MOBILE_DEPENDENT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Android_JNI" )
	set( COMMON_FLAGS "" )
	set( COMMON_FLAGS_RELEASE "-Wno-unused-command-line-argument" )
else()
	set( CMAKE_SYSTEM_NAME "Linux" )
	set( MOBILE_DEPENDENT_HEADERS "${CMAKE_SOURCE_DIR}/System_dependent/Linux" )
	set( COMMON_FLAGS "${COMMON_FLAGS}" -march=native )
	set( COMMON_FLAGS_RELEASE "" )

	# Add postfix when build type is debug
	string( TOLOWER "${CMAKE_BUILD_TYPE}" CMAKE_BUILD_TYPE_L )
	if( CMAKE_BUILD_TYPE_L MATCHES debug )
			set( CMAKE_DEBUG_POSTFIX "d" )
	endif()
endif()

# Remove default flags
set( ANDROID_COMPILER_FLAGS "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_CXX_FLAGS "-fPIC -Wno-weak-vtables" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_DEBUG "-D_GLIBCXX_DEBUG" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG -ffunction-sections -fdata-sections -fno-exceptions" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_CXX_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_EXE_LINKER_FLAGS "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_RELEASE "-Wl,-gc-sections -O3 -discard-all -fno-rtti -fno-exceptions -s -flto=full" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_EXE_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

set( CMAKE_SHARED_LINKER_FLAGS "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_DEBUG "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_RELEASE "-Wl,-gc-sections -O3 -discard-all -fno-rtti -fno-exceptions -s -flto=full" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_RELWITHDEBINFO "" CACHE STRING "CUSTOM" FORCE )
set( CMAKE_SHARED_LINKER_FLAGS_MINSIZEREL "" CACHE STRING "CUSTOM" FORCE )

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
set( GSL_HEADERS "${CMAKE_SOURCE_DIR}/third_party/GSL/include" )
set( GLM_HEADERS "${CMAKE_SOURCE_DIR}/third_party/glm" )

# Set common flags
set( COMMON_FLAGS "${COMMON_FLAGS}" -std=c++17 -pedantic-errors -Weverything -pedantic -Wpedantic -Wall -Wextra -Wfatal-errors -Werror -ferror-limit=1 )
set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-c++98-compat-pedantic -Wno-padded )
set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-exit-time-destructors -Wno-global-constructors )
set( COMMON_FLAGS "${COMMON_FLAGS}" -Wno-gnu-anonymous-struct -Wno-nested-anon-types )

set( COMMON_FLAGS_DEBUG -O0 -g3 -fno-optimize-sibling-calls -fno-omit-frame-pointer )

set( COMMON_FLAGS_RELEASE "${COMMON_FLAGS_RELEASE}" -O3 -DNDEBUG -ffunction-sections -fdata-sections -flto=full )

# Set common linker flags
set( COMMON_LINKER_FLAGS -Wl,-gc-sections -O3 -discard-all -fno-rtti -fno-exceptions )

set( COMMON_LINKER_FLAGS_DEBUG  )

set( COMMON_LINKER_FLAGS_RELEASE -s -flto=full )

# Add sub directories
add_subdirectory( MobileRT )
add_subdirectory( Components )
add_subdirectory( third_party )
add_subdirectory( System_dependent )
add_subdirectory( Unit_Testing )

# Project
project( AppInterface VERSION 1.0.0.0 LANGUAGES CXX )

include( CheckCXXCompilerFlag )
CHECK_CXX_COMPILER_FLAG( "-finline-limit=150" FLAG__FINLINE_LIMIT__SUPPORTED )

if( FLAG__FINLINE_LIMIT__SUPPORTED )
	set( COMMON_FLAGS "${COMMON_FLAGS}" -finline-limit=150 )
endif()

file( GLOB_RECURSE SCENES_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/Scenes/**.cpp" )

if( DEFINED ANDROID_ABI )
	add_library( ${PROJECT_NAME} SHARED ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} )
	target_link_libraries( ${PROJECT_NAME} PRIVATE android jnigraphics )
else ()
	find_package( GTK2 REQUIRED )
	add_executable( ${PROJECT_NAME} ${SCENES_SOURCES} ${MOBILE_DEPENDENT_SOURCES} )

	target_include_directories( ${PROJECT_NAME} PRIVATE "${GTK2_INCLUDE_DIRS}" )
	target_compile_options( ${PROJECT_NAME} PRIVATE -Wno-reserved-id-macro -Wno-documentation-unknown-command -Wno-old-style-cast -Wno-documentation -Wno-zero-as-null-pointer-constant -Wno-redundant-parens -Wno-unused-variable -Wno-weak-vtables -Wno-sign-conversion )
	target_link_libraries( ${PROJECT_NAME} PRIVATE "${GTK2_LIBRARIES}" GSL )
endif()

set_target_properties( ${PROJECT_NAME} PROPERTIES DEBUG_POSTFIX "${CMAKE_DEBUG_POSTFIX}" )

target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_RC_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${SCENES_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${MOBILE_APP_SOURCES}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${GSL_HEADERS}" )
target_include_directories( ${PROJECT_NAME} PUBLIC "${GLM_HEADERS}" )

target_compile_options( ${PROJECT_NAME} PRIVATE ${COMMON_FLAGS} )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:DEBUG>:${COMMON_FLAGS_DEBUG}> )
target_compile_options( ${PROJECT_NAME} PRIVATE $<$<CONFIG:RELEASE>:${COMMON_FLAGS_RELEASE}> )

#target_compile_options( ${PROJECT_NAME} PRIVATE $<$<NOT:$<BOOL:ANDROID_ABI MATCHES "^mips(64)?$" >>:-flto=full> )

target_link_libraries( ${PROJECT_NAME}
PRIVATE glm Components tinyobjloader
general "${COMMON_LINKER_FLAGS}"
debug "${COMMON_LINKER_FLAGS_DEBUG}"
optimized "${COMMON_LINKER_FLAGS_RELEASE}"
)

# Print all set variables
get_cmake_property( _variableNames VARIABLES )
foreach ( _variableName ${_variableNames} )
  #message( STATUS "${_variableName}=${${_variableName}}" )
endforeach()
#message( FATAL_ERROR "ACABOU" )
