package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.minelittlepony.unicopia.ducks.Farmland;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FarmlandBlock.class)
abstract class MixinFarmlandBlock extends Block {
    MixinFarmlandBlock() { super(null); }
    @Inject(
            method = "setToDirt(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
            at = @At("HEAD"),
            cancellable = true)
    public static void setToDirt(BlockState state, World world, BlockPos pos) {
        if (state.getBlock() instanceof Farmland) {
            BlockState dirtState = ((Farmland)state.getBlock()).getDirtState(state, world, pos);
            world.setBlockState(pos, pushEntitiesUpBeforeBlockChange(state, dirtState, world, pos));
        }
    }
}
