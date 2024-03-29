# Print all set environment variables
function( print_environment )
  get_cmake_property( _variableNames VARIABLES )
  message( STATUS "---------------------------------------------------------" )
  message( STATUS "---------------------------------------------------------" )
  message( STATUS "PRINT HOST ENVIRONMENT VARIABLES" )
  foreach( _variableName ${_variableNames} )
    message( STATUS "${_variableName}=${${_variableName}}" )
  endforeach()
  message( STATUS "---------------------------------------------------------" )
  message( STATUS "---------------------------------------------------------" )
endfunction()
