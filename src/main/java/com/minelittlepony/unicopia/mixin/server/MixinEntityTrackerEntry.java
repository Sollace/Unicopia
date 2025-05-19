package com.minelittlepony.unicopia.mixin.server;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.network.track.Trackable;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(EntityTrackerEntry.class)
abstract class MixinEntityTrackerEntry {
    @Shadow
    private @Final Entity entity;
    @Shadow
    abstract void sendSyncPacket(Packet<?> packet);

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void unicopia_onTick(CallbackInfo info) {
        Trackable.of(entity).getDataTrackers().tick(this::sendSyncPacket);
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "sendPackets", at = @At("RETURN"), require = 0)
    private void unicopia_onSendPackets(ServerPlayerEntity player, @Coerce Object sender, CallbackInfo info) {
        // NEO: Consumer<Packet<ClientPlayPacketListener>> replaced with PacketAndPayloadAcceptor<ClientPlayPacketListener>
        if (sender instanceof Consumer s) {
            Trackable.of(entity).getDataTrackers().sendInitial(player, s);
        }
    }
}
