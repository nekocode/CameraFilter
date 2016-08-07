#extension GL_OES_EGL_image_external : require
#extension GL_OES_standard_derivatives : enable
precision mediump float;

uniform vec3                iResolution;
uniform samplerExternalOES  sTexture;
varying vec2                texCoord;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 uv = fragCoord.xy;
    vec4 color =  texture2D(sTexture, fragCoord);
    float gray = length(color.rgb);
    fragColor = vec4(vec3(step(0.06, length(vec2(dFdx(gray), dFdy(gray))))), 1.0);
}

void main() {
    mainImage(gl_FragColor, texCoord);
}