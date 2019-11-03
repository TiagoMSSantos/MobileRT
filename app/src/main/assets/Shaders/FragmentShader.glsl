#version 100

precision lowp float;

uniform sampler2D uniformTexture;

varying vec2 fragmentTexCoord;

float toneMap (const float value) {
	return value;
}

void main () {
	vec4 color = texture2D(uniformTexture, fragmentTexCoord);
	gl_FragColor = vec4(toneMap(color[0]), toneMap(color[1]), toneMap(color[2]), 1.0);
}
