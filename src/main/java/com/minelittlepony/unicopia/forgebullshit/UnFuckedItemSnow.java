package com.minelittlepony.unicopia.forgebullshit;

import net.minecraft.block.Block;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * A fixed ItemSnow class
 */
@Deprecated
public class UnFuckedItemSnow extends ItemSnow {

    public UnFuckedItemSnow(Block block) {
        super(block);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, Direction side, PlayerEntity player, ItemStack stack) {
        BlockState state = world.getBlockState(pos);

        // Check explicitly for the maximum layers.
        // Without this layered blocks get stuck on the second layer because of a change introduced with Forge's patches.
        if (state.getBlock() instanceof SnowBlock && state.getValue(SnowBlock.LAYERS) < 8) {
            return true;
        }

        // return (state.getBlock() != net.minecraft.init.Blocks.SNOW_LAYER || ((Integer)state.getValue(BlockSnow.LAYERS)) > 7) ? super.canPlaceBlockOnSide(world, pos, side, player, stack) : true;
        return super.canPlaceBlockOnSide(world, pos, side, player, stack);
    }
}
