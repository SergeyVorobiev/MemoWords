package com.vsv.game.engine.screens;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.vsv.game.engine.GLHelpers;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.TextureSettings;
import com.vsv.game.engine.VertexBuffer;
import com.vsv.game.engine.gl.shaders.ShaderProgramGL2;
import com.vsv.memorizer.R;
import com.vsv.utils.GameMath;


public class VertexGL2 {

    private ShaderProgramGL2 program;

    private VertexBuffer buffer;

    private int[] tids;

    private final int vertexSize;

    private final int maxObjectSize;

    public VertexGL2(int vertexSize, int maxObjectSize) {
        this.vertexSize = vertexSize;
        this.maxObjectSize = maxObjectSize;
    }

    public void setupGL(float width, float height, @NonNull TextureSettings[] textureSettings) {
        setupBuffers();
        setupTexture(textureSettings);

        // Hardcore part
        setupProgram(textureSettings);
        setupMatrices(width, height);
    }

    private void setupBuffers() {
        buffer = new VertexBuffer(vertexSize, maxObjectSize);
    }

    private void setupTexture(@NonNull TextureSettings[] textureSettings) {
        if (textureSettings.length == 0) {
            throw new RuntimeException("Textures do not exist.");
        }
        if (tids != null) {
            throw new RuntimeException("Textures are already set up");
        }
        tids = GLHelpers.createTexturesGL2(textureSettings.length);
        for (int i = 0; i < textureSettings.length; i++) {
            TextureSettings textSettings = textureSettings[i];
            GLHelpers.loadTextureGL2(textSettings.slot, textSettings.bitmap, tids[i],
                    textSettings.minFilter, textSettings.magFilter, false, false, true);
        }
        GLHelpers.enableBlendGL2();
    }

    public void reloadTexture(@NonNull TextureSettings textureSettings, int index, boolean recycle) {
        GLHelpers.deleteTextureGL2(tids[index]);
        GLHelpers.loadTextureGL2(textureSettings.slot, textureSettings.bitmap, tids[index],
                textureSettings.minFilter, textureSettings.magFilter, false, recycle, true);
    }

    public void releaseResources() {
        if (buffer != null) {
            buffer.releaseResources();
            buffer = null;
        }
        if (tids != null) {
            GLHelpers.deleteTexturesGL2(tids);
            tids = null;
        }
        if (program != null) {
            program.deleteProgram();
            program = null;
        }
    }

    private void setupProgram(@NonNull TextureSettings[] textureSettings) {
        if (program == null) {
            int vertexCountPerObject = 4;
            program = GLHelpers.loadProgram("simple", "vertex.vsh", "fragment.vsh");
            program.enableFloatAttribute("vertexPositionIn", vertexCountPerObject, vertexSize, 0);
            program.enableFloatAttribute("vertexColorIn", vertexCountPerObject, vertexSize, 4);
            program.enableFloatAttribute("textureIn", vertexCountPerObject, vertexSize, 8);
            program.enableFloatAttribute("positionAngle", vertexCountPerObject, vertexSize, 11);
            program.use();
            int[] counts = new int[textureSettings.length];
            for (int i = 0; i < textureSettings.length; i++) {
                counts[i] = i;
            }
            program.setTextures("textures", counts);
        }
    }

    private void setupMatrices(float width, float height) {
        int mvpMatrixIndex = program.getUniformLocation("mvpMatrix");
        float[] projectionMatrix = GameMath.create2dOrthoMatrix(width, height);
        float[] viewMatrix = GameMath.createIdentityMatrix4x4();
        float[] modelMatrix = GameMath.createIdentityMatrix4x4();
        float[] mvpMatrix = GameMath.createMVP(modelMatrix, viewMatrix, projectionMatrix);
        program.setMatrix(mvpMatrixIndex, mvpMatrix);
    }

    public void put(VertexData vertexData) {
        buffer.put(vertexData);
    }

    public void draw() {
        for (int i = 0; i < tids.length; i++) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i); // same texture slot which we've used on initialization
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tids[i]);
        }
        buffer.draw();
        GLES20.glFinish();
    }
}
