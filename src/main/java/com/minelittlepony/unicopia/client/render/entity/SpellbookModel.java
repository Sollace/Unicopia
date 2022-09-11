package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.entity.SpellbookEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class SpellbookModel extends EntityModel<SpellbookEntity> {

    private final BookModel book;

    public SpellbookModel(ModelPart root) {
        book = new BookModel(root);
        root.getChild("seam").pivotX -= 0.01F;
    }

    public static TexturedModelData getTexturedModelData() {
        return BookModel.getTexturedModelData();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
       book.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }

    @Override
    public void setAngles(SpellbookEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
        float breath = MathHelper.sin((entity.age + customAngle) / 20) * 0.01F + 0.1F;

        float leftPageRot = Math.min(limbDistance + (breath * 10), 1);
        float rightPageRot = Math.min(1 - leftPageRot, 1);
        float open_angle = 0.9f - limbDistance;

        leftPageRot = entity.age % 250 < 5 ? (entity.age % 5) / 5F : leftPageRot;
        rightPageRot = entity.age % 250 > 105 && entity.age % 250 < 110  ? 1-(entity.age % 5) / 5F : rightPageRot;

        if (!entity.isOpen()) {
            leftPageRot = rightPageRot = open_angle = 0;
        }

        book.setPageAngles(breath, leftPageRot, rightPageRot, open_angle);
    }
}
