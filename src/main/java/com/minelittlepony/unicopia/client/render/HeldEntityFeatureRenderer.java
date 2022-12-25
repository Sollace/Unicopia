package com.minelittlepony.unicopia.client.render;

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
            Living<E> l = Living.living(entity);
            Vec3d carryPosition = getCarryPosition(l, passenger);

            LivingEntity p = passenger.asEntity();

            @SuppressWarnings("unchecked")
            EntityRenderer<LivingEntity> renderer = (EntityRenderer<LivingEntity>)MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(p);

            float f = tickDelta;

            matrices.push();

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));

            float leanAmount = ((LivingEntityDuck)entity).getLeaningPitch();

            //matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(leanAmount * 0));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-leanAmount * 90));

            carryPosition = carryPosition.rotateX(-leanAmount * MathHelper.PI / 4F)
                    .add(new Vec3d(0, -0.5F, 0).multiply(leanAmount));

            matrices.translate(carryPosition.x, carryPosition.y, carryPosition.z);
            if (!(passenger instanceof Pony)) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            }

            float oldYaw = entity.bodyYaw;
            float prevOldYaw = entity.prevBodyYaw;

            entity.bodyYaw = 0;
            entity.prevBodyYaw = 0;

            Entity vehicle = p.getVehicle();
            ((EntityDuck)p).setVehicle(null);

            p.prevBodyYaw = 0;
            p.bodyYaw = 0;
            p.headYaw = 0;
            p.prevHeadYaw = 0;
            p.prevYaw = 0;
            p.setYaw(0);
            p.setBodyYaw(0);
            renderer.render(p, 0, f, matrices, vertexConsumers, light);

            entity.bodyYaw = oldYaw;
            entity.prevBodyYaw = prevOldYaw;

            ((EntityDuck)p).setVehicle(vehicle);

            matrices.pop();
        });
    }

    @Override
    public void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, E entity, ModelPart arm, Arm side) {
        render(matrices, vertexConsumers, light, entity, 0, 0, 0, 0, 0, 0);
    }

    protected Vec3d getCarryPosition(Living<E> entity, Living<?> passenger) {
        float passengerHeight = passenger.asEntity().getHeight() / 2F;
        float carrierHeight = entity.asEntity().getHeight() / 5F;

        if (entity instanceof Pony pony && !MineLPDelegate.getInstance().getPlayerPonyRace(pony.asEntity()).isDefault() && pony.getPhysics().isFlying()) {
            return new Vec3d(0,
                    -passenger.asEntity().getHeight() - passengerHeight,
                    0
            );
        }

        return new Vec3d(0,
                -passengerHeight - carrierHeight,
                entity.asEntity().getWidth()
        );
    }
}
