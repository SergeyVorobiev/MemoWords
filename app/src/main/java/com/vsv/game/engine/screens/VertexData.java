package com.vsv.game.engine.screens;

import com.vsv.game.engine.objects.TextureRegion;

public class VertexData {

    private static final int mLeft1 = 0;
    private static final int mBottom1 = 1;
    private static final int mz1 = 2;
    private static final int mw1 = 3;
    private static final int r1 = 4;
    private static final int g1 = 5;
    private static final int b1 = 6;
    private static final int a1 = 7;
    private static final int u11 = 8;
    private static final int v11 = 9;
    private static final int tp1 = 10;
    private static final int x1 = 11;
    private static final int y1 = 12;
    private static final int z1 = 13;
    private static final int an1 = 14;

    private static final int mLeft2 = 15;
    private static final int mTop2 = 16;
    private static final int mz2 = 17;
    private static final int mw2 = 18;
    private static final int r2 = 19;
    private static final int g2 = 20;
    private static final int b2 = 21;
    private static final int a2 = 22;
    private static final int u21 = 23;
    private static final int v22 = 24;
    private static final int tp2 = 25;
    private static final int x2 = 26;
    private static final int y2 = 27;
    private static final int z2 = 28;
    private static final int an2 = 29;

    private static final int mRight3 = 30;
    private static final int mTop3 = 31;
    private static final int mz3 = 32;
    private static final int mw3 = 33;
    private static final int r3 = 34;
    private static final int g3 = 35;
    private static final int b3 = 36;
    private static final int a3 = 37;
    private static final int u32 = 38;
    private static final int v32 = 39;
    private static final int tp3 = 40;
    private static final int x3 = 41;
    private static final int y3 = 42;
    private static final int z3 = 43;
    private static final int an3 = 44;

    private static final int mRight4 = 45;
    private static final int mBottom4 = 46;
    private static final int mz4 = 47;
    private static final int mw4 = 48;
    private static final int r4 = 49;
    private static final int g4 = 50;
    private static final int b4 = 51;
    private static final int a4 = 52;
    private static final int u42 = 53;
    private static final int v41 = 54;
    private static final int tp4 = 55;
    private static final int x4 = 56;
    private static final int y4 = 57;
    private static final int z4 = 58;
    private static final int an4 = 59;

    private float width;

    private float height;

    private float scaleFactor = 1;

    public final float[] vertices = new float[60];

    private boolean calculated = false;

    public final int textureIndex;

    public VertexData(float width, float height, int textureIndex) {
        this.textureIndex = textureIndex;
        setSize(width, height, scaleFactor);
        setColor(1, 1, 1, 1);
        vertices[mz1] = 0;
        vertices[mz2] = 0;
        vertices[mz3] = 0;
        vertices[mz4] = 0;
        vertices[mw1] = 1;
        vertices[mw2] = 1;
        vertices[mw3] = 1;
        vertices[mw4] = 1;
        vertices[z1] = 0;
        vertices[z2] = 0;
        vertices[z3] = 0;
        vertices[z4] = 0;
    }

    public void setAngle(float angle) {
        vertices[an1] = angle;
        vertices[an2] = angle;
        vertices[an3] = angle;
        vertices[an4] = angle;
    }

    public void copy(VertexData vertexData) {
        System.arraycopy(vertexData.vertices, 0, vertices, 0, vertices.length);
    }

    public float getAngle() {
        return vertices[an1];
    }

    public void setXY(float x, float y) {
        vertices[x1] = x;
        vertices[x2] = x;
        vertices[x3] = x;
        vertices[x4] = x;
        vertices[y1] = y;
        vertices[y2] = y;
        vertices[y3] = y;
        vertices[y4] = y;
    }

    public void scale(float scaleFactor) {
        if (this.scaleFactor == scaleFactor && calculated) {
            return;
        }
        this.scaleFactor = scaleFactor;
        calculateModelView(width * scaleFactor, height * scaleFactor);
    }

    public void setColor(float a, float r, float g, float b) {
        vertices[a1] = a;
        vertices[a2] = a;
        vertices[a3] = a;
        vertices[a4] = a;
        vertices[r1] = r;
        vertices[r2] = r;
        vertices[r3] = r;
        vertices[r4] = r;
        vertices[g1] = g;
        vertices[g2] = g;
        vertices[g3] = g;
        vertices[g4] = g;
        vertices[b1] = b;
        vertices[b2] = b;
        vertices[b3] = b;
        vertices[b4] = b;
    }

    public void setColor(float[] argb) {
        setColor(argb[0], argb[1], argb[2], argb[3]);
    }

    public void resetColor() {
        setColor(1, 1, 1, 1);
    }

    public void setupTexturePosition(TextureRegion textureRegion, int textureIndex) {
        setupUV(textureRegion.u1, textureRegion.v1, textureRegion.u2, textureRegion.v2);
        setTextureIndex(textureIndex);
    }

    public void setupUV(float u1, float v1, float u2, float v2) {
        vertices[u11] = u1;
        vertices[u21] = u1;
        vertices[v11] = v1;
        vertices[v41] = v1;
        vertices[u32] = u2;
        vertices[u42] = u2;
        vertices[v22] = v2;
        vertices[v32] = v2;
    }

    public void setTextureIndex(int textureIndex) {
        vertices[tp1] = textureIndex;
        vertices[tp2] = textureIndex;
        vertices[tp3] = textureIndex;
        vertices[tp4] = textureIndex;
    }

    public float getScaleFactor() {
        return scaleFactor;

    }

    public void setSize(float width, float height, float scaleFactor) {
        this.width = width;
        this.height = height;
        this.scaleFactor = scaleFactor;
        calculateModelView(width * scaleFactor, height * scaleFactor);
    }

    private void calculateModelView(float width, float height) {
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        vertices[mLeft1] = -halfWidth;
        vertices[mRight3] = halfWidth;
        vertices[mTop2] = halfHeight;
        vertices[mBottom1] = -halfHeight;
        vertices[mLeft2] = vertices[mLeft1];
        vertices[mTop3] = vertices[mTop2];
        vertices[mRight4] = vertices[mRight3];
        vertices[mBottom4] = vertices[mBottom1];
        this.calculated = true;
    }
}
