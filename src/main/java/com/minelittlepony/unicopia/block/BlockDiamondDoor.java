package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Predicates;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDiamondDoor extends UDoor {

    public BlockDiamondDoor(String domain, String name, Supplier<Item> theItem) {
        super(Material.IRON, domain, name, theItem);
        setSoundType(SoundType.METAL);
        setHardness(5.0F);
    }

    @Override
    @Deprecated
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.DIAMOND;
    }

    @Override
    protected boolean canOpen(@Nullable EntityPlayer player) {
        return Predicates.MAGI.test(player);
    }

    @Override
    protected boolean onPowerStateChanged(World world, IBlockState state, BlockPos pos, boolean powered) {
        if (state.getValue(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, false), 2);

            return true;
        }

        return false;
    }
}
