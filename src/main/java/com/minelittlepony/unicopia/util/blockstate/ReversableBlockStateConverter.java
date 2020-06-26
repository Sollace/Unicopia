package com.minelittlepony.unicopia.util.blockstate;

public interface ReversableBlockStateConverter extends BlockStateConverter {
    BlockStateConverter getInverse();
}
