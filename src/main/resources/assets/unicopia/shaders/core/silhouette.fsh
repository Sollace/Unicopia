#version 150

#moj_import <minecraft:fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float alpha = texture(Sampler0, texCoord0).a;
    vec4 color = vertexColor * ColorModulator;
    fragColor = linear_fog(
      vec4(color.rgb, color.a * alpha),
      vertexDistance,
      FogStart,
      FogEnd,
      FogColor
    );
}
