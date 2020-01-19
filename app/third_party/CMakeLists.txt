cmake_minimum_required( VERSION 3.6.0 )

project( MobileRT VERSION 1.0.0.0 LANGUAGES CXX )

# Clone repositories
execute_process( COMMAND git clone https://github.com/g-truc/glm
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/syoyo/tinyobjloader
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/google/googletest
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/nothings/stb
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/boostorg/boost
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )


# Update repositories
execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/glm )

execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/tinyobjloader )

execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/googletest )

execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )

execute_process( COMMAND git pull --rebase
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/boost )


# Add third party dependencies
add_subdirectory( glm )
add_subdirectory( tinyobjloader )
add_subdirectory( googletest )

target_include_directories( MobileRT SYSTEM PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/stb" )

# Add boost dependencies
set ( BOOST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/boost/libs" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/core/include" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/utility/include" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/config/include" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/type_traits/include" )
#target_include_directories( MobileRT SYSTEM PRIVATE "${BOOST_PATH}/algorithm/include" )
