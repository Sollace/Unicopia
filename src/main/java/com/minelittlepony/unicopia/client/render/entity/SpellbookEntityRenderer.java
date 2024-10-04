package com.minelittlepony.unicopia.client.render.entity;

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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class SpellbookEntityRenderer extends LivingEntityRenderer<SpellbookEntity, SpellbookModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/spellbook/normal.png");
    private static final Identifier ALTAR_BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/end_crystal/end_crystal_beam.png");

    public SpellbookEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SpellbookModel(SpellbookModel.getTexturedModelData().createModel()), 0);
        addFeature(new AltarBeamFeature(this));
    }

    @Override
    public Identifier getTexture(SpellbookEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingAngle(SpellbookEntity entity) {
        return 0;
    }

    @Override
    protected void setupTransforms(SpellbookEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale) {
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw + 90, tickDelta, scale);

        if (entity.isOpen()) {
            matrices.translate(-1.25F, -0.35F, 0);

            float floatPosition = MathHelper.sin((animationProgress + entity.getId()) / 20) * 0.04F;

            matrices.translate(0, floatPosition, 0);
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(60));
        } else {
            matrices.translate(-1.5F, 0.1F, 0.2F);
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(90));
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(90));
        }
    }

    @Override
    protected boolean hasLabel(SpellbookEntity targetEntity) {
        return super.hasLabel(targetEntity)
                && (targetEntity.isCustomNameVisible()
                        || targetEntity.hasCustomName()
                        && targetEntity == dispatcher.targetedEntity);
    }

    static class AltarBeamFeature extends FeatureRenderer<SpellbookEntity, SpellbookModel> {
        public AltarBeamFeature(FeatureRendererContext<SpellbookEntity, SpellbookModel> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, SpellbookEntity entity, float limbPos, float limbSpeed, float tickDelta, float animationProgress, float yaw, float pitch) {
            if (!entity.hasBeams()) {
                return;
            }

            matrices.peek();
            matrices.pop();
            matrices.push();


            Altar altar = entity.getAltar().get();
            Vec3d center = altar.origin().toCenterPos().add(0, -1, 0);

            float x = (float)MathHelper.lerp(tickDelta, entity.prevX, entity.getX());
            float y = (float)MathHelper.lerp(tickDelta, entity.prevY, entity.getY());
            float z = (float)MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ());
            Vec3d bookPos = new Vec3d(x, y, z);
            Vec3d shift = bookPos.subtract(center);

            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            matrices.translate(shift.x, shift.y - 1, shift.z);

            for (BlockPos pillar : altar.pillars()) {
                renderBeam(center.subtract(pillar.toCenterPos()), -tickDelta, -entity.age, matrices, vertices, light, 1, 0, 1);
            }

            matrices.pop();
        }

        public static float getYOffset(float animationProgress) {
            animationProgress = MathHelper.sin(animationProgress * 0.2F) * 0.5F + 0.5F;
            return ((animationProgress * animationProgress + animationProgress) * 0.4F) - 1.4F;
        }
    }

    public static void renderBeam(Vec3d offset, float tickDelta, int age, MatrixStack matrices, VertexConsumerProvider buffers, int light, float r, float g, float b) {
        final float horizontalDistance = (float)offset.horizontalLength();
        final float distance = (float)offset.length();
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float)(-Math.atan2(offset.z, offset.x)) - 1.5707964f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation((float)(-Math.atan2(horizontalDistance, offset.y)) - 1.5707964f));
        VertexConsumer buffer = buffers.getBuffer(RenderLayer.getEntityTranslucent(ALTAR_BEAM_TEXTURE));
        final float minV = -(age + tickDelta) * 0.01f;
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
}