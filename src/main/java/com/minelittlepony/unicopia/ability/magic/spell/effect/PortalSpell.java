package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.CastSpellEntity;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.shape.*;

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
    private static final Shape PARTICLE_AREA = new Sphere(true, 2, 1, 1, 0);

    private final EntityReference<Entity> teleportationTarget = new EntityReference<>();

    private boolean publishedPosition;

    private final ParticleHandle particleEffect = new ParticleHandle();

    private float pitch;
    private float yaw;

    private Shape particleArea = PARTICLE_AREA;

    protected PortalSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean apply(Caster<?> caster) {
        setOrientation(caster.asEntity().getPitch(), caster.asEntity().getYaw());
        return toPlaceable().apply(caster);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (situation == Situation.GROUND) {

            if (source.isClient()) {
                Vec3d origin = source.getOriginVector();

                ParticleEffect effect = teleportationTarget.getTarget()
                        .map(target -> {
                            getType();
                            return new FollowingParticleEffect(UParticles.HEALTH_DRAIN, target.pos(), 0.2F).withChild(ParticleTypes.ELECTRIC_SPARK);
                        })
                        .orElse(ParticleTypes.ELECTRIC_SPARK);

                source.spawnParticles(origin, particleArea, 5, pos -> {
                    source.addParticle(effect, pos, Vec3d.ZERO);
                });

                teleportationTarget.getTarget().ifPresentOrElse(target -> {
                    particleEffect.update(getUuid(), source, spawner -> {
                        spawner.addParticle(new SphereParticleEffect(UParticles.DISK, getType().getColor(), 0.8F, 1.8F, new Vec3d(pitch, yaw, 0)), source.getOriginVector(), Vec3d.ZERO);
                    });
                }, () -> {
                    particleEffect.destroy();
                });
            } else {
                teleportationTarget.getTarget().ifPresent(target -> {
                    if (Ether.get(source.asWorld()).getEntry(getType(), target.uuid()).isEmpty()) {
                        Unicopia.LOGGER.debug("Lost sibling, breaking connection to " + target.uuid());
                        teleportationTarget.set(null);
                        setDirty();
                        source.asWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, source.getOrigin(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
                    }
                });

                getTarget(source).ifPresentOrElse(
                        entry -> tickWithTargetLink(source, entry),
                        () -> findLink(source)
                );
            }

            if (!publishedPosition) {
                publishedPosition = true;
                Ether.Entry entry = Ether.get(source.asWorld()).put(getType(), source);
                entry.pitch = pitch;
                entry.yaw = yaw;
            }
        }

        return !isDead();
    }

    private void tickWithTargetLink(Caster<?> source, Ether.Entry destination) {

        destination.entity.getTarget().ifPresent(target -> {
            source.findAllEntitiesInRange(1).forEach(entity -> {
                if (!entity.hasPortalCooldown() && entity.timeUntilRegen <= 0) {
                    Vec3d offset = entity.getPos().subtract(source.getOriginVector());
                    float yawDifference = pitch < 15 ? (180 - yaw + destination.yaw) : 0;
                    Vec3d dest = target.pos().add(offset.rotateY(yawDifference * MathHelper.RADIANS_PER_DEGREE)).add(0, 0.05, 0);

                    entity.resetPortalCooldown();
                    entity.timeUntilRegen = 100;

                    entity.setYaw(entity.getYaw() + yawDifference);
                    entity.setVelocity(entity.getVelocity().rotateY(yawDifference * MathHelper.RADIANS_PER_DEGREE));

                    entity.getWorld().playSoundFromEntity(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundCategory(), 1, 1);
                    entity.teleport(dest.x, dest.y, dest.z);
                    entity.getWorld().playSoundFromEntity(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundCategory(), 1, 1);
                    setDirty();

                    if (!source.subtractEnergyCost(Math.sqrt(entity.getPos().subtract(dest).length()))) {
                        setDead();
                    }
                }

                ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), entity, 7);
            });
        });
    }

    private void findLink(Caster<?> source) {
        if (source.isClient()) {
            return;
        }

        Ether ether = Ether.get(source.asWorld());
        ether.getEntries(getType())
            .stream()
            .filter(entry -> entry.isAvailable() && !entry.entity.referenceEquals(source.asEntity()) && entry.entity.isSet())
            .findAny()
            .ifPresent(entry -> {
                entry.setTaken(true);
                teleportationTarget.copyFrom(entry.entity);
                setDirty();
            });
    }

    private Optional<Ether.Entry> getTarget(Caster<?> source) {
        return teleportationTarget.getTarget().flatMap(target -> Ether.get(source.asWorld()).getEntry(getType(), target.uuid()));
    }

    @Override
    public void setOrientation(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
        particleArea = PARTICLE_AREA.rotate(
            pitch * MathHelper.RADIANS_PER_DEGREE,
            (180 - yaw) * MathHelper.RADIANS_PER_DEGREE
        );
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
        Ether ether = Ether.get(caster.asWorld());
        ether.remove(getType(), caster.asEntity().getUuid());
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
        particleArea = PARTICLE_AREA.rotate(
            pitch * MathHelper.RADIANS_PER_DEGREE,
            (180 - yaw) * MathHelper.RADIANS_PER_DEGREE
        );
    }

    @Override
    public void setDead() {
        super.setDead();
        particleEffect.destroy();
    }
}
