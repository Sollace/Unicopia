package com.minelittlepony.unicopia.ability.data.tree;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public record TreeTypeImpl (
        Identifier name,
        boolean wideTrunk,
        Set<Identifier> logs,
        Set<Identifier> leaves,
        Supplier<Optional<Supplier<ItemStack>>> pool
) implements TreeType {
    @Override
    public boolean isLeaves(BlockState state) {
        return findMatch(leaves, state) && isNonPersistent(state);
    }

    @Override
    public boolean isLog(BlockState state) {
        return findMatch(logs, state);
    }

    @Override
    public boolean isWide() {
        return wideTrunk;
    }

    @Override
    public ItemStack pickRandomStack(BlockState state) {
        return pool.get().map(Supplier::get).orElse(ItemStack.EMPTY);
    }

    private static boolean findMatch(Set<Identifier> ids, BlockState state) {
        return ids.contains(Registry.BLOCK.getId(state.getBlock()));
    }

    static boolean isNonPersistent(BlockState state) {
        return !state.contains(LeavesBlock.PERSISTENT) || !state.get(LeavesBlock.PERSISTENT);
    }
}
