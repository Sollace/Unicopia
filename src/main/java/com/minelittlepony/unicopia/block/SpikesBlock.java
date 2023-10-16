package com.minelittlepony.unicopia.block;

import org.joml.Vector3f;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SpikesBlock extends OrientedBlock {
    public SpikesBlock(Settings settings) {
        super(settings);
    }

    @Deprecated
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) {
            return;
        }

        if (!world.isClient) {
            Vec3d vel = entity.getVelocity().add(entity.getX() - entity.lastRenderX, entity.getY() - entity.lastRenderY, entity.getZ() - entity.lastRenderZ);
            Vector3f normVel = state.get(FACING).getUnitVector().mul(vel.toVector3f());

            if ((normVel.x + normVel.y + normVel.z) < -0.08F) {
                float damage = (float)vel.lengthSquared() * 26;
                entity.damage(world.getDamageSources().cactus(), damage);
            }
        }
    }

    @Deprecated
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient && !oldState.isOf(this)) {
            for (Entity e : world.getOtherEntities(null, new Box(pos))) {
                if (!(e instanceof LivingEntity) || e.getType() == EntityType.FOX || e.getType() == EntityType.BEE) {
                    continue;
                }
                e.damage(world.getDamageSources().cactus(), 6);
            }
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        pos = pos.offset(facing.getOpposite());
        state = world.getBlockState(pos);
        return state.isReplaceable() || state.isSideSolid(world, pos, facing, SideShapeType.FULL) || state.isOf(Blocks.HONEY_BLOCK);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction side = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(ctx.getSide().getOpposite())).isReplaceable() ? Direction.UP : ctx.getSide();
        return getDefaultState().with(FACING, side);
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.get(FACING).getOpposite() && !canPlaceAt(state, world, pos)) {
            if (!(neighborState.isOf(Blocks.STICKY_PISTON)
                    || neighborState.isOf(Blocks.PISTON)
                    || neighborState.isOf(Blocks.PISTON_HEAD)
                    || neighborState.isOf(Blocks.MOVING_PISTON))) {

                return Blocks.AIR.getDefaultState();
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
