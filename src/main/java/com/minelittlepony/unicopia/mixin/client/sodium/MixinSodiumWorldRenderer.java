package com.minelittlepony.unicopia.mixin.client.sodium;

import java.util.SortedSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.BlockBreakingInfo;

@Pseudo
@Mixin(
        targets = { "me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer" },
        remap = false
)
abstract class MixinSodiumWorldRenderer {
    @ModifyVariable(
            method = { "renderTileEntities", "renderBlockEntities" },
            at = @At("HEAD"),
            require = 0
    )
    public Long2ObjectMap<SortedSet<BlockBreakingInfo>> modifyDestruction(Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions) {
        ClientBlockDestructionManager destructions = ((ClientBlockDestructionManager.Source)MinecraftClient.getInstance().worldRenderer).getDestructionManager();
        return destructions.getCombinedDestructions(blockBreakingProgressions);
    }
}
