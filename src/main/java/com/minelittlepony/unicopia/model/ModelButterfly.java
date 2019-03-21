package com.minelittlepony.unicopia.model;

import com.minelittlepony.unicopia.entity.EntityButterfly;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelButterfly extends ModelBase {

    private ModelRenderer body;

    private ModelRenderer leftWingInner;
    private ModelRenderer leftWingOuter;

    private ModelRenderer rightWingInner;
    private ModelRenderer rightWingOuter;

    public ModelButterfly() {
        textureWidth = 64;
        textureHeight = 64;
        init();

    }

    void init() {
        this.boxList.clear();

        body = new ModelRenderer(this, 0, 0);
        body.rotationPointZ = -10;
        body.rotationPointY = 12;

        rightWingInner = new ModelRenderer(this, 42, 0);
        rightWingInner.rotateAngleZ = -0.2F;
        rightWingInner.addBox(-13, -5, 0, 10, 19, 1);

        body.addChild(rightWingInner);

        rightWingOuter = new ModelRenderer(this, 24, 16);
        rightWingOuter.setRotationPoint(-13, 10, 0.1F);
        rightWingOuter.rotateAngleZ = -0.2F;
        rightWingOuter.addBox(0, 0, 0, 10, 12, 1);

        rightWingInner.addChild(rightWingOuter);

        leftWingInner = new ModelRenderer(this, 42, 0);
        leftWingInner.mirror = true;
        leftWingInner.rotateAngleZ = 0.2F;
        leftWingInner.addBox(2, -5, 0, 10, 19, 1);

        body.addChild(leftWingInner);

        leftWingOuter = new ModelRenderer(this, 24, 16);
        leftWingOuter.mirror = true;
        leftWingOuter.rotateAngleZ = -0.2F;
        leftWingOuter.setRotationPoint(2, 10, 0.1F);
        leftWingOuter.addBox(0, 0, 0, 10, 12, 1);

        leftWingInner.addChild(leftWingOuter);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        GlStateManager.disableLighting();

        body.render(scale);

        GlStateManager.enableLighting();
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {

        float flap = MathHelper.cos(ageInTicks) * (float)Math.PI / 4;

        if (((EntityButterfly)entity).isResting()) {
            body.rotateAngleX = 0.8F;
            flap = MathHelper.cos(ageInTicks / 10) * (float)Math.PI / 6 + 0.7F;
        } else {
            body.rotateAngleX = ((float)Math.PI / 4) + MathHelper.cos(ageInTicks * 0.1F) * 0.15F;
        }

        rightWingInner.rotateAngleY = flap;
        leftWingInner.rotateAngleY = -flap;
    }
}