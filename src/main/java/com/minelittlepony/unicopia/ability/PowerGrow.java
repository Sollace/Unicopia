package com.minelittlepony.unicopia.ability;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.capabilities.IPlayer;
import com.minelittlepony.util.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PowerGrow implements IPower<Location> {

    @Override
    public String getKeyName() {
        return "unicopia.power.grow";
    }

    @Override
    public int getKeyCode() {
        return GLFW.GLFW_KEY_N;
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
    public Location tryActivate(IPlayer player) {
        HitResult ray = VecHelper.getObjectMouseOver(player.getOwner(), 3, 1);

        if (ray instanceof BlockHitResult && ray.getType() == HitResult.Type.BLOCK) {
            return new Location(((BlockHitResult)ray).getBlockPos());
        }

        return null;
    }

    @Override
    public Class<Location> getPackageType() {
        return Location.class;
    }

    @Override
    public void apply(IPlayer player, Location data) {
        int count = 0;

        for (BlockPos pos : BlockPos.iterate(
                new BlockPos(data.x - 2, data.y - 2, data.z - 2),
                new BlockPos(data.x + 2, data.y + 2, data.z + 2))) {
            count += applySingle(player.getWorld(), player.getWorld().getBlockState(pos), pos);
        }

        if (count > 0) {
            player.subtractEnergyCost(count * 5);
        }
    }

    protected int applySingle(World w, BlockState state, BlockPos pos) {

        ItemStack stack = new ItemStack(Items.BONE_MEAL);

        if (BoneMealItem.useOnFertilizable(stack, w, pos)
            || BoneMealItem.useOnGround(stack, w, pos, Direction.UP)) {
            return 1;
        }

        return 0;
    }

    @Override
    public void preApply(IPlayer player) {
        player.addExertion(3);

        if (player.getWorld().isClient()) {
            player.spawnParticles(UParticles.UNICORN_MAGIC, 1);
        }
    }

    @Override
    public void postApply(IPlayer player) {

    }
}
