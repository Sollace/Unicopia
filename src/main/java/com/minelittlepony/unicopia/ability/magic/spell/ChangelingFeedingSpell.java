package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.AbstractSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;

public class ChangelingFeedingSpell extends AbstractSpell {
    private List<EntityReference<LivingEntity>> targets = List.of();
    private int nextTargetIndex;

    private float healthToDrain;
    private int foodToDrain;

    private float damageThisTick;

    public ChangelingFeedingSpell(CustomisedSpellType<?> type) {
        super(type);
        setHidden(true);
    }

    public ChangelingFeedingSpell(List<LivingEntity> feedTarget, float healthToDrain, int foodToDrain) {
        this(SpellType.FEED.withTraits());
        this.targets = feedTarget.stream().map(EntityReference::new).collect(Collectors.toList() /* make mutable */);
        this.healthToDrain = healthToDrain;
        this.foodToDrain = foodToDrain;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!(source instanceof Pony changeling) || situation != Situation.BODY || !source.canUse(Abilities.FEED)) {
            return false;
        }

        PlayerEntity player = changeling.asEntity();
        if (!canFeed(changeling)) {
            changeling.playSound(USounds.Vanilla.ENTITY_PLAYER_BURP, 1, (float)player.getWorld().random.nextTriangular(1F, 0.2F));
            return false;
        }

        float tickDrain = Math.min(0.05F, healthToDrain);
        damageThisTick += tickDrain;

        if (damageThisTick > 1) {
            damageThisTick--;

            float healAmount = drain(changeling, 1);
            float foodAmount = Math.min(healAmount / 3F, foodToDrain);
            if (foodAmount > 0) {
                healAmount -= foodAmount;
            }

            foodAmount = MathHelper.clamp(foodAmount, 0, foodToDrain);
            healAmount = MathHelper.clamp(healAmount, 0, healthToDrain);

            int shanks = MathHelper.floor(foodAmount);
            player.getHungerManager().add(shanks, foodAmount - shanks);
            player.heal(healAmount);

            if (!canFeed(changeling)) {
                changeling.playSound(USounds.Vanilla.ENTITY_PLAYER_BURP, 1, (float)player.getWorld().random.nextTriangular(1F, 0.2F));
            } else {
                changeling.playSound(USounds.ENTITY_PLAYER_CHANGELING_FEED, 0.1F, changeling.getRandomPitch());
            }

            foodToDrain -= foodAmount;
            healthToDrain -= healAmount;
        }

        return !targets.isEmpty() && (healthToDrain > 0 || foodToDrain > 0);
    }

    private float drain(Pony changeling, float max) {
        List<EntityReference<LivingEntity>> targets = this.targets;
        while (!targets.isEmpty()) {
            int index = MathHelper.clamp(nextTargetIndex, 0, targets.size());
            LivingEntity l = targets.get(index).getOrEmpty(changeling.asWorld()).orElse(null);
            if (l != null && !l.isRemoved() && l.distanceTo(changeling.asEntity()) < 4) {
                nextTargetIndex = (nextTargetIndex + 1) % targets.size();
                return drainFrom(changeling, l, max);
            } else {
                targets.remove(index);
            }
        }
        return 0;
    }

    public float drainFrom(Pony changeling, LivingEntity living, float damage) {
        DamageSource d = changeling.damageOf(UDamageTypes.LOVE_DRAINING, changeling);

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

    public static boolean canFeed(Pony player) {
        return player.asEntity().getHealth() < player.asEntity().getMaxHealth()
            || player.asEntity().canConsume(false);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putFloat("healthToDrain", healthToDrain);
        compound.putInt("foodToDrain", foodToDrain);
        compound.putFloat("damageThisTick", damageThisTick);
        compound.put("targets", EntityReference.<LivingEntity>getSerializer().writeAll(targets));
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        healthToDrain = compound.getFloat("healthToDrain");
        foodToDrain = compound.getInt("foodToDrain");
        damageThisTick = compound.getFloat("damageThisTick");
        targets = compound.contains("targets", NbtElement.LIST_TYPE)
                ? EntityReference.<LivingEntity>getSerializer().readAll(compound.getList("targets", NbtElement.COMPOUND_TYPE)).toList()
                : List.of();
    }
}
