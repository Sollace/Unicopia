package com.minelittlepony.unicopia.client.render.entity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModel;

public class SpellbookModel extends EntityModel<SpellbookEntityRenderer.State> {

    private final BookModel book;

    public SpellbookModel(ModelPart root) {
        super(root);
        book = new BookModel(root);
    }

    public static TexturedModelData getTexturedModelData() {
        return BookModel.getTexturedModelData();
    }

    @Override
    public void setAngles(SpellbookEntityRenderer.State state) {
        book.resetTransforms();
        book.setPageAngles(state.breath, state.leftPageRot, state.rightPageRot, state.openAngle);
        root.getChild("seam").originX -= 0.01F;
    }
}
