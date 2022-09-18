package com.minelittlepony.unicopia.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.network.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(value = {
        ServerPlayerInteractionManager.class,
        ServerPlayNetworkHandler.class
})
abstract class MixinReachDistanceFix {
    @Redirect(
            target = {
                    @Desc(owner = ServerPlayerInteractionManager.class, value = "processBlockBreakingAction", args = {
                            BlockPos.class, PlayerActionC2SPacket.Action.class, Direction.class, int.class, int.class
                    }),
                    @Desc(owner = ServerPlayNetworkHandler.class, value = "onPlayerInteractBlock", args = { PlayerInteractBlockC2SPacket.class }),
                    @Desc(owner = ServerPlayNetworkHandler.class, value = "onPlayerInteractEntity", args = { PlayerInteractEntityC2SPacket.class })
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
