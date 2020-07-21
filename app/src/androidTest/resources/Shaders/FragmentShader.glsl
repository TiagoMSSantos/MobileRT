// Fragment Shader: for the 2 triangles with the texture where the
// Ray Tracing engine writes the bitmap

// OpenGL ES 2.0 version
#version 100

// High precision is not necessary
precision lowp float;

// Texture parameter
uniform sampler2D uniformTexture;

// Interpolated variable from the vertex shader
varying vec2 fragmentTexCoord;

// Function for the tonemap
float toneMap (const float value) {
	return value;
}

// Main to calculate the color for each pixel in gl_FragColor
void main () {
	// Get the color for this pixel (interpolated from the nearest vertices)
	vec4 color = texture2D(uniformTexture, fragmentTexCoord);

	// Output the color for this pixel
	gl_FragColor = vec4(toneMap(color[0]), toneMap(color[1]), toneMap(color[2]), 1.0);
}
