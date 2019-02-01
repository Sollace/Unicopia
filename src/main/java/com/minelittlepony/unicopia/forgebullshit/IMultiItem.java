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

    /**
     * Returns true if variants should all share the same model as the {0} data value
     */
    default boolean variantsAreHidden() {
        return false;
    }

    /**
     * Gets the highest metadata value allowed for this item.
     */
    default int getMaxMetadata() {
        return getVariants().length;
    }
}
