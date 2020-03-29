cmake_minimum_required( VERSION 3.0...3.0 FATAL_ERROR )

# Print all set environment variables
function(print_environment)
	get_cmake_property( _variableNames VARIABLES )
	foreach ( _variableName ${_variableNames} )
		message( STATUS "${_variableName}=${${_variableName}}" )
	endforeach()
	message( FATAL_ERROR "FINISHED" )
endfunction()
