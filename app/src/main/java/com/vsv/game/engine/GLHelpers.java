package com.vsv.game.engine;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import androidx.annotation.NonNull;

import com.vsv.game.engine.gl.shaders.Shader;
import com.vsv.game.engine.gl.shaders.ShaderProgramGL2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public final class GLHelpers {

    private static final int NONE_PROGRAM = -1;

    private GLHelpers() {

    }

    public static void enableBlendGL2() {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static ShaderProgramGL2 loadProgram(@NonNull String programName, @NonNull String vertexShaderName,
                                               @NonNull String fragmentShaderName) {
        String[] names = new String[]{vertexShaderName, fragmentShaderName};
        Shader[] shaders = new Shader[2];
        int i = 0;
        int type = GLES20.GL_VERTEX_SHADER;
        for (String name : names) {
            Shader shader = new Shader(name, type);
            int id = shader.createShader();
            if (id == Shader.NONE_SHADER) {
                throw new RuntimeException("Can not create a shader");
            }
            shaders[i++] = shader;
            type = GLES20.GL_FRAGMENT_SHADER;
        }
        ShaderProgramGL2 program = new ShaderProgramGL2(programName);
        int id = program.build(shaders);
        if (id == ShaderProgramGL2.NONE_PROGRAM) {
            throw new RuntimeException("Can not create a program");
        }
        for (Shader shader : shaders) {
            shader.deleteShader();
        }
        return program;
    }

    public static void loadTextureGL2(int textureSlot, Bitmap bitmap, int textureId, int minFilter, int magFilter, boolean generateMipMap, boolean recycle, boolean unbind) {
        GLES20.glActiveTexture(textureSlot);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
        GLES20.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        if (generateMipMap) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }
        if (recycle) {
            bitmap.recycle();
        }
        if (unbind) {
            GLES20.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        }
    }

    public static int[] createTexturesGL2(int count) {
        int[] textureIds = new int[count];
        GLES20.glGenTextures(count, textureIds, 0);
        return textureIds;
    }

    public static void deleteTexturesGL2(@NonNull int[] textureIds) {
        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        GLES20.glDeleteTextures(textureIds.length, textureIds, 0);
    }

    public static void deleteTextureGL2(int textureId) {
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
    }

    public static void clearScreenGL2(float[] argb) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(argb[1], argb[2], argb[3], argb[0]);
    }

    public static int createSingleArrayBufferAndBindGL2() {
        return createSingleBufferAndBindGL2(GLES20.GL_ARRAY_BUFFER);
    }

    public static int createSingleElementArrayBufferAndBindGL2() {
        return createSingleBufferAndBindGL2(GLES20.GL_ELEMENT_ARRAY_BUFFER);
    }

    private static int createSingleBufferAndBindGL2(int bufferType) {
        int[] buffers = new int[1];
        GLES20.glGenBuffers(1, buffers, 0);
        int bufferId = buffers[0];
        GLES20.glBindBuffer(bufferType, bufferId);
        return bufferId;
    }

    public static FloatBuffer createVertexBuffer(int verticesLength) {
        int size = verticesLength * 4;
        ByteBuffer verticesBuffer = ByteBuffer.allocateDirect(size);
        verticesBuffer.order(ByteOrder.nativeOrder());
        return verticesBuffer.asFloatBuffer();
    }

    public static IntBuffer createIndexBuffer(int indexCount) {
        ByteBuffer verticesBuffer = ByteBuffer.allocateDirect(indexCount * 4);
        verticesBuffer.order(ByteOrder.nativeOrder());
        return verticesBuffer.asIntBuffer();
    }

    public static void putIndicesAndFlip(@NonNull IntBuffer buffer, @NonNull int[] indices) {
        buffer.clear();
        buffer.put(indices);
        buffer.position(indices.length);
        buffer.flip();
    }
}
