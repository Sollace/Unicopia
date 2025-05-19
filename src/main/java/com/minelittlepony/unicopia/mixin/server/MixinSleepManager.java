package com.minelittlepony.unicopia.mixin.server;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.server.world.NocturnalSleepManager;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;

@Mixin(SleepManager.class)
abstract class MixinSleepManager {
    @ModifyVariable(method = "update(Ljava/util/List;)Z", at = @At("HEAD"))
    public List<ServerPlayerEntity> modifyPlayers(List<ServerPlayerEntity> players) {
        return players.size() <= 0 ? players : ((NocturnalSleepManager.Source)players.get(0).getWorld()).getNocturnalSleepManager().filterPlayers(players);
    }
}
