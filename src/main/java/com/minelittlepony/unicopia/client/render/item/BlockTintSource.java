package com.minelittlepony.unicopia.client.render.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.TintedBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.FoliageColors;

public record BlockTintSource() implements TintSource {
    public static final BlockTintSource INSTANCE = new BlockTintSource();
    public static final MapCodec<BlockTintSource> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int getTint(ItemStack stack, ClientWorld world, LivingEntity user) {
        return getColor(Block.getBlockFromItem(stack.getItem()).getDefaultState(), null, null, -1);
    }

    @Override
    public MapCodec<? extends TintSource> getCodec() {
        return CODEC;
    }

    public static int getColor(BlockState state, @Nullable BlockRenderView view, @Nullable BlockPos pos, int tintIndex) {
        int color = view == null || pos == null ? FoliageColors.DEFAULT : BiomeColors.getFoliageColor(view, pos);

        if (state.getBlock() instanceof TintedBlock block) {
            return ColorHelper.fullAlpha(block.getTint(state, view, pos, color));
        }

        return color;
    }

}
