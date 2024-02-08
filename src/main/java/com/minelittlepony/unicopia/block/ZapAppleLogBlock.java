package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ZapAppleLogBlock extends PillarBlock {
    public static final BooleanProperty NATURAL = ZapBlock.NATURAL;

    private final Block artifialModelBlock;

    ZapAppleLogBlock(Block artifialModelBlock, MapColor topMapColor, MapColor sideMapColor) {
        super(AbstractBlock.Settings.create().mapColor(
                    state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor
                )
                .instrument(Instrument.BASS)
                .strength(2.0f)
                .sounds(BlockSoundGroup.WOOD)
                .burnable());
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
        return super.getPlacementState(ctx).with(NATURAL, false);
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

        if (Pony.of(player).getCompositeRace().canUseEarth()) {
            delta *= 50;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }
}
