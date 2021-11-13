package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.util.Weighted;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldEvents;

public class NecromancySpell extends AbstractSpell {

    private final Weighted<EntityType<? extends LivingEntity>> spawnPool = new Weighted<EntityType<? extends LivingEntity>>()
            .put(7, EntityType.ZOMBIE)
            .put(4, EntityType.HUSK)
            .put(2, EntityType.ZOMBIFIED_PIGLIN)
            .put(1, EntityType.ZOMBIE_VILLAGER)
            .put(1, EntityType.ZOMBIE_HORSE);

    private final List<EntityReference<LivingEntity>> summonedEntities = new ArrayList<>();

    protected NecromancySpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        float radius = (source.getLevel().get() + 1) * 4 + getTraits().get(Trait.POWER);

        if (radius <= 0) {
            return false;
        }

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

        float additional = source.getWorld().getLocalDifficulty(source.getOrigin()).getLocalDifficulty() + getTraits().get(Trait.CHAOS, 0, 10);

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
        LivingEntity master = caster.getMaster();
        summonedEntities.forEach(ref -> {
            ref.ifPresent(caster.getWorld(), e -> {
                if (master != null) {
                    master.applyDamageEffects(master, e);
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

        source.getWorld().syncWorldEvent(WorldEvents.ZOMBIE_BREAKS_WOODEN_DOOR, zombie.getBlockPos(), 0);

        source.getWorld().spawnEntity(zombie);

        EntityReference<LivingEntity> ref = new EntityReference<>();
        ref.set(zombie);
        summonedEntities.add(ref);
        setDirty();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        if (summonedEntities.size() > 0) {
            NbtList list = new NbtList();
            summonedEntities.forEach(ref -> list.add(ref.toNBT()));
            compound.put("summonedEntities", list);
        }
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        summonedEntities.clear();
        if (compound.contains("summonedEntities")) {
            compound.getList("summonedEntities", 10).forEach(tag -> {
                EntityReference<LivingEntity> ref = new EntityReference<>();
                ref.fromNBT((NbtCompound)tag);
                summonedEntities.add(ref);
            });
        }
    }
}
