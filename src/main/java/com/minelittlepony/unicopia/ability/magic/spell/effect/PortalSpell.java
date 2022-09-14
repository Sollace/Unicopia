package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.data.Ether;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

public class PortalSpell extends AbstractSpell implements PlaceableSpell.PlacementDelegate, OrientedSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.LIFE, 10)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.ORDER, 25)
            .build();

    private final EntityReference<Entity> teleportationTarget = new EntityReference<>();

    private boolean publishedPosition;

    private final ParticleHandle particleEffect = new ParticleHandle();

    private float pitch;
    private float yaw;

    protected PortalSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean apply(Caster<?> caster) {
        setOrientation(caster.getEntity().getPitch(), caster.getEntity().getYaw());
        return toPlaceable().apply(caster);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (situation == Situation.GROUND) {

            if (source.isClient()) {

                Vec3d origin = source.getOriginVector();

                ParticleEffect effect = teleportationTarget.getPosition()
                        .map(target -> {
                            getType();
                            return new FollowingParticleEffect(UParticles.HEALTH_DRAIN, target, 0.2F).withChild(ParticleTypes.ELECTRIC_SPARK);
                        })
                        .orElse(ParticleTypes.ELECTRIC_SPARK);

                source.spawnParticles(origin, new Sphere(true, 2, 1, 1, 1), 1, pos -> {
                    source.addParticle(effect, pos, Vec3d.ZERO);
                });

                teleportationTarget.getPosition().ifPresentOrElse(position -> {
                    particleEffect.update(getUuid(), source, spawner -> {
                        spawner.addParticle(new SphereParticleEffect(UParticles.DISK, getType().getColor(), 0.8F, 2, new Vec3d(pitch, yaw, 0)), source.getOriginVector(), Vec3d.ZERO);
                    });
                }, () -> {
                    particleEffect.destroy();
                });
            } else {
                teleportationTarget.getId().ifPresent(id -> {
                    if (Ether.get(source.getReferenceWorld()).getEntry(getType(), id).isEmpty()) {
                        Unicopia.LOGGER.debug("Lost sibling, breaking connection to " + id);
                        teleportationTarget.set(null);
                        setDirty();
                        source.getReferenceWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, source.getOrigin(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
                    }
                });

                getTarget(source).ifPresentOrElse(
                        entry -> tickWithTargetLink(source, entry),
                        () -> findLink(source)
                );
            }

            if (!publishedPosition) {
                publishedPosition = true;
                Ether.Entry entry = Ether.get(source.getReferenceWorld()).put(getType(), source);
                entry.pitch = pitch;
                entry.yaw = yaw;
            }


        }

        return !isDead();
    }

    private void tickWithTargetLink(Caster<?> source, Ether.Entry destination) {

        destination.entity.getPosition().ifPresent(targetPos -> {
            source.findAllEntitiesInRange(1).forEach(entity -> {
                if (!entity.hasNetherPortalCooldown() && entity.timeUntilRegen <= 0) {
                    Vec3d offset = entity.getPos().subtract(source.getOriginVector());
                    float yawDifference = pitch < 15 ? (180 - yaw + destination.yaw) : 0;
                    Vec3d dest = targetPos.add(offset.rotateY(yawDifference * MathHelper.RADIANS_PER_DEGREE)).add(0, 0.05, 0);

                    entity.resetNetherPortalCooldown();
                    entity.timeUntilRegen = 100;

                    entity.setYaw(entity.getYaw() + yawDifference);
                    entity.setVelocity(entity.getVelocity().rotateY(yawDifference * MathHelper.RADIANS_PER_DEGREE));

                    entity.world.playSoundFromEntity(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundCategory(), 1, 1);
                    entity.teleport(dest.x, dest.y, dest.z);
                    entity.world.playSoundFromEntity(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundCategory(), 1, 1);
                    setDirty();

                    source.subtractEnergyCost(Math.sqrt(entity.getPos().subtract(dest).length()));
                }

                ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), entity, 7);
            });
        });
    }

    private void findLink(Caster<?> source) {
        if (source.isClient()) {
            return;
        }

        Ether ether = Ether.get(source.getReferenceWorld());
        ether.getEntries(getType())
            .stream()
            .filter(entry -> entry.isAvailable() && !entry.entity.referenceEquals(source.getEntity()) && entry.entity.getId().isPresent())
            .findAny()
            .ifPresent(entry -> {
                entry.setTaken(true);
                teleportationTarget.copyFrom(entry.entity);
                setDirty();
            });
    }

    private Optional<Ether.Entry> getTarget(Caster<?> source) {
        return teleportationTarget.getId().flatMap(id -> Ether.get(source.getReferenceWorld()).getEntry(getType(), id));
    }

    @Override
    public void setOrientation(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
        setDirty();
    }

    @Override
    public void onPlaced(Caster<?> source, PlaceableSpell parent, CastSpellEntity entity) {
        LivingEntity caster = source.getMaster();
        Vec3d targetPos = caster.getRotationVector().multiply(3).add(caster.getEyePos());
        parent.setOrientation(pitch, yaw);
        entity.setPos(targetPos.x, caster.getY() + 1.5, targetPos.z);
    }

    @Override
    public void updatePlacement(Caster<?> source, PlaceableSpell parent) {
        parent.getParticleEffectAttachment(source).ifPresent(attachment -> {
            attachment.setAttribute(Attachment.ATTR_RADIUS, 2);
            attachment.setAttribute(Attachment.ATTR_OPACITY, 0.92F);
        });
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        Ether ether = Ether.get(caster.getReferenceWorld());
        ether.remove(getType(), caster.getEntity().getUuid());
        getTarget(caster).ifPresent(e -> e.setTaken(false));
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("publishedPosition", publishedPosition);
        compound.put("teleportationTarget", teleportationTarget.toNBT());
        compound.putFloat("pitch", pitch);
        compound.putFloat("yaw", yaw);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        publishedPosition = compound.getBoolean("publishedPosition");
        teleportationTarget.fromNBT(compound.getCompound("teleportationTarget"));
        pitch = compound.getFloat("pitch");
        yaw = compound.getFloat("yaw");
    }

    @Override
    public void setDead() {
        super.setDead();
        particleEffect.destroy();
    }
}
