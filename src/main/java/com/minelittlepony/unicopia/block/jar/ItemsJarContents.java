package com.minelittlepony.unicopia.block.jar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.block.ItemJarBlock.JarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.TileData;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.mixin.MixinEntityBucketItem;
import com.minelittlepony.unicopia.util.FluidHelper;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;

public record ItemsJarContents (
        TileData tile,
        List<ItemStack> stacks
    ) implements JarContents, SidedInventory {
    private static final int MAX_SIZE = 16;
    private static final int[] SLOTS = IntStream.range(0, MAX_SIZE).toArray();
    private static final Codec<List<ItemStack>> STACKS_CODEC = ItemStack.CODEC.listOf(0, MAX_SIZE);

    public ItemsJarContents(TileData tile) {
        this(tile, new ArrayList<>(MAX_SIZE));
    }

    public ItemsJarContents(TileData tile, NbtCompound compound, WrapperLookup lookup) {
        this(tile, new ArrayList<>(NbtSerialisable.decode(STACKS_CODEC, compound.get("items"), lookup).orElse(List.of())));
    }

    @Override
    public TypedActionResult<JarContents> interact(PlayerEntity player, Hand hand) {
        ItemStack handStack = player.getStackInHand(hand);

        if (handStack.isEmpty()) {
            if (stacks.isEmpty()) {
                return TypedActionResult.fail(this);
            }
            Block.dropStack(tile.getWorld(), tile.getPos(), stacks.remove(0));
            markDirty();
            return TypedActionResult.success(this);
        }

        if (stacks.isEmpty()) {
            if (handStack.getItem() instanceof MixinEntityBucketItem bucket) {
                consumeAndSwap(player, hand, Items.BUCKET.getDefaultStack());
                player.playSound(bucket.getEmptyingSound(), 1, 1);
                markDirty();
                return TypedActionResult.success(new EntityJarContents(tile, bucket.getEntityType()));
            }

            Pair<Long, FluidVariant> fluid = FluidHelper.extract(handStack, player, hand).orElse(null);
            if (fluid != null) {
                fluid.getRight().getFluid().getBucketFillSound().ifPresent(sound -> player.playSound(sound, 1, 1));
                markDirty();
                return TypedActionResult.success(new FluidOnlyJarContents(tile, fluid.getLeft(), fluid.getRight()));
            }

            if (handStack.isOf(Items.MILK_BUCKET)) {
                consumeAndSwap(player, hand, handStack.getRecipeRemainder());
                player.playSound(USounds.Vanilla.ITEM_BUCKET_EMPTY, 1, 1);
                markDirty();
                return TypedActionResult.success(new FakeFluidJarContents(tile, "milk", 0xFFFFFFFF, Items.BUCKET, Items.MILK_BUCKET));
            }

            if (handStack.isOf(Items.POWDER_SNOW_BUCKET)) {
                consumeAndSwap(player, hand, Items.BUCKET.getDefaultStack());
                player.playSound(USounds.Vanilla.ITEM_BUCKET_EMPTY_POWDER_SNOW, 1, 1);
                markDirty();
                return TypedActionResult.success(new FakeFluidJarContents(tile, "powder_snow", 0xFFFFFFFF, Items.BUCKET, Items.POWDER_SNOW_BUCKET));
            }

            if (handStack.isOf(UItems.LOVE_BUCKET)) {
                consumeAndSwap(player, hand, handStack.getRecipeRemainder());
                player.playSound(USounds.Vanilla.ITEM_BUCKET_EMPTY, 1, 1);
                markDirty();
                return TypedActionResult.success(new FakeFluidJarContents(tile, "love", 0xFF3030, Items.BUCKET, UItems.LOVE_BUCKET));
            }

            if (handStack.isOf(UItems.JUICE)) {
                consumeAndSwap(player, hand, handStack.getRecipeRemainder());
                player.playSound(USounds.Vanilla.ITEM_BUCKET_EMPTY, 1, 1);
                markDirty();
                return TypedActionResult.success(new FakeFluidJarContents(tile, "apple_juice", 0x30FF30, Items.GLASS_BOTTLE, UItems.JUICE));
            }
        }

        if (stacks.size() >= size()) {
            return TypedActionResult.fail(this);
        }
        stacks.add(player.isCreative() ? handStack.copyWithCount(1) : handStack.split(1));
        markDirty();

        return TypedActionResult.success(this);
    }

    @Override
    public void onDestroyed() {
        stacks.forEach(stack -> Block.dropStack(tile.getWorld(), tile.getPos(), stack));
    }

    @Override
    public int size() {
        return MAX_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return slot < 0 || slot >= stacks.size() ? ItemStack.EMPTY : stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot < 0 || slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }

        try {
            ItemStack stack = stacks.get(slot);
            ItemStack removed = stack.split(1);
            if (stack.isEmpty()) {
                stacks.remove(slot);
            }
            return removed;
        } finally {
            markDirty();
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            return ItemStack.EMPTY;
        }

        try {
            return stacks.remove(slot);
        } finally {
            markDirty();
        }
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (slot >= stacks.size()) {
            stacks.add(stack);
        } else {
            stacks.set(slot, stack);
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        stacks.clear();
        markDirty();
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < size() && slot >= stacks.size();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < size() && slot < stacks.size();
    }

    @Override
    public NbtCompound toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.put("items", NbtSerialisable.encode(STACKS_CODEC, stacks, lookup));
        return compound;
    }

    @Override
    public void markDirty() {
        tile.markDirty();
    }
}
