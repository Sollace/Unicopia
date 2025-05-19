package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.PlayerPoser;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer.FeatureRoot;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

@Mixin(PlayerEntityRenderer.class)
abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    MixinPlayerEntityRenderer() { super(null, null, 0); }

    @SuppressWarnings("unchecked")
    @Inject(method = "renderArm", at = @At("RETURN"))
    private void onRenderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo info) {
        Arm a = this.getModel().leftArm == arm ? Arm.LEFT : Arm.RIGHT;
        ((FeatureRoot<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>)this).getAccessories().renderArm(matrices, vertexConsumers, light, player, arm, a);
    }

    @Inject(method = "renderArm",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/entity/model/PlayerEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
                shift = Shift.AFTER))
    private void onPoseArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, ModelPart arm, ModelPart sleeve, CallbackInfo info) {
        PlayerPoser.INSTANCE.applyPosing(matrices, player, getModel(), arm == getModel().leftArm ? PlayerPoser.Context.FIRST_PERSON_LEFT : PlayerPoser.Context.FIRST_PERSON_RIGHT);
    }
}
