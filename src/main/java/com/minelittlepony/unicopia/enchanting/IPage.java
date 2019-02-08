package com.minelittlepony.unicopia.enchanting;

import net.minecraft.util.ResourceLocation;

/**
 * A spellbook page
 */
public interface IPage extends Comparable<IPage> {
    /**
     * Gets the index.
     * This is the position the page appears in the book gui.
     */
    int getIndex();

    /**
     * The unique name of this page.
     */
    ResourceLocation getName();

    /**
     * Tests unlock conditions for this page.
     * Returns true if the owner is permitted to read this page.
     */
    boolean canUnlock(IPageOwner owner, IUnlockEvent event);

    /**
     * Gets the texture.
     * This is what's shown when this page is opened in the book gui.
     */
    ResourceLocation getTexture();

    /**
     * The default state.
     */
    PageState getDefaultState();

    IPage next();

    IPage prev();
}
