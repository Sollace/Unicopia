package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

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
import com.minelittlepony.unicopia.particle.*;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.shape.*;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
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

    @Nullable
    private UUID targetPortalId;
    private float targetPortalPitch;
    private float targetPortalYaw;
    private final EntityReference<Entity> teleportationTarget = new EntityReference<>();

    private boolean publishedPosition;

    private float pitch;
    private float yaw;

    private Shape particleArea = PARTICLE_AREA;

    protected PortalSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    public EntityReference<Entity> getDestinationReference() {
        return teleportationTarget;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getTargetPitch() {
        return targetPortalPitch;
    }

    public float getTargetYaw() {
        return targetPortalYaw;
    }

    public float getYawDifference() {
        return MathHelper.wrapDegrees(180 + targetPortalYaw - yaw);
    }

    @SuppressWarnings("unchecked")
    private Ether.Entry<PortalSpell> getDestination(Caster<?> source) {
        return targetPortalId == null ? null : getDestinationReference()
                .getTarget()
                .map(target -> Ether.get(source.asWorld()).get((SpellType<PortalSpell>)getType(), target.uuid(), targetPortalId))
                .filter(destination -> destination.isClaimedBy(getUuid()))
                .orElse(null);
    }

    @Override
    public boolean apply(Caster<?> caster) {
        return toPlaceable().apply(caster);
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
                            teleportationTarget.set(null);
                            targetPortalId = null;
                            setDirty();
                            source.asWorld().syncWorldEvent(WorldEvents.BLOCK_BROKEN, source.getOrigin(), Block.getRawIdFromState(Blocks.GLASS.getDefaultState()));
                        } else {
                            Ether.get(source.asWorld()).anyMatch(getType(), entry -> {
                                if (entry.isAlive() && !entry.hasClaimant() && !entry.entityMatches(source.asEntity().getUuid())) {
                                    entry.claim(getUuid());
                                    ownEntry.claim(entry.getSpellId());
                                    synchronized (entry) {
                                        if (entry.getSpell() instanceof PortalSpell portal) {
                                            portal.teleportationTarget.copyFrom(ownEntry.entity);
                                            portal.targetPortalId = getUuid();
                                            portal.targetPortalPitch = pitch;
                                            portal.targetPortalYaw = yaw;
                                            portal.setDirty();
                                        }
                                    }
                                    teleportationTarget.copyFrom(entry.entity);
                                    targetPortalId = entry.getSpellId();
                                    targetPortalPitch = entry.getPitch();
                                    targetPortalYaw = entry.getYaw();
                                    setDirty();
                                }
                                return false;
                            });
                        }
                    } else {
                        tickActive(source, targetEntry);
                    }
                }
            }

            var entry = Ether.get(source.asWorld()).getOrCreate(this, source);
            entry.setPitch(pitch);
            entry.setYaw(yaw);
        }

        return !isDead();
    }

    private void tickActive(Caster<?> source, Ether.Entry<?> destination) {
        destination.entity.getTarget().ifPresent(target -> {
            source.findAllEntitiesInRange(1).forEach(entity -> {
                if (!entity.hasPortalCooldown()) {

                    float approachYaw = Math.abs(MathHelper.wrapDegrees(entity.getYaw() - this.yaw));
                    if (approachYaw > 80) {
                        return;
                    }

                    Vec3d offset = entity.getPos().subtract(source.asEntity().getPos())
                            .add(new Vec3d(0, 0, -0.7F).rotateY(-getTargetYaw() * MathHelper.RADIANS_PER_DEGREE));
                    float yawDifference = getYawDifference();
                    Vec3d dest = target.pos().add(offset.rotateY(yawDifference * MathHelper.RADIANS_PER_DEGREE)).add(0, 0.1, 0);

                    if (entity.getWorld().isTopSolid(BlockPos.ofFloored(dest).up(), entity)) {
                        dest = dest.add(0, 1, 0);
                    }

                    entity.resetPortalCooldown();

                    float yaw = MathHelper.wrapDegrees(entity.getYaw() + yawDifference);

                    entity.setVelocity(entity.getVelocity().rotateY(yawDifference * MathHelper.RADIANS_PER_DEGREE));

                    entity.getWorld().playSoundFromEntity(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundCategory(), 1, 1);
                    entity.teleport((ServerWorld)entity.getWorld(), dest.x, dest.y, dest.z, PositionFlag.VALUES, yaw, entity.getPitch());
                    entity.getWorld().playSoundFromEntity(null, entity, USounds.ENTITY_PLAYER_UNICORN_TELEPORT, entity.getSoundCategory(), 1, 1);
                    setDirty();

                    Living.updateVelocity(entity);

                    if (!source.subtractEnergyCost(Math.sqrt(entity.getPos().subtract(dest).length()))) {
                        setDead();
                    }
                }

                ParticleUtils.spawnParticles(new MagicParticleEffect(getType().getColor()), entity, 7);
            });
        });
    }

    @Override
    public void setOrientation(Caster<?> caster, float pitch, float yaw) {
        this.pitch = 90 - pitch;
        this.yaw = -yaw;
        particleArea = PARTICLE_AREA.rotate(
            this.pitch * MathHelper.RADIANS_PER_DEGREE,
            this.yaw * MathHelper.RADIANS_PER_DEGREE
        );
        setDirty();
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
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        if (targetPortalId != null) {
            compound.putUuid("targetPortalId", targetPortalId);
        }
        compound.putBoolean("publishedPosition", publishedPosition);
        compound.put("teleportationTarget", teleportationTarget.toNBT());
        compound.putFloat("pitch", pitch);
        compound.putFloat("yaw", yaw);
        compound.putFloat("targetPortalPitch", targetPortalPitch);
        compound.putFloat("targetPortalYaw", targetPortalYaw);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        targetPortalId = compound.containsUuid("targetPortalId") ? compound.getUuid("targetPortalId") : null;
        publishedPosition = compound.getBoolean("publishedPosition");
        teleportationTarget.fromNBT(compound.getCompound("teleportationTarget"));
        pitch = compound.getFloat("pitch");
        yaw = compound.getFloat("yaw");
        targetPortalPitch = compound.getFloat("targetPortalPitch");
        targetPortalYaw = compound.getFloat("targetPortalYaw");
        particleArea = PARTICLE_AREA.rotate(
            pitch * MathHelper.RADIANS_PER_DEGREE,
            (180 - yaw) * MathHelper.RADIANS_PER_DEGREE
        );
    }
}
