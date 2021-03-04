package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A spell that pulls health from other entities and delivers it to the caster.
 */
public class SiphoningSpell extends AbstractPlacedSpell {

    protected SiphoningSpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public boolean onGroundTick(Caster<?> source) {
        super.onGroundTick(source);

        if (source.isClient()) {
            int radius = 4 + source.getLevel().get();
            int direction = isFriendlyTogether(source) ? 1 : -1;

            source.spawnParticles(new Sphere(true, radius, 1, 0, 1), 1, pos -> {
                if (!source.getWorld().isAir(new BlockPos(pos).down())) {

                    double dist = pos.distanceTo(source.getOriginVector());
                    Vec3d velocity = pos.subtract(source.getOriginVector()).normalize().multiply(direction * dist);

                    source.addParticle(direction == 1 ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER, pos, velocity);
                }
            });
        }

        if (source.getWorld().getTime() % 10 != 0) {
            return true;
        }

        if (isFriendlyTogether(source)) {
            distributeHealth(source);
        } else {
            collectHealth(source);
        }

        return true;
    }

    private Stream<LivingEntity> getTargets(Caster<?> source) {
        return VecHelper.findInRange(null, source.getWorld(), source.getOriginVector(), 4 + source.getLevel().get(), e -> e instanceof LivingEntity)
                .stream()
                .map(e -> (LivingEntity)e);
    }

    private void distributeHealth(Caster<?> source) {
        LivingEntity owner = source.getMaster();
        DamageSource damage = MagicalDamageSource.create("drain", owner);

        getTargets(source).forEach(e -> {
            float maxHealthGain = e.getMaxHealth() - e.getHealth();

            source.subtractEnergyCost(0.2F);

            if (maxHealthGain <= 0) {
                if (source.getWorld().random.nextInt(30) == 0) {
                    onDestroyed(source);
                } else {
                    e.damage(damage, e.getHealth() / 4);
                }
            } else {
                e.heal((float)Math.min(0.5F * (1 + source.getLevel().get()), maxHealthGain * 0.6));
                ParticleUtils.spawnParticle(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, e, 0.2F), e.world, e.getPos(), Vec3d.ZERO);
            }
        });
    }

    private void collectHealth(Caster<?> source) {
        LivingEntity owner = source.getMaster();
        float maxHealthGain = owner.getMaxHealth() - owner.getHealth();

        if (maxHealthGain == 0) {
            return;
        }

        List<LivingEntity> targets = getTargets(source).collect(Collectors.toList());
        if (targets.isEmpty()) {
            return;
        }

        float attackAmount = Math.max(maxHealthGain / targets.size(), 0.5F);

        DamageSource damage = MagicalDamageSource.create("drain", owner);

        float healthGain = 0;

        for (LivingEntity e : targets) {
            if (!e.equals(owner)) {
                float dealt = Math.min(e.getHealth(), attackAmount);

                if (e instanceof PlayerEntity) {
                    Pony player = Pony.of((PlayerEntity)e);

                    Race race = player.getSpecies();

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
}
