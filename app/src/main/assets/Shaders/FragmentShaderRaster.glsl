// Fragment Shader: for the rasterized scene (preview feature)

// OpenGL ES 2.0 version
#version 100

// High precision is not necessary
precision lowp float;

// Interpolated variable from the vertex shader
varying vec4 fragmentColor;

// Main to calculate the color for each pixel in gl_FragColor
void main () {
	// Output the color for this pixel
	gl_FragColor = fragmentColor;
}
