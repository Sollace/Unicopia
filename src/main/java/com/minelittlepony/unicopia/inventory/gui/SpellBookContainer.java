package com.minelittlepony.unicopia.inventory.gui;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.enchanting.IPageUnlockListener;
import com.minelittlepony.unicopia.magic.spells.SpellRegistry;
import com.minelittlepony.unicopia.world.UWorld;

import net.minecraft.container.Container;
import net.minecraft.container.Slot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class SpellBookContainer extends Container {

    private final World worldObj;

    private Inventory craftResult = new BasicInventory(1);

    private SpellBookInventory craftMatrix = new SpellBookInventory(craftResult, this, 5, 1);

    private IPageUnlockListener listener;

    private SpellbookResultSlot resultSlot = null;

    private final PlayerEntity player;

    public SpellBookContainer(PlayerInventory inventory, World world, BlockPos pos) {
        super(null, 0);// TODO
        worldObj = world;
        player = inventory.player;

        initCraftingSlots();

        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 121 + i * 18, 195));
        }

        onContentChanged(craftMatrix);
    }

    public void setListener(IPageUnlockListener listener) {
        this.listener = listener;

        if (resultSlot != null) {
            resultSlot.setListener(listener);
        }
    }

    public void initCraftingSlots() {
        addSlot(new SpellbookSlot(craftMatrix, 0, 175, 50));
        addSlot(new SpellbookSlot(craftMatrix, 1, 149, 94));
        addSlot(new SpellbookSlot(craftMatrix, 2, 175, 134));
        addSlot(new SpellbookSlot(craftMatrix, 3, 226, 120));
        addSlot(new SpellbookSlot(craftMatrix, 4, 227, 65));
        addSlot(resultSlot = new SpellbookResultSlot(listener, SpeciesList.instance().getPlayer(player), craftMatrix, craftResult, 0, 191, 92));
    }

    @Override
    public void onContentChanged(Inventory inventoryIn) {
        ItemStack current = craftResult.getInvStack(0);

        if (!current.isEmpty()) {
            // TODO: RecipeType.SPELL_BOOK
            ItemStack crafted = player.world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftMatrix, worldObj)
                    .map(Recipe::getOutput)
                    .orElse(ItemStack.EMPTY);

            if (!crafted.isEmpty()) {
                resultSlot.setCrafted(true);

                if  (crafted.getItem() != current.getItem()) {

                    craftResult.setInvStack(0, ItemStack.EMPTY);
                    resultSlot.onTakeItem(player, crafted);

                    player.dropItem(crafted, true);

                    player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_SNARE, 1, 1);

                    worldObj.createExplosion(null, player.x, player.y, player.z, 0, DestructionType.NONE);
                    worldObj.createExplosion(null, player.x, player.y, player.z, 0, DestructionType.NONE);
                    worldObj.addParticle(ParticleTypes.EXPLOSION, player.x, player.y, player.z, 1, 0, 0);

                    UWorld.enqueueTask(w -> {
                        player.container.close(player);
                    });

                    return;
                }

                current = crafted;
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
            } else {
                current = SpellRegistry.instance().disenchantStack(current);

                resultSlot.setCrafted(false);
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            }

            craftResult.setInvStack(0, current);
        }
    }

    @Nonnull
    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slotList.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            if (index > 5) {
                if (!resultSlot.hasStack() && resultSlot.canInsert(stack)) {
                    if (!insertItem(slotStack, 5, 6, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!insertItem(slotStack, 0, 5, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!insertItem(slotStack, 6, 15, false)) {
                    return ItemStack.EMPTY;
                }

                slot.onStackChanged(slotStack, stack);
                onContentChanged(craftMatrix);
            }

            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, slotStack);
        }

        return stack;
    }

    @Override
    public void close(PlayerEntity player) {
        dropInventory(player, worldObj, craftMatrix);
        dropInventory(player, worldObj, craftResult);
        super.close(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return EquinePredicates.MAGI.test(player);
    }

    static class SpellbookSlot extends Slot {

        public SpellbookSlot(Inventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        public boolean canBeHovered() {
            return true;
        }
    }
}
