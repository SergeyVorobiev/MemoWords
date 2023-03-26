package com.vsv.game.engine.gl.shaders;

import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.utils.AssetsContentLoader;

public class Shader {

    public static final int NONE_SHADER = -1;

    private final String code;

    private final int type;

    private String error;

    private int shaderId = NONE_SHADER;

    private final String name;

    public Shader(@NonNull String fileName, int type) {
        this.name = fileName;
        this.type = type;
        code = AssetsContentLoader.readFileAsString("shaders/" + fileName);
    }

    public int createShader() {
        if (shaderId != -1) {
            throw new RuntimeException(name + " shader is already created.");
        }
        shaderId = GLES20.glCreateShader(type);
        if (shaderId != NONE_SHADER) {
            GLES20.glShaderSource(shaderId, code);
            GLES20.glCompileShader(shaderId);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compiled, 0);
            error = GLES20.glGetShaderInfoLog(shaderId);
            if (error != null && !error.isEmpty()) {
                Log.e("GL", error);
            }
            if (compiled[0] == NONE_SHADER) {
                GLES20.glDeleteShader(shaderId);
                shaderId = NONE_SHADER;
            }
        }
        return shaderId;
    }

    public int getShaderId() {
        return shaderId;
    }

    public boolean isCreated() {
        return shaderId != -1;
    }

    @Nullable
    public String getError() {
        return this.error;
    }

    public void deleteShader() {
        if (shaderId != NONE_SHADER) {
            GLES20.glDeleteShader(shaderId);
            shaderId = NONE_SHADER;
        }
    }
}
