precision highp float;

uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec4 mask = texture2D(iChannel0, fragCoord);
    vec4 tempColor = vec4(0.393 * mask.r + 0.769 * mask.g + 0.189 * mask.b,
     0.349 * mask.r + 0.686 * mask.g + 0.168 * mask.b,
      0.272 * mask.r + 0.534 * mask.g + 0.131 * mask.b, 1.0);
    fragColor = tempColor;
}

void main() {
	mainImage(gl_FragColor, texCoord);
}