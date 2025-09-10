package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.LimbAnimationUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;

public class HeldEntityFeatureRenderer<S extends BipedEntityRenderState, E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<S> {
    public HeldEntityFeatureRenderer(FeatureRendererContext<S, ? extends BipedEntityModel<S>> context) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S entity, float limbAngle, float limbDistance) {
        CasterState state = CasterState.of(entity);
        CasterState.PassengerState carriedEntity = state.carriedEntity;
        if (carriedEntity.passenger != null) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180 - state.leanAmount * 90));

            matrices.translate(carriedEntity.carryPosition.x, carriedEntity.carryPosition.y, carriedEntity.carryPosition.z);
            if (!(carriedEntity.passenger instanceof Pony)) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            }

            renderCarriedEntity(carriedEntity.passenger.asEntity(), matrices, vertexConsumers, light, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
            matrices.pop();
        }
    }

    @Override
    public void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S entity, ModelPart arm, Arm side) {

    }

    @Override
    public boolean beforeRenderArms(ArmRenderer sender, MatrixStack matrices, VertexConsumerProvider vertexConsumers, S entity, int light) {
        CasterState state = CasterState.of(entity);
        CasterState.PassengerState carriedEntity = state.carriedEntity;
        if (carriedEntity.passenger != null) {
            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
            float f = -0.4f * MathHelper.sin(MathHelper.sqrt(entity.handSwingProgress) * (float)Math.PI);
            float g = 0.2f * MathHelper.sin(MathHelper.sqrt(entity.handSwingProgress) * ((float)Math.PI * 2));
            float h = -0.2f * MathHelper.sin(entity.handSwingProgress * (float)Math.PI);
            matrices.push();
            matrices.translate(f, g, h);
            matrices.translate(0, -1.3F, carriedEntity.passenger instanceof Pony ? -1.9F : -1.3F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(carriedEntity.passenger instanceof Pony ? 33 : 13));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(carriedEntity.passenger instanceof Pony ? 180 : 90));

            renderCarriedEntity(carriedEntity.passenger.asEntity(), matrices, vertexConsumers, light, tickDelta);
            matrices.pop();

            float equipProgress = 1 - sender.getEquipProgress(Hand.MAIN_HAND, tickDelta);

            matrices.push();
            sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, entity.handSwingProgress, Arm.LEFT);
            matrices.pop();
            matrices.push();
            sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, entity.handSwingProgress, Arm.RIGHT);
            matrices.pop();
            return true;
        }

        return false;
    }

    private void renderCarriedEntity(LivingEntity p, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        @SuppressWarnings("unchecked")
        var renderer = (EntityRenderer<LivingEntity, LivingEntityRenderState>)MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(p);

        LimbAnimationUtil.resetToZero(p.limbAnimator);
        Entity vehicle = p.getVehicle();
        ((EntityDuck)p).setVehicle(null);

        boolean onGround = p.isOnGround();
        p.setOnGround(true);
        var state = renderer.getAndUpdateRenderState(p, tickDelta);
        state.yawDegrees = 0;
        state.bodyYaw = 0;
        p.setOnGround(onGround);

        ((EntityDuck)p).setVehicle(vehicle);
        renderer.render(state, matrices, vertexConsumers, light);

    }

    public static Vec3d getCarryPosition(Living<?> entity, Living<?> passenger) {
        float passengerHeight = MineLPDelegate.getInstance().getPonyHeight(passenger.asEntity()) / 2F;
        float carrierHeight = MineLPDelegate.getInstance().getPonyHeight(entity.asEntity()) / 5F;

        if (entity instanceof Pony pony
                && MineLPDelegate.getInstance().getPlayerPonyRace(pony.asEntity()).isEquine()
                && pony.getPhysics().isFlying()) {
            return new Vec3d(0,
                    -carrierHeight * 10 - passengerHeight * 2,
                    0
            );
        }

        return new Vec3d(0,
                -passengerHeight - carrierHeight,
                entity.asEntity().getWidth()
        );
    }
}
