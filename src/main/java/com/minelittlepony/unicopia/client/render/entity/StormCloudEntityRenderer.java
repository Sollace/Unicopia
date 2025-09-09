package com.minelittlepony.unicopia.client.render.entity;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.StormCloudEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

public class StormCloudEntityRenderer extends EntityRenderer<StormCloudEntity, StormCloudEntityRenderer.State> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/storm_cloud.png");
    private static final int DEFAULT_COLOR = ColorHelper.withAlpha(Colors.WHITE, (int)(255 * 0.9F));

    private final Random rng = Random.create(0);
    private final StormCloudEntityModel model = new StormCloudEntityModel(StormCloudEntityModel.getTexturedModelData().createModel());

    public StormCloudEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(StormCloudEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.yaw = entity.getYaw(tickDelta);
        state.scale = entity.getSize(tickDelta);
        state.stormy = entity.isStormy();

        rng.setSeed(entity.getId());
        state.puffLocations = new Vector3f[rng.nextInt(7)];

        for (int i = 0; i < state.puffLocations.length; i++) {
            state.puffLocations[i] = new Vector3f(
                    (float)rng.nextGaussian(),
                    (float)rng.nextGaussian(),
                    (float)rng.nextGaussian()
            ).mul(16);
        }
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.scale(-1, -1, 1);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.yaw));

        matrices.scale(state.scale, state.scale, state.scale);
        matrices.translate(0, -1.45F, 0);

        VertexConsumer buffer = vertices.getBuffer(model.getLayer(TEXTURE));
        light = state.stormy ? 0 : light;

        model.setAngles(state);
        model.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, DEFAULT_COLOR);
        model.getRootPart().rotate(matrices);
        for (Vector3f puffPosition : state.puffLocations) {
            model.renderPuff(puffPosition, matrices, buffer, light, OverlayTexture.DEFAULT_UV, DEFAULT_COLOR);
        }
        matrices.pop();
        super.render(state, matrices, vertices, light);
    }

    @Override
    public boolean shouldRender(StormCloudEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    public static class State extends EntityRenderState {
        public float yaw;
        public float scale;
        public boolean stormy;

        public Vector3f[] puffLocations = new Vector3f[0];
    }
}