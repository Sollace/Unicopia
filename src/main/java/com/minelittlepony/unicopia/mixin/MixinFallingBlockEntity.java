package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;

@Mixin(FallingBlockEntity.class)
public interface MixinFallingBlockEntity {
    @Invoker("<init>")
    static FallingBlockEntity createInstance(World world, double x, double y, double z, BlockState state) {
        return null;
    }
}
