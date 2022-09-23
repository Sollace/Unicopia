package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ZapAppleLogBlock extends PillarBlock {
    ZapAppleLogBlock(MapColor topMapColor, MapColor sideMapColor) {
        super(AbstractBlock.Settings.of(Material.WOOD, state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor).strength(2.0f).sounds(BlockSoundGroup.WOOD).strength(500, 1200));
    }

    @Deprecated
    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        ZapAppleLeavesBlock.triggerLightning(state, world, pos, player);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float delta = super.calcBlockBreakingDelta(state, player, world, pos);

        if (Pony.of(player).getSpecies().canUseEarth()) {
            delta *= 50;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }
}
