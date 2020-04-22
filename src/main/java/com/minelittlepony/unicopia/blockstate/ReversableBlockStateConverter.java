package com.minelittlepony.unicopia.blockstate;

public interface ReversableBlockStateConverter extends BlockStateConverter {
    BlockStateConverter getInverse();
}
