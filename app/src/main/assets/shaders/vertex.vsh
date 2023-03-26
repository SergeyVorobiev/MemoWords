#version 100

precision highp float;

attribute vec4 vertexPositionIn; // Input to Vertex Shader

attribute vec4 vertexColorIn; // Input to Vertex Shader

attribute vec3 textureIn; // Input to Vertex Shader

attribute vec4 positionAngle;

varying vec4 vertexColor; // Output from Vertex shader

varying vec3 textureCoord; // Output texture

uniform mat4 projectionMatrix;

uniform mat4 viewMatrix;

uniform mat4 modelMatrix;

uniform mat4 mvpMatrix;

void main() {
    float angle = positionAngle.w * 0.017453292519943295;
    float sina = sin(angle);
    float cosa = cos(angle);

    mat4 rotate = mat4( cosa, -sina, 0, 0,
                        sina, cosa, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1);

    mat4 translate = mat4(1.0, 0.0, 0.0, positionAngle.x,
                            0.0, 1.0, 0.0, positionAngle.y,
                            0.0, 0.0, 1.0, positionAngle.z,
                            0.0, 0.0, 0.0, 1.0);
    vertexColor = vertexColorIn;
    vec4 pos = vertexPositionIn * rotate;
    pos = pos * translate;
    gl_Position = mvpMatrix * pos; //vec4(vertexPositionIn.x, vertexPositionIn.y, vertexPositionIn.z, 1.0);
    //gl_Position.x = gl_Position.x + 1.0;
    //gl_Position.y = gl_Position.y + 1.0;
    textureCoord = textureIn; //vec3(textureIn.x, textureIn.y, 0);
}

// uniform are per-primitive parameters (constant during an entire draw call).
// attribute are per-vertex parameters (typically : positions, normals, colors, UVs, ...).
// varying are per-fragment (or per-pixel) parameters : they vary from pixels to pixels.