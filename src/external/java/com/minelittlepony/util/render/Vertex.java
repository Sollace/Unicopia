package com.minelittlepony.util.render;

import net.minecraft.client.model.PositionTextureVertex;

//#MineLittlePony#
public class Vertex extends PositionTextureVertex {

    public Vertex(float x, float y, float z, float texX, float texY) {
        super(x, y, z, texX, texY);
    }

    private Vertex(Vertex old, float texX, float texY) {
        super(old, texX, texY);
    }

    @Override
    public Vertex setTexturePosition(float texX, float texY) {
        return new Vertex(this, texX, texY);
    }
}
