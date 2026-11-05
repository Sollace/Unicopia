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
        TreeTypeLoader.TreeTypeDef type,
        Supplier<Optional<Supplier<ItemStack>>> pool
) implements TreeType {
    @Override
    public boolean isLeaves(BlockState state) {
        return findMatch(type.leaves(), state) && isNonPersistent(state);
    }

    @Override
    public boolean isLog(BlockState state) {
        return findMatch(type.logs(), state);
    }

    @Override
    public float leavesRatio() {
        return type.leavesRatio();
    }

    @Override
    public boolean isWide() {
        return type.wideTrunk();
    }

    @Override
    public ItemStack pickRandomStack(Random random, BlockState state) {
        if (type.rarity() <= 0 || random.nextInt(type.rarity()) == 0) {
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
