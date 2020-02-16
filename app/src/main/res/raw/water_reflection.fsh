precision highp float;

uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

float waterLevel = 0.5;
float waveAmplitude = 0.01;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
  {
     if(fragCoord.y >= waterLevel){
        fragColor = texture2D(iChannel0, fragCoord);
     }else{
        fragColor = texture2D(iChannel0,vec2(fragCoord.x + fract(sin(dot(fragCoord.xy ,vec2(12.9898,78.233))) * 43758.5453) * waveAmplitude,
       				2.0 * waterLevel - fragCoord.y));
     }
   }

void main() {
 	mainImage(gl_FragColor, texCoord);
 }