package com.minelittlepony.unicopia.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class BlockStick extends Block implements IPlantable {

    static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(
            7/16F, -1/16F, 7/16F,
            9/16F, 15/16F, 9/16F
    );

    public BlockStick(String domain, String name) {
        super(Material.PLANTS);

        setRegistryName(domain, name);
        setTranslationKey(name);
        setHardness(0.2F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Deprecated
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Deprecated
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX.offset(state.getOffset(source, pos));
    }

    @Deprecated
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getBoundingBox(world, pos);
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.STICK;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float x, float y, float z) {
        if (!world.isRemote) {
            return ItemLead.attachToFence(player, world, pos);
        }

        ItemStack stack = player.getHeldItem(hand);

        return stack.getItem() == Items.LEAD || stack.isEmpty();
    }

    public boolean canSustainPlant(IBlockAccess world, BlockPos pos, IPlantable plantable) {

        pos = pos.down();

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof BlockStick) {
            return ((BlockStick)block).canSustainPlant(world, pos, plantable);
        }

        return block.canSustainPlant(state, world, pos, EnumFacing.UP, plantable);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        return getDefaultState();
    }
}
