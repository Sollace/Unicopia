package com.minelittlepony.unicopia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.minelittlepony.unicopia.entity.duck.ServerPlayerEntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
abstract class MixinServerPlayNetworkHandler implements ServerPlayPacketListener {
    @Shadow public ServerPlayerEntity player;
    @Shadow private boolean floating;
    @Shadow private int floatingTicks;

    @Inject(method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", at = @At("HEAD"))
    private void beforePlayerMove(PlayerMoveC2SPacket packet, CallbackInfo info, @Share("unicopia_prevMotionChecks") LocalBooleanRef prevMotionChecks) {
        NetworkThreadUtils.forceMainThread(packet, this, player.getServerWorld());
        prevMotionChecks.set(player.isInTeleportationState());
        ((ServerPlayerEntityDuck)player).setPreventMotionChecks(player.isInTeleportationState()
                || Pony.of(player).getPhysics().isFlyingSurvival
                || Pony.of(player).getTransportation().hasVehicle());
    }

    @Inject(method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", at = @At("RETURN"))
    private void afterPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo info, @Share("unicopia_prevMotionChecks") LocalBooleanRef prevMotionChecks) {
        ((ServerPlayerEntityDuck)player).setPreventMotionChecks(prevMotionChecks.get());
        unicopia_checkFlightConditions();
        Pony.of(player).getTransportation().onPlayerMoved(packet);
    }

    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", target = "net/minecraft/server/network/ServerPlayerEntity.playerTick()V"))
    private void onPlayerTick(ServerPlayerEntity player, Operation<Void> operation) {
        unicopia_checkFlightConditions();
        Pony.of(player).getTransportation().onPlayerTick(() -> operation.call(player));
    }

    @Unique
    private void unicopia_checkFlightConditions() {
        if (Pony.of(player).getPhysics().isFlyingSurvival) {
            floating = false;
            floatingTicks = 0;
            player.fallDistance = 0;
        }
    }
}
