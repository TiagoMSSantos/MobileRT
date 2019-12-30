cmake_minimum_required( VERSION 3.6.0 )

project( MobileRT VERSION 1.0.0.0 LANGUAGES CXX )


execute_process( COMMAND git clone https://github.com/g-truc/glm
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/syoyo/tinyobjloader
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/google/googletest
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/nothings/stb
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )


execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/glm )

execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )

execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )

execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )


add_subdirectory( glm )
add_subdirectory( tinyobjloader )
add_subdirectory( googletest )

target_include_directories( MobileRT SYSTEM PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/stb" )
