package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.PlayerPoser;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer.FeatureRoot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

@Mixin(PlayerEntityRenderer.class)
abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityRenderState, PlayerEntityModel> {
    MixinPlayerEntityRenderer() { super(null, null, 0); }

    @SuppressWarnings("unchecked")
    @Inject(method = "renderArm", at = @At("RETURN"))
    private void onRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Identifier skinTexture, ModelPart arm, boolean sleeveVisible, CallbackInfo info) {
        Arm a = this.getModel().leftArm == arm ? Arm.LEFT : Arm.RIGHT;
        PlayerEntityRenderState state = getAndUpdateRenderState(MinecraftClient.getInstance().player, MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false));
        ((FeatureRoot<PlayerEntityRenderState, ?>)this).getAccessories().renderArm(matrices, vertexConsumers, light, state, arm, a);
    }

    @Inject(method = "renderArm",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/client/model/ModelPart.render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
    private void onPoseArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Identifier skinTexture, ModelPart arm, boolean sleeveVisible, CallbackInfo info) {
        PlayerPoser.INSTANCE.applyPosing(matrices, getAndUpdateRenderState(MinecraftClient.getInstance().player, MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false)), getModel(), arm == getModel().leftArm ? PlayerPoser.Context.FIRST_PERSON_LEFT : PlayerPoser.Context.FIRST_PERSON_RIGHT);
    }
}
