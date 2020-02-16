precision highp float;

uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;



void mainImage( out vec4 fragColor, in vec2 fragCoord )
  {
    vec2 flipCoord = vec2(1.0-fragCoord.x, fragCoord.y);
    if(flipCoord.x >= 0.5){
    	fragColor = texture2D(iChannel0, vec2( flipCoord.x - 0.5, flipCoord.y ));
    } else {
    	fragColor = texture2D(iChannel0, vec2(  0.5 - flipCoord.x,flipCoord.y ));
    }
   }

void main() {
 	mainImage(gl_FragColor, texCoord);
 }
