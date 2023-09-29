package com.minelittlepony.unicopia.client;

import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

public class FirstPersonRendererOverrides {
    public static final FirstPersonRendererOverrides INSTANCE = new FirstPersonRendererOverrides();

    public Optional<HeldItemRenderer.HandRenderType> getHandRenderType(PlayerEntity player) {
        return Pony.of(player).getEntityInArms().map(e -> HeldItemRenderer.HandRenderType.RENDER_BOTH_HANDS);
    }

    public boolean beforeRenderHands(ArmRenderer sender, float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light) {
        var root = AccessoryFeatureRenderer.FeatureRoot.of(player);
        boolean cancelled = root != null && root.getAccessories().beforeRenderArms(sender, tickDelta, matrices, vertexConsumers, player, light);

        if (cancelled) {
            return true;
        }

        if (Pony.of(player).getAnimation().renderBothArms()) {
            float swingProgress = player.getHandSwingProgress(MinecraftClient.getInstance().getTickDelta());

            Hand hand = MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);

            if (player.getMainHandStack().isEmpty()) {
                matrices.push();
                sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, 1 - sender.getEquipProgress(Hand.MAIN_HAND, tickDelta), hand == Hand.MAIN_HAND ? swingProgress : 0, player.getMainArm());
                matrices.pop();
            }

            if (player.getOffHandStack().isEmpty()) {
                matrices.push();
                sender.invokeRenderArmHoldingItem(matrices, vertexConsumers, light, 1 - sender.getEquipProgress(Hand.OFF_HAND, tickDelta), hand == Hand.OFF_HAND ? swingProgress : 0, player.getMainArm().getOpposite());
                matrices.pop();
            }
        }
        return false;
    }

    public interface ArmRenderer {
        void invokeRenderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

        float getEquipProgress(Hand hand, float tickDelta);
    }
}
