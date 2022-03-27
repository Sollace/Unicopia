package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.AnimalPoser;
import com.minelittlepony.unicopia.client.render.PlayerPoser;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntityRenderer.class)
abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
    MixinLivingEntityRenderer() { super(null); }

    @Inject(method = "render",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V",
                shift = Shift.AFTER))
    private void onRender(
            T entity,
            float yaw, float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light,
            CallbackInfo into) {
        if (entity instanceof PlayerEntity player) {
            PlayerPoser.INSTANCE.applyPosing(matrices, player, (BipedEntityModel<?>)getModel());
        }
        if (entity instanceof MobEntity mob) {
            AnimalPoser.INSTANCE.applyPosing(matrices, mob, getModel());
        }
    }
}
