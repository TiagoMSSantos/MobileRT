###############################################################################
# Set up CMake jobs
###############################################################################
message( STATUS "Setting the number of CMake jobs as the number of CPU cores." )
include( ProcessorCount )
ProcessorCount( JOBS )
###############################################################################
###############################################################################


###############################################################################
# Clone third party repositories
###############################################################################
message( STATUS "Cloning third party repositories." )
execute_process( COMMAND git clone https://github.com/g-truc/glm
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/syoyo/tinyobjloader
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/google/googletest
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/nothings/stb
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/boostorg/boost
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )
###############################################################################
###############################################################################


###############################################################################
# Update third party repositories
###############################################################################
message( STATUS "Updating third party repositories." )
execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/glm )
execute_process( COMMAND git pull --all --rebase --recurse-submodules
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/glm )

execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )
execute_process( COMMAND git pull --all --rebase --recurse-submodules
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )

execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )
execute_process( COMMAND git pull --all --rebase --recurse-submodules
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )

execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )
execute_process( COMMAND git pull --all --rebase --recurse-submodules
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )

execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
execute_process( COMMAND git pull --all --rebase --recurse-submodules
  --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
###############################################################################
###############################################################################


###############################################################################
# Add third party dependencies
###############################################################################
message( STATUS "Adding third party dependencies." )
add_subdirectory( glm )
add_subdirectory( tinyobjloader )


# Remove Link Time Optimization for Google Test
string( REPLACE "-flto=full" "" CMAKE_CXX_FLAGS_RELEASE
  ${CMAKE_CXX_FLAGS_RELEASE} )
string( REPLACE "  " " " CMAKE_CXX_FLAGS_RELEASE ${CMAKE_CXX_FLAGS_RELEASE} )

add_subdirectory( googletest )

# Add Link Time Optimization again
set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE}" -flto=full )


target_include_directories( MobileRT SYSTEM PRIVATE
  "${CMAKE_CURRENT_SOURCE_DIR}/stb" )
###############################################################################
###############################################################################


###############################################################################
# Add C++ Boost modules
###############################################################################
message( STATUS "Adding C++ Boost dependencies." )
set( BOOST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/boost/libs" )

message( STATUS "Updating and adding all C++ Boost modules used." )
foreach( submodule assert )
  execute_process( COMMAND git submodule update --init
    ${BOOST_PATH}/${submodule}
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
  execute_process( COMMAND git checkout master --progress
    WORKING_DIRECTORY ${BOOST_PATH}/${submodule} )
  target_include_directories( MobileRT SYSTEM PUBLIC
    "${BOOST_PATH}/${submodule}/include" )
endforeach( submodule )

# Example of C++ Boost modules:
# core utility config type_traits algorithm
###############################################################################
###############################################################################
