#extension GL_OES_EGL_image_external : require
#extension GL_OES_standard_derivatives : enable
precision mediump float;

uniform vec3                iResolution;
uniform samplerExternalOES  sTexture;
varying vec2                texCoord;

void mainImage(out vec4 f,vec2 u)
{
    vec2 r = iResolution.xy;
    f = texture2D(sTexture,ceil(u / (r.x/1e2)) * r.x/1e2 / r);
}

void main() {
	mainImage(gl_FragColor, texCoord*iResolution.xy);
}