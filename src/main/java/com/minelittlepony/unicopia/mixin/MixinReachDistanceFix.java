package com.minelittlepony.unicopia.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.server.network.*;

@Mixin(value = {
        ServerPlayerInteractionManager.class,
        ServerPlayNetworkHandler.class
})
abstract class MixinReachDistanceFix {
    @Redirect(
            method = {
                    "processBlockBreakingAction",
                    "onPlayerInteractBlock",
                    "onPlayerInteractEntity"
            },
            at = @At(
                value = "FIELD",
                target = "net/minecraft/server/network/ServerPlayNetworkHandler.MAX_BREAK_SQUARED_DISTANCE:D",
                opcode = Opcodes.GETSTATIC
            )
    )
    private double getMaxBreakSquaredDistance() {
        Object o = this;
        ServerPlayerEntity player = o instanceof ServerPlayNetworkHandler s ? s.getPlayer() : ((MixinServerPlayerInteractionManager)o).getPlayer();
        double reach = 6 + Pony.of(player).getExtendedReach();
        return reach * reach;
    }
}
