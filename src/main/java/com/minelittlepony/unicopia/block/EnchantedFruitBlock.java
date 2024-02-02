package com.minelittlepony.unicopia.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

public class EnchantedFruitBlock extends FruitBlock {
    static final BooleanProperty ENCHANTED = BooleanProperty.of("enchanted");

    private static final MapCodec<EnchantedFruitBlock> CODEC = createCodec(EnchantedFruitBlock::new);

    public EnchantedFruitBlock(Direction attachmentFace, Block stem, VoxelShape shape, boolean flammable, Settings settings) {
        super(attachmentFace, stem, shape, flammable, settings);
        setDefaultState(getDefaultState().with(ENCHANTED, false));
    }

    @Override
    public MapCodec<? extends FruitBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ENCHANTED);
    }
}
