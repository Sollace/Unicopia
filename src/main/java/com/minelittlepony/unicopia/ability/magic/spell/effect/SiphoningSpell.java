package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A spell that pulls health from other entities and delivers it to the caster.
 */
public class SiphoningSpell extends AbstractAreaEffectSpell {
    static final Predicate<Entity> TARGET_PREDICATE = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_LIVING_ENTITY);

    private final DataTracker.Entry<Boolean> upset = dataTracker.startTracking(TrackableDataType.BOOLEAN, false);
    private int ticksUpset;

    protected SiphoningSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Affinity getAffinity() {
        return getTraits().get(Trait.DARKNESS) > 0 ? Affinity.BAD : Affinity.GOOD;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (ticksUpset > 0 && --ticksUpset <= 0) {
            upset.set(false);
        }

        if (source.isClient()) {
            float radius = source.getLevel().getScaled(5) + RANGE.get(getTraits());
            int direction = isFriendlyTogether(source) ? 1 : -1;

            source.spawnParticles(new Sphere(true, radius, 1, 0, 1), 1, pos -> {
                if (!source.asWorld().isAir(BlockPos.ofFloored(pos).down())) {

                    double dist = pos.distanceTo(source.getOriginVector());
                    Vec3d velocity = pos.subtract(source.getOriginVector()).normalize().multiply(direction * dist);

                    source.addParticle(direction == 1 && ticksUpset == 0 ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER, pos, velocity);
                }
            });
        } else {
            if (source.asWorld().getTime() % 10 != 0) {
                return true;
            }

            if (isFriendlyTogether(source)) {
                distributeHealth(source);
            } else {
                collectHealth(source);
            }
        }
        return !isDead();
    }

    private Stream<LivingEntity> getTargets(Caster<?> source) {
        return VecHelper.findInRange(null, source.asWorld(), source.getOriginVector(), 4 + source.getLevel().getScaled(6), TARGET_PREDICATE).stream().map(e -> (LivingEntity)e);
    }

    private void distributeHealth(Caster<?> source) {
        DamageSource damage = source.damageOf(UDamageTypes.LIFE_DRAINING, source);

        getTargets(source).forEach(e -> {
            float maxHealthGain = e.getMaxHealth() - e.getHealth();

            if (!source.subtractEnergyCost(0.2F)) {
                setDead();
            }

            if (ticksUpset > 0 || maxHealthGain <= 0) {
                if (source.asWorld().random.nextInt(3000) == 0) {
                    setDead();
                } else {
                    e.damage(damage, e.getHealth() / 4);
                    ticksUpset = 100;
                    upset.set(true);
                }
            } else {
                e.heal((float)Math.min(source.getLevel().getScaled(e.getHealth()) / 2F, maxHealthGain * 0.6));
                ParticleUtils.spawnParticle(e.getWorld(), new FollowingParticleEffect(UParticles.HEALTH_DRAIN, e, 0.2F), e.getPos(), Vec3d.ZERO);
            }
        });
    }

    private void collectHealth(Caster<?> source) {
        @Nullable
        LivingEntity owner = source.getMaster();
        float maxHealthGain = owner == null ? 0 : owner.getMaxHealth() - owner.getHealth();

        if (maxHealthGain == 0) {
            return;
        }

        List<LivingEntity> targets = getTargets(source).collect(Collectors.toList());
        if (targets.isEmpty()) {
            return;
        }

        float attackAmount = Math.max(maxHealthGain / targets.size(), 0.5F);

        DamageSource damage = source.damageOf(UDamageTypes.LIFE_DRAINING, source);

        float healthGain = 0;

        for (LivingEntity e : targets) {
            if (!e.equals(owner)) {
                float dealt = Math.min(e.getHealth(), attackAmount);

                if (e instanceof PlayerEntity) {
                    Pony player = Pony.of((PlayerEntity)e);

                    Race.Composite race = player.getCompositeRace();

                    if (race.canCast()) {
                        dealt /= 2;
                    }
                    if (race.canUseEarth()) {
                        dealt *= 2;
                    }
                }

                e.damage(damage, dealt);
                ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, owner, 0.2F), e, 1);

                healthGain += dealt;
            }
        }

        owner.heal(healthGain);
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        compound.putInt("upset", ticksUpset);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        ticksUpset = compound.getInt("upset");
        if (ticksUpset > 0) {
            upset.set(true);
        }
    }
}
