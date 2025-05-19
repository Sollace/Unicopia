package com.minelittlepony.unicopia.client.render.model;

public class PlaneModel extends BakedModel {
    public static final PlaneModel INSTANCE = new PlaneModel();

    private PlaneModel() {
        addVertex(-1, -1, 0, 0, 0);
        addVertex(-1,  1, 0, 1, 0);
        addVertex( 1,  1, 0, 1, 1);
        addVertex( 1, -1, 0, 0, 1);
    }
}
