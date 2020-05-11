package com.minelittlepony.unicopia.magic.spell;

import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SiphoningSpell extends AbstractRangedAreaSpell {

    @Override
    public String getName() {
        return "siphon";
    }

    @Override
    public int getTint() {
        return 0xe308ab;
    }

    @Override
    public boolean update(Caster<?> source) {

        int radius = 4 + source.getCurrentLevel();

        LivingEntity owner = source.getOwner();

        List<LivingEntity> target = source.findAllEntitiesInRange(radius)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity)e)
                .collect(Collectors.toList());

        DamageSource damage = damageSource(owner);

        if (source.getAffinity() == Affinity.BAD) {
            if (owner != null) {
                float healthGain = 0;
                float maxHealthGain = owner.getMaximumHealth() - owner.getHealth();

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
                float maxHealthGain = e.getMaximumHealth() - e.getHealth();

                if (maxHealthGain <= 0) {
                    if (source.getWorld().random.nextInt(30) == 0) {
                        setDead();
                    } else {
                        e.damage(damage, e.getHealth() / 4);
                    }
                } else {
                    e.heal((float)Math.min(0.5F * (1 + source.getCurrentLevel()), maxHealthGain * 0.6));
                }
            });
        }

        return false;
    }

    protected DamageSource damageSource(LivingEntity actor) {
        if (actor == null) {
            return MagicalDamageSource.create("drain");
        }

        return MagicalDamageSource.causeMobDamage("drain", actor);
    }

    @Override
    public void render(Caster<?> source) {
        int radius = 4 + source.getCurrentLevel();

        Vec3d origin = source.getOriginVector();
        int direction = source.getAffinity() == Affinity.GOOD ? 1 : -1;

        source.spawnParticles(new Sphere(true, radius, 1, 0, 1), 1, pos -> {
            if (!source.getWorld().isAir(new BlockPos(pos).down())) {

                double dist = pos.distanceTo(origin);
                Vec3d velocity = pos.subtract(origin).normalize().multiply(direction * dist);

                source.addParticle(direction == 1 ? ParticleTypes.HEART : ParticleTypes.ANGRY_VILLAGER, pos, velocity);
            }
        });
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

}
