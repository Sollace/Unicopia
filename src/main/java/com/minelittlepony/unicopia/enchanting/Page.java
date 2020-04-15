package com.minelittlepony.unicopia.enchanting;

import net.minecraft.util.Identifier;

/**
 * A spellbook page
 */
public interface Page extends Comparable<Page> {
    /**
     * Gets the index.
     * This is the position the page appears in the book gui.
     */
    int getIndex();

    /**
     * The unique name of this page.
     */
    Identifier getName();

    /**
     * Tests unlock conditions for this page.
     * Returns true if the owner is permitted to read this page.
     */
    boolean canUnlock(PageOwner owner, IUnlockEvent event);

    /**
     * Gets the texture.
     * This is what's shown when this page is opened in the book gui.
     */
    Identifier getTexture();

    /**
     * The default state.
     */
    PageState getDefaultState();

    Page next();

    Page prev();
}
