package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttribute;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.SpellAttributeType;
import com.minelittlepony.unicopia.ability.magic.spell.attribute.TooltipFactory;
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
import net.minecraft.entity.mob.HostileEntity;
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
    static final int ANGER_TICKS = 100;
    static final int PASSIVE_TICKS = -20;
    static final Predicate<Entity> TARGET_PREDICATE = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(EntityPredicates.VALID_LIVING_ENTITY);

    static final SpellAttribute<Boolean> INVERTED = SpellAttribute.createConditional(SpellAttributeType.INVERTED, Trait.DARKNESS, darkness -> darkness > 0);
    static final TooltipFactory TOOLTIP = TooltipFactory.of(RANGE, INVERTED);

    private final DataTracker.Entry<Boolean> upset = dataTracker.startTracking(TrackableDataType.BOOLEAN, false);
    private int ticksUpset = PASSIVE_TICKS;

    protected SiphoningSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public Affinity getAffinity() {
        return INVERTED.get(getTraits()) ? Affinity.BAD : Affinity.GOOD;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (source.isClient()) {
            float radius = source.getLevel().getScaled(5) + RANGE.get(getTraits());
            int direction = isFriendlyTogether(source) ? 1 : -1;

            source.spawnParticles(new Sphere(true, radius, 1, 0, 1), 1, pos -> {
                if (source.asWorld().isAir(BlockPos.ofFloored(pos))) {
                    pos = pos.add(0, -1.5, 0);

                    double dist = pos.distanceTo(source.getOriginVector());
                    Vec3d velocity = pos.subtract(source.getOriginVector()).normalize().multiply(direction * dist);

                    source.addParticle(direction == 1 && upset.get() ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HEART, pos, velocity);
                }
            });
        } else {
            if (ticksUpset > 0 && --ticksUpset <= 0) {
                upset.set(false);
                ticksUpset = PASSIVE_TICKS;
            }

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
        return VecHelper.findInRange(null, source.asWorld(), source.getOriginVector(), RANGE.get(getTraits()) + source.getLevel().getScaled(6), TARGET_PREDICATE)
                .stream()
                .map(e -> (LivingEntity)e);
    }

    /**
     *
     * Light effect:
     *
     * Distributes (heals) entities within the area of effect, converting the caster's mana to entity health.
     *
     * The spell gets angry if any entities spend too much time in its area or are max health when standing in it.
     *
     * @param source
     */
    private void distributeHealth(Caster<?> source) {
        DamageSource damage = source.damageOf(UDamageTypes.LIFE_DRAINING, source);
        float[] collectedHealth = new float[1];

        List<LivingEntity> recipients = new ArrayList<>();

        var targets = getTargets(source).toList();
        targets.forEach(e -> {
            float maxHealthGain = e.getMaxHealth() - e.getHealth();

            if (!source.subtractEnergyCost(0.2F + maxHealthGain)) {
                setDead();
            }

            if (e instanceof HostileEntity) {
                collectedHealth[0] += e.getHealth() / 4F;
                e.damage(damage, e.getHealth() / 4F);
                source.addParticle(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, e, 0.2F), source.getOriginVector(), Vec3d.ZERO);
            } else {
                if (ticksUpset > 0 || maxHealthGain <= 0) {
                    if (source.asWorld().random.nextInt(3000) == 0) {
                        setDead();
                    } else {
                        if (++ticksUpset >= 0) {
                            ticksUpset = ANGER_TICKS;
                            upset.set(true);
                            e.damage(damage, e.getHealth() / 4);
                        }
                    }
                } else {
                    collectedHealth[0] += maxHealthGain * 0.6F + (source.getLevel().getScaled(e.getHealth()) / 2F);
                    recipients.add(e);
                }
            }
        });

        float perTargetHealth = collectedHealth[0] / recipients.size();
        recipients.forEach(recipient -> {
            recipient.heal(perTargetHealth);
            source.addParticle(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, recipient, 0.2F), source.getOriginVector(), Vec3d.ZERO);
        });
    }

    /**
     * Dark effect:
     *
     * Collects health from entities in the area and sends them to the caster.
     *
     * @param source
     */
    private void collectHealth(Caster<?> source) {
        @Nullable
        LivingEntity owner = source.getMaster();
        float maxHealthGain = owner == null ? 0 : owner.getMaxHealth() - owner.getHealth();

        if (maxHealthGain == 0) {
            return;
        }

        List<LivingEntity> targets = getTargets(source).filter(e -> !source.isOwnerOrFriend(e)).toList();
        if (targets.isEmpty()) {
            return;
        }

        float attackAmount = Math.max(maxHealthGain / targets.size(), 0.5F);

        DamageSource damage = source.damageOf(UDamageTypes.LIFE_DRAINING, source);

        float healthGain = 0;

        for (LivingEntity e : targets) {
            float dealt = Math.min(e.getHealth(), attackAmount);

            if (e instanceof PlayerEntity p) {
                Pony player = Pony.of(p);

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
        upset.set(ticksUpset > 0);
    }
}
