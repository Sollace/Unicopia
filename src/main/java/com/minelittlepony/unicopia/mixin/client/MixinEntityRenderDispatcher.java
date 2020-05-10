package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.spell.DisguiseSpell;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher {

    private static final String RENDER = "render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V";

    @Inject(method = RENDER, at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void beforeRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {

        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        Pony pony = Pony.of((PlayerEntity)entity);

        WorldRenderDelegate.INSTANCE.beforeEntityRender(pony, matrices, x, y, z);

        DisguiseSpell effect = pony.getEffect(DisguiseSpell.class, true);

        if (effect == null || effect.isDead()) {
            return;
        }

        effect.update(pony, false);

        Entity e = effect.getDisguise();

        if (e != null) {
            info.cancel();
            if (DisguiseSpell.isAttachedEntity(e) && (x != 0 || y != 0 || z != 0)) {
                Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

                x = MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()) - cam.x;
                y = MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()) - cam.y;
                z = MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()) - cam.z;

            }

            ((EntityRenderDispatcher)(Object)this).render(e, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light);
        }
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private <E extends Entity> void afterRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {

        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        WorldRenderDelegate.INSTANCE.afterEntityRender(Pony.of((PlayerEntity)entity), matrices);
    }
}
