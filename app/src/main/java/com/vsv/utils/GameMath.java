package com.vsv.utils;

import android.opengl.Matrix;

import androidx.annotation.NonNull;

import com.vsv.game.engine.PositionInfo;

public final class GameMath {

    public static final float HALF_PI = (float) (Math.PI / 2);

    private GameMath() {

    }

    public static float getAngleInDegrees(float x1, float y1, float x2, float y2) {
        return (float) Math.toDegrees(Math.atan2(y1 - y2, x2 - x1));
    }

    public static float getAngleInDegrees(@NonNull PositionInfo point1, @NonNull PositionInfo point2) {
        return getAngleInDegrees(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    public static float getDistance(@NonNull PositionInfo point1, @NonNull PositionInfo point2) {
        return (float) Math.sqrt(Math.pow(point1.getX() - point2.getX(), 2) + Math.pow(point2.getY() - point2.getY(), 2));
    }

    public static float getDistance(float dx, float dy) {
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float getXProjection(float angleDeg, float value) {
        return (float) (value * Math.cos(Math.toRadians(angleDeg)));
    }

    public static float getYProjection(float angleDeg, float value) {
        return (float) (value * Math.sin(Math.toRadians(angleDeg)));
    }

    public static float getAngleByDiff(float dx, float dy) {
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    @NonNull
    public static float[] createIdentityMatrix4x4() {
        float[] matrix = createMatrix4x4();
        Matrix.setIdentityM(matrix, 0);
        return matrix;
    }

    @NonNull
    public static float[] create2dOrthoMatrix(float width, float height) {
        float[] orthoMatrix = GameMath.createMatrix4x4();
        Matrix.orthoM(orthoMatrix, 0, 0, width, 0f, height, 1f, -1f);
        return orthoMatrix;
    }

    @NonNull
    public static float[] createMVP(@NonNull float[] modelMatrix, @NonNull float[] viewMatrix, @NonNull float[] projectionMatrix) {
        float[] mvp = createMatrix4x4();
        Matrix.multiplyMM(mvp, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvp, 0, mvp, 0, modelMatrix, 0);
        return mvp;
    }

    @NonNull
    public static float[] createMatrix4x4() {
        return new float[16];
    }
}
