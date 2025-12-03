package com.minelittlepony.unicopia.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;

public interface VirtualBlockRenderView extends BlockRenderView {

    BlockRenderView proxy();

    @Override
    default BlockEntity getBlockEntity(BlockPos pos) {
        return proxy().getBlockEntity(pos);
    }

    @Override
    default BlockState getBlockState(BlockPos pos) {
        return proxy().getBlockState(pos);
    }

    @Override
    default FluidState getFluidState(BlockPos pos) {
        return proxy().getFluidState(pos);
    }

    @Override
    default int getHeight() {
        return proxy().getHeight();
    }

    @Override
    default int getBottomY() {
        return proxy().getBottomY();
    }

    @Override
    default float getBrightness(Direction direction, boolean shaded) {
        return proxy().getBrightness(direction, shaded);
    }

    @Override
    default LightingProvider getLightingProvider() {
        return proxy().getLightingProvider();
    }

    @Override
    default int getColor(BlockPos pos, ColorResolver colorResolver) {
        return proxy().getColor(pos, colorResolver);
    }
}
