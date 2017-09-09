precision highp float;
varying vec2 textureCoordinate;
uniform sampler2D vTexture;

//const mat3 mat=mat3(0.2990,0.5780,0.1140 ,
//                    0.5000,0.4187,- 0.0813,
//                     -0.1687,- 0.3313, 0.5000);
//const mat3 mat=mat3(0.2126  , 0.7152 , 0.0722,-0.1146 , -0.3854  ,0.5000,0.5000, -0.4542, -0.0468);
const mat4 mat=mat4((0.299, 0.587, 0.114,0),(-0.147,- 0.289,0.436,0.5),(0.615,- 0.515,- 0.100,0.5),(0,0,0,1));

const vec3 offset = vec3(0.0625f, 0.5f, 0.5f);
const vec3 rcoeff = vec3(0.182604f, 0.614526f, 0.061976f);
const vec3 gcoeff = vec3(-0.100640f, -0.338688f, 0.439327f);
const vec3 bcoeff = vec3(0.440654f, -0.400285f, -0.040370f);

vec3 rgb_to_yuv (vec3 val, vec3 offset, vec3 rcoeff, vec3 gcoeff, vec3 bcoeff) {
  vec3 yuv;
  yuv.r = dot(val.rgb, rcoeff);
  yuv.g = dot(val.rgb, gcoeff);
  yuv.b = dot(val.rgb, bcoeff);
  yuv += offset;
  return yuv;
}



void main() {
    vec4 rgba=texture2D( vTexture, textureCoordinate);
    vec4 yuv=vec4(0,0,0,1);
//    yuv.rgb=mat*rgba.rgb;
    yuv=rgb_to_yuv(rgba,offset,rcoeff,gcoeff,bcoeff);
    gl_FragColor = yuv;
}