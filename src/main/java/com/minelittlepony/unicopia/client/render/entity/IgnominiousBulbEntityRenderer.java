package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.IgnominiousBulbEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class IgnominiousBulbEntityRenderer extends EntityRenderer<IgnominiousBulbEntity, IgnominiousBulbEntityRenderer.State> {
    private static final Identifier IDLE_TEXTURE = Unicopia.id("textures/entity/poison_joke/bulb_idle.png");
    private static final Identifier ANGRY_TEXTURE = Unicopia.id("textures/entity/poison_joke/bulb_angry.png");

    private final IgnominiousBulbEntityModel model;

    public IgnominiousBulbEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new IgnominiousBulbEntityModel(IgnominiousBulbEntityModel.getTexturedModelData().createModel());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(IgnominiousBulbEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.angry = entity.isAngry();
        state.scale = entity.getScale(tickDelta);
        state.pitch = entity.getPitch(tickDelta);
        state.yaw = (180 + entity.getYaw(tickDelta)) * MathHelper.RADIANS_PER_DEGREE;
        state.hurting = entity.hurtTime > 0;
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.scale(-1, -1, 1);
        matrices.translate(0, -1.5F, 0);

        model.setAngles(state);
        model.render(matrices, vertices.getBuffer(model.getLayer(state.angry ? ANGRY_TEXTURE : IDLE_TEXTURE)), light, OverlayTexture.getUv(0, state.hurting), Colors.WHITE);
        matrices.pop();
        super.render(state, matrices, vertices, light);
    }

    public static class State extends EntityRenderState {
        public boolean angry;
        public boolean hurting;
        public float pitch;
        public float yaw;
        public float scale;
    }
}