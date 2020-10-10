package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.FallingBlock;
import net.minecraft.entity.FallingBlockEntity;

@Mixin(FallingBlock.class)
public interface MixinFallingBlock {
    @Invoker
    void invokeConfigureFallingBlockEntity(FallingBlockEntity entity);
}
