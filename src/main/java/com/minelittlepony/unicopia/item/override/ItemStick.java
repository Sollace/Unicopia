package com.minelittlepony.unicopia.item.override;

import com.minelittlepony.unicopia.init.UBlocks;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemStick extends Item {
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);
        IBlockState state = worldIn.getBlockState(pos);

        if (facing == EnumFacing.UP && player.canPlayerEdit(pos.offset(facing), facing, itemstack) && worldIn.isAirBlock(pos.up())) {

            worldIn.setBlockState(pos.up(), UBlocks.stick.getDefaultState());

            SoundType sound = state.getBlock().getSoundType(state, worldIn, pos, player);

            worldIn.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1) / 2, sound.getPitch());

            if (player instanceof EntityPlayerMP) {
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos.up(), itemstack);
            }

            if (!(player instanceof EntityPlayer && ((EntityPlayer)player).capabilities.isCreativeMode)) {
                itemstack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }
}
