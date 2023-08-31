package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.data.Numeric;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class BatEeeeAbility implements Ability<Numeric> {
    public static final int SELF_SPOOK_PROBABILITY = 20000;
    public static final int MOB_SPOOK_PROBABILITY = 1000;

    @Override
    public int getWarmupTime(Pony player) {
        return 30;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 5;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean activateOnEarlyRelease() {
        return true;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.BAT;
    }

    @Override
    public Optional<Numeric> prepare(Pony player) {
        return player.getAbilities().getActiveStat()
                .map(stat -> (int)(stat.getWarmup() * getWarmupTime(player)))
                .filter(i -> i >= 0)
                .map(Numeric::new);
    }

    @Override
    public Numeric.Serializer<Numeric> getSerializer() {
        return Numeric.SERIALIZER;
    }

    @Override
    public boolean apply(Pony player, Numeric data) {
        float strength = 1 - MathHelper.clamp(data.type() / (float)getWarmupTime(player), 0, 1);
        Random rng = player.asWorld().random;
        int count = 1 + rng.nextInt(10) + (int)(strength * 10);

        for (int i = 0; i < count; i++) {
            player.playSound(USounds.ENTITY_PLAYER_BATPONY_SCREECH,
                    (0.9F + (rng.nextFloat() - 0.5F) / 2F) * strength,
                    1.6F + (rng.nextFloat() - 0.5F)
            );
        }
        for (int j = 0; j < (int)(strength * 2); j++) {
            for (int k = 0; k < count; k++) {
                AwaitTickQueue.scheduleTask(player.asWorld(), w -> {
                    player.playSound(USounds.ENTITY_PLAYER_BATPONY_SCREECH,
                            (0.9F + (rng.nextFloat() - 0.5F) / 2F) * strength,
                            1.6F + (rng.nextFloat() - 0.5F)
                    );
                }, rng.nextInt(3));
            }
        }

        if (!player.getPhysics().isFlying()) {
            player.setAnimation(Animation.SPREAD_WINGS, Animation.Recipient.ANYONE);
        }

        Vec3d origin = player.getOriginVector();

        if (strength > 0.5F && rng.nextInt(SELF_SPOOK_PROBABILITY) == 0) {
            player.asEntity().damage(player.damageOf(UDamageTypes.BAT_SCREECH, player), 0.1F);
            UCriteria.SCREECH_SELF.trigger(player.asEntity());
        }

        int total = player.findAllEntitiesInRange((int)Math.max(1, 8 * strength)).mapToInt(e -> {
            if (e instanceof LivingEntity living && !SpellType.SHIELD.isOn(e)) {
                boolean isEarthPony = EquinePredicates.PLAYER_EARTH.test(e);
                e.damage(player.damageOf(UDamageTypes.BAT_SCREECH, player), isEarthPony ? 0.1F : 0.3F);
                if (e.getWorld().random.nextInt(MOB_SPOOK_PROBABILITY) == 0) {
                    RegistryUtils.pickRandom(e.getWorld(), UTags.SPOOKED_MOB_DROPS).ifPresent(drop -> {
                        e.dropStack(drop.getDefaultStack());
                        e.playSound(USounds.Vanilla.ENTITY_ITEM_PICKUP, 1, 0.1F);
                        UCriteria.SPOOK_MOB.trigger(player.asEntity());
                    });
                }

                Vec3d knockVec = origin.subtract(e.getPos()).multiply(strength);
                living.takeKnockback((isEarthPony ? 0.3F : 0.5F) * strength, knockVec.getX(), knockVec.getZ());
                if (!isEarthPony) {
                    e.addVelocity(0, 0.1 * strength, 0);
                }
                Living.updateVelocity(e);
                return 1;
            }
            return 0;
        }).sum();

        if (total >= 20) {
            UCriteria.SCREECH_TWENTY_MOBS.trigger(player.asEntity());
        }

        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        for (int i = 0; i < 20; i++) {
            player.addParticle(ParticleTypes.BUBBLE_POP, player.getPhysics().getHeadPosition().toCenterPos(), VecHelper.supply(() -> player.asWorld().getRandom().nextGaussian() - 0.5));
        }
    }
}
