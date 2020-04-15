package com.minelittlepony.unicopia.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.model.Quad;
import net.minecraft.client.model.Vertex;
import net.minecraft.client.render.BufferBuilder;

public class ModelQuads extends Box {

    public ModelQuads(Cuboid renderer) {
        super(renderer, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    protected List<Quad> quadList = new ArrayList<>();

    public ModelQuads addFace(Vertex... vertices) {
        quadList.add(new TexturedShape2d(vertices));

        return this;
    }

    @Override
    public void render(BufferBuilder renderer, float scale) {
        for (Quad i : quadList) {
            i.render(renderer, scale);
        }
    }
}
