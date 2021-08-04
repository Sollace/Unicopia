package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockEntity.class)
public interface MixinBlockEntity {
    @Accessor("pos")
    void setPos(BlockPos pos);
}
