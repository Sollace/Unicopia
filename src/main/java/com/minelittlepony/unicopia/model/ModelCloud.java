package com.minelittlepony.unicopia.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCloud extends ModelBase {

    private final ModelRenderer body;

    public ModelCloud() {
        body = new ModelRenderer(this, 0, 0);
        body.setTextureSize(250, 90);

        body.setTextureOffset(0, 0);
        body.addBox(-24, 5, -24, 48, 10, 48);

        body.setTextureOffset(0, 58);
        body.addBox(-10, 14.999F, -10, 30, 2, 30);

        body.setTextureOffset(120, 58);
        body.addBox(-10, 3.001F, -10, 30, 2, 30);

        body.rotationPointY += 4.2;
    }

    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        body.render(scale);
    }
}
