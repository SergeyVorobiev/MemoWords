package com.vsv.game.engine.objects;

import androidx.annotation.NonNull;

import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.screens.VertexData;

public class ViewEnemy {

    private final VertexData textVertexData;

    private final VertexData vertexData;

    public ViewEnemy(VertexData vertexData) {
        this.vertexData = vertexData;
        textVertexData = new VertexData(0, 0, 0);
    }

    public void draw(@NonNull DrawInstruments<?> drawInstruments, int textTextureId) {
        textVertexData.copy(vertexData);
        textVertexData.setAngle(0);
        textVertexData.setTextureIndex(textTextureId);
        textVertexData.setupUV(0, 1, 1, 0);
        drawInstruments.draw(textVertexData);
    }
}
