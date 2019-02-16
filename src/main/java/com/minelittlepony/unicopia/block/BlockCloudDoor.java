package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.init.UItems;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloudDoor extends BlockDoor implements ICloudBlock {

    public BlockCloudDoor(Material material, String domain, String name) {
        super(material);

        setTranslationKey(name);
        setRegistryName(domain, name);
        setSoundType(SoundType.CLOTH);
        setHardness(1);
        setResistance(200);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return blockMapColor;
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return super.isAir(state, world, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!getCanInteract(state, player)) {
            return false;
        }

        setHardness(3);
        setResistance(200);

        return super.onBlockActivated(worldIn, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        return "shovel";
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return 0;
    }

    @Deprecated
    @Override
    public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
        float hardness = super.getBlockHardness(blockState, world, pos);

        return Math.max(hardness, Math.min(60, hardness + (pos.getY() - 100)));
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public void onEntityCollision(World w, BlockPos pos, IBlockState state, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(w, pos, state, entity);
        }
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return getCanInteract(state, entity) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? Items.AIR : UItems.mist_door;
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        if (CloudType.NORMAL.canInteract(player)) {
            return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
        }
        return -1;
    }

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return CloudType.NORMAL;
    }
}
