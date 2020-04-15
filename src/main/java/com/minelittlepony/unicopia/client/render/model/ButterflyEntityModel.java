package com.minelittlepony.unicopia.client.render.model;

import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

public class ButterflyEntityModel extends EntityModel<ButterflyEntity> {

    private Cuboid body;

    private Cuboid leftWingInner;
    private Cuboid leftWingOuter;

    private Cuboid rightWingInner;
    private Cuboid rightWingOuter;

    public ButterflyEntityModel() {
        textureWidth = 64;
        textureHeight = 64;

        body = new Cuboid(this, 0, 0);
        body.rotationPointZ = -10;
        body.rotationPointY = 12;

        rightWingInner = new Cuboid(this, 42, 0);
        rightWingInner.roll = -0.2F;
        rightWingInner.addBox(-13, -5, 0, 10, 19, 1);

        body.addChild(rightWingInner);

        rightWingOuter = new Cuboid(this, 24, 16);
        rightWingOuter.setRotationPoint(-13, 10, 0.1F);
        rightWingOuter.roll = -0.2F;
        rightWingOuter.addBox(0, 0, 0, 10, 12, 1);

        rightWingInner.addChild(rightWingOuter);

        leftWingInner = new Cuboid(this, 42, 0);
        leftWingInner.mirror = true;
        leftWingInner.roll = 0.2F;
        leftWingInner.addBox(2, -5, 0, 10, 19, 1);

        body.addChild(leftWingInner);

        leftWingOuter = new Cuboid(this, 24, 16);
        leftWingOuter.mirror = true;
        leftWingOuter.roll = -0.2F;
        leftWingOuter.setRotationPoint(2, 10, 0.1F);
        leftWingOuter.addBox(0, 0, 0, 10, 12, 1);

        leftWingInner.addChild(leftWingOuter);
    }

    @Override
    public void render(ButterflyEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        setAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

        GlStateManager.disableLighting();

        body.render(scale);

        GlStateManager.enableLighting();
    }

    @Override
    public void setAngles(ButterflyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

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