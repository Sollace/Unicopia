package com.minelittlepony.unicopia.block.state;

class ReversableBlockStateMap extends BlockStateMap implements ReversableBlockStateConverter {
    private static final long serialVersionUID = 6154365988455383098L;

    private final BlockStateMap inverse = new BlockStateMap();

    @Override
    public BlockStateMap getInverse() {
        return inverse;
    }

    @Override
    public boolean add(StateMapping mapping) {
        inverse.add(mapping.inverse());
        return super.add(mapping);
    }
}
