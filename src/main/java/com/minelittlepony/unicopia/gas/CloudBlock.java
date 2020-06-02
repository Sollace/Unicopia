package com.minelittlepony.unicopia.gas;

import java.util.Random;

import com.minelittlepony.unicopia.blockstate.StateMaps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudBlock extends Block implements Gas {

    private final GasState variant;

    public CloudBlock(GasState variant) {
        super(variant.configure()
                .ticksRandomly()
                .build()
        );
        this.variant = variant;
    }

    @Override
    public GasState getGasState(BlockState state) {
        return variant;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView view, BlockPos pos) {
       return getGasState(state).isTranslucent() ? 0.9F : 0.5F;
    }

    @Override
    public boolean canSuffocate(BlockState state, BlockView view, BlockPos pos) {
       return !getGasState(state).isTranslucent();
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return getGasState(state).isTranslucent();
    }

    @Override
    public boolean isSimpleFullBlock(BlockState state, BlockView view, BlockPos pos) {
        return !getGasState(state).isTranslucent() && super.isSimpleFullBlock(state, view, pos);
    }

    @Override
    public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
        return getGasState(state).isTranslucent();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        if (getGasState(state).canPlace((CloudInteractionContext)context)) {
            return VoxelShapes.fullCube();
        }
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        if (getGasState(state).canTouch((CloudInteractionContext)context)) {
            return collidable ? VoxelShapes.fullCube() : VoxelShapes.empty();
        }
        return VoxelShapes.empty();
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
    @Environment(EnvType.CLIENT)
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {
        return isFaceCoverd(state, beside, face);
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
        if (GasState.NORMAL.canTouch(player)) {
            return super.calcBlockBreakingDelta(state, player, worldIn, pos);
        }
        return -1;
    }
}
