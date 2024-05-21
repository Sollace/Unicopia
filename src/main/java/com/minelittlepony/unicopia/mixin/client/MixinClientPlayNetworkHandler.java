package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
abstract class MixinClientPlayNetworkHandler {
    @Shadow private ClientWorld world;

    @Inject(method = "onEntityStatus", at = @At("TAIL"))
    private void onOnEntityStatus(EntityStatusS2CPacket packet, CallbackInfo info) {
        Living<?> living = Living.living(packet.getEntity(world));
        if (living != null) {
            living.getSpellSlot()
                .get(SpellPredicate.IS_DISGUISE)
                .map(Disguise::getDisguise)
                .map(EntityAppearance::getAppearance)
                .ifPresent(appearance -> {
                    appearance.handleStatus(packet.getStatus());
                });
        }
    }
}
