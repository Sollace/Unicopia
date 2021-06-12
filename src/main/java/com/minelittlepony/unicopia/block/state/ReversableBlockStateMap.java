package com.minelittlepony.unicopia.block.state;

import java.util.List;
import java.util.stream.Collectors;

class ReversableBlockStateMap extends BlockStateMap implements ReversableBlockStateConverter {
    private final BlockStateMap inverse;

    ReversableBlockStateMap(List<StateMapping> mappings) {
        super(mappings);
        inverse = new BlockStateMap(mappings.stream().map(StateMapping::inverse).collect(Collectors.toList()));
    }

    @Override
    public BlockStateMap getInverse() {
        return inverse;
    }

    public static class Builder extends BlockStateMap.Builder {
        @Override
        @SuppressWarnings("unchecked")
        public <T extends BlockStateConverter> T build() {
            return (T)new ReversableBlockStateMap(items);
        }
    }
}
