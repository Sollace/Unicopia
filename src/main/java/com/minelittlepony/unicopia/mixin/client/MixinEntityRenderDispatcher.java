package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;
import com.minelittlepony.unicopia.client.render.entity.HitboxController;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityHitboxAndView;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher implements SpellEffectsRenderDispatcher.RenderDispatcherAccessor {
    @WrapMethod(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V")
    private <E extends Entity, S extends EntityRenderState> void wrapRender(
            E entity,
            double x,
            double y,
            double z,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light,
            Operation<Void> operation) {
        WorldRenderDelegate.INSTANCE.handleEntityRender((entity1, x1, y1, z1, vertices1, light1, renderer1) -> {
            operation.call(entity1, x1, y1, z1, tickDelta, matrices, vertices1, light1, renderer1);
        }, entity, x, y, z, tickDelta, matrices, vertices, light);
    }

    @Accessor("renderShadows")
    @Override
    public abstract boolean shouldRenderShadows();
}

@Mixin(EntityRenderer.class)
abstract class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "createHitbox", at = @At("HEAD"), cancellable = true)
    private void onCreateHitbox(T entity, float tickProgress, boolean green, CallbackInfoReturnable<EntityHitboxAndView> info) {
        if (!HitboxController.of(Untyped.cast(this)).shouldRenderHitbox(entity)) {
            info.setReturnValue(null);
        }
    }
}