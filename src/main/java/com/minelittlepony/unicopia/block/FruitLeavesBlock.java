package com.minelittlepony.unicopia.block;

import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.SpeciesList;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.TemperatureGroup;

public class FruitLeavesBlock extends LeavesBlock implements IColourful {

    public static final BooleanProperty HEAVY = BooleanProperty.of("heavy");

    private boolean hardy;

    private int baseGrowthChance;
    private int customTint;

    private Function<World, ItemStack> fruitProducer = w -> ItemStack.EMPTY;
    private Function<World, ItemStack> compostProducer = w -> ItemStack.EMPTY;

    public FruitLeavesBlock() {
        super(FabricBlockSettings.of(Material.LEAVES)
                .strength(0.2F, 0.2F)
                .ticksRandomly()
                .sounds(BlockSoundGroup.GRASS)
                .build()
        );

        setDefaultState(stateFactory.getDefaultState()
            .with(HEAVY, false)
            .with(DISTANCE, 7)
            .with(PERSISTENT, false)
        );
    }

    public FruitLeavesBlock hardy(boolean value) {
        hardy = value;
        return this;
    }

    public FruitLeavesBlock fruit(@Nonnull Function<World, ItemStack> producer) {
        fruitProducer = producer;
        return this;
    }

    public FruitLeavesBlock compost(@Nonnull Function<World, ItemStack> producer) {
        compostProducer = producer;
        return this;
    }

    public FruitLeavesBlock growthChance(int chance) {
        baseGrowthChance = chance;
        return this;
    }

    public FruitLeavesBlock tint(int tint) {
        customTint = tint;
        return this;
    }

    @Override
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);

        if (SpeciesList.instance().getPlayer(player).getSpecies().canUseEarth()) {
            if (stack.isEmpty()) {

                if (state.get(HEAVY)) {
                    dropContents(world, pos, state, 0);
                }
            } else if (stack.getItem() instanceof DyeItem && ((DyeItem)stack.getItem()).getColor() == DyeColor.WHITE) {
                if (!state.get(HEAVY)) {
                    world.setBlockState(pos, state.with(HEAVY, true));

                    if (!world.isClient) {
                        world.playGlobalEvent(2005, pos, 0);
                    }

                    if (!player.abilities.creativeMode) {
                        stack.decrement(1);
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (!world.isClient && world.isBlockLoaded(pos) && !state.get(PERSISTENT)) {
            int growthChance = getGrowthChance(world, pos, state);

            if (!state.get(HEAVY) && (growthChance <= 0 || rand.nextInt(growthChance) == 0)) {
                world.setBlockState(pos, state.with(HEAVY, true));
            } else {
                growthChance /= 10;

                if (state.get(HEAVY) && (growthChance <= 0 || rand.nextInt(growthChance) == 0)) {
                    dropContents(world, pos, state, 0);
                } else {
                    super.onScheduledTick(state, world, pos, rand);
                }
            }
        }
    }

    protected int getGrowthChance(World world, BlockPos pos, BlockState state) {
        int chance = baseGrowthChance;

        if (!hardy && !world.isDaylight()) {
            chance *= 40;
        }

        if (world.getLightLevel(pos) >= 4) {
            chance /= 3;
        }

        TemperatureGroup temp = world.getBiome(pos).getTemperatureGroup();

        if (!hardy && temp == TemperatureGroup.COLD) {
            chance *= 1000;
        }

        if (temp == TemperatureGroup.WARM) {
            chance /= 100;
        }

        if (temp == TemperatureGroup.MEDIUM) {
            chance /= 50;
        }

        return chance;
    }

    @Override
    public int getCustomTint(BlockState state, int tint) {
        return customTint;
    }

    protected void dropContents(World world, BlockPos pos, BlockState state, int chance) {
        Function<World, ItemStack> fruit = world.random.nextInt(40) == 0 ? compostProducer : fruitProducer;
        dropStack(world, pos, fruit.apply(world));

        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_PLACE, SoundCategory.BLOCKS, 0.3F, 1);
        world.setBlockState(pos, state.with(HEAVY, false));
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HEAVY);
    }
}
