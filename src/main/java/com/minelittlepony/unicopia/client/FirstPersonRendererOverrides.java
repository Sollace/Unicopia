package com.minelittlepony.unicopia.client;

import java.util.Optional;

import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;

public class FirstPersonRendererOverrides {
    public static final FirstPersonRendererOverrides INSTANCE = new FirstPersonRendererOverrides();

    public Optional<HeldItemRenderer.HandRenderType> getHandRenderType(PlayerEntity player) {
        return Pony.of(player).getEntityInArms().map(e -> HeldItemRenderer.HandRenderType.RENDER_BOTH_HANDS);
    }

    public boolean beforeRenderHands(ArmRenderer sender, float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light) {
        var root = AccessoryFeatureRenderer.FeatureRoot.of(player);
        return root != null && root.getAccessories().beforeRenderArms(sender, tickDelta, matrices, vertexConsumers, player, light);
    }

    public interface ArmRenderer {
        void invokeRenderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

        float getEquipProgress(float tickDelta);
    }
}
