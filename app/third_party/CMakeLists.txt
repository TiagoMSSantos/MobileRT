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

# These work via conan but not for all environments
execute_process( COMMAND git clone https://github.com/g-truc/glm
  --shallow-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/nothings/stb
  --shallow-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )


execute_process( COMMAND git clone https://github.com/syoyo/tinyobjloader
  --shallow-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/google/googletest
  --shallow-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/boostorg/boost
  --shallow-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/imneme/pcg-cpp
  --shallow-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )
###############################################################################
###############################################################################


###############################################################################
# Update third party repositories
###############################################################################
message( STATUS "Updating third party repositories." )

execute_process(COMMAND git submodule foreach git remote update --prune;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach git fetch --tags --all;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach git clean -dfx;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )
###############################################################################
###############################################################################


###############################################################################
# Change to stable versions
###############################################################################
message( STATUS "Changing to stable versions." )

execute_process( COMMAND git checkout 0.9.9.8 --progress;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/glm )
execute_process( COMMAND git checkout master --progress;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )
execute_process( COMMAND git checkout master --progress;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )
execute_process( COMMAND git checkout master --progress;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )
execute_process( COMMAND git checkout master --progress;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
execute_process( COMMAND git checkout master --progress;
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/pcg-cpp )

execute_process( COMMAND git submodule foreach "
    dependency=$(git remote -v | head -n1 | awk '{print $2}');
    last_stable_release=$(git tag -l --sort=-version:refname | grep -i [0-9] | grep -v beta | grep -v rc | grep -v preview | grep -v bug | head -1);
    if [ -z \${last_stable_release} ]; then
      last_stable_release='master';
    fi;
    used_version=$(git name-rev --name-only HEAD);
    echo Dependency: \${dependency};
    echo Last stable version detected: \${last_stable_release};
    echo Using version: \${used_version};
    echo;
  "
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach "git pull --rebase --progress"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )
###############################################################################
###############################################################################


###############################################################################
# Add third party dependencies
###############################################################################
message( STATUS "Adding third party dependencies." )

#add_subdirectory( glm )

if( TARGET Components )
  add_subdirectory( tinyobjloader )
endif( TARGET Components )

# Remove Link Time Optimization for Google Test
string( REPLACE "-flto=full" "" CMAKE_CXX_FLAGS_RELEASE
  ${CMAKE_CXX_FLAGS_RELEASE} )
string( REPLACE "  " " " CMAKE_CXX_FLAGS_RELEASE ${CMAKE_CXX_FLAGS_RELEASE} )

add_subdirectory( googletest )

# Add Link Time Optimization again
set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE}" -flto=full )


# This work via conan but not for all environments
target_include_directories( MobileRT SYSTEM PRIVATE
  "${CMAKE_CURRENT_SOURCE_DIR}/stb" )


target_include_directories( MobileRT SYSTEM PUBLIC
  "${CMAKE_CURRENT_SOURCE_DIR}/pcg-cpp/include" )
###############################################################################
###############################################################################


###############################################################################
# Add C++ Boost modules
###############################################################################
message( STATUS "Adding C++ Boost dependencies." )
set( BOOST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/boost/libs" )

message( STATUS "Updating and adding all C++ Boost modules used." )
foreach( submodule assert )
  execute_process( COMMAND git submodule update --init --rebase
    ${BOOST_PATH}/${submodule}
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
  execute_process( COMMAND git checkout master --progress
    WORKING_DIRECTORY ${BOOST_PATH}/${submodule} )
  target_include_directories( MobileRT SYSTEM PUBLIC
    "${BOOST_PATH}/${submodule}/include" )
endforeach( submodule )
###############################################################################
###############################################################################
