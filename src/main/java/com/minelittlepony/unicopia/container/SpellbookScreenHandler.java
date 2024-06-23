package com.minelittlepony.unicopia.container;

import java.util.*;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.container.inventory.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.mojang.datafixers.util.Pair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class SpellbookScreenHandler extends ScreenHandler {
    private static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{
            PlayerScreenHandler.EMPTY_BOOTS_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_LEGGINGS_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_CHESTPLATE_SLOT_TEXTURE,
            PlayerScreenHandler.EMPTY_HELMET_SLOT_TEXTURE
    };

    private final int MAX_INGREDIENTS;

    public final int GEM_SLOT_INDEX;
    private final int HOTBAR_START;
    private final int HOTBAR_END;

    private final SpellbookInventory input;

    public InputSlot gemSlot;
    public OutputSlot outputSlot;
    private final CraftingResultInventory result = new CraftingResultInventory();

    private final PlayerInventory inventory;

    private final ScreenHandlerContext context;

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

        List<HexagonalCraftingGrid.Slot> grid = new ArrayList<>();
        List<HexagonalCraftingGrid.Slot> gemPos = new ArrayList<>();
        HexagonalCraftingGrid.create(34, 65, 3, grid, gemPos);

        MAX_INGREDIENTS = grid.size();
        GEM_SLOT_INDEX = MAX_INGREDIENTS;
        HOTBAR_START = GEM_SLOT_INDEX + 1;
        HOTBAR_END = HOTBAR_START + 9;

        input = new SpellbookInventory(this, MAX_INGREDIENTS + 1, 1);

        for (int i = 0; i < MAX_INGREDIENTS; i++) {
            var pos = grid.get(i);
            addSlot(new IngredientSlot(this, input, i, pos));
        }

        addSlot(gemSlot = new InputSlot(this, input, MAX_INGREDIENTS, gemPos.get(0)));

        final int slotSpacing = 18;
        final int halfSpacing = slotSpacing / 2;

        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 121 + i * slotSpacing, 195));
        }

        final int inventoryX = 225;
        final int inventoryY = 45;

        for (int i = 0; i < PlayerInventory.MAIN_SIZE - 9; ++i) {
            int x = i % 4;
            int y = i / 4;
            addSlot(new InventorySlot(this, inventory, i + 9, inventoryX + x * slotSpacing, inventoryY + y * slotSpacing));
        }


        final int armorX = 330;
        final int armorY = inventoryY + slotSpacing;
        final int equipmentY = armorY + halfSpacing;
        final int leftHandX = armorX - slotSpacing;
        final int rightHandX = armorX + slotSpacing;

        for (int i = 0; i < 4; i++) {
            final EquipmentSlot eq = EquipmentSlot.values()[5 - i];
            addSlot(new InventorySlot(this, inventory, PlayerInventory.OFF_HAND_SLOT - i - 1, armorX, armorY + (i * slotSpacing)) {
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
        addSlot(new InventorySlot(this, inventory, PlayerInventory.OFF_HAND_SLOT, rightHandX, equipmentY + slotSpacing) {
            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT);
            }
        });

        TrinketsDelegate.getInstance(inv.player).createSlot(this, inv.player, TrinketsDelegate.FACE, 0, rightHandX, inventoryY + slotSpacing * 6).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance(inv.player).createSlot(this, inv.player, TrinketsDelegate.NECKLACE, 0, leftHandX, equipmentY + slotSpacing).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance(inv.player).createSlot(this, inv.player, TrinketsDelegate.MAIN_GLOVE, 0, leftHandX, equipmentY).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance(inv.player).createSlot(this, inv.player, TrinketsDelegate.SECONDARY_GLOVE, 0, rightHandX, equipmentY).ifPresent(this::addSlot);

        addSlot(outputSlot = new OutputSlot(this, inventory.player, input, result, 0, gemPos.get(0)));

        addSlot(new SpellSlot(this, Pony.of(inventory.player), Hand.MAIN_HAND, inventory, 0, leftHandX, equipmentY - slotSpacing));
        addSlot(new SpellSlot(this, Pony.of(inventory.player), Hand.OFF_HAND, inventory, 0, rightHandX, equipmentY - slotSpacing));

        onContentChanged(input);
    }

    public SpellbookState getSpellbookState() {
        return state;
    }

    public boolean canShowSlots(SlotType type) {
        Identifier pageId = state.getCurrentPageId().orElse(null);
        boolean isCraftingPage = SpellbookState.CRAFTING_ID.equals(pageId);
        return switch (type) {
            case INVENTORY -> isCraftingPage ? state.getState(pageId).getOffset() == 0 : SpellbookState.PROFILE_ID.equals(pageId);
            case CRAFTING -> isCraftingPage;
        };
    }

    public int getOutputSlotId() {
        return outputSlot.id;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.isAlive();
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
                        .map(recipe -> recipe.craft(input, world.getRegistryManager()))
                        .orElseGet(this::getFallbackStack) : ItemStack.EMPTY;
                outputSlot.setStack(resultStack);

                setPreviousTrackedSlot(outputSlot.id, resultStack);
                ((ServerPlayerEntity)this.inventory.player).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), outputSlot.id, outputSlot.getStack()));
            }
        });
    }

    private ItemStack getFallbackStack() {
        ItemStack gemStack = gemSlot.getStack();
        if (gemStack.isOf(UItems.GEMSTONE) || gemStack.isOf(UItems.BOTCHED_GEM)) {
            return input.getTraits().applyTo(UItems.BOTCHED_GEM.getDefaultStack());
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return slot != null && slot.canInsert(stack) && slot.isEnabled();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot sourceSlot = slots.get(index);

        if (sourceSlot == null || !sourceSlot.hasStack() || (sourceSlot instanceof SpellSlot)) {
            return ItemStack.EMPTY;
        }

        ItemStack transferredStack = sourceSlot.getStack();
        ItemStack stack = transferredStack.copy();

        if (sourceSlot instanceof OutputSlot result) {
            result.onTakeItem(player, stack);
        }

        if (index >= HOTBAR_START && !(sourceSlot instanceof OutputSlot || sourceSlot instanceof InputSlot)) {
            // hotbar or inventory -> crafting grid
            if (canShowSlots(SlotType.CRAFTING)) {
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
            }

            if (index < HOTBAR_END) {
                if (canShowSlots(SlotType.INVENTORY)) {
                    // hotbar -> inventory
                    // insert into inventory - armor
                    if (insertItem(transferredStack, HOTBAR_END + 27, HOTBAR_END + 27 + 4, false)) {
                        sourceSlot.onQuickTransfer(transferredStack, stack);
                        onContentChanged(input);
                        return ItemStack.EMPTY;
                    }

                    // insert into inventory - inventory
                    if (insertItem(transferredStack, HOTBAR_END, HOTBAR_END + 27, false)) {
                        sourceSlot.onQuickTransfer(transferredStack, stack);
                        onContentChanged(input);
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                // inventory -> hotbar
                if (insertItem(transferredStack, HOTBAR_START, HOTBAR_END, true)) {
                    sourceSlot.onQuickTransfer(transferredStack, stack);
                    onContentChanged(input);
                    return ItemStack.EMPTY;
                }
            }
        } else {
            // crafting grid -> hotbar
            if (insertItem(transferredStack, HOTBAR_START, HOTBAR_END, true)) {
                sourceSlot.onQuickTransfer(transferredStack, stack);
                onContentChanged(input);
                return ItemStack.EMPTY;
            }

            if (canShowSlots(SlotType.INVENTORY)) {
                // crafting grid -> armor
                if (insertItem(transferredStack, HOTBAR_END + 27, HOTBAR_END + 27 + 4, false)) {
                    sourceSlot.onQuickTransfer(transferredStack, stack);
                    onContentChanged(input);
                    return ItemStack.EMPTY;
                }

                // crafting grid -> inventory
                if (insertItem(transferredStack, HOTBAR_END, HOTBAR_END + 27, false)) {
                    sourceSlot.onQuickTransfer(transferredStack, stack);
                    onContentChanged(input);
                    return ItemStack.EMPTY;
                }
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
                int available = Math.min(Math.min(current.getMaxCount(), slot.getMaxItemCount(stack)) - current.getCount(), stack.getCount());

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
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        context.run((world, pos) -> {
            dropInventory(player, input);
        });
    }

}
