package com.minelittlepony.unicopia.block.cloud;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface Soakable {
    IntProperty MOISTURE = IntProperty.of("moisture", 1, 7);
    Direction[] DIRECTIONS = Arrays.stream(Direction.values()).filter(d -> d != Direction.UP).toArray(Direction[]::new);

    BlockState getSoggyState(int moisture);

    int getMoisture(BlockState state);

    static void addMoistureParticles(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(5) == 0) {
            world.addParticle(ParticleTypes.DRIPPING_WATER,
                    pos.getX() + random.nextFloat(),
                    pos.getY(),
                    pos.getZ() + random.nextFloat(),
                    0, 0, 0
            );
        }
    }

    static ActionResult tryCollectMoisture(Block dryBlock, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() == Items.GLASS_BOTTLE) {
            if (!player.isCreative()) {
                stack.split(1);
            }
            if (stack.isEmpty()) {
                player.setStackInHand(hand, Items.POTION.getDefaultStack());
            } else {
                player.giveItemStack(Items.POTION.getDefaultStack());
            }
            world.playSound(player, player.getX(), player.getY(), player.getZ(), USounds.Vanilla.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1, 1);
            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
            updateMoisture(dryBlock, state, world, pos, state.get(MOISTURE) - 1);

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    static void tickMoisture(Block dryBlock, BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int moisture = state.get(MOISTURE);

        if (world.hasRain(pos) && world.isAir(pos.up())) {
            if (moisture < 7) {
                world.setBlockState(pos, state.with(MOISTURE, moisture + 1));
            }
        } else {
            if (moisture > 1) {
                BlockPos neighborPos = pos.offset(Util.getRandom(Soakable.DIRECTIONS, random));
                BlockState neighborState = world.getBlockState(neighborPos);

                if (neighborState.getBlock() instanceof Soakable soakable && soakable.getMoisture(neighborState) < moisture) {
                    int half = Math.max(1, moisture / 2);
                    @Nullable
                    BlockState newNeighborState = soakable.getSoggyState(half);
                    if (newNeighborState != null) {
                        updateMoisture(dryBlock, state, world, pos, moisture - half);
                        world.setBlockState(neighborPos, soakable.getSoggyState(half));
                        world.emitGameEvent(null, GameEvent.BLOCK_CHANGE, neighborPos);
                        return;
                    }
                }
            }
            updateMoisture(dryBlock, state, world, pos, moisture - 1);
        }
    }

    private static void updateMoisture(Block dryBlock, BlockState state, World world, BlockPos pos, int newMoisture) {
        if (newMoisture <= 0) {
            world.setBlockState(pos, copyProperties(state, dryBlock.getDefaultState()));
        } else {
            world.setBlockState(pos, state.with(MOISTURE, newMoisture));
        }
        world.playSound(null, pos, SoundEvents.ENTITY_SALMON_FLOP, SoundCategory.BLOCKS, 1, (float)world.random.nextTriangular(0.5, 0.3F));
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    static BlockState copyProperties(BlockState from, BlockState to) {
        for (Property property : from.getProperties()) {
            to = to.withIfExists(property, from.get(property));
        }
        return to;
    }
}
