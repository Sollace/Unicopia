package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.*;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgCasterLookRequest;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.shape.*;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

public class PortalSpell extends AbstractSpell implements PlacementControlSpell.PlacementDelegate, OrientedSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.LIFE, 10)
            .with(Trait.KNOWLEDGE, 1)
            .with(Trait.ORDER, 25)
            .build();
    private static final Shape PARTICLE_AREA = new Sphere(true, 2, 1, 1, 0);

    private final DataTracker.Entry<Optional<UUID>> targetPortalId = dataTracker.startTracking(TrackableDataType.UUID, Optional.empty());
    private final DataTracker.Entry<Float> targetPortalPitch = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);
    private final DataTracker.Entry<Float> targetPortalYaw = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);
    private final EntityReference<Entity> teleportationTarget = dataTracker.startTracking(new EntityReference<>());

    private final DataTracker.Entry<Float> pitch = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);
    private final DataTracker.Entry<Float> yaw = dataTracker.startTracking(TrackableDataType.FLOAT, 0F);

    private Shape particleArea = PARTICLE_AREA;

    protected PortalSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    public EntityReference<Entity> getDestinationReference() {
        return teleportationTarget;
    }

    public float getPitch() {
        return pitch.get();
    }

    public float getYaw() {
        return yaw.get();
    }

    public float getTargetPitch() {
        return targetPortalPitch.get();
    }

    public float getTargetYaw() {
        return targetPortalYaw.get();
    }

    public float getYawDifference() {
        return MathHelper.wrapDegrees(180 + getTargetYaw() - getYaw());
    }

    @SuppressWarnings("unchecked")
    private Ether.Entry<PortalSpell> getDestination(Caster<?> source) {
        return targetPortalId.get().flatMap(id -> getDestinationReference()
                .getTarget()
                .map(target -> Ether.get(source.asWorld()).get((SpellType<PortalSpell>)getType(), target.uuid(), id))
                .filter(destination -> destination.isClaimedBy(getUuid()))
        ).orElse(null);
    }

    @Override
    public boolean apply(Caster<?> caster) {
        return toPlaceable().apply(caster);
    }

    protected void setDestination(@Nullable Ether.Entry<?> destination) {
        if (destination == null) {
            teleportationTarget.set(null);
            targetPortalId.set(Optional.empty());
        } else {
            teleportationTarget.copyFrom(destination.entity);
            targetPortalId.set(Optional.of(destination.getSpellId()));
            targetPortalPitch.set(destination.getPitch());
            targetPortalYaw.set(destination.getYaw());
        }
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (situation == Situation.GROUND) {
            if (source.isClient()) {
                source.spawnParticles(particleArea, 5, pos -> {
                    source.addParticle(ParticleTypes.ELECTRIC_SPARK, pos, Vec3d.ZERO);
                });
            } else {
                var ownEntry = Ether.get(source.asWorld()).get(this, source);
                synchronized (ownEntry) {
                    var targetEntry = getDestination(source);

                    if (targetEntry == null) {
                        if (teleportationTarget.isSet()) {
                            setDestination(null);
                            source.asWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, source.getOrigin(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
                        } else {
                            Ether.get(source.asWorld()).anyMatch(getType(), entry -> {
                                if (entry.isAlive() && !entry.hasClaimant() && !entry.entityMatches(source.asEntity().getUuid())) {
                                    entry.claim(getUuid());
                                    ownEntry.claim(entry.getSpellId());
                                    synchronized (entry) {
                                        if (entry.getSpell() instanceof PortalSpell portal) {
                                            portal.setDestination(ownEntry);
                                        }
                                    }
                                    setDestination(entry);
                                }
                                return false;
                            });
                        }
                    } else {
                        targetPortalPitch.set(targetEntry.getPitch());
                        targetPortalYaw.set(targetEntry.getYaw());

                        tickActive(source, targetEntry);
                    }
                }

                ownEntry.setPitch(getPitch());
                ownEntry.setYaw(getYaw());
            }
        }

        return !isDead();
    }

    private void tickActive(Caster<?> source, Ether.Entry<?> destination) {
        destination.entity.getTarget().ifPresent(target -> {
            Quaternionf rotationChange = getOrientationChange();
            var matrix = getPositionMatrix(source, source.asEntity().getPos(), rotationChange, new Matrix4f());

            float yawDifference = getYawDifference();

            source.findAllEntitiesInRange(1).forEach(entity -> {
                if (!entity.hasPortalCooldown()) {

                    float approachYaw = Math.abs(MathHelper.wrapDegrees(entity.getYaw() - this.yaw.get()));
                    if (approachYaw > 80) {
                        return;
                    }

                    var dest4f = matrix.transform(new Vector4f(entity.getPos().toVector3f(), 1));
                    Vec3d dest = new Vec3d(dest4f.x, dest4f.y - 0.5, dest4f.z).add(new Vec3d(0, 0, -0.7F).rotateY(-getTargetYaw() * MathHelper.RADIANS_PER_DEGREE));

                    for (int i = 0; i < 2; i++) {
                        BlockPos destBlock = BlockPos.ofFloored(dest);
                        BlockState state = entity.getWorld().getBlockState(destBlock);
                        if (entity.getWorld().isTopSolid(destBlock, entity)) {
                            double maxY = state.getCollisionShape(entity.getWorld(), destBlock).getMax(Axis.Y);
                            dest = new Vec3d(dest.x, destBlock.getY() + maxY, dest.z);
                        }
                    }

                    entity.resetPortalCooldown();

                    float yaw = MathHelper.wrapDegrees(entity.getYaw() + yawDifference);

                    entity.setVelocity(entity.getVelocity().rotateY(yawDifference * MathHelper.RADIANS_PER_DEGREE));

                    entity.getWorld().playSoundFromEntity(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundCategory(), 1, 1);
                    entity.teleport((ServerWorld)entity.getWorld(), dest.x, dest.y, dest.z, PositionFlag.VALUES, yaw, entity.getPitch());
                    entity.getWorld().playSoundFromEntity(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundCategory(), 1, 1);

                    Living.updateVelocity(entity);

                    if (!source.subtractEnergyCost(Math.sqrt(entity.getPos().subtract(dest).length()))) {
                        setDead();
                    }
                }

                ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), entity, 7);
            });
        });
    }

    public Matrix4f getPositionMatrix(Caster<?> source, Vec3d pos, Quaternionf orientationChange, Matrix4f matrix) {
        getDestinationReference().getTarget().ifPresent(destEntity -> {
            Vector3f destPos = destEntity.pos().toVector3f();
            Vector3f sourcePos = pos.toVector3f();

            matrix.rotateAround(orientationChange.conjugate(), destPos.x, destPos.y, destPos.z);
            matrix.translate(destPos.sub(sourcePos));
        });
        return matrix;
    }

    public Quaternionf getOrientationChange() {
        return new Quaternionf().rotateTo(
                Vec3d.fromPolar(getPitch(), getYaw()).toVector3f(),
                Vec3d.fromPolar(getTargetPitch(), getTargetYaw()).toVector3f()
        );
    }

    @Override
    public void setOrientation(Caster<?> caster, float pitch, float yaw) {
        this.pitch.set(90 - pitch);
        this.yaw.set(-yaw);
        particleArea = PARTICLE_AREA.rotate(
            this.pitch.get() * MathHelper.RADIANS_PER_DEGREE,
            (180 - this.yaw.get()) * MathHelper.RADIANS_PER_DEGREE
        );
    }

    @Override
    public void onPlaced(Caster<?> source, PlacementControlSpell parent) {
        Entity caster = source.asEntity();
        Vec3d targetPos = caster.getRotationVector().multiply(3).add(caster.getEyePos());
        parent.setOrientation(source, -90 - source.asEntity().getPitch(), -source.asEntity().getYaw());
        parent.setPosition(new Vec3d(targetPos.x, caster.getPos().y, targetPos.z));
        if (source instanceof Pony pony) {
            Channel.SERVER_REQUEST_PLAYER_LOOK.sendToPlayer(new MsgCasterLookRequest(parent.getUuid()), (ServerPlayerEntity)pony.asEntity());
        }
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (!caster.isClient()) {
            var destination = getDestination(caster);
            if (destination != null) {
                destination.release(getUuid());
            }
        }
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        targetPortalId.get().ifPresent(i -> compound.putUuid("targetPortalId", i));
        compound.put("teleportationTarget", teleportationTarget.toNBT(lookup));
        compound.putFloat("pitch", getPitch());
        compound.putFloat("yaw", getYaw());
        compound.putFloat("targetPortalPitch", getTargetPitch());
        compound.putFloat("targetPortalYaw", getTargetYaw());
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        targetPortalId.set(compound.containsUuid("targetPortalId") ? Optional.of(compound.getUuid("targetPortalId")) : Optional.empty());
        teleportationTarget.fromNBT(compound.getCompound("teleportationTarget"), lookup);
        pitch.set(compound.getFloat("pitch"));
        yaw.set(compound.getFloat("yaw"));
        targetPortalPitch.set(compound.getFloat("targetPortalPitch"));
        targetPortalYaw.set(compound.getFloat("targetPortalYaw"));
        particleArea = PARTICLE_AREA.rotate(
            pitch.get() * MathHelper.RADIANS_PER_DEGREE,
            (180 - yaw.get()) * MathHelper.RADIANS_PER_DEGREE
        );
    }
}
