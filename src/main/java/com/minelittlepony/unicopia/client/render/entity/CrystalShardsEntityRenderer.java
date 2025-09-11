package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;

import org.joml.Quaternionf;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.CrystalShardsEntity;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class CrystalShardsEntityRenderer extends EntityRenderer<CrystalShardsEntity, CrystalShardsEntityRenderer.State> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/crystal_shards/normal.png");
    private static final Identifier[] CORRUPTED = List.of("corrupt", "dark", "darker").stream()
            .map(name -> Unicopia.id("textures/entity/crystal_shards/" + name + ".png"))
            .toArray(Identifier[]::new);

    private final CrystalShardsEntityModel model;

    public CrystalShardsEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new CrystalShardsEntityModel(CrystalShardsEntityModel.getTexturedModelData().createModel());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(CrystalShardsEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.shaking = entity.isShaking();
        state.destructionStage = FloatingArtefactEntityRenderer.getDestructionStage(entity);
        state.rotation = entity.getAttachmentFace().getRotationQuaternion();
        state.growth = entity.getGrowth(tickDelta);
        state.yaw = entity.getYaw(tickDelta);
        state.corruption = entity.isCorrupt() ? (int)(Math.abs(entity.getUuid().getMostSignificantBits()) % CORRUPTED.length) : -1;
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        vertices = FloatingArtefactEntityRenderer.getDestructionOverlayProvider(matrices, vertices, 4, state.destructionStage);

        matrices.push();
        matrices.multiply(state.rotation);
        matrices.scale(-1, -1, 1);

        matrices.scale(state.growth, state.growth, state.growth);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.yaw));

        model.setAngles(state);
        model.render(matrices, vertices.getBuffer(model.getLayer(state.corruption > -1 ? CORRUPTED[state.corruption] : TEXTURE)), light, OverlayTexture.DEFAULT_UV, Colors.WHITE);
        matrices.pop();
        super.render(state, matrices, vertices, light);
    }

    public static class State extends EntityRenderState {
        public boolean shaking;
        public int destructionStage;
        public Quaternionf rotation;
        public float growth;
        public float yaw;

        public int corruption = -1;
    }
}