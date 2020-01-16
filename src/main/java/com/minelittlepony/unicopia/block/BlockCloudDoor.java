package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockCloudDoor extends UDoor implements ICloudBlock {

    public BlockCloudDoor(Material material, String domain, String name, Supplier<Item> theItem) {
        super(material, domain, name, theItem);

        setSoundType(SoundType.CLOTH);
        setHardness(3);
        setResistance(200);
    }

    @Override
    public MapColor getMapColor(BlockState state, BlockView worldIn, BlockPos pos) {
        return blockMapColor;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity player, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!getCanInteract(state, player)) {
            return false;
        }

        return super.onBlockActivated(worldIn, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public String getHarvestTool(BlockState state) {
        return "shovel";
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return 0;
    }

    @Deprecated
    @Override
    public float getBlockHardness(BlockState blockState, World world, BlockPos pos) {
        float hardness = super.getBlockHardness(blockState, world, pos);

        return Math.max(hardness, Math.min(60, hardness + (pos.getY() - 100)));
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public void onEntityCollision(World w, BlockPos pos, BlockState state, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(w, pos, state, entity);
        }
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        return getCanInteract(state, entity) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, World worldIn, BlockPos pos) {
        if (CloudType.NORMAL.canInteract(player)) {
            return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
        }
        return -1;
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return CloudType.NORMAL;
    }
}
