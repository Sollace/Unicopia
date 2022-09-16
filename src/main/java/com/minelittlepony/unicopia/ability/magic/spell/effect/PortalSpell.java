package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.block.data.Ether;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

public class PortalSpell extends AbstractSpell {
    public static final int MAX_COOLDOWN = 20;
    private final EntityReference<CastSpellEntity> teleportationTarget = new EntityReference<>();

    private boolean publishedPosition;

    private final ParticleHandle particlEffect = new ParticleHandle();

    private int cooldown = MAX_COOLDOWN;

    protected PortalSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean apply(Caster<?> caster) {
        return toPlaceable().apply(caster);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {

        if (situation == Situation.GROUND) {
            if (!source.isClient()) {
                teleportationTarget.getId().ifPresent(id -> {
                    Ether ether = Ether.get(source.getReferenceWorld());
                    if (ether.getEntry(getType(), id).isEmpty()) {
                        Unicopia.LOGGER.debug("Lost sibling, breaking connection to " + id);
                        teleportationTarget.set(null);
                        setDirty();
                        source.getReferenceWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, source.getOrigin(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
                    }
                });
            }

            teleportationTarget.getPosition().ifPresentOrElse(
                    targetPos -> tickWithTargetLink(source, targetPos),
                    () -> findLink(source)
            );

            if (!publishedPosition) {
                publishedPosition = true;
                Ether.get(source.getReferenceWorld()).put(getType(), source);
            }

            if (source.isClient() && cooldown <= 0) {
                Vec3d origin = source.getOriginVector();

                ParticleEffect effect = teleportationTarget.getPosition()
                        .map(target -> {
                            getType();
                            return new FollowingParticleEffect(UParticles.HEALTH_DRAIN, target, 0.2F).withChild(ParticleTypes.ELECTRIC_SPARK);
                        })
                        .orElseGet(() -> {
                            new MagicParticleEffect(getType().getColor());
                            return ParticleTypes.ELECTRIC_SPARK;
                        });

                source.spawnParticles(origin, new Sphere(true, 2, 1, 0, 1), 3, pos -> {
                    source.addParticle(effect, pos, Vec3d.ZERO);
                });
            }
        }

        return true;
    }

    private void tickWithTargetLink(Caster<?> source, Vec3d targetPos) {
        particlEffect.update(getUuid(), source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(UParticles.DISK, getType().getColor(), 0.8F, 2), source.getOriginVector(), Vec3d.ZERO);
        });

        if (cooldown > 0) {
            cooldown--;
            setDirty();
            return;
        }

        Vec3d center = source.getOriginVector();
        source.findAllEntitiesInRange(1).filter(e -> true).forEach(entity -> {
            if (!entity.hasPortalCooldown() && entity.timeUntilRegen <= 0) {
                Vec3d destination = entity.getPos().subtract(center).add(targetPos);
                entity.resetPortalCooldown();
                entity.timeUntilRegen = 100;

                entity.playSound(USounds.ENTITY_PLAYER_UNICORN_TELEPORT, 1, 1);
                entity.teleport(destination.x, destination.y, destination.z);
                entity.playSound(USounds.ENTITY_PLAYER_UNICORN_TELEPORT, 1, 1);
                setDirty();
            }

            ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), entity, 7);
        });
    }

    @SuppressWarnings("unchecked")
    private void findLink(Caster<?> source) {

        if (source.isClient()) {
            return;
        }

        Ether ether = Ether.get(source.getReferenceWorld());
        ether.getEntries(getType())
            .stream()
            .filter(entry -> entry.isAvailable() && !entry.entity.referenceEquals(source.getEntity()))
            .findAny()
            .ifPresent(entry -> {
                entry.setTaken(true);
                teleportationTarget.copyFrom((EntityReference<CastSpellEntity>)entry.entity);
                setDirty();
            });
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        Ether ether = Ether.get(caster.getReferenceWorld());
        ether.remove(getType(), caster.getEntity().getUuid());
        teleportationTarget.getId().flatMap(id -> ether.getEntry(getType(), id)).ifPresent(e -> e.setTaken(false));
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("publishedPosition", publishedPosition);
        compound.put("teleportationTarget", teleportationTarget.toNBT());
        compound.putInt("cooldown", cooldown);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        publishedPosition = compound.getBoolean("publishedPosition");
        teleportationTarget.fromNBT(compound.getCompound("teleportationTarget"));
        cooldown = compound.getInt("cooldown");
    }

    @Override
    public void setDead() {
        super.setDead();
        particlEffect.destroy();
    }
}
