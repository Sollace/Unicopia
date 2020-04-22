package com.minelittlepony.unicopia.client.render.model;

import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.entity.CloudEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.CompositeEntityModel;

public class CloudEntityModel extends CompositeEntityModel<CloudEntity> {

    private final ModelPart body;

    public CloudEntityModel() {
        body = new ModelPart(this, 0, 0);
        body.setTextureSize(250, 90);

        body.setTextureOffset(0, 0);
        body.addCuboid(-24, 5, -24, 48, 10, 48);

        body.setTextureOffset(0, 58);
        body.addCuboid(-10, 14.999F, -10, 30, 2, 30);

        body.setTextureOffset(120, 58);
        body.addCuboid(-10, 3.001F, -10, 30, 2, 30);

        body.pivotY += 4.2;
    }

    @Override
    public Iterable<ModelPart> getParts() {
        return ImmutableList.of(body);
    }

    @Override
    public void setAngles(CloudEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
    }
}
