package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.block.data.Ether;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

public class PortalSpell extends AbstractSpell {
    private final EntityReference<CastSpellEntity> teleportationTarget = new EntityReference<>();

    private boolean publishedPosition;

    private final ParticleHandle particlEffect = new ParticleHandle();

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
            teleportationTarget.getPosition().ifPresentOrElse(
                    targetPos -> tickWithTargetLink(source, targetPos),
                    () -> findLink(source)
            );

            if (!publishedPosition) {
                publishedPosition = true;
                Ether.get(source.getReferenceWorld()).put(getType(), source);
            }

            if (source.isClient()) {
                Vec3d origin = source.getOriginVector();

                source.spawnParticles(origin, new Sphere(true, 2, 1, 0, 1), 17, pos -> {
                    source.addParticle(new MagicParticleEffect(getType().getColor()), pos, Vec3d.ZERO);
                });
            }
        }

        return true;
    }

    private void tickWithTargetLink(Caster<?> source, Vec3d targetPos) {
        particlEffect.update(getUuid(), source, spawner -> {
            spawner.addParticle(new SphereParticleEffect(UParticles.DISK, getType().getColor(), 0.9F, 2), source.getOriginVector(), Vec3d.ZERO);
        }).ifPresent(p -> {
            p.setAttribute(Attachment.ATTR_COLOR, getType().getColor());
        });

        Vec3d center = source.getOriginVector();
        source.findAllEntitiesInRange(1).filter(e -> true).forEach(entity -> {
            if (!entity.hasPortalCooldown() && entity.timeUntilRegen <= 0) {
                Vec3d destination = entity.getPos().subtract(center).add(targetPos);
                entity.resetPortalCooldown();
                entity.timeUntilRegen = 100;

                entity.playSound(USounds.ENTITY_PLAYER_UNICORN_TELEPORT, 1, 1);
                entity.teleport(destination.x, destination.y, destination.z);
            }
            ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), entity, 7);
        });
    }

    @SuppressWarnings("unchecked")
    private void findLink(Caster<?> source) {
        Ether ether = Ether.get(source.getReferenceWorld());
        ether.getEntries(getType())
            .stream()
            .filter(entry -> entry.isAvailable() && !entry.entity.referenceEquals(source.getEntity()))
            .findAny()
            .ifPresent(entry -> {
                entry.setTaken(true);
                teleportationTarget.copyFrom((EntityReference<CastSpellEntity>)entry.entity);
            });
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        Ether.get(caster.getReferenceWorld()).remove(getType(), caster.getEntity().getUuid());
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("publishedPosition", publishedPosition);
        compound.put("teleportationTarget", teleportationTarget.toNBT());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        publishedPosition = compound.getBoolean("publishedPosition");
        teleportationTarget.fromNBT(compound.getCompound("teleportationTarget"));
    }

    @Override
    public void setDead() {
        super.setDead();
        particlEffect.destroy();
    }
}
