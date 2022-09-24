package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockRenderView;

public interface TintedBlock {
    List<Block> REGISTRY = new ArrayList<>();

    int MAX_BIT_SHIFT = 8 * 3;
    int WHITE = 0xFFFFFF;

    int getTint(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int foliageColor);

    /**
     * Rotates a color's components by a given number of bytes.
     */
    static int rotate(int color, int bitShift) {
        bitShift = MathHelper.clamp(bitShift, -MAX_BIT_SHIFT, MAX_BIT_SHIFT);
        return WHITE & (
                (color << bitShift)
             | ((color >> (MAX_BIT_SHIFT - bitShift)) & ~(WHITE << bitShift))
        );
    }

    /**
     * Blends two colors together using the overlay color's opacity value
     */
    static int blend(int color, int overlay) {
        return blend(color, overlay & WHITE, ((overlay >> MAX_BIT_SHIFT) & 0xFF) / 255F);
    }

    /**
     * Blends two colors an alpha ratio to determine the opacity of the overlay.
     */
    static int blend(int color, int overlay, float blend) {
        return blendComponent(color & 0xFF, overlay & 0xFF, blend)
            | (blendComponent((color >> 8) & 0xFF, (overlay >> 8) & 0xFF, blend) << 8)
            | (blendComponent((color >> 16) & 0xFF, (overlay >> 16) & 0xFF, blend) << 16);

    }

    private static int blendComponent(int color, int overlay, float blend) {
        return (int)((color * (1F - blend)) + (overlay * blend));
    }
}
