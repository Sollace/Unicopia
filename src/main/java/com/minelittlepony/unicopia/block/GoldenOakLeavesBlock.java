package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

public class GoldenOakLeavesBlock extends FruitBearingBlock {

    public GoldenOakLeavesBlock(Settings settings, int overlay, Supplier<Block> fruit,
            Supplier<ItemStack> rottenFruitSupplier) {
        super(settings, overlay, fruit, rottenFruitSupplier);
    }

    @Override
    protected boolean shouldAdvance(Random random) {
        return random.nextInt(1000) == 0;
    }

    @Override
    public BlockState getPlacedFruitState(Random random) {
        return super.getPlacedFruitState(random).with(EnchantedFruitBlock.ENCHANTED, random.nextInt(1000) == 0);
    }
}
