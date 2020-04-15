package com.minelittlepony.unicopia.client.render.model;

import com.minelittlepony.unicopia.entity.CloudEntity;

import net.minecraft.client.model.Cuboid;
import net.minecraft.client.render.entity.model.EntityModel;

public class CloudEntityModel extends EntityModel<CloudEntity> {

    private final Cuboid body;

    public CloudEntityModel() {
        body = new Cuboid(this, 0, 0);
        body.setTextureSize(250, 90);

        body.setTextureOffset(0, 0);
        body.addBox(-24, 5, -24, 48, 10, 48);

        body.setTextureOffset(0, 58);
        body.addBox(-10, 14.999F, -10, 30, 2, 30);

        body.setTextureOffset(120, 58);
        body.addBox(-10, 3.001F, -10, 30, 2, 30);

        body.rotationPointY += 4.2;
    }

    @Override
    public void render(CloudEntity cloud, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        body.render(scale);
    }
}
