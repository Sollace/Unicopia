package com.minelittlepony.unicopia.ability;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;

/**
 * Changeling ability to restore health from mobs
 */
public class ChangelingFeedAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 5;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return canFeed(player) ? 15 : 80;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.CHANGELING;
    }

    @Nullable
    @Override
    public Hit tryActivate(Pony player) {
        if (canFeed(player)) {
            if (!getTargets(player).isEmpty()) {
                return Hit.INSTANCE;
            }
        }

        return null;
    }

    private boolean canFeed(Pony player) {
        return player.asEntity().getHealth() < player.asEntity().getMaxHealth()
            || player.asEntity().canConsume(false);
    }

    private boolean canDrain(Entity e) {
        return (e instanceof LivingEntity)
            && (e instanceof CowEntity
            || e instanceof MerchantEntity
            || e instanceof PlayerEntity
            || e instanceof SheepEntity
            || e instanceof PigEntity
            || e instanceof HostileEntity);
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    protected List<LivingEntity> getTargets(Pony player) {
        List<Entity> list = VecHelper.findInRange(player.asEntity(), player.asWorld(), player.getOriginVector(), 3, this::canDrain);

        TraceHelper.<LivingEntity>findEntity(player.asEntity(), 17, 1,
                looked -> looked instanceof LivingEntity && !list.contains(looked) && canDrain(looked))
            .ifPresent(list::add);

        return list.stream().map(i -> (LivingEntity)i).collect(Collectors.toList());
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public void apply(Pony iplayer, Hit data) {
        if (!canFeed(iplayer)) {
            return;
        }

        PlayerEntity player = iplayer.asEntity();

        float maximumHealthGain = player.getMaxHealth() - player.getHealth();
        int maximumFoodGain = player.canConsume(false) ? (20 - player.getHungerManager().getFoodLevel()) : 0;

        if (maximumHealthGain > 0 || maximumFoodGain > 0) {

            float healAmount = 0;

            for (LivingEntity i : getTargets(iplayer)) {
                healAmount += drainFrom(iplayer, i);
            }

            int foodAmount = (int)Math.floor(Math.min(healAmount / 3, maximumFoodGain));

            if (foodAmount > 0) {
                healAmount -= foodAmount;
            }
            player.getHungerManager().add(Math.max(1, foodAmount), 0.125f);

            player.heal(Math.max(1, Math.min(healAmount, maximumHealthGain)));
        }


        if (!canFeed(iplayer)) {
            iplayer.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1, (float)player.world.random.nextTriangular(1F, 0.2F));
        } else {
            iplayer.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.1F, iplayer.getRandomPitch());
        }
    }

    public float drainFrom(Pony changeling, LivingEntity living) {

        DamageSource d = changeling.damageOf(UDamageTypes.LOVE_DRAINING, changeling);

        float damage = living.getHealth()/2;

        if (damage > 0) {
            living.damage(d, damage);
        }

        ParticleUtils.spawnParticles(UParticles.CHANGELING_MAGIC, living, 7);
        ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, changeling.asEntity(), 0.2F), living, 1);

        if (changeling.asEntity().hasStatusEffect(StatusEffects.NAUSEA)) {
            StatusEffectInstance effect = changeling.asEntity().getStatusEffect(StatusEffects.NAUSEA);
            changeling.asEntity().removeStatusEffect(StatusEffects.NAUSEA);
            living.addStatusEffect(effect);
        } else if (changeling.asWorld().random.nextInt(2300) == 0) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20, 1));
        }

        if (living instanceof PlayerEntity) {
            damage ++;
            damage *= 1.6F;

            if (!changeling.asEntity().hasStatusEffect(StatusEffects.HEALTH_BOOST)) {
                changeling.asEntity().addStatusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 13000, 1));
            }
        }

        return damage;
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().add(6);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        if (player.asWorld().random.nextInt(10) == 0) {
            player.spawnParticles(ParticleTypes.HEART, 1);
        }
    }
}
