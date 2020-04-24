package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.spell.DisguiseSpell;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(EntityRenderDispatcher.class)
abstract class MixinEntityRenderDispatcher {

    private static final String RENDER = "render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V";

    @Inject(method = RENDER, at = @At("HEAD"))
    private <E extends Entity> void beforeRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        matrices.push();

        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        Pony pony = Pony.of((PlayerEntity)entity);

        if (pony.getGravity().getGravitationConstant() < 0) {
            matrices.translate(0, entity.getDimensions(entity.getPose()).height, 0);
            matrices.scale(1, -1, 1);
            entity.prevPitch *= -1;
            entity.pitch *= -1;
        }

        DisguiseSpell effect = pony.getEffect(DisguiseSpell.class, false);

        if (effect == null || effect.isDead()) {
            return;
        }

        Entity e = effect.getDisguise();

        if (e != null) {
            info.cancel();
            ((EntityRenderDispatcher)(Object)this).render(entity, x, y, z, yaw, tickDelta, matrices, vertexConsumers, light);
        }
    }

    @Inject(method = RENDER, at = @At("HEAD"))
    private <E extends Entity> void afterRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        matrices.pop();

        if (!(entity instanceof PlayerEntity)) {
            return;
        }

        Pony pony = Pony.of((PlayerEntity)entity);

        if (pony.getGravity().getGravitationConstant() < 0) {
            entity.prevPitch *= -1;
            entity.pitch *= -1;
        }
    }
}
