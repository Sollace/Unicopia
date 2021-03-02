package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.util.WorldEvent;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

public class NecromancySpell extends AbstractSpell implements Attached {

    private final List<EntityType<? extends LivingEntity>> spawns = Lists.newArrayList(
            EntityType.ZOMBIE,
            EntityType.HUSK,
            EntityType.ZOMBIFIED_PIGLIN
    );

    protected NecromancySpell(SpellType<?> type, Affinity affinity) {
        super(type, affinity);
    }

    @Override
    public boolean onBodyTick(Caster<?> source) {

        int radius = source.getLevel().get() + 1;

        if (source.isClient()) {
            Shape affectRegion = new Sphere(false, radius * 4);

            source.spawnParticles(affectRegion, 5, pos -> {
                if (!source.getWorld().isAir(new BlockPos(pos).down())) {
                    source.addParticle(ParticleTypes.FLAME, pos, Vec3d.ZERO);
                }
            });

            return true;
        }

        if (source.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return true;
        }

        float additional = source.getWorld().getLocalDifficulty(source.getOrigin()).getLocalDifficulty();

        Shape affectRegion = new Sphere(false, radius * 4);

        if (source.getWorld().random.nextInt(100) != 0) {
            return true;
        }

        Vec3d origin = source.getOriginVector();

        if (source.findAllEntitiesInRange(radius * 4, e -> e instanceof ZombieEntity).count() >= 10 * (1 + additional)) {
            return true;
        }

        for (int i = 0; i < 10; i++) {
            Vec3d pos = affectRegion.computePoint(source.getWorld().random).add(origin);

            BlockPos loc = new BlockPos(pos);

            if (source.getWorld().isAir(loc.up()) && !source.getWorld().isAir(loc)) {
                spawnMonster(source, pos);

                return false;
            }
        }


        return true;
    }

    protected void spawnMonster(Caster<?> source, Vec3d pos) {
        int index = (int)MathHelper.nextDouble(source.getWorld().random, 0, spawns.size());
        LivingEntity zombie = spawns.get(index).create(source.getWorld());

        zombie.updatePositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        zombie.setVelocity(0, 0.3, 0);

        source.getWorld().syncWorldEvent(WorldEvent.ZOMBIE_BREAK_WOODEN_DOOR, zombie.getBlockPos(), 0);

        source.getWorld().spawnEntity(zombie);
    }
}
