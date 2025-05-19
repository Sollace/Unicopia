package com.minelittlepony.unicopia.mixin;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.unicopia.block.BlockEntityTypeSupportHelper;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
abstract class MixinBlockEntityType implements BlockEntityTypeSupportHelper {
    @Shadow
    @Mutable
    private @Final Set<Block> blocks;

    @Override
    public BlockEntityTypeSupportHelper addSupportedBlocks(Block...blocks) {
        this.blocks = new HashSet<>(this.blocks);
        this.blocks.addAll(Set.of(blocks));
        return this;
    }
}
