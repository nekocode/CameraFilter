precision highp float;

uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

const float PI = 3.14159265;
const float rotateRadian = PI/3.0;
const float radiusRatio = 0.8;
const float center = 0.5;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
  {
    float radius = min(iResolution.x,iResolution.y)*radiusRatio/2.0;
    vec2 texCoord = fragCoord;
    vec2 currentUV = texCoord;
    currentUV.x *= iResolution.x;
    currentUV.y *= iResolution.y;
    vec2 centerUV = iResolution.xy * center;
    vec2 deltaUV = currentUV - centerUV;

    float deltaR = length(deltaUV);
    float beta = atan(deltaUV.y, deltaUV.x) + rotateRadian * 2.0 * (-(deltaR/radius)*(deltaR/radius) + 1.0);

    vec2 dstUV = currentUV;
    if(deltaR <= radius){
        dstUV = centerUV + deltaR*vec2(cos(beta), sin(beta));
    }
    dstUV.x /=iResolution.x;
    dstUV.y /=iResolution.y;
    fragColor = texture2D(iChannel0, dstUV);
   }

void main() {
 	mainImage(gl_FragColor, texCoord);
 }
