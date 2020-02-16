precision highp float;

uniform vec3                iResolution;
uniform sampler2D           iChannel0;
varying vec2                texCoord;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
  {
    vec4 color = texture2D(iChannel0, fragCoord);
    float newR = abs(color.r + color.g * 2.0 - color.b) * color.r;
    float newG = abs(color.r + color.b * 2.0 - color.g) * color.r;
    float newB = abs(color.r + color.b * 2.0 - color.g) * color.g;
	vec4 newColor = vec4(newR, newG, newB, 1.0);
	fragColor = newColor;
   }

void main() {
 	mainImage(gl_FragColor, texCoord);
 }