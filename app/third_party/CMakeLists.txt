cmake_minimum_required( VERSION 3.6.0 )

project( MobileRT VERSION 1.0.0.0 LANGUAGES CXX )


execute_process( COMMAND git clone https://github.com/g-truc/glm
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/syoyo/tinyobjloader
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/google/googletest
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )

execute_process( COMMAND git clone https://github.com/Microsoft/GSL
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
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/GSL )

execute_process( COMMAND git pull --rebase
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/stb )


add_subdirectory( glm )
add_subdirectory( tinyobjloader )
add_subdirectory( googletest )
add_subdirectory( GSL )

target_include_directories( MobileRT PRIVATE "${CMAKE_CURRENT_SOURCE_DIR}/stb" )
#file( GLOB_RECURSE STB_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/stb/*.h" )
#add_library( stb STATIC ${STB_SOURCES} )
#set_target_properties( stb PROPERTIES LINKER_LANGUAGE CXX )

target_compile_options( MobileRT PRIVATE -Wno-zero-as-null-pointer-constant -Wno-old-style-cast -Wno-cast-qual
        -Wno-sign-conversion -Wno-reserved-id-macro -Wno-disabled-macro-expansion -Wno-cast-align
        -Wno-double-promotion -Wno-implicit-int-conversion -Wno-implicit-fallthrough )
