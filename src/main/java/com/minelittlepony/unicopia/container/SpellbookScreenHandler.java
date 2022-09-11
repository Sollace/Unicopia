package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.client.sound.BufferedExecutor;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.datafixers.util.Pair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class SpellbookScreenHandler extends ScreenHandler {
    private static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{
            PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE
    };

    private final int MAX_INGREDIENTS;

    private final int GEM_SLOT_INDEX;
    private final int HOTBAR_START;
    private final int HOTBAR_END;

    private final SpellbookInventory input;

    private InputSlot gemSlot;
    private ResultSlot outputSlot;
    private final CraftingResultInventory result = new CraftingResultInventory();

    private final PlayerInventory inventory;

    private final ScreenHandlerContext context;

    private Predicate<SlotType> canShowSlots;

    private final SpellbookState state;

    @Nullable
    public UUID entityId;

    protected SpellbookScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        this(syncId, inv, ScreenHandlerContext.EMPTY, new SpellbookState().fromPacket(buf), null);
    }

    public SpellbookScreenHandler(int syncId, PlayerInventory inv, ScreenHandlerContext context, SpellbookState state, UUID entityId) {
        super(UScreenHandlers.SPELL_BOOK, syncId);
        this.entityId = entityId;
        this.state = state;
        inventory = inv;
        this.context = context;

        List<int[]> grid = new ArrayList<>();
        List<int[]> gemPos = new ArrayList<>();
        createGrid(grid, gemPos);

        MAX_INGREDIENTS = grid.size();
        GEM_SLOT_INDEX = MAX_INGREDIENTS;
        HOTBAR_START = GEM_SLOT_INDEX + 1;
        HOTBAR_END = HOTBAR_START + 9;

        input = new SpellbookInventory(this, MAX_INGREDIENTS + 1, 1);

        for (int i = 0; i < MAX_INGREDIENTS; i++) {
            var pos = grid.get(i);
            addSlot(new ModifierSlot(input, i, pos));
        }

        addSlot(gemSlot = new InputSlot(input, MAX_INGREDIENTS, gemPos.get(0)));

        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 121 + i * 18, 195));
        }
        for (int i = 0; i < PlayerInventory.MAIN_SIZE - 9; ++i) {
            int x = i % 5;
            int y = i / 5;
            addSlot(new InventorySlot(inventory, i + 9, 225 + x * 20, 50 + y * 20));
        }

        for (int i = 0; i < 4; i++) {
            final EquipmentSlot eq = EquipmentSlot.values()[5 - i];
            addSlot(new InventorySlot(inventory, PlayerInventory.OFF_HAND_SLOT - i - 1, 340, 50 + (i * 20)) {
                @Override
                public int getMaxItemCount() {
                    return 1;
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                    return eq == MobEntity.getPreferredEquipmentSlot(stack);
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    ItemStack stack = getStack();
                    if (!stack.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasBindingCurse(stack)) {
                        return false;
                    }
                    return super.canTakeItems(playerEntity);
                }

                @Override
                public Pair<Identifier, Identifier> getBackgroundSprite() {
                    return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_ARMOR_SLOT_TEXTURES[eq.getEntitySlotId()]);
                }
            });
        }

        addSlot(new InventorySlot(inventory, PlayerInventory.OFF_HAND_SLOT, 340, 150) {
            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT);
            }
        });

        addSlot(outputSlot = new ResultSlot(inventory.player, input, result, 0, gemPos.get(0)));

        onContentChanged(input);
    }

    public SpellbookState getSpellbookState() {
        return state;
    }

    public void addSlotShowingCondition(Predicate<SlotType> canShowSlots) {
        this.canShowSlots = canShowSlots;
    }

    public int getOutputSlotId() {
        return outputSlot.id;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return EquinePredicates.IS_CASTER.test(player);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        context.run((world, pos) -> {
            if (!world.isClient && !gemSlot.getStack().isEmpty()) {
                ItemStack resultStack = input.hasIngredients() ? world.getServer().getRecipeManager()
                        .getAllMatches(URecipes.SPELLBOOK, input, world)
                        .stream().sorted(Comparator.comparing(SpellbookRecipe::getPriority))
                        .findFirst()
                        .filter(recipe -> result.shouldCraftRecipe(world, (ServerPlayerEntity)this.inventory.player, recipe))
                        .map(recipe -> recipe.craft(input))
                        .orElse(input.getTraits().applyTo(UItems.BOTCHED_GEM.getDefaultStack())) : ItemStack.EMPTY;
                outputSlot.setStack(resultStack);

                setPreviousTrackedSlot(outputSlot.id, resultStack);
                ((ServerPlayerEntity)this.inventory.player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), outputSlot.id, outputSlot.getStack()));
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

        if (sourceSlot instanceof ResultSlot result) {
            result.onTakeItem(player, stack);
        }

        if (index >= HOTBAR_START && !(sourceSlot instanceof ResultSlot || sourceSlot instanceof InputSlot)) {
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

            if (insertItem(transferredStack, HOTBAR_END + 27, HOTBAR_END + 27 + 4, false)) {
                sourceSlot.onQuickTransfer(transferredStack, stack);
                onContentChanged(input);
                return ItemStack.EMPTY;
            }

            if (insertItem(transferredStack, HOTBAR_END, HOTBAR_END + 27, false)) {
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
        super.close(playerEntity);
        context.run((world, pos) -> {
            dropInventory(playerEntity, input);
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
            return gemSlot.getStack();
        }

        public boolean hasIngredients() {
            for (int i = 0; i < GEM_SLOT_INDEX; i++) {
                if (!getStack(i).isEmpty()) {
                    return true;
                }
            }
            return false;
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

    public class InventorySlot extends Slot implements SpellbookSlot {
        public InventorySlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getRing() {
            return 0;
        }

        @Override
        public boolean isEnabled() {
           return canShowSlots.test(SlotType.INVENTORY);
        }
    }

    public class ModifierSlot extends Slot implements SpellbookSlot {
        private final int ring;

        public ModifierSlot(Inventory inventory, int index, int[] params) {
            super(inventory, index, params[0], params[1]);
            ring = params[2];
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return true;
        }

        @Override
        public int getRing() {
            return ring;
        }

        @Override
        public boolean isEnabled() {
           return canShowSlots.test(SlotType.CRAFTING) && super.isEnabled();
        }
    }

    public class InputSlot extends Slot implements SpellbookSlot {
        private final int ring;

        public InputSlot(Inventory inventory, int index, int[] params) {
            super(inventory, index, params[0], params[1]);
            this.ring = params[2];
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
        public boolean isEnabled() {
           return canShowSlots.test(SlotType.CRAFTING) && !outputSlot.isEnabled();
        }
    }

    public class ResultSlot extends CraftingResultSlot implements SpellbookSlot {
        private final PlayerEntity player;
        private final SpellbookInventory input;

        private final int ring;

        public ResultSlot(PlayerEntity player, SpellbookInventory input, Inventory inventory, int index, int[] params) {
            super(player, input, inventory, index, params[0], params[1]);
            this.player = player;
            this.input = input;
            this.ring = params[2];
        }

        @Override
        public void setStack(ItemStack stack) {
            if (!stack.isEmpty() && !ItemStack.areEqual(stack, getStack())) {
                BufferedExecutor.bufferExecution(player, () -> {
                    player.playSound(stack.getItem() == UItems.BOTCHED_GEM ? USounds.GUI_ABILITY_FAIL : USounds.GUI_SPELL_CRAFT_SUCCESS, SoundCategory.MASTER, 1, 0.3F);
                });
            }
            super.setStack(stack);
        }

        @Override
        public int getRing() {
            return ring;
        }

        @Override
        public boolean isEnabled() {
           return canShowSlots.test(SlotType.CRAFTING) && hasStack();
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            Pony pony = Pony.of(player);
            InventoryUtil.stream(input).forEach(s -> {
                pony.getDiscoveries().unlock(s.getItem());
            });
           //gemSlot.setStack(ItemStack.EMPTY);
            super.onTakeItem(player, stack);
        }
    }

    public enum SlotType {
        INVENTORY,
        CRAFTING
    }
}
