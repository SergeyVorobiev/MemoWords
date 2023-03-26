package com.vsv.game.engine;

import android.opengl.GLES20;

import com.vsv.game.engine.screens.VertexData;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VertexBuffer {

    private FloatBuffer vertices;

    private IntBuffer indices;

    private final int objectCount;

    private int currentObjects;

    private final int verticesLength;

    private final int indicesLength;

    private int vertexBufferId;

    private int indexBufferId;

    public VertexBuffer(int vertexSize, int maxObjectSize) {
        this.objectCount = maxObjectSize;
        int vertexCount = objectCount * 4; // 4 vertices per a mesh.
        verticesLength = vertexCount * vertexSize;
        indicesLength = 6 * objectCount; // 6 indices per a mesh.
        this.vertices = GLHelpers.createVertexBuffer(verticesLength);
        this.indices = GLHelpers.createIndexBuffer(indicesLength);
        initializeIndices();
        setupBuffers();
    }

    public void put(VertexData vertexData) {
        currentObjects++;
        vertices.put(vertexData.vertices);
    }

    private void setupBuffers() {
        vertexBufferId = GLHelpers.createSingleArrayBufferAndBindGL2();
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesLength * Float.BYTES, this.vertices, GLES20.GL_STATIC_DRAW);
        indexBufferId = GLHelpers.createSingleElementArrayBufferAndBindGL2();
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesLength * Integer.BYTES, this.indices, GLES20.GL_STATIC_DRAW);
    }

    public void releaseResources() {
        if (vertices != null) {
            vertices.clear();
            vertices = null;
        }
        if (indices != null) {
            indices.clear();
            indices = null;
        }
    }

    private void initializeIndices() {
        int[] temp = new int[6 * objectCount];
        int i1 = 0;
        int i2 = 1;
        int i3 = 2;
        int i4 = 3;
        int k = 0;
        for (int i = 0; i < objectCount; i++) {
            temp[k++] = i1;
            temp[k++] = i2;
            temp[k++] = i3;
            temp[k++] = i3;
            temp[k++] = i4;
            temp[k++] = i1;
            i1 += 4;
            i2 += 4;
            i3 += 4;
            i4 += 4;
        }
        indices.put(temp);
        indices.flip();
    }

    private void applyVertices() {
        int size = vertices.position() * Float.BYTES;
        vertices.flip();
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, size, this.vertices, GLES20.GL_STATIC_DRAW);
        vertices.clear();
    }

    public void draw() {
        applyVertices();
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6 * currentObjects, GLES20.GL_UNSIGNED_INT, 0);
        currentObjects = 0;
    }
}
