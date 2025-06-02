// Vertex Shader: for the 2 triangles with the texture where the
// Ray Tracing engine writes the bitmap

// OpenGL ES 2.0 version
#version 100

// High precision is not necessary
precision lowp float;

// Vertex position for each vertex
attribute vec4 vertexPosition;

// Vertex texture coordinates for each vertex
attribute vec2 vertexTexCoord;

// Interpolated variable to the fragment shader
varying vec2 fragmentTexCoord;

// Main to calculate the position and texture coordinate for each vertex in
// gl_Position and fragmentTexCoord
void main() {
    // Output the position for this vertex
    gl_Position = vertexPosition;
    // Output the texture coordinates for this vertex
    fragmentTexCoord = vertexTexCoord;
}
