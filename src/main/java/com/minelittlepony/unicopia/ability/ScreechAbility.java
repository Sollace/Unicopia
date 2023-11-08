package com.minelittlepony.unicopia.ability;

import java.util.Optional;

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
import net.minecraft.world.event.GameEvent;

/**
 * An ability to scream very loud
 */
public class ScreechAbility implements Ability<Numeric> {
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
        return race == Race.HIPPOGRIFF;
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

        playSounds(player, rng, strength);

        if (!player.getPhysics().isFlying()) {
            player.setAnimation(Animation.SPREAD_WINGS, Animation.Recipient.ANYONE);
        }

        int total = player.findAllEntitiesInRange((int)Math.max(1, 8 * strength)).mapToInt(e -> {
            if (e instanceof LivingEntity living && !SpellType.SHIELD.isOn(e)) {
                spookMob(player, living, strength);
                return 1;
            }
            return 0;
        }).sum();

        if (total >= 20) {
            UCriteria.SCREECH_TWENTY_MOBS.trigger(player.asEntity());
        }

        return true;
    }

    protected void playSounds(Pony player, Random rng, float strength) {
        player.playSound(USounds.ENTITY_PLAYER_HIPPOGRIFF_SCREECH,
                (1.2F + (rng.nextFloat() - 0.5F) / 2F) * strength,
                1.1F + (rng.nextFloat() - 0.5F)
        );
        player.asWorld().emitGameEvent(player.asEntity(), GameEvent.ENTITY_ROAR, player.asEntity().getEyePos());
    }

    protected void spookMob(Pony player, LivingEntity living, float strength) {
        boolean isEarthPony = EquinePredicates.PLAYER_EARTH.test(living);
        boolean isBracing = isEarthPony && player.asEntity().isSneaking();

        if (!isBracing) {
            living.damage(player.damageOf(UDamageTypes.BAT_SCREECH, player), isEarthPony ? 0.1F : 0.3F);


            if (living.getWorld().random.nextInt(MOB_SPOOK_PROBABILITY) == 0) {
                RegistryUtils.pickRandom(living.getWorld(), UTags.SPOOKED_MOB_DROPS).ifPresent(drop -> {
                    living.dropStack(drop.getDefaultStack());
                    living.playSound(USounds.Vanilla.ENTITY_ITEM_PICKUP, 1, 0.1F);
                    UCriteria.SPOOK_MOB.trigger(player.asEntity());
                });
            }
        }

        Vec3d knockVec = player.getOriginVector().subtract(living.getPos()).multiply(strength);
        living.takeKnockback((isBracing ? 0.2F : isEarthPony ? 0.3F : 0.5F) * strength, knockVec.getX(), knockVec.getZ());
        if (!isEarthPony) {
            living.addVelocity(0, 0.1 * strength, 0);
        }
        Living.updateVelocity(living);
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        for (int i = 0; i < 20; i++) {
            player.addParticle(ParticleTypes.BUBBLE_POP, player.asEntity().getEyePos(),
                    VecHelper.supply(() -> (player.asWorld().getRandom().nextGaussian() - 0.5) * 0.3)
            );
        }
    }
}
