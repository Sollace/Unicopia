package com.minelittlepony.unicopia.forgebullshit;

/**
 * An item that has multiple subtypes.
 */
public interface IMultiItem {
    /**
     * Returns the names for all subtypes this item supports.
     * Used to register models and textures on the client.
     */
    String[] getVariants();
}
