package com.minelittlepony.unicopia.enchanting;

import com.minelittlepony.unicopia.UItems;
import com.minelittlepony.unicopia.enchanting.PagesList.IPageEvent;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.item.ItemStack;

/**
 * A basic event for unlocking a page when a gem is crafted for the given spell
 */
public class BasicCraftingEvent implements IPageEvent {

	private final String matched;
	private final int pageIndex;

	public BasicCraftingEvent(int page, String effectName) {
		matched = effectName;
		pageIndex = page;
	}

	@Override
	public boolean matches(IPageOwner prop, ItemStack stack) {
		return stack.getItem() == UItems.spell && SpellRegistry.getKeyFromStack(stack).equals(matched);
	}

	@Override
	public int getPage(int stackSize) {
		return pageIndex;
	}

}
