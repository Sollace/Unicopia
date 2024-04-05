package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
abstract class MixinServerPlayNetworkHandler implements ServerPlayPacketListener, PlayerAssociatedNetworkHandler, TickablePacketListener {
    @Shadow public ServerPlayerEntity player;

    @ModifyVariable(method = "onPlayerMove", at = @At("STORE"), ordinal = 0)
    private boolean flipLandingFlag(boolean value) {
        if (Pony.of(this.player).getPhysics().isGravityNegative()) {
            return !value;
        }
        return value;
    }
}
