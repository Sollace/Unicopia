package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class BatEeeeAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 1;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 1;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.BAT;
    }

    @Override
    public Hit tryActivate(Pony player) {
        return Hit.INSTANCE;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public void apply(Pony player, Hit data) {
        Random rng = player.asWorld().random;
        int count = 1 + rng.nextInt(10);

        for (int i = 0; i < count; i++) {
            player.asWorld().playSound(null, player.getOrigin(), USounds.ENTITY_PLAYER_BATPONY_SCREECH, SoundCategory.PLAYERS,
                    0.9F + (rng.nextFloat() - 0.5F) / 2F,
                    1.6F + (rng.nextFloat() - 0.5F)
            );
        }
        AwaitTickQueue.scheduleTask(player.asWorld(), w -> {
            for (int i = 0; i < count; i++) {
                player.asWorld().playSound(null, player.getOrigin(), USounds.ENTITY_PLAYER_BATPONY_SCREECH, SoundCategory.PLAYERS,
                        0.9F + (rng.nextFloat() - 0.5F) / 2F,
                        1.6F + (rng.nextFloat() - 0.5F)
                );
            }
        }, rng.nextInt(10));

        Vec3d origin = player.getOriginVector();

        if (rng.nextInt(20000) == 0) {
            player.asEntity().damage(MagicalDamageSource.create("eeee", player).setBreakSunglasses(), 0.1F);
            UCriteria.SCREECH_SELF.trigger(player.asEntity());
        }

        int total = player.findAllEntitiesInRange(5).mapToInt(e -> {
            if (e instanceof LivingEntity living && !SpellType.SHIELD.isOn(e)) {
                boolean isEarthPony = EquinePredicates.PLAYER_EARTH.test(e);
                e.damage(MagicalDamageSource.create("eeee", player).setBreakSunglasses(), isEarthPony ? 0.1F : 0.3F);

                Vec3d knockVec = origin.subtract(e.getPos());
                living.takeKnockback(isEarthPony ? 0.3F : 0.5F, knockVec.getX(), knockVec.getZ());
                if (!isEarthPony) {
                    e.addVelocity(0, 0.1, 0);
                }
                Living.updateVelocity(e);
            }
            return 1;
        }).sum();

        if (total >= 20) {
            UCriteria.SCREECH_TWENTY_MOBS.trigger(player.asEntity());
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }
}
