package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.container.inventory.*;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.trinkets.TrinketsDelegate;
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

        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventory, i, 121 + i * 18, 195));
        }
        for (int i = 0; i < PlayerInventory.MAIN_SIZE - 9; ++i) {
            int x = i % 5;
            int y = i / 5;
            addSlot(new InventorySlot(this, inventory, i + 9, 225 + x * 20, 50 + y * 20));
        }

        for (int i = 0; i < 4; i++) {
            final EquipmentSlot eq = EquipmentSlot.values()[5 - i];
            addSlot(new InventorySlot(this, inventory, PlayerInventory.OFF_HAND_SLOT - i - 1, 340, 50 + (i * 20)) {
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

        TrinketsDelegate.getInstance().createSlot(this, inv.player, TrinketsDelegate.FACE, 0, 340 + 20, 60).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance().createSlot(this, inv.player, TrinketsDelegate.NECKLACE, 0, 340 + 20, 60 + 20).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance().createSlot(this, inv.player, TrinketsDelegate.MAINHAND, 0, 350 - 20, 170).ifPresent(this::addSlot);
        TrinketsDelegate.getInstance().createSlot(this, inv.player, TrinketsDelegate.OFFHAND, 0, 330 + 20, 170).ifPresent(this::addSlot);

        addSlot(new InventorySlot(this, inventory, PlayerInventory.OFF_HAND_SLOT, 340, 150) {
            @Override
            public Pair<Identifier, Identifier> getBackgroundSprite() {
                return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT);
            }
        });

        addSlot(outputSlot = new OutputSlot(this, inventory.player, input, result, 0, gemPos.get(0)));

        onContentChanged(input);
    }

    public SpellbookState getSpellbookState() {
        return state;
    }

    public void addSlotShowingCondition(Predicate<SlotType> canShowSlots) {
        this.canShowSlots = canShowSlots;
    }

    public boolean canShowSlots(SlotType type) {
        return canShowSlots == null || canShowSlots.test(type);
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
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot sourceSlot = slots.get(index);

        if (sourceSlot == null || !sourceSlot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack transferredStack = sourceSlot.getStack();
        ItemStack stack = transferredStack.copy();

        if (sourceSlot instanceof OutputSlot result) {
            result.onTakeItem(player, stack);
        }

        if (index >= HOTBAR_START && !(sourceSlot instanceof OutputSlot || sourceSlot instanceof InputSlot)) {
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
