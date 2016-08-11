precision highp float;

uniform int                 iFrame;
uniform vec3                iResolution;
uniform vec3                iResolution2;
uniform float               iGlobalTime;
uniform sampler2D           sBufC;
uniform sampler2D           sBufB;
varying vec2                texCoord;

// A secondary buffer to get clean Voronoi every N-th frame

// this must be in sync with JFA algorithm constant
const float c_maxSteps = 8.0;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    //if (mod(float(iFrame+1), c_maxSteps) < .5) {
        fragColor = texture2D(sBufB, fragCoord); // update to new voronoi cell
    //} else {
    //    fragColor = texture2D(sBufC, fragCoord); // no change
    //}
}


void main() {
	mainImage(gl_FragColor, texCoord);
}