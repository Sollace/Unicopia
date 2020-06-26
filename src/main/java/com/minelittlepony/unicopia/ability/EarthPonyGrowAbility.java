package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.particles.MagicParticleEffect;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Earth Pony ability to grow crops
 */
public class EarthPonyGrowAbility implements Ability<Pos> {

    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 50;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.EARTH;
    }

    @Override
    public Pos tryActivate(Pony player) {
        HitResult ray = VecHelper.getObjectMouseOver(player.getOwner(), 3, 1);

        if (ray instanceof BlockHitResult && ray.getType() == HitResult.Type.BLOCK) {
            return new Pos(((BlockHitResult)ray).getBlockPos());
        }

        return null;
    }

    @Override
    public Hit.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public void apply(Pony player, Pos data) {
        int count = 0;

        for (BlockPos pos : BlockPos.iterate(
                data.pos().add(-2, -2, -2),
                data.pos().add( 2,  2,  2))) {
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
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().addExertion(30);

        if (player.getWorld().isClient()) {
            player.spawnParticles(MagicParticleEffect.UNICORN, 1);
        }
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {

    }
}
