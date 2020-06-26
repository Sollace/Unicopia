package com.minelittlepony.unicopia;

import java.util.stream.Collectors;

import com.minelittlepony.unicopia.util.Weighted;
import com.minelittlepony.unicopia.world.item.UItems;

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
    // TODO: move to datapack
    private static final Set<TreeType> REGISTRY = new HashSet<>();

    private static final Supplier<ItemStack> ROTTEN = () -> new ItemStack(UItems.ROTTEN_APPLE);
    private static final Supplier<ItemStack> SWEET = () -> new ItemStack(UItems.SWEET_APPLE);
    private static final Supplier<ItemStack> GREEN = () -> new ItemStack(UItems.GREEN_APPLE);
    private static final Supplier<ItemStack> ZAP = () -> new ItemStack(UItems.ZAP_APPLE);
    private static final Supplier<ItemStack> SOUR = () -> new ItemStack(UItems.SOUR_APPLE);
    private static final Supplier<ItemStack> RED = () -> new ItemStack(Items.APPLE);

    public static final TreeType NONE = new TreeType("none", new Weighted<Supplier<ItemStack>>());
    public static final TreeType OAK = new TreeType("oak", new Weighted<Supplier<ItemStack>>()
            .put(1, ROTTEN)
            .put(2, GREEN)
            .put(3, RED), Blocks.OAK_LOG, Blocks.OAK_LEAVES);
    public static final TreeType BIRCH = new TreeType("birch", new Weighted<Supplier<ItemStack>>()
            .put(1, ROTTEN)
            .put(2, SWEET)
            .put(5, GREEN), Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES);
    public static final TreeType SPRUCE = new TreeType("spruce", new Weighted<Supplier<ItemStack>>()
            .put(1, SOUR)
            .put(2, GREEN)
            .put(3, SWEET)
            .put(4, ROTTEN), Blocks.SPRUCE_LOG, Blocks.SPRUCE_LEAVES);
    public static final TreeType ACACIA = new TreeType("acacia", new Weighted<Supplier<ItemStack>>()
            .put(1, ROTTEN)
            .put(2, SWEET)
            .put(5, GREEN), Blocks.ACACIA_LOG, Blocks.ACACIA_LEAVES);
    public static final TreeType JUNGLE = new TreeType("jungle", new Weighted<Supplier<ItemStack>>()
            .put(5, GREEN)
            .put(2, SWEET)
            .put(1, ZAP), Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES);
    public static final TreeType DARK_OAK = new TreeType("dark_oak", new Weighted<Supplier<ItemStack>>()
            .put(1, ROTTEN)
            .put(2, SWEET)
            .put(5, ZAP), Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES);

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
