package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.TentacleEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TentacleEntityRenderer extends EntityRenderer<TentacleEntity> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/poison_joke/tentacle.png");

    private final TentacleEntityModel model;

    public TentacleEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new TentacleEntityModel(TentacleEntityModel.getTexturedModelData().createModel());
    }

    @Override
    public void render(TentacleEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.scale(-1, -1, 1);
        float scale = entity.getGrowth(tickDelta);

        matrices.scale(scale, scale, scale);
        matrices.translate(0, -0.9F, 0);

        model.setAngles(entity, 0, 0, tickDelta, entity.getYaw(tickDelta), entity.getPitch(tickDelta));
        model.render(matrices, vertices.getBuffer(model.getLayer(getTexture(entity))), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertices, light);
    }

    @Override
    public Identifier getTexture(TentacleEntity entity) {
        return TEXTURE;
    }
}