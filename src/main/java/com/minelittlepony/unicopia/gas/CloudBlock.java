package com.minelittlepony.unicopia.gas;

import java.util.Random;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.blockstate.StateMaps;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.HoeUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudBlock extends Block implements Gas, HoeUtil.Tillable {

    private final CloudType variant;

    public CloudBlock(CloudType variant) {
        super(variant.configure()
                .ticksRandomly()
                .build()
        );
        this.variant = variant;
        HoeUtil.registerTillingAction(this, UBlocks.CLOUD_FARMLAND.getDefaultState());
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return variant == CloudType.NORMAL;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (rand.nextInt(10) == 0) {
            pos = pos.offset(Direction.random(rand), 1 + rand.nextInt(2));
            state = world.getBlockState(pos);

            BlockState converted = StateMaps.MOSS_AFFECTED.getInverse().getConverted(state);

            if (!state.equals(converted)) {
                world.setBlockState(pos, converted);
            }
        }
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {
        if (beside.getBlock() instanceof Gas) {
            Gas cloud = ((Gas)beside.getBlock());

            if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                return true;
            }
        }

        return super.isSideInvisible(state, beside, face);
    }

    @Override
    public void onLandedUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!applyLanding(entityIn, fallDistance)) {
            super.onLandedUpon(world, pos, entityIn, fallDistance);
        }
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (!applyRebound(entity)) {
            super.onEntityLand(world, entity);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(state, w, pos, entity);
        }
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView worldIn, BlockPos pos) {
        if (CloudType.NORMAL.canInteract(player)) {
            return super.calcBlockBreakingDelta(state, player, worldIn, pos);
        }
        return -1;
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return variant;
    }

    @Override
    public boolean canTill(ItemUsageContext context) {
        return context.getPlayer() == null || Pony.of(context.getPlayer()).getSpecies().canInteractWithClouds();
    }
}
