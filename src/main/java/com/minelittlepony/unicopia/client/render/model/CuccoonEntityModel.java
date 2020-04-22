package com.minelittlepony.unicopia.client.render.model;

import com.minelittlepony.unicopia.entity.CuccoonEntity;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class CuccoonEntityModel extends EntityModel<CuccoonEntity> {

    private final ModelPart body;

    private float breatheAmount;

    public CuccoonEntityModel() {
        body = new ModelPart(this, 0, 0);
        body.setTextureSize(250, 250);

        body.setTextureOffset(0, 0);

        // cuccoon shape
        body.addCuboid(-4, -2, -4, 8, 2, 8);
        body.addCuboid(-7.5F, 0, -7.5F, 15, 6, 15);
        body.addCuboid(-10, 4, -10, 20, 6, 20);
        body.addCuboid(-11.5F, 10, -11.5F, 23, 8, 23);
        body.addCuboid(-10, 17, -10, 20, 6, 20);
        body.addCuboid(-11.5F, 22, -11.5F, 23, 2, 23);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        matrices.push();

        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableRescaleNormal();

        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);

        matrices.scale(1 - breatheAmount, 1 + breatheAmount, 1 - breatheAmount);
        matrices.translate(0, -breatheAmount * 1.3F, 0);

        body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);

        matrices.scale(0.9F, 0.9F, 0.9F);
        matrices.translate(0, 0.2F, 0);

        body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);

        RenderSystem.disableRescaleNormal();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    @Override
    public void setAngles(CuccoonEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
        breatheAmount = entity.getBreatheAmount(customAngle) / 8;
    }
}
