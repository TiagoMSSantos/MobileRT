cmake_minimum_required( VERSION 3.6.0 )

project( MobileRT VERSION 1.0.0.0 LANGUAGES CXX )

include( ProcessorCount )
ProcessorCount( JOBS )
set( JOBS 1 )


# Clone repositories
execute_process( COMMAND git clone https://github.com/g-truc/glm --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/syoyo/tinyobjloader --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/google/googletest --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/nothings/stb --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/boostorg/boost --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )


# Update repositories
execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/glm )
execute_process( COMMAND git pull --all --rebase --recurse-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/glm )

execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )
execute_process( COMMAND git pull --all --rebase --recurse-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )

execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )
execute_process( COMMAND git pull --all --rebase --recurse-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )

execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )
execute_process( COMMAND git pull --all --rebase --recurse-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )

execute_process( COMMAND git checkout master --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
execute_process( COMMAND git pull --all --rebase --recurse-submodules --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )


# Checkout to specific versions
execute_process( COMMAND git -c advice.detachedHead=false checkout release-1.10.0 --progress
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )


# Add third party dependencies
add_subdirectory( glm )
add_subdirectory( tinyobjloader )
add_subdirectory( googletest )

target_include_directories( MobileRT SYSTEM PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/stb" )


# Add boost dependencies
set ( BOOST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/boost/libs" )

foreach( submodule assert )
  execute_process( COMMAND git submodule update --init ${BOOST_PATH}/${submodule}
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
  target_include_directories( MobileRT SYSTEM PUBLIC "${BOOST_PATH}/${submodule}/include" )
endforeach( submodule )

#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/core/include" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/utility/include" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/config/include" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/type_traits/include" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/algorithm/include" )
