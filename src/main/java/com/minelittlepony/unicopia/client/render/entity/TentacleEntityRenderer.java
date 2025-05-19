package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.TentacleEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

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

        matrices.translate(0, -0.75F + 3F * (1 - scale), 0);
        scale = MathHelper.clamp(scale, 0.5F, 1);
        matrices.scale(scale, scale, scale);

        model.setAngles(entity, 0, 0, tickDelta, entity.getYaw(tickDelta), entity.getPitch(tickDelta));
        model.render(matrices, vertices.getBuffer(model.getLayer(getTexture(entity))), light, OverlayTexture.getUv(0, entity.hurtTime > 0), Colors.WHITE);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertices, light);
    }

    @Override
    public Identifier getTexture(TentacleEntity entity) {
        return TEXTURE;
    }
}