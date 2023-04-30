package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.duck.ServerPlayerEntityDuck;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
abstract class MixinServerPlayerEntity extends PlayerEntity implements ScreenHandlerListener, Equine.Container<Pony>, ServerPlayerEntityDuck {
    MixinServerPlayerEntity() {super(null, null, 0, null);}

    @Override
    @Accessor("inTeleportationState")
    public abstract void setPreventMotionChecks(boolean enabled);

    @SuppressWarnings("unchecked")
    @Inject(method = "copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V", at = @At("HEAD"))
    private void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo info) {
        get().copyFrom(((Equine.Container<Pony>)oldPlayer).get(), alive);
    }

    @Redirect(method = "trySleep", at = @At(
            value = "INVOKE",
            target = "net/minecraft/world/World.isDay()Z"
    ))
    private boolean redirectIsDay(World world) {
        return get().isDaytime();
    }
}
