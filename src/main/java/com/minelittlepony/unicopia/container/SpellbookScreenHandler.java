package com.minelittlepony.unicopia.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;

public class SpellbookScreenHandler extends ScreenHandler {

    private final int MAX_INGREDIENTS;

    private final int GEM_SLOT_INDEX;
    private final int HOTBAR_START;
    private final int HOTBAR_END;

    private final SpellbookInventory input;

    private OutputSlot gemSlot;
    private final CraftingResultInventory result = new CraftingResultInventory();

    private final PlayerInventory inventory;

    private final ScreenHandlerContext context;

    protected SpellbookScreenHandler(int syncId, PlayerInventory inv) {
        this(syncId, inv, ScreenHandlerContext.EMPTY);
    }

    public SpellbookScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext context) {
        super(UScreenHandlers.SPELL_BOOK, syncId);
        inventory = inv;
        this.context = context;

        List<int[]> grid = new ArrayList<>();
        List<int[]> gemPos = new ArrayList<>();
        createGrid(grid, gemPos);

        GEM_SLOT_INDEX = MAX_INGREDIENTS = grid.size();
        HOTBAR_START = GEM_SLOT_INDEX + 1;
        HOTBAR_END = HOTBAR_START + 9;

        input = new SpellbookInventory(this, MAX_INGREDIENTS, 1);

        for (int i = 0; i < MAX_INGREDIENTS; i++) {
            var pos = grid.get(i);
            addSlot(new InputSlot(input, i, pos));
        }

        addSlot(gemSlot = new OutputSlot(inventory.player, input, result, 0, gemPos.get(0)));

        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 121 + i * 18, 195));
        }

        onContentChanged(input);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return EquinePredicates.IS_CASTER.test(player);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        context.run((world, pos) -> {
            if (!world.isClient && !gemSlot.getStack().isEmpty()) {
                world.getServer().getRecipeManager().getFirstMatch(URecipes.SPELLBOOK, input, world)
                    .filter(recipe -> result.shouldCraftRecipe(world, (ServerPlayerEntity)this.inventory.player, recipe))
                    .map(recipe -> recipe.craft(input))
                    .ifPresentOrElse(gemSlot::setCrafted, gemSlot::setUncrafted);

                ((ServerPlayerEntity)this.inventory.player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, GEM_SLOT_INDEX, gemSlot.getStack()));
            }
        });
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        Slot sourceSlot = slots.get(index);

        if (sourceSlot == null || !sourceSlot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack transferredStack = sourceSlot.getStack();
        ItemStack stack = transferredStack.copy();

        if (index >= HOTBAR_START) {
            if (!gemSlot.hasStack() && gemSlot.canInsert(stack)) {
                if (insertItem(transferredStack, GEM_SLOT_INDEX, GEM_SLOT_INDEX + 1, false)) {
                    onContentChanged(input);
                    return ItemStack.EMPTY;
                }
            }

            if (insertItem(transferredStack, 0, GEM_SLOT_INDEX, false)) {
                sourceSlot.onQuickTransfer(transferredStack, stack);
                onContentChanged(input);
                return ItemStack.EMPTY;
            }
        } else {
            if (insertItem(transferredStack, HOTBAR_START, HOTBAR_END, true)) {
                sourceSlot.onQuickTransfer(transferredStack, stack);
                onContentChanged(input);
                return ItemStack.EMPTY;
            }
        }

        if (transferredStack.getCount() == stack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTakeItem(player, transferredStack);

        return stack;
    }

    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean success = false;

        int i = fromLast ? endIndex - 1 : startIndex;

        while (true) {
            if (i < startIndex || i >= endIndex) {
                break;
            }

            Slot slot = getSlot(i);
            ItemStack current = slot.getStack();

            if (!current.isEmpty() && ItemStack.canCombine(stack, current)) {
                // abide by the slot's max item count when trying to insert stacks
                int available = Math.min(Math.min(current.getMaxCount(), slot.getMaxItemCount()) - current.getCount(), stack.getCount());

                if (available > 0) {
                    current.increment(available);
                    stack.decrement(available);
                    slot.markDirty();
                    success = true;
                }
            }

            i += fromLast ? -1 : 1;
        }

        i = fromLast ? endIndex - 1 : startIndex;

        while (true) {
            if (i < startIndex || i >= endIndex) {
                break;
            }

            Slot slot = getSlot(i);
            ItemStack current = slot.getStack();

            if (current.isEmpty() && slot.canInsert(stack)) {
                if (stack.getCount() > slot.getMaxItemCount()) {
                    slot.setStack(stack.split(slot.getMaxItemCount()));
                } else {
                    slot.setStack(stack.split(stack.getCount()));
                }
                slot.markDirty();
                success = true;
                break;
            }

            i += fromLast ? -1 : 1;
        }

        return success;
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        gemSlot.setUncrafted();
        super.close(playerEntity);
        context.run((world, pos) -> {
            dropInventory(playerEntity, input);
            dropInventory(playerEntity, result);
        });
    }

    /**
     * Creates a hexagonal crafting grid.
     * @param grid   Output for normal slot positions.
     * @param gemPos Output for the gem slot position.
     */
    private static void createGrid(List<int[]> grid, List<int[]> gemPos) {
        int cols = 4;
        int spacing = 23;

        int top = 34;
        int left = 65;

        for (int row = 0; row < 7; row++) {
            for (int i = 0; i < cols; i++) {

                int ring = 3;
                if (row == 0 || row == 6) {
                    ring = 1;
                } else if ((row == 1 || row == 5) && i > 0 && i < cols - 1) {
                    ring = 2;
                } else {
                    if (i == 0 || i == cols - 1) {
                        ring = 1;
                    } else if (i == 1 || i == cols - 2) {
                        ring = 2;
                    }
                }

                (row == 3 && i == 3 ? gemPos : grid).add(new int[] {
                        left + (i * spacing),
                        top,
                        row == 3 && i == 3 ? 4 : ring
                });
            }
            top += spacing * 0.9;
            left -= (spacing / 2) * (row > 2 ? -1 : 1);
            cols += row > 2 ? -1 : 1;
        }
    }

    public interface SpellbookSlot {
        int getRing();
    }

    public class SpellbookInventory extends CraftingInventory {

        public SpellbookInventory(ScreenHandler handler, int width, int height) {
            super(handler, width, height);
        }

        public ItemStack getItemToModify() {
            return gemSlot.uncrafted.orElse(gemSlot.getStack());
        }

        public int getRing(int slot) {
            Slot s = slots.get(slot);
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

    public class InputSlot extends Slot implements SpellbookSlot {
        private final int ring;

        public InputSlot(Inventory inventory, int index, int[] params) {
            super(inventory, index, params[0], params[1]);
            ring = params[2];
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public int getRing() {
            return ring;
        }
    }

    public static class OutputSlot extends CraftingResultSlot implements SpellbookSlot {

        private Optional<ItemStack> uncrafted = Optional.empty();

        private final PlayerEntity player;
        private final SpellbookInventory input;

        private final int ring;

        public OutputSlot(PlayerEntity player, SpellbookInventory input, Inventory inventory, int index, int[] params) {
            super(player, input, inventory, index, params[0], params[1]);
            this.player = player;
            this.input = input;
            this.ring = params[2];
        }

        public void setCrafted(ItemStack crafted) {
            uncrafted = uncrafted.or(() -> Optional.of(getStack()));
            setStack(crafted);
            player.playSound(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.MASTER, 1, 0.3F);
        }

        public void setUncrafted() {
            player.playSound(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.MASTER, 0.2F, 0.2F);
            uncrafted = uncrafted.filter(stack -> {
                setStack(stack);
                return false;
            });
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.getItem() == UItems.GEMSTONE;
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public int getRing() {
            return ring;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            if (uncrafted.isPresent()) {
                uncrafted = Optional.empty();
                onCrafted(stack);

                Pony pony = Pony.of(player);
                InventoryUtil.iterate(input).forEach(s -> {
                    pony.getDiscoveries().unlock(s.getItem());
                });

                DefaultedList<ItemStack> defaultedList = player.world.getRecipeManager().getRemainingStacks(URecipes.SPELLBOOK, input, player.world);

                for (int i = 0; i < defaultedList.size(); ++i) {
                   ItemStack itemStack = input.getStack(i);
                   ItemStack itemStack2 = defaultedList.get(i);
                   if (!itemStack.isEmpty()) {
                      input.removeStack(i, 1);
                      itemStack = input.getStack(i);
                   }

                   if (!itemStack2.isEmpty()) {
                      if (itemStack.isEmpty()) {
                         input.setStack(i, itemStack2);
                      } else if (ItemStack.areItemsEqualIgnoreDamage(itemStack, itemStack2) && ItemStack.areTagsEqual(itemStack, itemStack2)) {
                         itemStack2.increment(itemStack.getCount());
                         input.setStack(i, itemStack2);
                      } else if (!player.getInventory().insertStack(itemStack2)) {
                         player.dropItem(itemStack2, false);
                      }
                   }
                }
            }
        }
    }
}
