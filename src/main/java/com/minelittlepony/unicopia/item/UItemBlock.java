package com.minelittlepony.unicopia.item;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UItemBlock extends UItemDecoration {

    private final Predicate<EntityPlayer> abilityTest;

    public UItemBlock(Block block, String domain, String name, Predicate<EntityPlayer> abilityTest) {
        super(block, domain, name);

        this.abilityTest = abilityTest;
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        if (!abilityTest.test(player)) {
            return player.capabilities.isCreativeMode;
        }

        return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);
    }

}
