package com.minelittlepony.unicopia.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.track.Trackable;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler {
    protected MixinClientPlayNetworkHandler() { super(null, null, null); }

    @Shadow private ClientWorld world;

    @Nullable
    private ClientPlayerEntity oldPlayer;

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

    @Inject(method = "onPlayerRespawn", at = @At("HEAD"))
    public void beforeOnPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo info) {
        oldPlayer = client.player;
    }

    @Inject(method = "onPlayerRespawn", at = @At("RETURN"))
    public void afterOnPlayerRespawn(PlayerRespawnS2CPacket packet, CallbackInfo info) {
        if (oldPlayer != null && oldPlayer != client.player) {
            Trackable.of(oldPlayer).getDataTrackers().copyTo(Trackable.of(client.player).getDataTrackers());
            Pony.of(client.player).copyFrom(Pony.of(oldPlayer), true);
        }
    }
}
