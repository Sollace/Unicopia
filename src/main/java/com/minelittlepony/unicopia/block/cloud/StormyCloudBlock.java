package com.minelittlepony.unicopia.block.cloud;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StormyCloudBlock extends CloudBlock {
    private static final int MAX_CHARGE = 6;
    private static final IntProperty CHARGE = IntProperty.of("charge", 0, MAX_CHARGE);

    public StormyCloudBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(CHARGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.onLandedUpon(world, state, pos, entity, fallDistance);
        if (state.get(CHARGE) < MAX_CHARGE) {
            if (world.random.nextInt(5) == 0) {
                world.setBlockState(pos, state.cycle(CHARGE));
            }
        } else {

        }
    }
}
