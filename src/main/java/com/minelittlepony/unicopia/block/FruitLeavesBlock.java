package com.minelittlepony.unicopia.block;

import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.ducks.Colourful;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.TemperatureGroup;

public class FruitLeavesBlock extends LeavesBlock implements Colourful {

    public static final BooleanProperty HEAVY = BooleanProperty.of("heavy");

    private boolean hardy;

    private int baseGrowthChance;
    private int customTint;

    private Function<World, ItemStack> fruitProducer = w -> ItemStack.EMPTY;
    private Function<World, ItemStack> compostProducer = w -> ItemStack.EMPTY;

    public FruitLeavesBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState()
            .with(HEAVY, false)
            .with(DISTANCE, 7)
            .with(PERSISTENT, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HEAVY);
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
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);

        if (Pony.of(player).getSpecies().canUseEarth()) {
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

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (!world.isClient && world.isChunkLoaded(pos) && !state.get(PERSISTENT)) {
            int growthChance = getGrowthChance(world, pos, state);

            if (!state.get(HEAVY) && (growthChance <= 0 || rand.nextInt(growthChance) == 0)) {
                world.setBlockState(pos, state.with(HEAVY, true));
            } else {
                growthChance /= 10;

                if (state.get(HEAVY) && (growthChance <= 0 || rand.nextInt(growthChance) == 0)) {
                    dropContents(world, pos, state, 0);
                } else {
                    super.scheduledTick(state, world, pos, rand);
                }
            }
        }
    }

    protected int getGrowthChance(World world, BlockPos pos, BlockState state) {
        int chance = baseGrowthChance;

        if (!hardy && !world.isDay()) {
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
}
