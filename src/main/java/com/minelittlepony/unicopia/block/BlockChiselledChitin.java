package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.init.UMaterials;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockChiselledChitin extends BlockDirected {

    public BlockChiselledChitin(String domain, String name) {
        super(UMaterials.hive, domain, name);

        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(50);
        setResistance(2000);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.BLACK;
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        float hardness = super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);

        IPlayer iplayer = PlayerSpeciesList.instance().getPlayer(player);
        Race race = iplayer.getPlayerSpecies();

        if (race == Race.CHANGELING) {
            hardness *= 80;
        } else if (race.canInteractWithClouds()) {
            hardness /= 4;
        } else if (race.canUseEarth()) {
            hardness *= 10;
        }

        return hardness;
    }
}
