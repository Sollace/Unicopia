package com.minelittlepony.unicopia.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.duck.ServerPlayerEntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.EntityTrackingListener;

@Mixin(ServerPlayNetworkHandler.class)
abstract class MixinServerPlayNetworkHandler implements EntityTrackingListener, ServerPlayPacketListener {
    @Shadow public ServerPlayerEntity player;
    @Shadow private boolean floating;
    @Shadow private int floatingTicks;

    private boolean prevMotionChecks;

    @Inject(method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", at = @At("HEAD"))
    private void beforePlayerMove(PlayerMoveC2SPacket packet, CallbackInfo info) {
        NetworkThreadUtils.forceMainThread(packet, this, player.getServerWorld());
        prevMotionChecks = player.isInTeleportationState();
        if (Pony.of(player).getPhysics().isFlyingSurvival) {
            ((ServerPlayerEntityDuck)player).setPreventMotionChecks(true);
        }
    }

    @Inject(method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", at = @At("RETURN"))
    private void afterPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo info) {
        ((ServerPlayerEntityDuck)player).setPreventMotionChecks(prevMotionChecks);
        if (Pony.of(player).getPhysics().isFlyingSurvival) {
            floating = false;
            floatingTicks = 0;
            player.fallDistance = 0;
        }
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void beforePlayerTick(CallbackInfo info) {
        if (Pony.of(player).getPhysics().isFlyingSurvival) {
            floating = false;
            floatingTicks = 0;
            player.fallDistance = 0;
        }
    }
}
