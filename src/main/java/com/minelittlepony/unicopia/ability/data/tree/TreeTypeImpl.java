package com.minelittlepony.unicopia.ability.data.tree;

import com.minelittlepony.unicopia.util.Weighted;

import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class TreeTypeImpl implements TreeType {
    private final Identifier name;
    private final boolean wideTrunk;
    private final Set<Identifier> logs;
    private final Set<Identifier> leaves;
    private final Weighted<Supplier<ItemStack>> pool;

    TreeTypeImpl(Identifier name, boolean wideTrunk, Weighted<Supplier<ItemStack>> pool, Set<Identifier> logs, Set<Identifier> leaves) {
        this.name = name;
        this.wideTrunk = wideTrunk;
        this.pool = pool;
        this.logs = logs;
        this.leaves = leaves;
    }

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

    @Override
    public boolean equals(Object o) {
        return o instanceof TreeTypeImpl && name.compareTo(((TreeTypeImpl)o).name) == 0;
    }

    private static boolean findMatch(Set<Identifier> ids, BlockState state) {
        return ids.contains(Registry.BLOCK.getId(state.getBlock()));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    static boolean isNonPersistent(BlockState state) {
        return !state.contains(LeavesBlock.PERSISTENT) || !state.get(LeavesBlock.PERSISTENT);
    }
}
