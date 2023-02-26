package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.EntityDuck;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.*;

public class HeldEntityFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {
    public HeldEntityFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, E entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        Pony.of(entity).flatMap(Pony::getEntityInArms).ifPresent(passenger -> {
            float leanAmount = ((LivingEntityDuck)entity).getLeaningPitch();

            matrices.push();
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180 - leanAmount * 90));

            Vec3d carryPosition = getCarryPosition(Living.living(entity), passenger)
                    .rotateX(-leanAmount * MathHelper.PI / 4F)
                    .add(new Vec3d(0, -0.5F, 0).multiply(leanAmount));

            matrices.translate(carryPosition.x, carryPosition.y, carryPosition.z);
            if (!(passenger instanceof Pony)) {
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
            }

            renderCarriedEntity(passenger.asEntity(), matrices, vertexConsumers, light, tickDelta);
            matrices.pop();
        });
    }

    @Override
    public void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, E entity, ModelPart arm, Arm side) {

    }

    @Override
    public boolean beforeRenderArms(ArmRenderer sender, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, E entity, int light) {
        return Pony.of(entity).flatMap(Pony::getEntityInArms).filter(passenger -> {
            float swingProgress = entity.getHandSwingProgress(MinecraftClient.getInstance().getTickDelta());
            float f = -0.4f * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
            float g = 0.2f * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2));
            float h = -0.2f * MathHelper.sin(swingProgress * (float)Math.PI);
            matrices.push();
            matrices.translate(f, g, h);
            matrices.translate(0, -1.3F, -1.3F);
            matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(13));
            if (!(passenger instanceof Pony)) {
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
            }

            renderCarriedEntity(passenger.asEntity(), matrices, vertexConsumers, light, tickDelta);
            matrices.pop();

            float equipProgress = 1 - sender.getEquipProgress(tickDelta);

            matrices.push();
            sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, Arm.LEFT);
            matrices.pop();
            matrices.push();
            sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, Arm.RIGHT);
            matrices.pop();
            return true;
        }).isPresent();
    }

    private void renderCarriedEntity(LivingEntity p, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta) {
        Entity vehicle = p.getVehicle();
        ((EntityDuck)p).setVehicle(null);

        p.prevBodyYaw = 0;
        p.bodyYaw = 0;
        p.headYaw = 0;
        p.prevHeadYaw = 0;
        p.prevYaw = 0;
        p.setYaw(0);
        p.setBodyYaw(0);
        @SuppressWarnings("unchecked")
        EntityRenderer<LivingEntity> renderer = (EntityRenderer<LivingEntity>)MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(p);
        renderer.render(p, 0, tickDelta, matrices, vertexConsumers, light);

        ((EntityDuck)p).setVehicle(vehicle);
    }

    protected Vec3d getCarryPosition(Living<E> entity, Living<?> passenger) {
        float passengerHeight = MineLPDelegate.getInstance().getPonyHeight(passenger.asEntity()) / 2F;
        float carrierHeight = entity.asEntity().getHeight() / 5F;

        if (entity instanceof Pony pony && MineLPDelegate.getInstance().getPlayerPonyRace(pony.asEntity()).isEquine() && pony.getPhysics().isFlying()) {
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
