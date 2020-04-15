package com.minelittlepony.unicopia.redux.enchanting;

import com.minelittlepony.unicopia.core.enchanting.Page;

@FunctionalInterface
public interface IPageUnlockListener {
    /**
     * Called when a page is unlocked.
     *
     * @param page The page that has been unlocked
     * @return True to allow, false to block.
     */
	boolean onPageUnlocked(Page page);
}
