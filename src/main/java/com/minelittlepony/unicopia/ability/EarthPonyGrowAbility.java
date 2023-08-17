package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Pos;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.TraceHelper;
import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        return race.canUseEarth();
    }

    @Override
    public Optional<Pos> prepare(Pony player) {
        return TraceHelper.findBlock(player.asEntity(), 3, 1).map(Pos::new);
    }

    @Override
    public Hit.Serializer<Pos> getSerializer() {
        return Pos.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 10;
    }

    @Override
    public boolean apply(Pony player, Pos data) {
        int count = 0;

        for (BlockPos pos : BlockPos.iterate(
                data.pos().add(-2, -2, -2),
                data.pos().add( 2,  2,  2))) {
            count += applySingle(player.asWorld(), player.asWorld().getBlockState(pos), pos);
        }

        if (count > 0) {
            player.subtractEnergyCost(count / 5D);
        }
        return true;
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
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(30);

        if (player.asWorld().isClient()) {
            player.spawnParticles(MagicParticleEffect.UNICORN, 1);
        }
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {

    }
}
