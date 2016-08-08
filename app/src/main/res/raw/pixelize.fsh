#extension GL_OES_EGL_image_external : require
#extension GL_OES_standard_derivatives : enable
precision mediump float;

uniform vec3                iResolution;
uniform samplerExternalOES  sTexture;
varying vec2                texCoord;

#define S (iResolution.x / 6e1) // The cell size.

void mainImage(out vec4 c, vec2 p)
{
    c = texture2D(sTexture, floor((p + .5) / S) * S / iResolution.xy);
}

void main() {
	mainImage(gl_FragColor, texCoord*iResolution.xy);
}