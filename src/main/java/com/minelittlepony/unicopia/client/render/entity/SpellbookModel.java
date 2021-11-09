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
    }

    public static TexturedModelData getTexturedModelData() {
        return BookModel.getTexturedModelData();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
       book.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }

    @Deprecated
    public void setPageAngles(float breath, float leftPageRot, float rightPageRot, float openAngle) {
       book.setPageAngles(breath, leftPageRot, rightPageRot, openAngle);
    }

    @Override
    public void setAngles(SpellbookEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
        float breath = MathHelper.sin((entity.age + customAngle) / 20) * 0.01F + 0.1F;

        float first_page_rot = limbDistance + (breath * 10);
        float second_page_rot = 1 - first_page_rot;
        float open_angle = 0.9f - limbDistance;

        if (first_page_rot > 1) first_page_rot = 1;
        if (second_page_rot > 1) second_page_rot = 1;

        if (!entity.isOpen()) {
            first_page_rot = second_page_rot = open_angle = 0;
        }

        book.setPageAngles(breath, first_page_rot, second_page_rot, open_angle);
    }
}
