package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.util.InventoryUtil;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SpellbookInventory extends CraftingInventory {
    private final SpellbookScreenHandler handler;

    public SpellbookInventory(SpellbookScreenHandler handler, int width, int height) {
        super(handler, width, height);
        this.handler = handler;
    }

    public ItemStack getItemToModify() {
        return handler.gemSlot.getStack();
    }

    public boolean hasIngredients() {
        for (int i = 0; i < handler.GEM_SLOT_INDEX; i++) {
            if (!getStack(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int getRing(int slot) {
        Slot s = handler.slots.get(slot);
        return s instanceof SpellbookSlot ? ((SpellbookSlot)s).getRing() : 0;
    }

    public SpellTraits getTraits() {
        return SpellTraits.union(InventoryUtil.slots(this)
                .map(slot -> SpellTraits.of(getStack(slot)).multiply(getRingFactor(getRing(slot))))
                .toArray(SpellTraits[]::new)
        );
    }

    public static float getRingFactor(int ring) {
        switch (ring) {
            case 1: return 1;
            case 2: return 0.6F;
            case 3: return 0.3F;
            default: return 0;
        }
    }
}