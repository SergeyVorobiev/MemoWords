package com.vsv.game.engine.gl.shaders;

import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.NonNull;

public class ShaderProgramGL2 {

    public static final int NONE_PROGRAM = -1;

    private final String name;

    private int program = NONE_PROGRAM;

    private String error;

    public ShaderProgramGL2(@NonNull String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getError() {
        return error;
    }

    public int getProgram() {
        return program;
    }

    public void use() {
        GLES20.glUseProgram(program);
    }

    public int build(@NonNull Shader[] shaders) {
        if (program != NONE_PROGRAM) {
            throw new RuntimeException(name + " program is already built");
        }
        if (shaders.length == 0) {
            throw new RuntimeException("There is no shaders to build the program");
        }
        program = GLES20.glCreateProgram();
        if (program != NONE_PROGRAM) {
            for (Shader shader : shaders) {
                GLES20.glAttachShader(program, shader.getShaderId());
                int err = GLES20.glGetError();
                if (err != GLES20.GL_NO_ERROR) {
                    error = String.valueOf(err);
                    Log.e("GL", error);
                    return NONE_PROGRAM;
                }
            }
            program = linkProgram();
        }
        return program;
    }

    public int getAttributeLocation(String attributeName) {
        return GLES20.glGetAttribLocation(program, attributeName);
    }

    public int enableFloatAttribute(@NonNull String attribName,
                                    int vertexCount, int vertexSize, int offset) {
        int attribIndex = this.getAttributeLocation(attribName);
        GLES20.glVertexAttribPointer(attribIndex, vertexCount, GLES20.GL_FLOAT, false, vertexSize * 4, offset * 4);
        GLES20.glEnableVertexAttribArray(attribIndex);
        return attribIndex;
    }

    public void setTexture(@NonNull String fieldName, int textureSlot) {
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, fieldName), textureSlot);
    }

    public void setTextures(@NonNull String fieldName, int[] textureSlots) {
        GLES20.glUniform1iv(GLES20.glGetUniformLocation(program, fieldName), textureSlots.length, textureSlots, 0);
    }

    public int getUniformLocation(@NonNull String fieldName) {
        return GLES20.glGetUniformLocation(program, fieldName);
    }

    public void setMatrix(int location, @NonNull float[] matrix) {
        GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
    }

    private int linkProgram() {
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            error = GLES20.glGetProgramInfoLog(program);
            Log.e("GL", "Could not link program: ");
            Log.e("GL", error);
            GLES20.glDeleteProgram(program);
            program = NONE_PROGRAM;
            return program;
        }
        return program;
    }

    public void deleteProgram() {
        if (program != NONE_PROGRAM) {
            GLES20.glDeleteProgram(program);
            program = NONE_PROGRAM;
        }
    }
}
