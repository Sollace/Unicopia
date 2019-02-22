package com.minelittlepony.unicopia.item;

import java.util.function.Predicate;

import net.minecraft.block.BlockSlab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UItemSlab extends ItemSlab {

    private final Predicate<EntityPlayer> abilityTest;

    public UItemSlab(BlockSlab singleSlab, BlockSlab doubleSlab, Predicate<EntityPlayer> abilityTest) {
        super(singleSlab, singleSlab, doubleSlab);
        this.abilityTest = abilityTest;

        setHasSubtypes(false);
        setRegistryName(singleSlab.getRegistryName());
        setTranslationKey(singleSlab.getRegistryName().getPath());
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos origin, EnumFacing side, EntityPlayer player, ItemStack stack) {
        if (!(player.capabilities.isCreativeMode || abilityTest.test(player))) {
            return false;
        }

        return super.canPlaceBlockOnSide(world, origin, side, player, stack);
    }
}
