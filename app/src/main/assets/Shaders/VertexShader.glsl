#version 100

precision lowp float;

attribute vec4 vertexPosition;
attribute vec2 vertexTexCoord;

varying vec2 fragmentTexCoord;

void main () {
	gl_Position = vertexPosition;
	fragmentTexCoord = vertexTexCoord;
}
