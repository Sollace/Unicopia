package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;
import com.minelittlepony.unicopia.entity.Equine;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher {

    private static final String RENDER = "render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V";

    @Inject(method = RENDER, at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void beforeRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        if (WorldRenderDelegate.INSTANCE.beforeEntityRender((EntityRenderDispatcher)(Object)this, entity, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light)) {
            info.cancel();
        }
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private <E extends Entity> void afterRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        Equine.of(entity).ifPresent(eq -> WorldRenderDelegate.INSTANCE.afterEntityRender(eq, matrices));
    }
}
