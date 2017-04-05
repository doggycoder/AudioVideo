precision mediump float;
uniform sampler2D texY,texU,texV;
varying vec2 textureCoordinate;

void main(){                           
  vec4 color = vec4((texture2D(texY, textureCoordinate).r - 16./255.) * 1.164);
  vec4 U = vec4(texture2D(texU, textureCoordinate).r - 128./255.);
  vec4 V = vec4(texture2D(texV, textureCoordinate).r - 128./255.);
  color += U * vec4(0, -0.392, 2.017, 0);
  color += V * vec4(1.596, -0.813, 0, 0);
  color.a = 1.0;
  gl_FragColor = color;
}