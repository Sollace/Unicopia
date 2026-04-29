package com.minelittlepony.unicopia.ability.data.tree;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.RegistryKey;

public record TreeTypeImpl (
        Identifier name,
        boolean wideTrunk,
        Set<RegistryKey<Block>> logs,
        Set<RegistryKey<Block>> leaves,
        Supplier<Optional<Supplier<ItemStack>>> pool,
        int rarity,
        float leavesRatio
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
    public ItemStack pickRandomStack(Random random, BlockState state) {
        if (rarity <= 0 || random.nextInt(rarity) == 0) {
            return pool.get().map(Supplier::get).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @SuppressWarnings("deprecation")
    private static boolean findMatch(Set<RegistryKey<Block>> ids, BlockState state) {
        return ids.contains(state.getBlock().getRegistryEntry().registryKey());
    }

    static boolean isNonPersistent(BlockState state) {
        return !state.contains(LeavesBlock.PERSISTENT) || !state.get(LeavesBlock.PERSISTENT);
    }
}
