package com.minelittlepony.render.model;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.util.render.Box;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;

public class ModelQuads extends Box<ModelRenderer> {

    public ModelQuads(ModelRenderer renderer) {
        super(renderer, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    protected List<TexturedQuad> quadList = new ArrayList<TexturedQuad>();

    public ModelQuads addFace(PositionTextureVertex... vertices) {
        quadList.add(new TexturedShape2d(vertices));

        return this;
    }

    public void render(BufferBuilder renderer, float scale) {
        for (TexturedQuad i : quadList) {
            i.draw(renderer, scale);
        }
    }
}
