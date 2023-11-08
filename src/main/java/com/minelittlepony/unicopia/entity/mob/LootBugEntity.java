package com.minelittlepony.unicopia.entity.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public class LootBugEntity extends SilverfishEntity {
    public LootBugEntity(EntityType<? extends SilverfishEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();

        getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK,
                getParticleX(1), this.getEyeY(), getParticleZ(1),
                random.nextFloat() - 0.5F, random.nextFloat(), random.nextFloat() - 0.5F
        );
    }
}
