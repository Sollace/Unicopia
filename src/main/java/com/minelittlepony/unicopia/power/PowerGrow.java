package com.minelittlepony.unicopia.power;

import org.lwjgl.input.Keyboard;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.power.data.Location;
import com.minelittlepony.util.vector.VecHelper;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class PowerGrow implements IPower<Location> {

    @Override
    public String getKeyName() {
        return "unicopia.power.grow";
    }

    @Override
    public int getKeyCode() {
        return Keyboard.KEY_N;
    }

    @Override
    public int getWarmupTime(IPlayer player) {
        return 10;
    }

    @Override
    public int getCooldownTime(IPlayer player) {
        return 50;
    }

    @Override
    public boolean canUse(Race playerSpecies) {
        return playerSpecies == Race.EARTH;
    }

    @Override
    public Location tryActivate(EntityPlayer player, World w) {
        RayTraceResult ray = VecHelper.getObjectMouseOver(player, 3, 1);

        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            return new Location(ray.getBlockPos());
        }

        return null;
    }

    @Override
    public Class<Location> getPackageType() {
        return Location.class;
    }

    @Override
    public void apply(EntityPlayer player, Location data) {
        int count = 0;

        for (BlockPos pos : BlockPos.getAllInBox(
                new BlockPos(data.x - 2, data.y - 2, data.z - 2),
                new BlockPos(data.x + 2, data.y + 2, data.z + 2))) {
            count += applySingle(player.world, player.world.getBlockState(pos), pos);
        }

        if (count > 0) {
            IPower.takeFromPlayer(player, count * 5);
        }
    }

    protected int applySingle(World w, IBlockState state, BlockPos pos) {
        if (state.getBlock() instanceof IGrowable
                && !(state.getBlock() instanceof BlockGrass)) {

            IGrowable g = ((IGrowable)state.getBlock());

            if (g.canGrow(w, pos, state, w.isRemote) && g.canUseBonemeal(w, w.rand, pos, state)) {
                if (ItemDye.applyBonemeal(new ItemStack(Items.DYE, 1), w, pos)) {
                    w.playEvent(2005, pos, 0);

                    if (g instanceof BlockDoublePlant) {
                        w.playEvent(2005, pos.up(), 0);
                    }
                }

                return 1;
            }
        }
        return 0;
    }

    @Override
    public void preApply(IPlayer player) {
        player.addExertion(3);

        if (player.getWorld().isRemote) {
            IPower.spawnParticles(UParticles.MAGIC_PARTICLE, player, 1);
        }
    }

    @Override
    public void postApply(IPlayer player) {

    }
}
