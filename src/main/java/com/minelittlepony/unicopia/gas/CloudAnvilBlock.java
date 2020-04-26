package com.minelittlepony.unicopia.gas;

import java.util.Random;

import com.minelittlepony.unicopia.util.WorldEvent;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tools.FabricToolTags;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudAnvilBlock extends AnvilBlock implements Gas {
    public CloudAnvilBlock() {
        super(FabricBlockSettings.of(Material.WOOL)
                .strength(0.025F, 1)
                .resistance(2000)
                .breakByTool(FabricToolTags.SHOVELS, 0)
                .sounds(BlockSoundGroup.WOOL)
                .ticksRandomly()
                .build()
        );
    }

    @Override
    public void onLandedUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!applyLanding(entityIn, fallDistance)) {
            super.onLandedUpon(world, pos, entityIn, fallDistance);
        }
    }

    @Override
    public void onLanding(World world, BlockPos pos, BlockState fallingState, BlockState hitState) {
        WorldEvent.ENTITY_TAKEOFF.play(world, pos);
    }

    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos) {
        WorldEvent.ENTITY_TAKEOFF.play(world, pos);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (!applyRebound(entity)) {
            super.onEntityLand(world, entity);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        BlockState below = world.getBlockState(pos.down());

        if (below.getBlock() instanceof Gas) {
            if (((Gas)below.getBlock()).getGasType(below).isDense()) {
                return;
            }
        }

        super.scheduledTick(state, world, pos, rand);
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(state, w, pos, entity);
        }
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (!CloudType.NORMAL.canInteract(player)) {
            return -1;
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }


    @Override
    public CloudType getGasType(BlockState blockState) {
        return CloudType.NORMAL;
    }
}
