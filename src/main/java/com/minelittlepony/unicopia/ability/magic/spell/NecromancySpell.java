package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.List;

import com.google.common.collect.Lists;
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

public class NecromancySpell extends AbstractPlacedSpell {

    private final List<EntityType<? extends LivingEntity>> spawns = Lists.newArrayList(
            EntityType.ZOMBIE,
            EntityType.HUSK,
            EntityType.ZOMBIFIED_PIGLIN
    );

    protected NecromancySpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public boolean onGroundTick(Caster<?> source) {

        int radius = (source.getLevel().get() + 1) * 4;

        if (source.isClient()) {
            source.spawnParticles(origin, new Sphere(false, radius), 5, pos -> {
                if (!source.getWorld().isAir(new BlockPos(pos).down())) {
                    source.addParticle(ParticleTypes.FLAME, pos, Vec3d.ZERO);
                }
            });
            return true;
        }

        if (source.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return true;
        }

        float additional = source.getWorld().getLocalDifficulty(placement).getLocalDifficulty();

        Shape affectRegion = new Sphere(false, radius);

        if (source.getWorld().random.nextInt(100) != 0) {
            return true;
        }

        if (source.findAllEntitiesInRange(radius, e -> e instanceof ZombieEntity).count() >= 10 * (1 + additional)) {
            return false;
        }

        for (int i = 0; i < 10; i++) {
            Vec3d pos = affectRegion.computePoint(source.getWorld().random).add(origin);

            BlockPos loc = new BlockPos(pos);

            if (source.getWorld().isAir(loc.up()) && !source.getWorld().isAir(loc)) {
                spawnMonster(source, pos);
            }
        }

        return true;
    }

    protected void spawnMonster(Caster<?> source, Vec3d pos) {
        int index = (int)MathHelper.nextDouble(source.getWorld().random, 0, spawns.size());
        LivingEntity zombie = spawns.get(index).create(source.getWorld());

        source.subtractEnergyCost(3);

        zombie.updatePositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        zombie.setVelocity(0, 0.3, 0);

        source.getWorld().syncWorldEvent(WorldEvent.ZOMBIE_BREAK_WOODEN_DOOR, zombie.getBlockPos(), 0);

        source.getWorld().spawnEntity(zombie);
    }
}
