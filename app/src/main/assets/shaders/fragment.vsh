#version 100

precision highp float;

varying vec4 vertexColor; // Input to Fragment Shader

varying vec3 textureCoord;

uniform sampler2D textures[5];

void main()
{
    int index = int(textureCoord.z);
    // gl_FragColor = vertexColor; //vec4(1.0, 0.5, 0.2, 1.0);
    gl_FragColor = texture2D(textures[index], textureCoord.xy) * vertexColor;
}