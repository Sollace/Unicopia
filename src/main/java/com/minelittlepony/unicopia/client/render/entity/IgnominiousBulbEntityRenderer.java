package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.IgnominiousBulbEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class IgnominiousBulbEntityRenderer extends EntityRenderer<IgnominiousBulbEntity> {
    private static final Identifier IDLE_TEXTURE = Unicopia.id("textures/entity/poison_joke/bulb_idle.png");
    private static final Identifier ANGRY_TEXTURE = Unicopia.id("textures/entity/poison_joke/bulb_angry.png");

    private final IgnominiousBulbEntityModel model;

    public IgnominiousBulbEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new IgnominiousBulbEntityModel(IgnominiousBulbEntityModel.getTexturedModelData().createModel());
    }

    @Override
    public void render(IgnominiousBulbEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.scale(-1, -1, 1);
        matrices.translate(0, -1.5F, 0);

        model.setAngles(entity, 0, 0, tickDelta, entity.getYaw(tickDelta), entity.getPitch(tickDelta));
        model.render(matrices, vertices.getBuffer(model.getLayer(getTexture(entity))), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertices, light);
    }

    @Override
    public Identifier getTexture(IgnominiousBulbEntity entity) {
        return entity.isAngry() ? ANGRY_TEXTURE : IDLE_TEXTURE;
    }
}