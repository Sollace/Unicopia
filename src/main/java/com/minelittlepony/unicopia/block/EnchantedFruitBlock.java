package com.minelittlepony.unicopia.block;

import java.util.Set;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class EnchantedFruitBlock extends FruitBlock {
    public static final BooleanProperty ENCHANTED = BooleanProperty.of("enchanted");

    private static final MapCodec<EnchantedFruitBlock> CODEC = createCodec(EnchantedFruitBlock::new);

    public EnchantedFruitBlock(Direction attachmentFace, Block stem, RegistryKey<Item> fruitKey, VoxelShape shape, boolean flammable, Settings settings) {
        this(attachmentFace, Set.of(stem), fruitKey, shape, flammable, settings);
    }

    public EnchantedFruitBlock(Direction attachmentFace, Set<Block> stem, RegistryKey<Item> fruitKey, VoxelShape shape, boolean flammable, Settings settings) {
        super(attachmentFace, stem, fruitKey, shape, flammable, settings);
        setDefaultState(getDefaultState().with(ENCHANTED, false));
    }

    @Override
    public MapCodec<? extends FruitBlock> getCodec() {
        return CODEC;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return (state.get(ENCHANTED) ? Items.ENCHANTED_GOLDEN_APPLE : Items.GOLDEN_APPLE).getDefaultStack();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (state.get(ENCHANTED)) {
            ParticleUtil.spawnParticle(world, pos, random, ParticleTypes.ELECTRIC_SPARK);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(ENCHANTED);
    }
}
