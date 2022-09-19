package com.minelittlepony.unicopia.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.server.network.*;

@Mixin(ServerPlayNetworkHandler.class)
abstract class MixinReachDistanceFix {
    @Redirect(
            method = {
                    "onPlayerInteractBlock",
                    "onPlayerInteractEntity"
            },
            at = @At(
                value = "FIELD",
                target = "net/minecraft/server/network/ServerPlayNetworkHandler.MAX_BREAK_SQUARED_DISTANCE:D",
                opcode = Opcodes.GETSTATIC
            ),
            require = 0
    )
    private double bgetMaxBreakSquaredDistance() {
        double reach = 6 + Pony.of(((ServerPlayNetworkHandler)(Object)this).getPlayer()).getExtendedReach();
        return reach * reach;
    }
}
