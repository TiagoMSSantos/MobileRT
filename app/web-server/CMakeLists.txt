###############################################################################
# Add source files
###############################################################################
message( STATUS "Adding web server interface source files." )
file( GLOB MOBILE_WEB_SERVER_SOURCES "${CMAKE_CURRENT_SOURCE_DIR}/src/**.cpp" )
###############################################################################
###############################################################################


###############################################################################
# Make source files visible to root CMake
###############################################################################
message( STATUS "Appending web server source files." )
set( MOBILE_WEB_SERVER_SOURCES "${MOBILE_WEB_SERVER_SOURCES}" CACHE STRING "CUSTOM" FORCE )
message( STATUS "MOBILE_WEB_SERVER_SOURCES = ${MOBILE_WEB_SERVER_SOURCES}" )
###############################################################################
###############################################################################
