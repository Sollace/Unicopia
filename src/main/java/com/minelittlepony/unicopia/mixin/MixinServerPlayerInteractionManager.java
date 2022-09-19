package com.minelittlepony.unicopia.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.server.network.*;

@Mixin(ServerPlayerInteractionManager.class)
abstract class MixinServerPlayerInteractionManager {
    @Accessor
    public abstract ServerPlayerEntity getPlayer();

    @Redirect(
            method = "processBlockBreakingAction",
            at = @At(
                value = "FIELD",
                target = "net/minecraft/server/network/ServerPlayNetworkHandler.MAX_BREAK_SQUARED_DISTANCE:D",
                opcode = Opcodes.GETSTATIC
            ),
            require = 0
    )
    private double bgetMaxBreakSquaredDistance() {
        double reach = 6 + Pony.of(getPlayer()).getExtendedReach();
        return reach * reach;
    }
}
