package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.SpellbookEntity;
import com.minelittlepony.unicopia.server.world.Altar;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class SpellbookEntityRenderer extends LivingEntityRenderer<SpellbookEntity, SpellbookEntityRenderer.State, SpellbookModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/spellbook/normal.png");
    private static final Identifier ALTAR_BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/end_crystal/end_crystal_beam.png");

    public SpellbookEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SpellbookModel(SpellbookModel.getTexturedModelData().createModel()), 0);
        addFeature(new AltarBeamFeature(this));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(SpellbookEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.floatPosition = MathHelper.sin((entity.age + entity.getId()) / 20) * 0.04F;
        state.hasBeams = entity.hasBeams();
        state.altar = entity.getAltar().orElse(null);

        if (state.open) {
            state.breath = MathHelper.sin(entity.age / 20) * 0.01F + 0.1F;

            state.leftPageRot = Math.min(state.limbSwingAnimationProgress + (state.breath * 10), 1);
            state.rightPageRot = Math.min(1 - state.leftPageRot, 1);
            state.openAngle = 0.9f - state.limbSwingAmplitude;

            state.leftPageRot = state.age % 250 < 5 ? (state.age % 5) / 5F : state.leftPageRot;
            state.rightPageRot = state.age % 250 > 105 && state.age % 250 < 110  ? 1-(state.age % 5) / 5F : state.rightPageRot;
        } else {
            state.leftPageRot = 0;
            state.rightPageRot = 0;
            state.openAngle = 0;
            state.breath = 0;
        }
    }

    @Override
    public Identifier getTexture(State entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingPositionRotationDegrees() {
        return 0;
    }

    @Override
    protected void setupTransforms(State state, MatrixStack matrices, float animationProgress, float bodyYaw) {
        super.setupTransforms(state, matrices, animationProgress, bodyYaw + 90);

        if (state.open) {
            matrices.translate(-1.25F, -0.35F, 0);
            matrices.translate(0, state.floatPosition, 0);
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(60));
        } else {
            matrices.translate(-1.5F, 0.1F, 0.2F);
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(90));
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(90));
        }
    }

    @Override
    protected boolean hasLabel(SpellbookEntity targetEntity, double distance) {
        return super.hasLabel(targetEntity, distance)
                && (targetEntity.isCustomNameVisible()
                        || targetEntity.hasCustomName()
                        && targetEntity == dispatcher.targetedEntity);
    }

    static class AltarBeamFeature extends FeatureRenderer<SpellbookEntityRenderer.State, SpellbookModel> {
        public AltarBeamFeature(FeatureRendererContext<SpellbookEntityRenderer.State, SpellbookModel> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, SpellbookEntityRenderer.State state, float limbPos, float limbSpeed) {
            if (!state.hasBeams) {
                return;
            }

            matrices.peek();
            matrices.pop();
            matrices.push();


            Vec3d center = state.altar.origin().toCenterPos().add(0, -1, 0);

            Vec3d bookPos = new Vec3d(state.x, state.y, state.z);
            Vec3d shift = bookPos.subtract(center);

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            matrices.translate(shift.x, shift.y - 1, shift.z);

            for (BlockPos pillar : state.altar.pillars()) {
                renderBeam(center.subtract(pillar.toCenterPos()), state.age, matrices, vertices, light, 1, 0, 1);
            }

            matrices.pop();
        }

        public static float getYOffset(float animationProgress) {
            animationProgress = MathHelper.sin(animationProgress * 0.2F) * 0.5F + 0.5F;
            return ((animationProgress * animationProgress + animationProgress) * 0.4F) - 1.4F;
        }
    }

    public static void renderBeam(Vec3d offset, float age, MatrixStack matrices, VertexConsumerProvider buffers, int light, float r, float g, float b) {
        final float horizontalDistance = (float)offset.horizontalLength();
        final float distance = (float)offset.length();
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float)(-Math.atan2(offset.z, offset.x)) - 1.5707964f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation((float)(-Math.atan2(horizontalDistance, offset.y)) - 1.5707964f));
        VertexConsumer buffer = buffers.getBuffer(RenderLayer.getEntityTranslucent(ALTAR_BEAM_TEXTURE));
        final float minV = age * 0.01f;
        final float maxV = minV + (distance / 32F);
        final int sides = 8;
        final float diameter = 0.35F;
        float segmentX = 0;
        float segmentY = diameter;
        float minU = 0;
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMat = entry.getPositionMatrix();

        for (int i = 1; i <= sides; i++) {
            float o = MathHelper.sin(i * MathHelper.TAU / sides) * diameter;
            float p = MathHelper.cos(i * MathHelper.TAU / sides) * diameter;
            float maxU = i / (float)sides;
            buffer.vertex(positionMat, segmentX * 0.2F, segmentY * 0.2F, 0).color(0, 0, 0, 255).texture(minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, -1, 0);
            buffer.vertex(positionMat, segmentX, segmentY, distance).color(r, g, b, 1).texture(minU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, -1, 0);
            buffer.vertex(positionMat, o, p, distance).color(r, g, b, 1).texture(maxU, maxV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, -1, 0);
            buffer.vertex(positionMat, o * 0.2F, p * 0.2F, 0).color(0, 0, 0, 255).texture(maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, -1, 0);
            segmentX = o;
            segmentY = p;
            minU = maxU;
        }
        matrices.pop();
    }

    public static class State extends LivingEntityRenderState {
        public boolean open;
        public boolean hasBeams;
        public float floatPosition;

        @Nullable
        public Altar altar;

        public float breath;

        public float leftPageRot;
        public float rightPageRot;
        public float openAngle;
    }
}