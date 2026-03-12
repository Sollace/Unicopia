package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation.Recipient;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.util.PosHelper;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Acrobatics implements Tickable, NbtSerialisable {
    private int ticksHanging;

    private Direction attachDirection;
    double distanceClimbed;

    private final Pony pony;
    private final PlayerEntity entity;

    private final DataTracker.Entry<Optional<BlockPos>> hangingPos;

    public Acrobatics(Pony pony, DataTracker tracker) {
        this.pony = pony;
        this.entity = pony.asEntity();
        this.hangingPos = tracker.startTracking(TrackableDataType.OPTIONAL_POS, Optional.empty());
        pony.addTicker(this::checkDislodge);
    }

    public boolean isImmobile() {
        return isFloppy() && entity.isOnGround();
    }

    public boolean isFloppy() {
        if (entity.isCreative() && entity.getAbilities().flying) {
            return false;
        }
        return pony.getCompositeRace().any(Race::isFish) && !entity.isTouchingWater() && !entity.getWorld().isWater(PosHelper.findNearestSurface(entity.getWorld(), entity.getBlockPos()));
    }

    @Override
    public void tick() {
        if (!pony.getPhysics().isFlying() && !entity.getAbilities().flying
                && entity.getClimbingPos().isPresent()
                && pony.getObservedSpecies() == Race.CHANGELING
                && !entity.getBlockStateAtPos().isIn(BlockTags.CLIMBABLE)) {
            Vec3d vel = entity.getVelocity();
            if (entity.isSneaking()) {
                entity.setVelocity(vel.x, 0, vel.z);
            }

            distanceClimbed += Math.abs(pony.getMotion().getTrackedVelocity().y);

            BlockPos hangingPos = pony.getPhysics().getHeadPosition();
            boolean canhangHere = canHangAt(hangingPos);

            if (distanceClimbed > 1.5) {
                if (vel.length() > 0.08F && entity.age % (3 + entity.getRandom().nextInt(5)) == 0) {
                    entity.playSound(USounds.ENTITY_PLAYER_CHANGELING_CLIMB,
                            (float)entity.getRandom().nextTriangular(0.5, 0.3),
                            entity.getSoundPitch()
                    );
                }

                boolean skipHangCheck = false;
                Direction newAttachDirection = entity.getHorizontalFacing();
                if (isFaceClimbable(entity.getWorld(), entity.getBlockPos(), newAttachDirection) && (newAttachDirection != attachDirection)) {
                    attachDirection = newAttachDirection;
                    skipHangCheck = true;
                }

                if (!skipHangCheck && canhangHere) {
                    if (!isHanging()) {
                        startHanging(hangingPos);
                    } else {
                        if (((LivingEntityDuck)entity).isJumping()) {
                            // Jump to let go
                            return;
                        }
                        entity.setVelocity(entity.getVelocity().multiply(1, 0, 1));
                        entity.setSneaking(false);
                    }
                } else if (attachDirection != null) {
                    if (isFaceClimbable(entity.getWorld(), entity.getBlockPos(), attachDirection)) {
                        entity.setBodyYaw(attachDirection.asRotation());
                        entity.prevBodyYaw = attachDirection.asRotation();
                    } else {
                        entity.setVelocity(vel);
                        entity.isClimbing();
                    }
                }
            }

            if (canhangHere) {
                pony.setAnimation(Animation.HANG, Recipient.ANYONE);
            } else if (distanceClimbed > 1.5) {
                pony.setAnimation(Animation.CLIMB, Recipient.ANYONE);
            }
        } else {
            distanceClimbed = 0;
            attachDirection = null;
        }
    }

    private void checkDislodge() {
        if (isHanging()) {
            ((LivingEntityDuck)entity).setLeaningPitch(0);
            if (!pony.isClient() && !canKeepHanging()) {
                stopHanging();
            }
        } else {
            ticksHanging = 0;
        }


        if (pony.getCompositeRace().includes(Race.SEAPONY) && !entity.isSubmergedInWater() && pony.landedChanged()) {
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_GUARDIAN_FLOP, SoundCategory.PLAYERS, 1, 1);
        }
    }

    boolean isFaceClimbable(World world, BlockPos pos, Direction direction) {
        pos = pos.offset(direction);
        BlockState state = world.getBlockState(pos);
        return !state.isOf(Blocks.SCAFFOLDING) && state.isSideSolid(world, pos, direction, SideShapeType.CENTER);
    }

    public Optional<BlockPos> getHangingPosition() {
        return hangingPos.get();
    }

    public boolean isHanging() {
        return getHangingPosition().isPresent();
    }

    public void stopHanging() {
        hangingPos.set(Optional.empty());
        entity.calculateDimensions();
        ticksHanging = 0;
    }

    public void startHanging(BlockPos pos) {
        hangingPos.set(Optional.of(pos));
        entity.requestTeleport(pos.getX() + 0.5, pos.getY() + pony.getPhysics().getGravitySignum(), pos.getZ() + 0.5);
        entity.setVelocity(Vec3d.ZERO);
        entity.setSneaking(false);
        entity.stopGliding();
        pony.getPhysics().cancelFlight(true);
    }

    public boolean canHangAt(BlockPos pos) {
        int gravity = pony.getPhysics().getGravitySignum() * (isHanging() && pony.getCompositeRace().includes(Race.BAT) ? -1 : 1);
        BlockState state = pony.asWorld().getBlockState(pos);

        if (!pony.asWorld().isBlockSpaceEmpty(pony.asEntity(), pony.getPhysics().getBoxAtPosition(pos.toBottomCenterPos(), gravity > 0))) {
            return false;
        }

        pos = pos.up(gravity);
        state = pony.asWorld().getBlockState(pos);
        return state.isSolidSurface(pony.asWorld(), pos, entity, gravity > 0 ? Direction.UP : Direction.DOWN);
    }

    private boolean canKeepHanging() {
        return pony.getCompositeRace().any(Race::canHang) && (ticksHanging++ <= 20 || getHangingPosition().filter(hangingPos -> {

            int y = (int)(pony.asEntity().getBoundingBox().maxY - 0.5);
            var pos = pony.asEntity().getBlockPos().withY(y);
            Debug.drawBoxSelection(pony, ParticleTypes.PORTAL, hangingPos);
            return pony.getCompositeRace().includes(Race.CHANGELING) || hangingPos.isWithinDistance(pos, 1.5) && (!pony.isPosLoaded(hangingPos) || canHangAt(hangingPos));
        }).isPresent());
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.putInt("ticksHanging", ticksHanging);
        getHangingPosition().ifPresent(pos -> {
            compound.put("hangingPosition", NbtSerialisable.encode(BlockPos.CODEC, pos, lookup));
        });
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        ticksHanging = compound.getInt("ticksHanging");
        hangingPos.set(NbtSerialisable.decode(BlockPos.CODEC, compound.get("hangingPosition"), lookup));
    }
}
