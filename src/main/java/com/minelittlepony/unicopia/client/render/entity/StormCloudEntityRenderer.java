package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.StormCloudEntity;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;
import net.minecraft.util.math.RotationAxis;

public class StormCloudEntityRenderer extends EntityRenderer<StormCloudEntity> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/storm_cloud.png");
    private static final int DEFAULT_COLOR = Argb.withAlpha(Colors.WHITE, (int)(255 * 0.9F));

    private final StormCloudEntityModel model;

    public StormCloudEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new StormCloudEntityModel(StormCloudEntityModel.getTexturedModelData().createModel());
    }

    @Override
    public void render(StormCloudEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.scale(-1, -1, 1);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));

        float scale = entity.getSize(tickDelta);

        matrices.scale(scale, scale, scale);
        matrices.translate(0, -1.45F, 0);


        model.setAngles(entity, 0, 0, 0, 0, 0);
        model.render(matrices, vertices.getBuffer(model.getLayer(getTexture(entity))),
                entity.isStormy() ? 0 : light,
                OverlayTexture.DEFAULT_UV, DEFAULT_COLOR);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertices, light);
    }

    @Override
    public Identifier getTexture(StormCloudEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(StormCloudEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }
}