#extension GL_OES_EGL_image_external : require
precision highp float;

uniform vec3                iResolution;
uniform vec3                iResolution2;
uniform float               iGlobalTime;
uniform samplerExternalOES  sCamera;
uniform sampler2D           sBufA;
varying vec2                texCoord;

// A super simple video source with feature detection

float grayScale(vec4 c) { return c.x*.29 + c.y*.58 + c.z*.13; }

//============================================================
vec4 GenerateSeed (in vec2 fragCoord)
{
    vec2 uv = fragCoord;
    vec3 dataStep = vec3( vec2(1.) / iResolution2.xy, 0.);

    vec4 fragColor = texture2D( sCamera, uv );

    float d = grayScale(fragColor);
    float dL = grayScale(texture2D( sCamera, uv - dataStep.xz ));
    float dR = grayScale(texture2D( sCamera, uv + dataStep.xz ));
    float dU = grayScale(texture2D( sCamera, uv - dataStep.zy ));
    float dD = grayScale(texture2D( sCamera, uv + dataStep.zy ));

    float scale = 0.99;
    float w = float( d*scale > max(max(dL, dR), max(dU, dD)) );

    w = max(w, texture2D( sBufA, uv ).w*.9); // get some from previous frame

    fragColor.w = w;

    return fragColor;
}

//============================================================
void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    fragColor = GenerateSeed(fragCoord);
}



void main() {
	mainImage(gl_FragColor, texCoord);
}