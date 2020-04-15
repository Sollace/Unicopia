package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.enchanting.IPageUnlockListener;
import com.minelittlepony.unicopia.enchanting.SpellCraftingEvent;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.MagicGemItem;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DefaultedList;

public class SpellbookResultSlot extends SpellBookContainer.SpellbookSlot {

    private final Pony player;
    private final SpellBookInventory craftMatrix;

    private IPageUnlockListener listener;

    private boolean crafted;

    public SpellbookResultSlot(IPageUnlockListener listener, Pony player, SpellBookInventory craftMatric, Inventory inventory, int index, int xPosition, int yPosition) {
        super(inventory, index, xPosition, yPosition);
        this.player = player;
        this.listener = listener;
        craftMatrix = craftMatric;
    }

    public void setListener(IPageUnlockListener listener) {
        this.listener = listener;
    }

    public void setCrafted(boolean crafted) {
        this.crafted = crafted;
    }

    @Override
    public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
        if (crafted) {
            onCrafted(stack);

            ItemStack current = craftMatrix.getCraftResultMatrix().getInvStack(0);
            craftMatrix.getCraftResultMatrix().setInvStack(0, stack);

            // TODO: URecipeType.SPELL_BOOK
            DefaultedList<ItemStack> remaining = player.world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING,  craftMatrix, player.world);

            craftMatrix.getCraftResultMatrix().setInvStack(0, current);

            for (int i = 0; i < remaining.size(); ++i) {
                current = craftMatrix.getInvStack(i);
                ItemStack remainder = remaining.get(i);

                if (!current.isEmpty()) {
                    if (current.getCount() < stack.getCount()) {
                        craftMatrix.setInvStack(i, ItemStack.EMPTY);
                    } else {
                        craftMatrix.takeInvStack(i, stack.getCount());
                    }

                    if (!remainder.isEmpty()) {
                        if (craftMatrix.getInvStack(i).isEmpty()) {
                            craftMatrix.setInvStack(i, remainder);
                        } else {
                            remainder.setCount(stack.getCount());
                            if (!player.inventory.insertStack(remainder)) {
                                player.dropItem(remainder, true);
                            }
                        }
                    }
                }
            }
        }

        return super.onTakeItem(player, stack);
    }

    @Override
    protected void onCrafted(ItemStack stack, int amount) {
        onCrafted(stack);
    }

    @Override
    protected void onCrafted(ItemStack stack) {
        SpellCraftingEvent.trigger(player.getPages(), stack, listener);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return (stack.getItem() instanceof MagicGemItem || stack.getItem() instanceof MusicDiscItem)
                && !SpellRegistry.stackHasEnchantment(stack);
    }

    @Override
    public String getBackgroundSprite() {
        return "unicopia:items/empty_slot_gem";
    }
}