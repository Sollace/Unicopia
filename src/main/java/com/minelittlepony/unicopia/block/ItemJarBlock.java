package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ItemJarBlock extends JarBlock implements BlockEntityProvider, InventoryProvider {

    public ItemJarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.OFF_HAND) {
            return ActionResult.PASS;
        }
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(data -> {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isEmpty()) {
                return data.removeItem(world, pos);
            }

            return data.insertItem(world, pos, player.isCreative() ? stack.copyWithCount(1) : stack.split(1));
        }).orElse(ActionResult.PASS);
    }

    @Deprecated
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).ifPresent(data -> {
                data.getStacks().forEach(stack -> {
                    dropStack(world, pos, stack);
                });
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).map(data -> Math.min(16, data.getStacks().size())).orElse(0);
    }

    @Deprecated
    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileData(pos, state);
    }


    @Override
    public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
        return world.getBlockEntity(pos, UBlockEntities.ITEM_JAR).orElse(null);
    }

    public static class TileData extends BlockEntity implements SidedInventory {
        private static final int[] SLOTS = IntStream.range(0, 16).toArray();
        private final List<ItemStack> stacks = new ArrayList<>();

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.ITEM_JAR, pos, state);
        }

        public ActionResult insertItem(World world, BlockPos pos, ItemStack stack) {
            if (stacks.size() >= size()) {
                return ActionResult.FAIL;
            }
            stacks.add(stack);
            markDirty();

            return ActionResult.SUCCESS;
        }

        public ActionResult removeItem(World world, BlockPos pos) {
            if (stacks.isEmpty()) {
                return ActionResult.FAIL;
            }
            dropStack(world, pos, stacks.remove(0));
            markDirty();
            return ActionResult.SUCCESS;
        }

        public List<ItemStack> getStacks() {
            return stacks;
        }

        @Override
        public int size() {
            return 15;
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
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeStack(int slot) {
            if (slot < 0 || slot >= stacks.size()) {
                try {
                    return stacks.remove(slot);
                } finally {
                    markDirty();
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot >= stacks.size()) {
                if (stacks.size() >= size()) {
                    dropStack(getWorld(), getPos(), stack);
                } else {
                    stacks.add(stack);
                }
            } else {
                ItemStack existing = stacks.get(slot);
                if (!ItemStack.canCombine(existing, stack)) {
                    dropStack(getWorld(), getPos(), stack);
                } else {
                    existing.setCount(existing.getCount() + stack.split(Math.max(0, existing.getMaxCount() - existing.getCount())).getCount());
                    if (!stack.isEmpty()) {
                        dropStack(getWorld(), getPos(), stack);
                    }
                }
            }
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
            return (slot >= 0 && slot < size()) && (slot >= stacks.size() || (
                    ItemStack.canCombine(stacks.get(slot), stack)
                    && (stacks.get(slot).getCount() + stack.getCount()) <= Math.min(stacks.get(slot).getMaxCount(), stack.getMaxCount())
            ));
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return true;
        }
    }
}
