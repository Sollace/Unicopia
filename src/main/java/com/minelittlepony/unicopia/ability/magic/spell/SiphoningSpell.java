package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
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
public class SiphoningSpell extends AbstractSpell implements Attached {

    protected SiphoningSpell(SpellType<?> type, Affinity affinity) {
        super(type, affinity);
    }

    @Override
    public boolean onBodyTick(Caster<?> source) {
        int radius = 4 + source.getLevel().get();

        if (source.isClient()) {
            Vec3d origin = source.getOriginVector();
            int direction = !isEnemy(source) ? 1 : -1;

            source.spawnParticles(new Sphere(true, radius, 1, 0, 1), 1, pos -> {
                if (!source.getWorld().isAir(new BlockPos(pos).down())) {

                    double dist = pos.distanceTo(origin);
                    Vec3d velocity = pos.subtract(origin).normalize().multiply(direction * dist);

                    source.addParticle(direction == 1 ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER, pos, velocity);
                }
            });
        }

        LivingEntity owner = source.getMaster();

        List<LivingEntity> target = source.findAllEntitiesInRange(radius)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity)e)
                .collect(Collectors.toList());

        DamageSource damage = MagicalDamageSource.create("drain", owner);

        if (!isFriendlyTogether(source)) {
            if (owner != null) {
                float healthGain = 0;
                float maxHealthGain = owner.getMaxHealth() - owner.getHealth();

                if (maxHealthGain > 0) {
                    float attackAmount = Math.max(maxHealthGain / target.size(), 0.5F);

                    for (LivingEntity e : target) {
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

                            healthGain += dealt;
                        }
                    }
                }

                owner.heal(healthGain);
            }

        } else {
            target.forEach(e -> {
                float maxHealthGain = e.getMaxHealth() - e.getHealth();

                if (maxHealthGain <= 0) {
                    if (source.getWorld().random.nextInt(30) == 0) {
                        onDestroyed(source);
                    } else {
                        e.damage(damage, e.getHealth() / 4);
                    }
                } else {
                    e.heal((float)Math.min(0.5F * (1 + source.getLevel().get()), maxHealthGain * 0.6));
                }
            });
        }

        return false;
    }
}
