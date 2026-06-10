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

# Cloning needs the network and can fail transiently (e.g. DNS outage:
# 'Could not resolve host: github.com'). Skip the clone when the directory
# already has content (e.g. local checkouts with the submodules initialized),
# retry it a few times otherwise, and stop the configure step loudly if the
# content still did not materialize, instead of poisoning the build with
# missing headers, which no retry of the build step can recover from.
function( clone_third_party_repository dependency url )
  foreach( attempt RANGE 1 3 )
    file( GLOB DEPENDENCY_CONTENTS "${CMAKE_CURRENT_SOURCE_DIR}/${dependency}/*" )
    if( DEPENDENCY_CONTENTS )
      return()
    endif()
    if( attempt GREATER 1 )
      message( STATUS "Retrying to clone '${dependency}' in 5 seconds (attempt ${attempt} of 3)." )
      execute_process( COMMAND ${CMAKE_COMMAND} -E sleep 5 )
    endif()
    execute_process( COMMAND git clone ${url}
      --shallow-submodules --jobs=${JOBS}
      WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )
  endforeach( attempt )
  file( GLOB DEPENDENCY_CONTENTS "${CMAKE_CURRENT_SOURCE_DIR}/${dependency}/*" )
  if( NOT DEPENDENCY_CONTENTS )
    message( FATAL_ERROR "Third party dependency '${dependency}' was not cloned into '${CMAKE_CURRENT_SOURCE_DIR}/${dependency}' after 3 attempts. Check the network connection to github.com and retry." )
  endif()
endfunction( clone_third_party_repository )

# These work via conan but not for all environments.
clone_third_party_repository( glm https://github.com/g-truc/glm )
clone_third_party_repository( stb https://github.com/nothings/stb )

clone_third_party_repository( tinyobjloader https://github.com/syoyo/tinyobjloader )
clone_third_party_repository( googletest https://github.com/google/googletest )
clone_third_party_repository( boost https://github.com/boostorg/boost )
clone_third_party_repository( pcg-cpp https://github.com/imneme/pcg-cpp )
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

execute_process(COMMAND git submodule foreach "git submodule update;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process(COMMAND git submodule foreach "git fetch --tags --all;"
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )
###############################################################################
###############################################################################


###############################################################################
# Set stable versions to be used
###############################################################################
set( BOOST_VERSION "boost-1.90.0" )
set( GLM_VERSION "1.0.3" )
set( STB_VERSION "master" )
set( TINYOBJLOADER_VERSION "v1.0.7" )
set( GOOGLETEST_VERSION "v1.17.0" )
set( PCG_CPP_VERSION "master" )
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
    releases=$(git for-each-ref --sort=-creatordate refs/tags | awk '{ print $3 }' | cut -d '/' -f 3);
    stable_releases=$(echo \"\${releases}\" | grep -i [0-9] | grep -i [.] | grep -v alpha | grep -v \"\\-a\" | grep -v beta | grep -v \"\\-b\" | grep -v rc | grep -v preview | grep -v \"\\-pre\" | grep -v bug | grep -v .x);
    echo \"Detected stable versions for '\${dependency}': \${stable_releases}\";
    last_stable_release=$(echo \"\${stable_releases}\" | head -1 || true);
    if [ -z \"\${last_stable_release}\" ]; then
      echo \"Didn't detect any stable version for '\${dependency}'\";
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


# Remove Link Time Optimization for Google Test.
string( REPLACE "-flto=full" "" CMAKE_CXX_FLAGS_RELEASE ${CMAKE_CXX_FLAGS_RELEASE} )
string( REPLACE "  " " " CMAKE_CXX_FLAGS_RELEASE ${CMAKE_CXX_FLAGS_RELEASE} )

add_subdirectory( googletest )

# Add Link Time Optimization again.
set( CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -flto=full" )

# This work via conan but not for all environments.
target_include_directories( MobileRT SYSTEM PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/stb" )


target_include_directories( MobileRT SYSTEM PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/pcg-cpp/include" )

# Add dependencies headers installed from conan package manager.
target_include_directories( MobileRT SYSTEM PUBLIC ${CMAKE_INCLUDE_PATH} )
###############################################################################
###############################################################################


###############################################################################
# Add C++ Boost modules
###############################################################################
message( STATUS "Adding C++ Boost dependencies." )
set( BOOST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/boost/libs" )

# Initializing a C++ Boost submodule needs the network and can fail transiently
# (e.g. DNS outage: 'Could not resolve host: github.com'). Retry it a few times
# and fail the configure step loudly if the module headers still did not
# materialize. Otherwise the build proceeds and fails later with confusing
# 'boost/*.hpp file not found' errors that persist across build retries.
function( checkout_boost_module submodule )
  foreach( attempt RANGE 1 3 )
    if( attempt GREATER 1 )
      message( STATUS "Retrying to initialize C++ Boost module '${submodule}' in 5 seconds (attempt ${attempt} of 3)." )
      execute_process( COMMAND ${CMAKE_COMMAND} -E sleep 5 )
    endif()
    execute_process( COMMAND git submodule update --init --rebase
      ${BOOST_PATH}/${submodule}
      WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
    execute_process( COMMAND git checkout --progress -f ${BOOST_VERSION}
      WORKING_DIRECTORY ${BOOST_PATH}/${submodule} )
    if( IS_DIRECTORY "${BOOST_PATH}/${submodule}/include" )
      return()
    endif()
  endforeach( attempt )
  message( FATAL_ERROR "C++ Boost module '${submodule}' was not initialized in '${BOOST_PATH}/${submodule}' after 3 attempts. Check the network connection to github.com and retry." )
endfunction( checkout_boost_module )

message( STATUS "Updating and adding C++ Boost modules used." )
foreach( submodule assert config core mpl preprocessor utility type_traits static_assert range iterator sort )
  checkout_boost_module( ${submodule} )
  target_include_directories( MobileRT SYSTEM PUBLIC "${BOOST_PATH}/${submodule}/include" )
  target_include_directories( MobileRT SYSTEM PUBLIC "${BOOST_PATH}/${submodule}/conversion/include" )
endforeach( submodule )

if( CMAKE_HOST_SYSTEM MATCHES "Linux" )
  message( STATUS "Updating and adding Linux only C++ Boost modules used." )
  foreach( submodule stacktrace predef container_hash )
    checkout_boost_module( ${submodule} )
    target_include_directories( MobileRT SYSTEM PUBLIC "${BOOST_PATH}/${submodule}/include" )
  endforeach( submodule )
endif()
###############################################################################
###############################################################################
