#extension GL_OES_EGL_image_external : require
precision highp float;

uniform samplerExternalOES sTexture;
varying vec2 texCoord;

vec2 tile_num = vec2(40.0, 20.0);

void main() {
    vec2 uv = texCoord.xy;
    vec2 uv2 = floor(uv*tile_num)/tile_num;
    uv -= uv2;
    uv *= tile_num;
    gl_FragColor = texture2D(sTexture, uv2 + vec2(step(1.0-uv.y,uv.x)/(2.0*tile_num.x),
                                                    //0,
                                                    step(uv.x,uv.y)/(2.0*tile_num.y)
                                                    //0
    ));
}