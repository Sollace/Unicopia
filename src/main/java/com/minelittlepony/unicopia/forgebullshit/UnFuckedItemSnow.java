package com.minelittlepony.unicopia.forgebullshit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSnow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A fixed ItemSnow class
 */
@FUF(reason = "GG Forge. I have to undo your changes so this class will work again.")
public class UnFuckedItemSnow extends ItemSnow {

    public UnFuckedItemSnow(Block block) {
        super(block);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        IBlockState state = world.getBlockState(pos);

        // Check explicitly for the maximum layers.
        // Without this layered blocks get stuck on the second layer because of a change introduced with Forge's patches.
        if (state.getBlock() instanceof BlockSnow && state.getValue(BlockSnow.LAYERS) < 8) {
            return true;
        }

        // return (state.getBlock() != net.minecraft.init.Blocks.SNOW_LAYER || ((Integer)state.getValue(BlockSnow.LAYERS)) > 7) ? super.canPlaceBlockOnSide(world, pos, side, player, stack) : true;
        return super.canPlaceBlockOnSide(world, pos, side, player, stack);
    }
}
