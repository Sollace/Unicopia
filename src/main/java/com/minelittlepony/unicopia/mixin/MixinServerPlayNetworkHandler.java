package com.minelittlepony.unicopia.mixin;

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
    @Shadow private boolean floating;
    @Shadow private int floatingTicks;

    private boolean flyingSurvival;
    private boolean prevMotionChecks;

    @Inject(method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", at = @At("HEAD"))
    private void beforePlayerMove(PlayerMoveC2SPacket packet, CallbackInfo info) {
        ServerPlayerEntity player = ((ServerPlayNetworkHandler)(Object)this).player;
        NetworkThreadUtils.forceMainThread(packet, this, player.getServerWorld());
        flyingSurvival = Pony.of(player).getPhysics().isFlyingSurvival;

        if (flyingSurvival) {
            setPreventMotionChecks(true);
        }
    }

    @Inject(method = "onPlayerMove(Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket;)V", at = @At("RETURN"))
    private void afterPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo info) {
        if (flyingSurvival) {
            setPreventMotionChecks(prevMotionChecks);
        }
    }

    private void setPreventMotionChecks(boolean motionChecks) {
        ServerPlayerEntity player = ((ServerPlayNetworkHandler)(Object)this).player;
        prevMotionChecks = player.isInTeleportationState();
        ((ServerPlayerEntityDuck)player).setPreventMotionChecks(motionChecks);
        player.fallDistance = 0;
        floating = false;
        floatingTicks = 0;
    }
}
