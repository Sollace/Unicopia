package com.minelittlepony.unicopia.block.zap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ZapAppleLogBlock extends PillarBlock implements ElectrifiedBlock {
    private static final MapCodec<ZapAppleLogBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("model_block").forGetter(b -> b.artifialModelBlock),
            BedBlock.createSettingsCodec()
    ).apply(instance, ZapAppleLogBlock::new));
    public static final BooleanProperty NATURAL = BooleanProperty.of("natural");

    public static Settings settings(MapColor topMapColor, MapColor sideMapColor) {
        return Settings.create()
            .mapColor(state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0f)
            .sounds(BlockSoundGroup.WOOD)
            .burnable();
    }

    private final BlockState artifialModelBlock;

    public ZapAppleLogBlock(BlockState artifialModelBlock, Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(NATURAL, true));
        this.artifialModelBlock = artifialModelBlock;
    }

    @Override
    public MapCodec<? extends ZapAppleLogBlock> getCodec() {
        return CODEC;
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
        triggerLightning(state, world, pos);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (!state.get(NATURAL)) {
            return artifialModelBlock.calcBlockBreakingDelta(player, world, pos);
        }

        return getBlockBreakingDelta(super.calcBlockBreakingDelta(state, player, world, pos), player);
    }
}
