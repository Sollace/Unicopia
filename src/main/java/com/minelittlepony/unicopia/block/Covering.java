package com.minelittlepony.unicopia.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;

public enum Covering implements StringIdentifiable {
    COVERED,
    UNCOVERED,
    SNOW_COVERED;

    public static final EnumProperty<Covering> PROPERTY = EnumProperty.of("covering", Covering.class);

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public String asString() {
        return name().toLowerCase();
    }

    public static Covering getCovering(IWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.SNOW_BLOCK || block == Blocks.SNOW) {
            return Covering.SNOW_COVERED;
        }

        if ((state.isOpaque() && state.isSimpleFullBlock(world, pos)) || Block.isFaceFullSquare(state.getCollisionShape(world, pos, EntityContext.absent()), Direction.DOWN)) {
            return Covering.COVERED;
        }

        return Covering.UNCOVERED;
    }
}