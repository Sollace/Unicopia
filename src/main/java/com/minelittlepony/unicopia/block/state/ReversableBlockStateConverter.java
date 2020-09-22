package com.minelittlepony.unicopia.block.state;

public interface ReversableBlockStateConverter extends BlockStateConverter {
    BlockStateConverter getInverse();
}
