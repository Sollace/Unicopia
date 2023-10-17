package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation.Recipient;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.ExplosionUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World.ExplosionSourceType;

/**
 * Kirin ability to transform into a nirik
 */
public class NirikBlastAbility implements Ability<Hit> {
    @Override
    public int getWarmupTime(Pony player) {
        return 10;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 3;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.KIRIN;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(EquinePredicates.RAGING.test(player.asEntity()));
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean apply(Pony player, Hit data) {

        player.asWorld().createExplosion(player.asEntity(), player.damageOf(DamageTypes.FIREBALL), ExplosionUtil.NON_DESTRUCTIVE, player.getOriginVector(), 5, true, ExplosionSourceType.MOB);
        player.setInvulnerabilityTicks(5);

        player.setAnimation(Animation.ARMS_UP, Recipient.ANYONE, 12);
        player.playSound(SoundEvents.ENTITY_POLAR_BEAR_WARNING, 2F, 0.1F);
        player.subtractEnergyCost(25);

        coolDown(player, AbilitySlot.NONE);

        for (Entity e : player.findAllEntitiesInRange(5, EntityPredicates.VALID_LIVING_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR)).toList()) {
            Vec3d offset = player.getOriginVector().subtract(e.getPos());
            ((LivingEntity)e).takeKnockback(1, offset.x, offset.z);
        }

        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        Random rng = player.asWorld().random;

        for (int i = 0; i < 26; i++) {
            Vec3d pos = player.getOriginVector().add(rng.nextGaussian(), 0, rng.nextGaussian());
            player.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, pos, new Vec3d(rng.nextGaussian(), 0.3, rng.nextGaussian()));
            player.addParticle(ParticleTypes.FLAME, pos, new Vec3d(rng.nextGaussian(), 0.8, rng.nextGaussian()));
            player.addParticle(ParticleTypes.LAVA, pos, new Vec3d(rng.nextGaussian(), 0.8, rng.nextGaussian()));
        }

    }
}
