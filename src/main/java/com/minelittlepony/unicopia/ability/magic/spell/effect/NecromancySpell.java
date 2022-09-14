package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractAreaEffectSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.util.Weighted;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.WorldEvents;

/**
 * An area-effect spell that summons the undead.
 */
public class NecromancySpell extends AbstractAreaEffectSpell {
    private final Weighted<EntityType<? extends LivingEntity>> spawnPool = new Weighted<EntityType<? extends LivingEntity>>()
            .put(7, EntityType.ZOMBIE)
            .put(4, EntityType.HUSK)
            .put(3, EntityType.DROWNED)
            .put(2, EntityType.ZOMBIFIED_PIGLIN)
            .put(1, EntityType.ZOMBIE_VILLAGER);

    private final List<EntityReference<LivingEntity>> summonedEntities = new ArrayList<>();

    private int spawnCountdown;

    protected NecromancySpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        float radius = source.getLevel().getScaled(4) * 4 + getTraits().get(Trait.POWER);

        if (radius <= 0) {
            return false;
        }

        boolean rainy = source.getReferenceWorld().hasRain(source.getOrigin());

        if (source.isClient()) {
            source.spawnParticles(new Sphere(true, radius * 2), rainy ? 98 : 125, pos -> {
                BlockPos bpos = new BlockPos(pos);

                if (!source.getReferenceWorld().isAir(bpos.down())) {
                    source.addParticle(source.getReferenceWorld().hasRain(bpos) ? ParticleTypes.SMOKE : ParticleTypes.FLAME, pos, Vec3d.ZERO);
                }
            });
            return true;
        }

        if (source.getReferenceWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return true;
        }

        summonedEntities.removeIf(ref -> ref.getOrEmpty(source.getReferenceWorld()).filter(e -> {
            if (e.getPos().distanceTo(source.getOriginVector()) > radius * 2) {
                e.world.sendEntityStatus(e, (byte)60);
                e.discard();
                return false;
            }
            return true;
        }).isEmpty());

        float additional = source.getReferenceWorld().getLocalDifficulty(source.getOrigin()).getLocalDifficulty() + getTraits().get(Trait.CHAOS, 0, 10);

        setDirty();
        if (--spawnCountdown > 0 && !summonedEntities.isEmpty()) {
            return true;
        }
        spawnCountdown = 1200 + source.getReferenceWorld().random.nextInt(rainy ? 2000 : 1000);

        if (summonedEntities.size() > 10 + additional) {
            return true;
        }

        Shape affectRegion = new Sphere(false, radius);

        for (int i = 0; i < 10; i++) {
            Vec3d pos = affectRegion.computePoint(source.getReferenceWorld().random).add(source.getOriginVector());

            BlockPos loc = new BlockPos(pos);

            if (source.getReferenceWorld().isAir(loc.up()) && !source.getReferenceWorld().isAir(loc)) {
                spawnPool.get().ifPresent(type -> {
                    spawnMonster(source, pos, type);
                });
            }
        }

        return true;
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        if (caster.isClient()) {
            return;
        }
        LivingEntity master = caster.getMaster();
        summonedEntities.forEach(ref -> {
            ref.ifPresent(caster.getReferenceWorld(), e -> {
                if (master != null) {
                    master.applyDamageEffects(master, e);
                }
                e.world.sendEntityStatus(e, (byte)60);
                e.discard();
            });
        });
    }

    protected void spawnMonster(Caster<?> source, Vec3d pos, EntityType<? extends LivingEntity> type) {
        LivingEntity minion = type.create(source.getReferenceWorld());

        source.subtractEnergyCost(3);

        minion.updatePositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        minion.setVelocity(0, 0.3, 0);

        source.getReferenceWorld().syncWorldEvent(WorldEvents.DRAGON_BREATH_CLOUD_SPAWNS, minion.getBlockPos(), 0);
        source.playSound(SoundEvents.BLOCK_BELL_USE, 1, 0.3F);
        source.spawnParticles(ParticleTypes.LARGE_SMOKE, 10);
        minion.equipStack(EquipmentSlot.HEAD, Items.IRON_HELMET.getDefaultStack());

        Equine.of(minion).filter(eq -> eq instanceof Creature).ifPresent(eq -> {
            ((Creature)eq).setMaster(source);
        });

        source.getReferenceWorld().spawnEntity(minion);
        summonedEntities.add(new EntityReference<>(minion));
        setDirty();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("spawnCountdown", spawnCountdown);
        if (summonedEntities.size() > 0) {
            NbtList list = new NbtList();
            summonedEntities.forEach(ref -> list.add(ref.toNBT()));
            compound.put("summonedEntities", list);
        }
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        spawnCountdown = compound.getInt("spawnCountdown");
        summonedEntities.clear();
        if (compound.contains("summonedEntities")) {
            compound.getList("summonedEntities", NbtElement.COMPOUND_TYPE).forEach(tag -> {
                summonedEntities.add(new EntityReference<>((NbtCompound)tag));
            });
        }
    }
}
