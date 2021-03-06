package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.util.Weighted;
import com.minelittlepony.unicopia.util.WorldEvent;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

public class NecromancySpell extends AbstractPlacedSpell {

    private final Weighted<EntityType<? extends LivingEntity>> spawnPool = new Weighted<EntityType<? extends LivingEntity>>()
            .put(7, EntityType.ZOMBIE)
            .put(4, EntityType.HUSK)
            .put(2, EntityType.ZOMBIFIED_PIGLIN)
            .put(1, EntityType.ZOMBIE_VILLAGER)
            .put(1, EntityType.ZOMBIE_HORSE);

    private final List<EntityReference<LivingEntity>> summonedEntities = new ArrayList<>();

    protected NecromancySpell(SpellType<?> type) {
        super(type);
    }

    @Override
    public boolean onGroundTick(Caster<?> source) {
        super.onGroundTick(source);

        int radius = (source.getLevel().get() + 1) * 4;

        if (source.isClient()) {
            source.spawnParticles(new Sphere(false, radius), 5, pos -> {
                if (!source.getWorld().isAir(new BlockPos(pos).down())) {
                    source.addParticle(ParticleTypes.FLAME, pos, Vec3d.ZERO);
                }
            });
            return true;
        }

        if (source.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return true;
        }

        summonedEntities.removeIf(ref -> !ref.isPresent(source.getWorld()));

        float additional = source.getWorld().getLocalDifficulty(source.getOrigin()).getLocalDifficulty();

        Shape affectRegion = new Sphere(false, radius);

        if (source.getWorld().random.nextInt(100) != 0) {
            return true;
        }

        if (source.findAllEntitiesInRange(radius, e -> e instanceof ZombieEntity).count() >= 10 * (1 + additional)) {
            return false;
        }

        for (int i = 0; i < 10; i++) {
            Vec3d pos = affectRegion.computePoint(source.getWorld().random).add(source.getOriginVector());

            BlockPos loc = new BlockPos(pos);

            if (source.getWorld().isAir(loc.up()) && !source.getWorld().isAir(loc)) {
                spawnPool.get().ifPresent(type -> {
                    spawnMonster(source, pos, type);
                });
            }
        }

        return true;
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        LivingEntity master = caster.getMaster();
        summonedEntities.forEach(ref -> {
            ref.ifPresent(caster.getWorld(), e -> {
                if (master != null) {
                    master.dealDamage(master, e);
                }
                if (caster.getWorld().random.nextInt(2000) != 0) {
                    e.setHealth(0);
                }
            });
        });
    }

    protected void spawnMonster(Caster<?> source, Vec3d pos, EntityType<? extends LivingEntity> type) {
        LivingEntity zombie = type.create(source.getWorld());

        source.subtractEnergyCost(3);

        zombie.updatePositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        zombie.setVelocity(0, 0.3, 0);

        source.getWorld().syncWorldEvent(WorldEvent.ZOMBIE_BREAK_WOODEN_DOOR, zombie.getBlockPos(), 0);

        source.getWorld().spawnEntity(zombie);

        EntityReference<LivingEntity> ref = new EntityReference<>();
        ref.set(zombie);
        summonedEntities.add(ref);
        setDirty();
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);
        if (summonedEntities.size() > 0) {
            ListTag list = new ListTag();
            summonedEntities.forEach(ref -> list.add(ref.toNBT()));
            compound.put("summonedEntities", list);
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);
        summonedEntities.clear();
        if (compound.contains("summonedEntities")) {
            compound.getList("summonedEntities", 10).forEach(tag -> {
                EntityReference<LivingEntity> ref = new EntityReference<>();
                ref.fromNBT((CompoundTag)tag);
                summonedEntities.add(ref);
            });
        }
    }
}
