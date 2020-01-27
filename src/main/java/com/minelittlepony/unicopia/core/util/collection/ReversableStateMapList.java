package com.minelittlepony.unicopia.core.util.collection;

public class ReversableStateMapList extends StateMapList {
    private static final long serialVersionUID = 6154365988455383098L;

    private final StateMapList inverse = new StateMapList();

    public StateMapList getInverse() {
        return inverse;
    }

    public boolean add(IStateMapping mapping) {
        inverse.add(mapping.inverse());
        return super.add(mapping);
    }
}
