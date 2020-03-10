include( ProcessorCount )
ProcessorCount( JOBS )
set( JOBS 1 )


# Clone repositories
execute_process( COMMAND git clone https://github.com/g-truc/glm --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/syoyo/tinyobjloader --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/google/googletest --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/nothings/stb --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )

execute_process( COMMAND git clone https://github.com/boostorg/boost --progress --jobs=${JOBS}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} ERROR_QUIET )


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


# Add third party dependencies
add_subdirectory( glm )
add_subdirectory( tinyobjloader )
add_subdirectory( googletest )

target_include_directories( MobileRT SYSTEM PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/stb" )


# Add boost dependencies
set ( BOOST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/boost/libs" )

# core utility config type_traits algorithm
foreach( submodule assert )
  execute_process( COMMAND git submodule update --init ${BOOST_PATH}/${submodule}
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )
  target_include_directories( MobileRT SYSTEM PUBLIC "${BOOST_PATH}/${submodule}/include" )
endforeach( submodule )
