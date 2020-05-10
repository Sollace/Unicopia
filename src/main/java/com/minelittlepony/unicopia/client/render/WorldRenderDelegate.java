package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class WorldRenderDelegate {

    public static final WorldRenderDelegate INSTANCE = new WorldRenderDelegate();

    public void beforeEntityRender(Pony pony, MatrixStack matrices, double x, double y, double z) {
        if (pony.getPhysics().isGravityNegative()) {
            matrices.push();

            Entity entity = pony.getOwner();

            matrices.translate(x, y, z);
            matrices.translate(0, entity.getHeight(), 0);
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180));
            matrices.translate(-x, -y, -z);

            flipAngles(entity);
        }
    }

    public void afterEntityRender(Pony pony, MatrixStack matrices) {
        if (pony.getPhysics().isGravityNegative()) {
            matrices.pop();

            flipAngles(pony.getOwner());

        }
    }

    private void flipAngles(Entity entity) {
        if (MinecraftClient.getInstance().options.perspective > 0) {
            entity.prevYaw *= -1;
            entity.yaw *= -1;

            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;

                living.bodyYaw = -living.bodyYaw;
                living.prevBodyYaw = -living.prevBodyYaw;
                living.headYaw = -living.headYaw;
                living.prevHeadYaw = -living.prevHeadYaw;
            }
        }
        entity.prevPitch *= -1;
        entity.pitch *= -1;
    }

    public void applyWorldTransform(MatrixStack matrices, float tickDelta) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && MinecraftClient.getInstance().cameraEntity == player) {
            float roll = Pony.of(player).getCamera().calculateRoll();

            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(roll));
        }
    }
}
