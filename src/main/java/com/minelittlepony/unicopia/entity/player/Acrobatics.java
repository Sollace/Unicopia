package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation.Recipient;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.mob.StormCloudEntity;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Acrobatics implements Tickable, NbtSerialisable {
    static final TrackedData<Optional<BlockPos>> HANGING_POSITION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    private int ticksHanging;

    private Direction attachDirection;
    double distanceClimbed;

    private final Pony pony;
    private final PlayerEntity entity;

    public Acrobatics(Pony pony) {
        this.pony = pony;
        this.entity = pony.asEntity();
        pony.addTicker(this::checkDislodge);
    }

    public void initDataTracker() {
        entity.getDataTracker().startTracking(HANGING_POSITION, Optional.empty());
    }

    public boolean isImmobile() {
        return isFloppy() && entity.isOnGround();
    }

    public boolean isFloppy() {
        if (entity.isCreative() && entity.getAbilities().flying) {
            return false;
        }
        return pony.getCompositeRace().any(Race::isFish) && !entity.isTouchingWater() && !entity.getWorld().isWater(StormCloudEntity.findSurfaceBelow(entity.getWorld(), entity.getBlockPos()));
    }

    @Override
    public void tick() {
        BlockPos climbingPos = entity.getClimbingPos().orElse(null);

        if (!pony.getPhysics().isFlying() && !entity.getAbilities().flying
                && climbingPos != null
                && pony.getObservedSpecies() == Race.CHANGELING
                && !entity.getBlockStateAtPos().isIn(BlockTags.CLIMBABLE)) {
            Vec3d vel = entity.getVelocity();
            if (entity.isSneaking()) {
                entity.setVelocity(vel.x, 0, vel.z);
            }

            distanceClimbed += Math.abs(pony.getMotion().getClientVelocity().y);
            BlockPos hangingPos = entity.getBlockPos().up();
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
        return entity.getDataTracker().get(HANGING_POSITION);
    }

    public boolean isHanging() {
        return getHangingPosition().isPresent();
    }

    public void stopHanging() {
        entity.getDataTracker().set(HANGING_POSITION, Optional.empty());
        entity.calculateDimensions();
        ticksHanging = 0;
    }

    public void startHanging(BlockPos pos) {
        entity.getDataTracker().set(HANGING_POSITION, Optional.of(pos));
        entity.teleport(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5);
        entity.setVelocity(Vec3d.ZERO);
        entity.setSneaking(false);
        entity.stopFallFlying();
        pony.getPhysics().cancelFlight(true);
    }

    public boolean canHangAt(BlockPos pos) {
        if (!pony.asWorld().isAir(pos) || !pony.asWorld().isAir(pos.down())) {
            return false;
        }

        pos = pos.up();
        BlockState state = pony.asWorld().getBlockState(pos);

        return state.isSolidSurface(pony.asWorld(), pos, entity, Direction.DOWN) && entity.getWorld().isAir(entity.getBlockPos().down());
    }

    private boolean canKeepHanging() {
        Race race = pony.getObservedSpecies();
        if (!race.canHang()) {
            return false;
        }
        if (ticksHanging++ <= 2) {
            return true;
        }
        return getHangingPosition().filter(hangingPos -> {
            return (race != Race.BAT || hangingPos.equals(pony.getOrigin().down())) && canHangAt(hangingPos);
        }).isPresent();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putInt("ticksHanging", ticksHanging);
        BLOCK_POS.writeOptional("hangingPosition", compound, getHangingPosition());
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        ticksHanging = compound.getInt("ticksHanging");
        pony.asEntity().getDataTracker().set(HANGING_POSITION, NbtSerialisable.BLOCK_POS.readOptional("hangingPosition", compound));
    }
}
