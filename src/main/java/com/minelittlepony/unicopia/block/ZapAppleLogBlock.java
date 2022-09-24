package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ZapAppleLogBlock extends PillarBlock {
    public static final BooleanProperty NATURAL = Properties.PERSISTENT;

    private final Block artifialModelBlock;

    ZapAppleLogBlock(Block artifialModelBlock, MapColor topMapColor, MapColor sideMapColor) {
        super(AbstractBlock.Settings.of(Material.WOOD,
                    state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor
                )
                .sounds(BlockSoundGroup.WOOD)
                .strength(500, 1200));
        setDefaultState(getDefaultState().with(NATURAL, true));
        this.artifialModelBlock = artifialModelBlock;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(NATURAL);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(NATURAL, false);
    }

    @Deprecated
    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        ZapBlock.triggerLightning(state, world, pos, player);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (!state.get(NATURAL)) {
            return artifialModelBlock.calcBlockBreakingDelta(artifialModelBlock.getDefaultState(), player, world, pos);
        }

        float delta = super.calcBlockBreakingDelta(state, player, world, pos);

        if (Pony.of(player).getSpecies().canUseEarth()) {
            delta *= 50;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }
}
