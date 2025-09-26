package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

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
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (random.nextInt(15) == 0) {
            ParticleUtil.spawnParticlesAround(world, pos, 1 + random.nextInt(4), ParticleTypes.ELECTRIC_SPARK);
        }
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
