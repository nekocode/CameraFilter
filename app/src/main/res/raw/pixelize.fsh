#extension GL_OES_EGL_image_external : require
#extension GL_OES_standard_derivatives : enable
#define PIXEL_SIZE 10.0
precision mediump float;

uniform samplerExternalOES sTexture;
varying vec2 texCoord;

void main() {
	vec2 uv = texCoord.xy;

    float dx = PIXEL_SIZE / 500.0;
    float dy = PIXEL_SIZE / 275.0;

    uv.x = dx * floor(uv.x / dx);
    uv.y = dy * floor(uv.y / dy);

    gl_FragColor = texture2D(sTexture, uv);
}