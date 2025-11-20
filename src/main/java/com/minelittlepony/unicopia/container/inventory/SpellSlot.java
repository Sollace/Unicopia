package com.minelittlepony.unicopia.container.inventory;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.EnchantableItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class SpellSlot extends Slot implements SpellbookSlot {
    private final SpellbookScreenHandler handler;

    private final Pony pony;
    private final Hand hand;

    public SpellSlot(SpellbookScreenHandler handler, Pony pony, Hand hand, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.handler = handler;
        this.pony = pony;
        this.hand = hand;
    }

    public CustomisedSpellType<?> getSpell() {
        return pony.getCharms().getEquippedSpell(hand);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public boolean isEnabled() {
       return handler.canShowSlots(SlotType.INVENTORY) && !handler.canShowSlots(SlotType.CRAFTING);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack getStack() {
        var spell = getSpell();
        return spell.isEmpty() ? Items.AIR.getDefaultStack() : spell.getDefaultStack();
    }

    @Override
    public void setStackNoCallbacks(ItemStack stack) {

    }

    public void swapSpells(ItemStack stack) {
        var result = EnchantableItem.consumeSpell(stack, pony.asEntity(), null, true);
        var spell = getSpell();
        spell.traits().applyTo(EnchantableItem.enchant(stack, spell.type()));
        pony.getCharms().equipSpell(hand, result.getValue());
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public void markDirty() {
    }

    @Override
    @Nullable
    public Identifier getForegroundIdentifier() {
        return GEM;
    }

    @Override
    public boolean showTraits() {
        return false;
    }
}