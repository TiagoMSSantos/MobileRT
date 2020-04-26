###############################################################################
# Add source files
###############################################################################
message( STATUS "Adding OS interface source files." )
if( DEFINED ANDROID_ABI )
	file( GLOB_RECURSE MOBILE_DEPENDENT_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/Android_JNI/**.cpp"
  )
else()
	file( GLOB MOBILE_DEPENDENT_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/Linux/**.cpp"
  )
	file( GLOB_RECURSE MOBILE_DEPENDENT_SOURCES_QT
    "${CMAKE_CURRENT_SOURCE_DIR}/Linux/Qt/MobileRT/**.cpp"
  )
endif()
###############################################################################
###############################################################################


###############################################################################
# Make source files visible to root CMake
###############################################################################
message( STATUS "Appending Qt source files." )
set( MOBILE_DEPENDENT_SOURCES
  "${MOBILE_DEPENDENT_SOURCES}" "${MOBILE_DEPENDENT_SOURCES_QT}"
  CACHE STRING "CUSTOM" FORCE
)
message( STATUS "MOBILE_DEPENDENT_SOURCES = ${MOBILE_DEPENDENT_SOURCES}" )
###############################################################################
###############################################################################
