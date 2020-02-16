precision highp float;

uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec4 mask = texture2D(iChannel0, fragCoord);
    vec4 color =vec4(1.0-mask.r, 1.0-mask.g, 1.0-mask.r,1.0);
    fragColor = color;
}

void main() {
	mainImage(gl_FragColor, texCoord);
}