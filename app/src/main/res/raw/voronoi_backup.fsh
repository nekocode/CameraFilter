precision highp float;

uniform vec3                iResolution;
uniform vec3                iResolution2;
uniform float               iGlobalTime;
uniform sampler2D           sBufC;
uniform sampler2D           sBufA;
varying vec2                texCoord;


void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
	vec2 uv = fragCoord.xy / iResolution.xy;

#if 0
    fragColor = texture2D(sBufA, uv).wwww;

#else
	vec4 cell = texture2D(sBufC, uv);
    vec2 cell_uv = cell.xy / iResolution2.xy;
    vec4 video = texture2D(sBufA, cell_uv);
    vec2 dcell = cell.xy - fragCoord.xy;
    float len = length(dcell);
    vec3 color = video.xyz * (.9 + len*.005);
    fragColor = vec4(color, 1.);

#endif
}

void main() {
	mainImage(gl_FragColor, texCoord*iResolution.xy);
}