package com.minelittlepony.unicopia.world.container;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.world.recipe.URecipes;
import com.minelittlepony.unicopia.world.recipe.enchanting.IPageUnlockListener;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion.DestructionType;

public class SpellBookContainer extends ScreenHandler {

    private final World world;

    private Inventory result = new SimpleInventory(1);

    private SBInventory input = new SBInventory(result, this, 5, 1);

    private IPageUnlockListener listener;

    private SpellbookResultSlot output = null;

    private final PlayerEntity player;

    public SpellBookContainer(int sync, PlayerInventory inv) {
        super(UContainers.SPELL_BOOK, sync);
        world = inv.player.world;
        this.player = inv.player;

        initCraftingSlots();

        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(player.inventory, i, 121 + i * 18, 195));
        }

        onContentChanged(input);
    }

    public void setListener(IPageUnlockListener listener) {
        this.listener = listener;

        if (output != null) {
            output.setListener(listener);
        }
    }

    public void initCraftingSlots() {
        addSlot(new InputSlot(input, 0, 175, 50));
        addSlot(new InputSlot(input, 1, 149, 94));
        addSlot(new InputSlot(input, 2, 175, 134));
        addSlot(new InputSlot(input, 3, 226, 120));
        addSlot(new InputSlot(input, 4, 227, 65));
        addSlot(output = new SpellbookResultSlot(listener, Pony.of(player), input, result, 0, 191, 92));
    }

    @Override
    public void onContentChanged(Inventory inventoryIn) {
        ItemStack current = result.getStack(0);

        if (!current.isEmpty()) {
            ItemStack crafted = player.world.getRecipeManager().getFirstMatch(URecipes.SPELL_BOOK, input, world)
                    .map(Recipe::getOutput)
                    .orElse(ItemStack.EMPTY);

            if (!crafted.isEmpty()) {
                output.setCrafted(true);

                if  (crafted.getItem() != current.getItem()) {

                    result.setStack(0, ItemStack.EMPTY);
                    output.onTakeItem(player, crafted);

                    player.dropItem(crafted, true);

                    player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_SNARE, 1, 1);

                    world.createExplosion(null, player.getX(), player.getY(), player.getZ(), 0, DestructionType.NONE);
                    world.createExplosion(null, player.getX(), player.getY(), player.getZ(), 0, DestructionType.NONE);
                    world.addParticle(ParticleTypes.EXPLOSION, player.getX(), player.getY(), player.getZ(), 1, 0, 0);

                    AwaitTickQueue.scheduleTask(player.world, w -> player.currentScreenHandler.close(player), 0);

                    return;
                }

                current = crafted;
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
            } else {
                current = SpellRegistry.instance().disenchantStack(current);

                output.setCrafted(false);
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            }

            result.setStack(0, current);
        }
    }

    @Nonnull
    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            stack = slotStack.copy();

            if (index > 5) {
                if (!output.hasStack() && output.canInsert(stack)) {
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
                onContentChanged(input);
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
        dropInventory(player, world, input);
        dropInventory(player, world, result);
        super.close(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return EquinePredicates.PLAYER_UNICORN.test(player);
    }

    public static class InputSlot extends Slot {

        public InputSlot(Inventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        public boolean canBeHovered() {
            return true;
        }
    }

    public static class SBInventory extends CraftingInventory {

        private final Inventory craftResult;

        public SBInventory(Inventory resultMatrix, ScreenHandler eventHandler, int width, int height) {
            super(eventHandler, width, height);
            craftResult = resultMatrix;
        }

        public Inventory getCraftResultMatrix() {
            return craftResult;
        }
    }
}
