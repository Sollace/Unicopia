package com.minelittlepony.unicopia.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.HeldItemRenderer.HandRenderType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

@Mixin(HeldItemRenderer.class)
abstract class MixinHeldItemRenderer implements FirstPersonRendererOverrides.ArmRenderer {
    @Shadow
    private float equipProgressMainHand;
    @Shadow
    private float prevEquipProgressMainHand;

    @Shadow
    private float equipProgressOffHand;
    @Shadow
    private float prevEquipProgressOffHand;

    @Override
    @Invoker("renderArmHoldingItem")
    public abstract void invokeRenderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

    @Override
    public float getEquipProgress(Hand hand, float tickDelta) {
        return MathHelper.lerp(tickDelta,
                hand == Hand.MAIN_HAND ? prevEquipProgressMainHand : prevEquipProgressOffHand,
                hand == Hand.MAIN_HAND ? equipProgressMainHand : equipProgressOffHand
        );
    }

    @Inject(
        method = "getHandRenderType",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void onGetHandRenderType(ClientPlayerEntity player, CallbackInfoReturnable<HandRenderType> info) {
        FirstPersonRendererOverrides.INSTANCE.getHandRenderType(player).ifPresent(info::setReturnValue);
    }

    @Inject(
        method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
        at = @At(
            value = "FIELD",
            target = "net/minecraft/client/render/item/HeldItemRenderer$HandRenderType.renderMainHand:Z",
            opcode = Opcodes.GETFIELD,
            shift = Shift.BEFORE
        ),
        cancellable = true)
    private void onRenderItem(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo info) {
        if (FirstPersonRendererOverrides.INSTANCE.beforeRenderHands(this, tickDelta, matrices, vertexConsumers, player, light)) {
            info.cancel();
        }
    }
}
