package com.minelittlepony.unicopia.magic.spell;

import java.util.List;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.magic.AbstractSpell;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.WorldEvent;
import com.minelittlepony.unicopia.util.shape.IShape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

public class NecromancySpell extends AbstractSpell.RangedAreaSpell {

    private final List<EntityType<? extends LivingEntity>> spawns = Lists.newArrayList(
            EntityType.ZOMBIE,
            EntityType.HUSK,
            EntityType.ZOMBIE_PIGMAN
    );

    @Override
    public String getName() {
        return "necromancy";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.BAD;
    }

    @Override
    public int getTint() {
        return 0x3A3A3A;
    }

    @Override
    public boolean update(ICaster<?> source) {

        if (source.getWorld().isClient || source.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return true;
        }


        float additional = source.getWorld().getLocalDifficulty(source.getOrigin()).getLocalDifficulty();

        int radius = source.getCurrentLevel() + 1;

        IShape affectRegion = new Sphere(false, radius * 4);

        if (source.getWorld().random.nextInt(100) != 0) {
            return true;
        }

        Vec3d origin = source.getOriginVector();

        if (VecHelper.findAllEntitiesInRange(source.getEntity(), source.getWorld(), source.getOrigin(), radius * 4)
                .filter(e -> e instanceof ZombieEntity)
                .count() >= 10 * (1 + additional)) {
            return true;
        }

        for (int i = 0; i < 10; i++) {
            Vec3d pos = affectRegion.computePoint(source.getWorld().random).add(origin);

            BlockPos loc = new BlockPos(pos);

            if (source.getWorld().isAir(loc.up()) && !source.getWorld().isAir(loc)) {
                spawnMonster(source, pos);

                return true;
            }
        }


        return true;
    }

    protected void spawnMonster(ICaster<?> source, Vec3d pos) {
        int index = (int)MathHelper.nextDouble(source.getWorld().random, 0, spawns.size());
        LivingEntity zombie = spawns.get(index).create(source.getWorld());
        zombie.setPosition(pos.x, pos.y, pos.z);

        zombie.setVelocity(0, 0.3, 0);

        WorldEvent.DOOR_BROKEN.play(source.getWorld(), zombie.getBlockPos());

        source.getWorld().spawnEntity(zombie);
    }

    @Override
    public void render(ICaster<?> source) {
        IShape affectRegion = new Sphere(false, (1 + source.getCurrentLevel()) * 4);

        source.spawnParticles(affectRegion, 5, pos -> {
            if (!source.getWorld().isAir(new BlockPos(pos).down())) {
                source.getWorld().addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0, 0, 0);
            }
        });
    }
}
