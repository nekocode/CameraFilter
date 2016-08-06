#extension GL_OES_EGL_image_external : require
precision highp float;

uniform vec3                iResolution;
uniform samplerExternalOES  sTexture;
varying vec2                texCoord;

float c = 0.05; //amout of blocks = c*iResolution.x

void mainImage( out vec4 fragColor, in vec2 fragCoord ){
    //blocked pixel coordinate
    vec2 middle = floor(fragCoord*c+.5)/c;

    vec3 color = texture2D(sTexture, middle/iResolution.xy).rgb;

    //lego block effects
        //stud
        float dis = distance(fragCoord,middle)*c*2.;
        if(dis<.65&&dis>.55){
            color *= dot(vec2(0.707),normalize(fragCoord-middle))*.5+1.;
        }

        //side shadow
        vec2 delta = abs(fragCoord-middle)*c*2.;
        float sdis = max(delta.x,delta.y);
        if(sdis>.9){
            color *= .8;
        }

	fragColor = vec4(color,1.0);
}

void main() {
	mainImage(gl_FragColor, texCoord*iResolution.xy);
}