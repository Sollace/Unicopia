package com.minelittlepony.unicopia;

import java.util.stream.Collectors;

import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.Weighted;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class TreeType {
    // XXX: move to datapack
    private static final Set<TreeType> REGISTRY = new HashSet<>();

    public static final TreeType NONE = new TreeType("none", new Weighted<Supplier<ItemStack>>());
    public static final TreeType OAK = new TreeType("oak", new Weighted<Supplier<ItemStack>>()
            .put(1, () -> new ItemStack(UItems.ROTTEN_APPLE))
            .put(2, () -> new ItemStack(UItems.GREEN_APPLE))
            .put(3, () -> new ItemStack(Items.APPLE)), Blocks.OAK_LOG, Blocks.OAK_LEAVES);
    public static final TreeType BIRCH = new TreeType("birch", new Weighted<Supplier<ItemStack>>()
            .put(1, () -> new ItemStack(UItems.ROTTEN_APPLE))
            .put(2, () -> new ItemStack(UItems.SWEET_APPLE))
            .put(5, () -> new ItemStack(UItems.GREEN_APPLE)), Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES);
    public static final TreeType SPRUCE = new TreeType("spruce", new Weighted<Supplier<ItemStack>>()
            .put(1, () -> new ItemStack(UItems.SOUR_APPLE))
            .put(2, () -> new ItemStack(UItems.GREEN_APPLE))
            .put(3, () -> new ItemStack(UItems.SWEET_APPLE))
            .put(4, () -> new ItemStack(UItems.ROTTEN_APPLE)), Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES);
    public static final TreeType ACACIA = new TreeType("acacia", new Weighted<Supplier<ItemStack>>()
            .put(1, () -> new ItemStack(UItems.ROTTEN_APPLE))
            .put(2, () -> new ItemStack(UItems.SWEET_APPLE))
            .put(5, () -> new ItemStack(UItems.GREEN_APPLE)), Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES);
    public static final TreeType JUNGLE = new TreeType("jungle", new Weighted<Supplier<ItemStack>>()
            .put(5, () -> new ItemStack(UItems.GREEN_APPLE))
            .put(2, () -> new ItemStack(UItems.SWEET_APPLE))
            .put(1, () -> new ItemStack(UItems.SOUR_APPLE)), Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES);
    public static final TreeType DARK_OAK = new TreeType("dark_oak", new Weighted<Supplier<ItemStack>>()
            .put(1, () -> new ItemStack(UItems.ROTTEN_APPLE))
            .put(2, () -> new ItemStack(UItems.SWEET_APPLE))
            .put(5, () -> new ItemStack(UItems.ZAP_APPLE)), Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES);

    private final String name;
    private final Set<Identifier> blocks;
    private final Weighted<Supplier<ItemStack>> pool;

    private TreeType(String name, Weighted<Supplier<ItemStack>> pool, Block...blocks) {
        this.name = name;
        this.pool = pool;
        this.blocks = Arrays.stream(blocks).map(Registry.BLOCK::getId)
                .collect(Collectors.toSet());
        REGISTRY.add(this);
    }

    public boolean matches(BlockState state) {
        return blocks.contains(Registry.BLOCK.getId(state.getBlock()));
    }

    public ItemStack pickRandomStack() {
        return pool.get().map(Supplier::get).orElse(ItemStack.EMPTY);
    }

    public static TreeType get(BlockState state) {
        return REGISTRY.stream().filter(type -> type.matches(state)).findFirst().orElse(TreeType.NONE);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TreeType && name.compareTo(((TreeType)o).name) == 0;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
