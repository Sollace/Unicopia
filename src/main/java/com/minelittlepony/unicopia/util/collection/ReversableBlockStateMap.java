package com.minelittlepony.unicopia.util.collection;

public class ReversableBlockStateMap extends BlockStateMap {
    private static final long serialVersionUID = 6154365988455383098L;

    private final BlockStateMap inverse = new BlockStateMap();

    public BlockStateMap getInverse() {
        return inverse;
    }

    public boolean add(StateMapping mapping) {
        inverse.add(mapping.inverse());
        return super.add(mapping);
    }
}
