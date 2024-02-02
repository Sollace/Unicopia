package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import com.minelittlepony.unicopia.util.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;

public class GoldenOakLeavesBlock extends FruitBearingBlock {
    private static final MapCodec<GoldenOakLeavesBlock> CODEC = RecordCodecBuilder.<GoldenOakLeavesBlock>mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("overlay").forGetter(b -> b.overlay),
            CodecUtils.supplierOf(Registries.BLOCK.getCodec()).fieldOf("fruit").forGetter(b -> b.fruit),
            CodecUtils.supplierOf(ItemStack.CODEC).fieldOf("rotten_fruit").forGetter(b -> b.rottenFruitSupplier),
            BedBlock.createSettingsCodec()
    ).apply(instance, GoldenOakLeavesBlock::new));

    public GoldenOakLeavesBlock(int overlay, Supplier<Block> fruit, Supplier<ItemStack> rottenFruitSupplier, Settings settings) {
        super(overlay, fruit, rottenFruitSupplier, settings);
    }

    @Override
    public MapCodec<? extends GoldenOakLeavesBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean shouldAdvance(Random random) {
        return random.nextInt(1000) == 0;
    }

    @Override
    protected BlockState getPlacedFruitState(Random random) {
        return super.getPlacedFruitState(random).with(EnchantedFruitBlock.ENCHANTED, random.nextInt(1000) == 0);
    }
}
