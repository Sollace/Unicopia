package com.minelittlepony.unicopia.model;

import com.minelittlepony.unicopia.entity.EntityCuccoon;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;

public class ModelCuccoon extends ModelBase {

    private final ModelRenderer body;

    public ModelCuccoon() {
        body = new ModelRenderer(this, 0, 0);
        body.setTextureSize(250, 250);

        body.setTextureOffset(0, 0);

        // cuccoon shape
        body.addBox(-4, -2, -4, 8, 2, 8);
        body.addBox(-7.5F, 0, -7.5F, 15, 6, 15);
        body.addBox(-10, 4, -10, 20, 6, 20);
        body.addBox(-11.5F, 10, -11.5F, 23, 8, 23);
        body.addBox(-10, 17, -10, 20, 6, 20);
        body.addBox(-11.5F, 22, -11.5F, 23, 2, 23);


        // pile of blocks
        // body.addBox(-10, offsetY + 10, -10, 12, 12, 12);
        // body.addBox(-14, offsetY + 14, 4, 10, 10, 10);
        // body.addBox(-17, offsetY + 17, 3, 8, 8, 8);
        // body.addBox(0, offsetY + 10, 0, 12, 12, 12);
        // body.addBox(-7, offsetY + 6, -7, 16, 16, 16);
        // body.addBox(-7, offsetY + 0, -7, 12, 12, 12);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        float breatheAmount = ((EntityCuccoon)entity).getBreatheAmount(ageInTicks) / 8;

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableNormalize();

        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.scale(1 - breatheAmount, 1 + breatheAmount, 1 - breatheAmount);
        GlStateManager.translate(0, -breatheAmount * 1.3F, 0);

        body.render(scale);

        GlStateManager.scale(0.9, 0.9, 0.9);
        GlStateManager.translate(0, 0.2F, 0);

        body.render(scale);

        GlStateManager.disableNormalize();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }
}
