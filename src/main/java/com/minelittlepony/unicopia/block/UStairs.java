package com.minelittlepony.unicopia.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class UStairs extends BlockStairs {

    protected Block theBlock;
    protected IBlockState theState;

    @SuppressWarnings("deprecation")
    public UStairs(IBlockState inherited, String domain, String name) {
        super(inherited);
        setTranslationKey(name);
        setRegistryName(domain, name);
        theBlock = inherited.getBlock();
        theState = inherited;

        setTickRandomly(theBlock.getTickRandomly());

        useNeighborBrightness = true;
    }

    @Override
    @Deprecated
    public boolean isTranslucent(IBlockState state) {
        return theBlock.isTranslucent(state);
    }

    @Override
    @Deprecated
    public boolean isNormalCube(IBlockState state) {
        return theBlock.isNormalCube(state);
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return theBlock.isPassable(worldIn, pos);
    }

    @Override
    public void onFallenUpon(World w, BlockPos pos, Entity entity, float fallDistance) {
        theBlock.onFallenUpon(w, pos, entity, fallDistance);
    }

    @Override
    public void onLanded(World w, Entity entity) {
        theBlock.onLanded(w, entity);
    }

    @Override
    public void onEntityCollision(World w, BlockPos pos, IBlockState state, Entity entity) {
        theBlock.onEntityCollision(w, pos, theState, entity);
    }

    @Override
    public void onEntityWalk(World w, BlockPos pos, Entity entity) {
        theBlock.onEntityWalk(w, pos, entity);
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return theBlock.canEntityDestroy(state, world, pos, entity);
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        return theBlock.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }
}
