package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
abstract class MixinPlayerManager {
    @Inject(method = "remove", at = @At("HEAD"))
    public void onRemove(ServerPlayerEntity player, CallbackInfo info) {
        player.getPassengerList().stream()
            .flatMap(passenger -> Pony.of(passenger).stream())
            .forEach(passenger -> passenger.setCarrier((Entity)null));
        player.removeAllPassengers();

        @Nullable
        Entity vehicle = null;
        if (player.hasVehicle() && (vehicle = player.getRootVehicle()).hasPlayerRider()) {
            if (vehicle.streamPassengersAndSelf().anyMatch(e -> {
                return e != player && e instanceof PlayerEntity;
            })) {
                player.stopRiding();
                Pony.of(player).setCarrier((Entity)null);
            }
        }
    }

    @Inject(method = "respawnPlayer", at = @At("HEAD"))
    private void beforeRespawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info) {
        InteractionManager.getInstance().setEquineContext(EquineContext.of(player));
    }

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void afterRespawnPlayer(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info) {
        InteractionManager.getInstance().setEquineContext(EquineContext.ABSENT);
    }
}
