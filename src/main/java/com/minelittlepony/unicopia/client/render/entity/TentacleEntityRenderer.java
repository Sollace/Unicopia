package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.TentacleEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public class TentacleEntityRenderer extends EntityRenderer<TentacleEntity, TentacleEntityRenderer.State> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/poison_joke/tentacle.png");

    private final TentacleEntityModel model;

    public TentacleEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new TentacleEntityModel(TentacleEntityModel.getTexturedModelData().createModel());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(TentacleEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.growth = entity.getGrowth(tickDelta);
        state.yaw = entity.getYaw(tickDelta);
        state.pitch = entity.getPitch(tickDelta);
        state.growing = state.growth < 1;
        state.animationFrame = state.age + (entity.getUuid().getMostSignificantBits() % 100);
        state.animationTime = entity.getAnimationTimer(tickDelta);
        state.attackProgress = entity.isAttacking() ? Math.abs(MathHelper.sin(entity.getAttackProgress(tickDelta) * MathHelper.PI)) : 0;
        state.bendIntensity = 1 + entity.getAttackProgress(tickDelta) / 2F;
        state.hurting = entity.hurtTime > 0;
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.scale(-1, -1, 1);
        float scale = state.growth;

        matrices.translate(0, -0.75F + 3F * (1 - scale), 0);
        scale = MathHelper.clamp(scale, 0.5F, 1);
        matrices.scale(scale, scale, scale);

        model.setAngles(state);
        model.render(matrices, vertices.getBuffer(model.getLayer(TEXTURE)), light, OverlayTexture.getUv(0, state.hurting), Colors.WHITE);
        matrices.pop();
        super.render(state, matrices, vertices, light);
    }


    @Override
    protected Box getBoundingBox(TentacleEntity entity) {
        return super.getBoundingBox(entity).expand(10, 0, 10).stretch(0, 10, 0);
    }

    public static class State extends EntityRenderState {
        public float growth;
        public float yaw;
        public float pitch;
        public float animationFrame;
        public float animationTime;

        public float attackProgress;
        public float bendIntensity;

        public boolean growing;
        public boolean hurting;
    }

}