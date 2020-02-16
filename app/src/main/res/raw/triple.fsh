precision highp float;

uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

uniform float u_time;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
  {
    if (fragCoord.y <=  0.333){
       fragColor = texture2D(iChannel0, vec2(fragCoord.x,fragCoord.y + 0.333));
    }else if(fragCoord.y > 0.333 && fragCoord.y<= 0.666){
       fragColor = texture2D(iChannel0, fragCoord);
    }else{
       fragColor = texture2D(iChannel0, vec2(fragCoord.x,fragCoord.y - 0.333));
    }
   }

void main() {
 	mainImage(gl_FragColor, texCoord);
 }