package com.minelittlepony.unicopia.enchanting;

import com.minelittlepony.unicopia.enchanting.PagesList.IPageEvent;

import net.minecraft.item.ItemStack;

/**
 * An unlock event that requires other pages to be unlocked before it too can be unlocked.
 *
 */
public class MultiPageUnlockEvent implements IPageEvent {

	private final int pageIndex;

	private final int[][] otherPageIndeces;

	public MultiPageUnlockEvent(int page, int[]... otherPages) {
		pageIndex = page;
		otherPageIndeces = otherPages;
	}

	@Override
	public boolean matches(IPageOwner prop, ItemStack stack) {
		for (int i = 0; i < otherPageIndeces.length; i++) {
			if (!checkPageUnlockSet(prop, otherPageIndeces[i])) return false;
		}
		return true;
	}

	private boolean checkPageUnlockSet(IPageOwner prop, int[] pages) {
		for (int i = 0; i < pages.length; i++) {
			if (prop.hasPageUnlock(pages[i])) return true;
		}
		return false;
	}

	@Override
	public int getPage(int stackSize) {
		return pageIndex;
	}
}
