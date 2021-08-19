package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.unicopia.entity.behaviour.FallingBlockBehaviour;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockEntity.class)
abstract class MixinBlockEntity implements FallingBlockBehaviour.Positioned {
    @Shadow
    @Mutable
    private @Final BlockPos pos;

    @Override
    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
}
