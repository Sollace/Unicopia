package com.minelittlepony.unicopia.redux.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.redux.CloudType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudBanisterBlock extends CloudFenceBlock {

    public static final float l = 11;
    public static final float d = 5;
    public static final float h = 2;

    public static final Box SOUTH_AABB = new Box(d, 0, l, l, h, 1);
    public static final Box WEST_AABB = new Box(0, 0, d, d, h, l);
    public static final Box NORTH_AABB = new Box(d, 0, 0, l, h, d);
    public static final Box EAST_AABB = new Box(l, 0, d, 1, h, l);

    public CloudBanisterBlock(Material material) {
        super(material, CloudType.ENCHANTED);
        this.collisionShapes = this.createShapes(l, d, h, 0.0F, d);
        this.boundingShapes = this.createShapes(l, d, h, 0.0F, d);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canConnect(BlockState myState, boolean bool, Direction facing) {
        return myState.getBlock() instanceof CloudBanisterBlock
                && super.canConnect(myState, bool, facing);
    }

    @Override
    public boolean canConnect(BlockView world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos.offset(facing));

        return state.getBlock() instanceof CloudBanisterBlock
                && state.getMaterial() == world.getBlockState(pos).getMaterial();
    }
}
