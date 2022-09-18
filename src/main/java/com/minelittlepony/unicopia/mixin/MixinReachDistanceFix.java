package com.minelittlepony.unicopia.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.server.network.*;

@Mixin(value = {
        ServerPlayerInteractionManager.class,
        ServerPlayNetworkHandler.class
})
abstract class MixinReachDistanceFix {

    @Accessor
    public abstract ServerPlayerEntity getPlayer();

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
        double reach = 6 + Pony.of(getPlayer()).getExtendedReach();
        return reach * reach;
    }
/*
    @ModifyConstant(
            method = {
                    "processBlockBreakingAction",
                    "onPlayerInteractBlock",
                    "onPlayerInteractEntity"
            },
            constant = @Constant(doubleValue = 36D)
    )
    private double modifyMaxBreakSquaredDistance(double initial) {
        double reach = 6 + Pony.of(getPlayer()).getExtendedReach();
        return reach * reach;
    }*/
}
