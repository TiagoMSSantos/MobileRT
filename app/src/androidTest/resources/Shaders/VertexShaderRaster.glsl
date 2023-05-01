// Vertex Shader: for the rasterized scene (preview feature)

// OpenGL ES 2.0 version
#version 100

// High precision is not necessary
precision lowp float;

// Model parameter
uniform mat4 uniformModelMatrix;

// View parameter
uniform mat4 uniformViewMatrix;

// Projection parameter
uniform mat4 uniformProjectionMatrix;

// Vertex position for each vertex
attribute vec4 vertexPosition;

// Vertex color for each vertex
attribute vec4 vertexColor;

// Interpolated variable to the fragment shader
varying vec4 fragmentColor;

// Main to calculate the position and color for each vertex in gl_Position and
// fragmentColor
void main () {
    // Output the position for this vertex
    gl_Position = uniformProjectionMatrix * uniformViewMatrix * uniformModelMatrix * vertexPosition;
    // Output the color for this vertex
    fragmentColor = vertexColor;
}
