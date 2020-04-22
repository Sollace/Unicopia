package com.minelittlepony.unicopia.client.render.model;

import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class ButterflyEntityModel extends EntityModel<ButterflyEntity> {

    private ModelPart body;

    private ModelPart leftWingInner;
    private ModelPart leftWingOuter;

    private ModelPart rightWingInner;
    private ModelPart rightWingOuter;

    public ButterflyEntityModel() {
        textureWidth = 64;
        textureHeight = 64;

        body = new ModelPart(this, 0, 0);
        body.pivotX = -10;
        body.pivotY = 12;

        rightWingInner = new ModelPart(this, 42, 0);
        rightWingInner.roll = -0.2F;
        rightWingInner.addCuboid(-13, -5, 0, 10, 19, 1);

        body.addChild(rightWingInner);

        rightWingOuter = new ModelPart(this, 24, 16);
        rightWingOuter.setPivot(-13, 10, 0.1F);
        rightWingOuter.roll = -0.2F;
        rightWingOuter.addCuboid(0, 0, 0, 10, 12, 1);

        rightWingInner.addChild(rightWingOuter);

        leftWingInner = new ModelPart(this, 42, 0);
        leftWingInner.mirror = true;
        leftWingInner.roll = 0.2F;
        leftWingInner.addCuboid(2, -5, 0, 10, 19, 1);

        body.addChild(leftWingInner);

        leftWingOuter = new ModelPart(this, 24, 16);
        leftWingOuter.mirror = true;
        leftWingOuter.roll = -0.2F;
        leftWingOuter.setPivot(2, 10, 0.1F);
        leftWingOuter.addCuboid(0, 0, 0, 10, 12, 1);

        leftWingInner.addChild(leftWingOuter);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        RenderSystem.disableLighting();
        body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        RenderSystem.enableLighting();
    }

    @Override
    public void setAngles(ButterflyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {

        float flap = MathHelper.cos(ageInTicks) * (float)Math.PI / 4;

        if (entity.isResting()) {
            body.pitch = 0.8F;
            flap = MathHelper.cos(ageInTicks / 10) * (float)Math.PI / 6 + 0.7F;
        } else {
            body.pitch = ((float)Math.PI / 4) + MathHelper.cos(ageInTicks * 0.1F) * 0.15F;
        }

        rightWingInner.yaw = flap;
        leftWingInner.yaw = -flap;
    }
}