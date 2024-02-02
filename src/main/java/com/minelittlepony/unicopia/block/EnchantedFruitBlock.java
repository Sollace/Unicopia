package com.minelittlepony.unicopia.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class EnchantedFruitBlock extends FruitBlock {
    static final BooleanProperty ENCHANTED = BooleanProperty.of("enchanted");

    public EnchantedFruitBlock(Settings settings, Direction attachmentFace, Block stem, VoxelShape shape) {
        super(settings, attachmentFace, stem, shape);
        setDefaultState(getDefaultState().with(ENCHANTED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ENCHANTED);
    }

}
