package com.minelittlepony.unicopia.world.client.render.model;

import com.minelittlepony.unicopia.world.client.render.RenderLayers;
import com.minelittlepony.unicopia.world.entity.CloudEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class CloudEntityModel extends EntityModel<CloudEntity> {

    private final ModelPart body;

    public CloudEntityModel() {
        super(RenderLayers::cloud);
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
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }

    @Override
    public void setAngles(CloudEntity entity, float limbAngle, float limbDistance, float customAngle, float headYaw, float headPitch) {
    }
}
