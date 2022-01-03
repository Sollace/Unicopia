package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

public class PlayerPoser {
    public static final PlayerPoser INSTANCE = new PlayerPoser();

    public void applyPosing(PlayerEntity entity, BipedEntityModel<?> model) {
        Pony pony = Pony.of(entity);
        float progress = pony.getAnimationProgress(MinecraftClient.getInstance().getTickDelta());

        switch (pony.getAnimation()) {
            case WOLOLO: {
                float roll = MathHelper.sin(entity.age / 10F);
                float yaw = MathHelper.cos(entity.age / 10F);

                model.leftArm.pitch += -1;
                model.rightArm.pitch += -1;

                model.leftArm.roll = -roll;
                model.rightArm.roll = roll;

                model.leftArm.yaw = yaw;
                model.rightArm.yaw = yaw;
                break;
            }
            case ARMS_FORWARD: {
                float roll = (progress + 1) / 2F;

                float pitch = 1.5F * roll;
                float yaw = 0.5F * roll;

                model.leftArm.pitch -= pitch;
                model.rightArm.pitch -= pitch;

                model.leftArm.roll = 0;
                model.rightArm.roll = 0;

                model.leftArm.yaw = yaw;
                model.rightArm.yaw = -yaw;
                break;
            }
            case ARMS_UP: {
                float roll = (progress + 1) / 2F;

                float pitch = 3F * roll;
                float yaw = 0.5F * roll;

                model.leftArm.pitch -= pitch;
                model.rightArm.pitch -= pitch;

                model.leftArm.roll = 0;
                model.rightArm.roll = 0;

                model.leftArm.yaw = yaw;
                model.rightArm.yaw = -yaw;
                break;
            }
            case KICK: {
                float roll = (progress + 1) / 2F;

                model.rightArm.pitch += roll / 5F;
                model.leftArm.roll -= roll / 5F;
                model.rightArm.roll += roll / 5F;

                if (entity.getMainArm() == Arm.LEFT) {
                    model.rightLeg.pitch = -roll * 1.5F;
                    model.rightLeg.roll = roll / 10F;
                } else {
                    model.leftLeg.pitch = -roll * 1.5F;
                    model.leftLeg.roll = -roll / 10F;
                }
            }
            default:
        }
    }

    public enum Animation {
        NONE,
        WOLOLO,
        ARMS_FORWARD,
        ARMS_UP,
        KICK
    }
}
