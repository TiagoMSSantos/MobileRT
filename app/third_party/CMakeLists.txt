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

execute_process( COMMAND git clone https://github.com/syoyo/tinyobjloader
  --shallow-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/boostorg/boost
  --shallow-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )
###############################################################################
###############################################################################


###############################################################################
# Update third party repositories
###############################################################################
message( STATUS "Updating third party repositories." )

execute_process(COMMAND git submodule foreach "git clean -fxd;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach "git remote update --prune;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach "git fetch --tags --all;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )
###############################################################################
###############################################################################


###############################################################################
# Set stable versions to be used
###############################################################################
set( BOOST_VERSION "boost-1.80.0" )
set( TINYOBJLOADER_VERSION "v1.0.7" )
###############################################################################
###############################################################################


###############################################################################
# Change to stable versions
###############################################################################
message( STATUS "Changing to stable versions." )

execute_process( COMMAND git checkout --progress -f ${TINYOBJLOADER_VERSION};
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )

execute_process( COMMAND git checkout --progress -f ${BOOST_VERSION};
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )

execute_process( COMMAND git submodule foreach "
    dependency=$(git remote -v | head -n1 | awk '{print $2}');
    releases=$(git for-each-ref --sort=-creatordate refs/tags | awk '{ print $3 }' | cut -d '/' -f 3);
    stable_releases=$(echo \"\${releases}\" | grep -i [0-9] | grep -i [.] | grep -v alpha | grep -v \"\\-a\" | grep -v beta | grep -v \"\\-b\" | grep -v rc | grep -v preview | grep -v \"\\-pre\" | grep -v bug | grep -v .x);
    echo \"Detected stable versions: \${stable_releases}\";
    last_stable_release=$(echo \"\${stable_releases}\" | head -1 || true);
    if [ -z \"\${last_stable_release}\" ]; then
      last_stable_release='master';
    fi;
    used_version_raw=$(git name-rev --name-only HEAD);
    used_version=$(echo \"\${used_version_raw}\" | cut -d '/' -f2 | cut -d '^' -f1);

    if [ ! -z \"\$(git status -s | xargs echo -n)\" ]; then
      git status;
      echo \"$(tput -T xterm setaf 1)      Dependency \${dependency} has some unexpected unstaged diff! Please fix this! $(tput -T xterm sgr0)\";
      exit 1;
    fi;

    echo \"\";
    echo \"###############################################################\";
    if [ \"\${used_version}\" = \"\${last_stable_release}\" ]; then
      echo \"Dependency: \${dependency}\";
      echo \"Using version: \${used_version}\";
    else
      echo \"$(tput -T xterm setaf 3)      Dependency: \${dependency} $(tput -T xterm sgr0)\";
      echo \"$(tput -T xterm setaf 1)      Last stable version detected: \${last_stable_release} $(tput -T xterm sgr0)\";
      echo \"$(tput -T xterm setaf 1)      Using version: \${used_version} $(tput -T xterm sgr0)\";
      echo \"$(tput -T xterm setaf 1)      Using version raw: \${used_version_raw} $(tput -T xterm sgr0)\";
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

if( TARGET Components )
  add_subdirectory( tinyobjloader )
endif( TARGET Components )

# Add dependencies headers installed from conan package manager.
set( pcg-cpp_DIR "${CMAKE_SOURCE_DIR}/../build_conan-native" )
find_package( pcg-cpp )
target_include_directories( MobileRT SYSTEM PUBLIC ${pcg-cpp_INCLUDE_DIRS_RELEASE} )

set( glm_DIR "${CMAKE_SOURCE_DIR}/../build_conan-native" )
find_package( glm )
target_include_directories( MobileRT SYSTEM PUBLIC ${glm_INCLUDE_DIRS_RELEASE} )

set( stb_DIR "${CMAKE_SOURCE_DIR}/../build_conan-native" )
find_package( stb )
target_include_directories( MobileRT SYSTEM PUBLIC ${stb_INCLUDE_DIRS_RELEASE} )

print_environment()
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
