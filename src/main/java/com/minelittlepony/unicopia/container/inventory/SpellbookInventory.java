package com.minelittlepony.unicopia.container.inventory;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
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

    public float getFactor(int slot) {
        Slot s = handler.slots.get(slot);
        return s instanceof SpellbookSlot ? ((SpellbookSlot)s).getWeight() : 0;
    }

    public SpellTraits getTraits() {
        return SpellTraits.union(InventoryUtil.slots(this)
                .map(slot -> SpellTraits.of(getStack(slot)).multiply(getFactor(slot)))
                .toArray(SpellTraits[]::new)
        );
    }

    public SpellbookRecipe.Input createInput() {
        float[] factors = new float[size()];
        ItemStack[] stacks = new ItemStack[size()];
        for (int i = 0; i < size(); i++) {
            factors[i] = getFactor(i);
            stacks[i] = getStack(i);
        }
        return new SpellbookRecipe.Input(getItemToModify(), stacks, factors, getTraits(), handler.GEM_SLOT_INDEX);
    }
}