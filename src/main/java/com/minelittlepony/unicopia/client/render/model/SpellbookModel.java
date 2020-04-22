package com.minelittlepony.unicopia.client.render.model;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.entity.SpellbookEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class SpellbookModel extends EntityModel<SpellbookEntity> {
    private final ModelPart leftCover = (new ModelPart(64, 32, 0, 0)).addCuboid(-6, -5, -0.005F, 6, 10, 0.005F);
    private final ModelPart rightCover = (new ModelPart(64, 32, 16, 0)).addCuboid(0, -5, -0.005F, 6, 10, 0.005F);
    private final ModelPart leftBlock = (new ModelPart(64, 32, 0, 10)).addCuboid(0, -4, -0.99F, 5, 8, 1);
    private final ModelPart rightBlock = (new ModelPart(64, 32, 12, 10)).addCuboid(0, -4, -0.01F, 5, 8, 1);
    private final ModelPart leftPage = (new ModelPart(64, 32, 24, 10)).addCuboid(0, -4, 0, 5, 8, 0.005F);
    private final ModelPart rightPage = (new ModelPart(64, 32, 24, 10)).addCuboid(0, -4, 0, 5, 8, 0.005F);
    private final ModelPart spine = (new ModelPart(64, 32, 12, 0)).addCuboid(-1, -5, 0, 2, 10, 0.005F);

    private final List<ModelPart> parts = ImmutableList.of(leftCover, rightCover, spine, leftBlock, rightBlock, leftPage, rightPage);

    public SpellbookModel() {
       leftCover.setPivot(0, 0, -1);
       rightCover.setPivot(0, 0, 1);
       spine.yaw = 1.5707964F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
       parts.forEach(modelPart -> {
          modelPart.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
       });
    }

    public void setPageAngles(float breath, float leftPageRot, float rightPageRot, float openAngle) {
       float j = (MathHelper.sin(breath * 0.02F) * 0.1F + 1.25F) * openAngle;
       leftCover.yaw = 3.1415927F + j;
       rightCover.yaw = -j;
       leftBlock.yaw = j;
       rightBlock.yaw = -j;
       leftPage.yaw = j - j * 2 * leftPageRot;
       rightPage.yaw = j - j * 2 * rightPageRot;
       leftBlock.pivotX = MathHelper.sin(j);
       rightBlock.pivotX = MathHelper.sin(j);
       leftPage.pivotX = MathHelper.sin(j);
       rightPage.pivotX = MathHelper.sin(j);
    }

    @Override
    public void setAngles(SpellbookEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
        float breath = MathHelper.sin((entity.age + customAngle) / 20) * 0.01F + 0.1F;

        float first_page_rot = limbDistance + (breath * 10);
        float second_page_rot = 1 - first_page_rot;
        float open_angle = 0.9f - limbDistance;

        if (first_page_rot > 1) first_page_rot = 1;
        if (second_page_rot > 1) second_page_rot = 1;

        if (!entity.getIsOpen()) {
            first_page_rot = second_page_rot = open_angle = 0;
        }

        setPageAngles(breath, first_page_rot, second_page_rot, open_angle);
    }
}
