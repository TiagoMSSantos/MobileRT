#version 100

precision lowp float;

uniform sampler2D uniformTexture;

varying vec2 fragmentTexCoord;

float toneMap (const float value) {
	//return 1.0 - cos(sqrt(value));
	//return pow(1.0 - exp(-value * 1.0), 1.0 / 2.2);
	return value;
}

void main () {
	vec4 color = texture2D(uniformTexture, fragmentTexCoord);
	gl_FragColor = vec4(toneMap(color[0]), toneMap(color[1]), toneMap(color[2]), 1.0);
}
