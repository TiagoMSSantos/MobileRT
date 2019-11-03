#version 100

precision lowp float;

varying vec4 fragmentColor;

void main () {
	gl_FragColor = fragmentColor;
}
