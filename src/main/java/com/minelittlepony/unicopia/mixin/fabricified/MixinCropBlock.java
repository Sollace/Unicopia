package com.minelittlepony.unicopia.mixin.fabricified;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.minelittlepony.unicopia.block.CropBlockAccessor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(CropBlock.class)
public class MixinCropBlock implements CropBlockAccessor {
    @Override
    public float unicopia_getAvailableMoisture(BlockState state, BlockView world, BlockPos pos) {
        return getAvailableMoisture(state.getBlock(), world, pos);
    }

    @Shadow
    private static float getAvailableMoisture(Block block, BlockView world, BlockPos pos) {
        return 0;
    }
}
