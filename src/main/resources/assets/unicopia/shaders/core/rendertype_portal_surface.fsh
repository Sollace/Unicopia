#version 150

#moj_import <matrix.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 texProj0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
  vec4 scale = vec4(0.25, 0.25, 0.25, 0.25);
  fragColor = textureProj(Sampler0, texProj0 * scale);
}
