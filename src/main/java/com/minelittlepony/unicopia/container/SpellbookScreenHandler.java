package com.minelittlepony.unicopia.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class SpellbookScreenHandler extends ScreenHandler {

    private final int MAX_INGREDIENTS;

    private final int GEM_SLOT_INDEX;
    private final int HOTBAR_START;
    private final int HOTBAR_END;

    private final CraftingInventory input;

    private OutputSlot gemSlot;
    private final CraftingResultInventory result = new CraftingResultInventory();

    private final PlayerInventory inventory;

    protected SpellbookScreenHandler(int syncId, PlayerInventory inv) {
        super(UScreenHandlers.SPELL_BOOK, syncId);
        inventory = inv;

        List<Pair<Integer, Integer>> grid = new ArrayList<>();
        List<Pair<Integer, Integer>> gemPos = new ArrayList<>();
        createGrid(grid, gemPos);

        GEM_SLOT_INDEX = MAX_INGREDIENTS = grid.size();
        HOTBAR_START = GEM_SLOT_INDEX + 1;
        HOTBAR_END = HOTBAR_START + 9;

        input = new CraftingInventory(this, MAX_INGREDIENTS, 1);

        for (int i = 0; i < MAX_INGREDIENTS; i++) {
            var pos = grid.get(i);
            addSlot(new InputSlot(input, i, pos.getLeft(), pos.getRight()));
        }

        addSlot(gemSlot = new OutputSlot(inventory.player, input, result, 0, gemPos.get(0).getLeft(), gemPos.get(0).getRight()));

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
        World world = this.inventory.player.world;
        if (!world.isClient) {
            world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, input, world)
                .map(recipe -> recipe.craft(input))
                .ifPresentOrElse(gemSlot::setCrafted, gemSlot::setUncrafted);
        }
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
                if (!insertItem(transferredStack, GEM_SLOT_INDEX, GEM_SLOT_INDEX + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (!insertItem(transferredStack, 0, GEM_SLOT_INDEX, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!insertItem(transferredStack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }

            sourceSlot.onQuickTransfer(transferredStack, stack);
            onContentChanged(input);
        }

        if (transferredStack.getCount() == stack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTakeItem(player, transferredStack);

        return stack;
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        gemSlot.setUncrafted();
        super.close(playerEntity);
        dropInventory(playerEntity, input);
        dropInventory(playerEntity, result);
    }

    private static void createGrid(List<Pair<Integer, Integer>> grid, List<Pair<Integer, Integer>> gemPos) {
        int cols = 4;
        int spacing = 23;

        int top = 34;
        int left = 65;

        for (int row = 0; row < 7; row++) {
            for (int i = 0; i < cols; i++) {
                (row == 3 && i == 3 ? gemPos : grid).add(new Pair<>(left + (i * spacing), top));
            }
            top += spacing * 0.9;
            left -= (spacing / 2) * (row > 2 ? -1 : 1);
            cols += row > 2 ? -1 : 1;
        }
    }

    public interface SpellbookSlot {}

    public class InputSlot extends Slot implements SpellbookSlot {
        public InputSlot(Inventory inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }

    public static class OutputSlot extends CraftingResultSlot implements SpellbookSlot {

        private Optional<ItemStack> uncrafted = Optional.empty();

        public OutputSlot(PlayerEntity player, CraftingInventory input, Inventory inventory, int index, int x, int y) {
            super(player, input, inventory, index, x, y);
        }

        public void setCrafted(ItemStack crafted) {
            uncrafted = uncrafted.or(() -> Optional.of(getStack()));
            setStack(crafted);
        }

        public void setUncrafted() {
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
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            if (uncrafted.isPresent()) {
                uncrafted = Optional.empty();
                super.onTakeItem(player, stack);
            }
        }
    }
}
