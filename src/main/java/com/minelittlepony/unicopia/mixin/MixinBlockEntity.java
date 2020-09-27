package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public interface MixinBlockEntity {
    @Accessor("cachedState")
    void setCachedState(BlockState state);
}
