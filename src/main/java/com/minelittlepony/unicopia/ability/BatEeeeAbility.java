package com.minelittlepony.unicopia.ability;

import java.util.Random;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.ShieldSpell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class BatEeeeAbility implements Ability<Hit> {

    private static final Predicate<Entity> HAS_SHIELD = EquinePredicates.carryingSpell(ShieldSpell.class);

    @Override
    public int getWarmupTime(Pony player) {
        return 1;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 1;
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
        Random rng = player.getWorld().random;
        int count = 1 + rng.nextInt(10);

        for (int i = 0; i < count; i++) {
            player.getWorld().playSound(null, player.getOrigin(), USounds.BATPONY_EEEE, SoundCategory.PLAYERS,
                    0.9F + (rng.nextFloat() - 0.5F) / 2F,
                    1.6F + (rng.nextFloat() - 0.5F)
            );
        }
        AwaitTickQueue.scheduleTask(player.getWorld(), w -> {
            for (int i = 0; i < count; i++) {
                player.getWorld().playSound(null, player.getOrigin(), USounds.BATPONY_EEEE, SoundCategory.PLAYERS,
                        0.9F + (rng.nextFloat() - 0.5F) / 2F,
                        1.6F + (rng.nextFloat() - 0.5F)
                );
            }
        }, rng.nextInt(10));

        Vec3d origin = player.getOriginVector();

        if (rng.nextInt(20000) == 0) {
            player.getOwner().damage(MagicalDamageSource.create("eeee", player.getOwner()), 0.1F);
        }

        player.findAllEntitiesInRange(5).forEach(e -> {
            if (e instanceof LivingEntity && !HAS_SHIELD.test(e)) {
                boolean isEarthPony = EquinePredicates.PLAYER_EARTH.test(e);
                e.damage(MagicalDamageSource.create("eeee", player.getOwner()), isEarthPony ? 0.1F : 0.3F);

                Vec3d knockVec = origin.subtract(e.getPos());
                ((LivingEntity) e).takeKnockback(isEarthPony ? 0.3F : 0.5F, knockVec.getX(), knockVec.getZ());
                if (!isEarthPony) {
                    e.addVelocity(0, 0.1, 0);
                }
            }
        });
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }
}
