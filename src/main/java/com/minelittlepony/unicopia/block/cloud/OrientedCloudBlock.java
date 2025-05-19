package com.minelittlepony.unicopia.block.cloud;

import com.minelittlepony.unicopia.EquineContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class OrientedCloudBlock extends CloudBlock {
    private static final MapCodec<OrientedCloudBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("meltable").forGetter(b -> b.meltable),
            BedBlock.createSettingsCodec()
    ).apply(instance, OrientedCloudBlock::new));
    public static final DirectionProperty FACING = Properties.FACING;

    public OrientedCloudBlock(boolean meltable, Settings settings) {
        super(meltable, settings);
        this.setDefaultState(getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    public MapCodec<? extends CloudBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected BlockState getPlacementState(ItemPlacementContext ctx, EquineContext equineContext) {
        return getDefaultState().with(FACING, ctx.getSide().getOpposite());
    }
}
