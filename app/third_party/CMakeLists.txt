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

execute_process(COMMAND git submodule foreach "git remote update --prune;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach "git submodule update;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach "git fetch --tags --all;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach "git clean -dfx;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )
###############################################################################
###############################################################################


###############################################################################
# Set stable versions to be used
###############################################################################
set( BOOST_VERSION "boost-1.79.0" )
set( GLM_VERSION "0.9.9.8" )
set( STB_VERSION "master" )
set( TINYOBJLOADER_VERSION "v1.0.7" )
set( GOOGLETEST_VERSION "release-1.11.0" )
set( PCG_CPP_VERSION "v0.98.1" )
###############################################################################
###############################################################################


###############################################################################
# Change to stable versions
###############################################################################
message( STATUS "Changing to stable versions." )

execute_process( COMMAND git checkout --progress -f ${GLM_VERSION};
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/glm )

execute_process( COMMAND git checkout --progress -f ${STB_VERSION};
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )

execute_process( COMMAND git checkout --progress -f ${TINYOBJLOADER_VERSION};
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )

execute_process( COMMAND git checkout --progress -f ${GOOGLETEST_VERSION};
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )

execute_process( COMMAND git checkout --progress -f ${BOOST_VERSION};
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )

execute_process( COMMAND git checkout --progress -f ${PCG_CPP_VERSION};
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/pcg-cpp )

execute_process( COMMAND git submodule foreach "
    dependency=$(git remote -v | head -n1 | awk '{print $2}');
    last_stable_release=$(git tag -l --sort=-committerdate | grep -i [0-9] | grep -v beta | grep -v rc | grep -v preview | grep -v bug | head -1 || true);
    if [ -z \${last_stable_release} ]; then
      last_stable_release='master';
    fi;
    used_version=$(git name-rev --name-only HEAD | cut -d '/' -f2 | cut -d '^' -f1);

    if [ ! -z \$(git status -s) ]; then
      git status;
      echo \"\\033[0;31m      Dependency \${dependency} has some unexpected unstaged diff! Please fix this! \\e[0m\";
      exit 1;
    fi;

    echo \"\";
    echo \"###############################################################\";
    if [ \${used_version} = \${last_stable_release} ]; then
      echo \"Dependency: \${dependency}\";
      echo \"Using version: \${used_version}\";
    else
      echo \"\\033[0;33m      Dependency: \${dependency} \\e[0m\";
      echo \"\\033[0;31m      Last stable version detected: \${last_stable_release} \\e[0m\";
      echo \"\\033[0;31m      Using version: \${used_version} \\e[0m\";
    fi;
    echo \"###############################################################\";
    echo \"\";
  "
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} RESULT_VARIABLE ret )
if( NOT ret EQUAL "0" )
  message( FATAL_ERROR "Bad exit status" )
endif()

execute_process(COMMAND git submodule foreach "git pull --rebase --progress || true;"
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
  execute_process( COMMAND git checkout --progress -f ${BOOST_VERSION}
    WORKING_DIRECTORY ${BOOST_PATH}/${submodule} )
  target_include_directories( MobileRT SYSTEM PUBLIC
    "${BOOST_PATH}/${submodule}/include" )
endforeach( submodule )
###############################################################################
###############################################################################
