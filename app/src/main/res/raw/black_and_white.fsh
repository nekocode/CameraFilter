precision highp float;

uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec4 mask = texture2D(iChannel0, fragCoord);
    float color = (mask.r + mask.g + mask.b) / 3.0;
    color = step(0.5, color);
    vec4 tempColor = vec4(color, color, color, 1.0);
    fragColor = tempColor;
}

void main() {
	mainImage(gl_FragColor, texCoord);
}