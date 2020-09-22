package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
abstract class MixinServerPlayerEntity extends PlayerEntity implements ScreenHandlerListener, PonyContainer<Pony> {
    MixinServerPlayerEntity() {super(null, null, 0, null);}

    @SuppressWarnings("unchecked")
    @Inject(method = "copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", at = @At("HEAD"))
    private void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
        get().copyFrom(((PonyContainer<Pony>)oldPlayer).get());
    }
}
