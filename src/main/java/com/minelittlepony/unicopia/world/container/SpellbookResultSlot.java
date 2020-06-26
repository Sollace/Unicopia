package com.minelittlepony.unicopia.world.container;

import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.world.container.SpellBookContainer.SBInventory;
import com.minelittlepony.unicopia.world.item.MagicGemItem;
import com.minelittlepony.unicopia.world.recipe.URecipes;
import com.minelittlepony.unicopia.world.recipe.enchanting.IPageUnlockListener;
import com.minelittlepony.unicopia.world.recipe.enchanting.SpellCraftingEvent;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class SpellbookResultSlot extends SpellBookContainer.InputSlot {
    public static final Identifier EMPTY_GEM_SLOT = new Identifier("unicopia", "item/empty_gem_slot");

    private final Pony player;
    private final SBInventory craftMatrix;

    private IPageUnlockListener listener;

    private boolean crafted;

    public SpellbookResultSlot(IPageUnlockListener listener, Pony player, SBInventory craftMatric, Inventory inventory, int index, int xPosition, int yPosition) {
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

            ItemStack current = craftMatrix.getCraftResultMatrix().getStack(0);
            craftMatrix.getCraftResultMatrix().setStack(0, stack);

            DefaultedList<ItemStack> remaining = player.world.getRecipeManager().getRemainingStacks(URecipes.SPELL_BOOK,  craftMatrix, player.world);

            craftMatrix.getCraftResultMatrix().setStack(0, current);

            for (int i = 0; i < remaining.size(); ++i) {
                current = craftMatrix.getStack(i);
                ItemStack remainder = remaining.get(i);

                if (!current.isEmpty()) {
                    if (current.getCount() < stack.getCount()) {
                        craftMatrix.setStack(i, ItemStack.EMPTY);
                    } else {
                        craftMatrix.removeStack(i, stack.getCount());
                    }

                    if (!remainder.isEmpty()) {
                        if (craftMatrix.getStack(i).isEmpty()) {
                            craftMatrix.setStack(i, remainder);
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
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_GEM_SLOT);
    }
}