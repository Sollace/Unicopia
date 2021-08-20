package com.minelittlepony.unicopia.entity;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ButterflyEntity extends AmbientEntity {
    private static final TrackedData<Boolean> RESTING = DataTracker.registerData(ButterflyEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(ButterflyEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Nullable
    private BlockPos hoveringPosition;

    private int ticksResting;

    public ButterflyEntity(EntityType<ButterflyEntity> type, World world) {
        super(type, world);
        setVariant(Variant.random(world.random));
        setResting(true);
    }

    public static DefaultAttributeContainer.Builder createButterflyAttributes() {
        return createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 2);
    }

    @Override
    public float getSoundPitch() {
        return super.getSoundPitch() * 0.95F;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_BAT_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        getDataTracker().startTracking(VARIANT, Variant.BUTTERFLY.ordinal());
        getDataTracker().startTracking(RESTING, false);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean collides() {
        return true;
    }

    @Override
    protected void pushAway(Entity entity) { }

    @Override
    protected void tickCramming() { }

    @Override
    public void tick() {
        super.tick();

        Vec3d vel = getVelocity();
        setVelocity(vel.x, vel.y * 0.6 + 0.02F, vel.z);
    }

    public boolean isResting() {
        return getDataTracker().get(RESTING);
    }

    public void setResting(boolean resting) {
        getDataTracker().set(RESTING, resting);
        if (!resting) {
            hoveringPosition = null;
        }
    }

    public Variant getVariant() {
        return Variant.byId(getDataTracker().get(VARIANT));
    }

    public void setVariant(Variant variant) {
        getDataTracker().set(VARIANT, variant.ordinal());
    }

    protected boolean isAggressor(Entity e) {
        if (e instanceof ButterflyEntity) {
            return false;
        }

        if (e instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)e;

            if (player.isCreative() || player.isSpectator()) {
                return false;
            }

            if (player.isSprinting() || player.forwardSpeed > 0 || player.sidewaysSpeed > 0) {
                return true;
            }
        } else if (!EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(e)) {
            return false;
        }

        return e.getVelocity().horizontalLength() > 1.4F;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        BlockPos below = new BlockPos(getPos().add(0, -0.5, 0));

        if (isResting()) {
            if (world.getBlockState(below).isAir()
                || !world.getOtherEntities(this, getBoundingBox().expand(7), this::isAggressor).isEmpty()
                || (ticksResting++ > 40 && world.random.nextInt(500) == 0)
                || world.hasRain(below)) {
                setResting(false);
            }
        } else {
            ticksResting = 0;

            // invalidate the hovering position
            if (hoveringPosition != null && (!world.isAir(hoveringPosition) || hoveringPosition.getY() < 1)) {
                hoveringPosition = null;
            }

            BlockPos pos = getBlockPos();

            // select a new hovering position
            if (hoveringPosition == null || random.nextInt(30) == 0 || hoveringPosition.getSquaredDistance(pos) < 4) {
                hoveringPosition = pos.add(
                        random.nextInt(7) - random.nextInt(7),
                        random.nextInt(6) - 2,
                        random.nextInt(7) - random.nextInt(7)
                );
            }

            // hover casually towards the chosen position
            Vec3d motion = Vec3d.ofCenter(hoveringPosition, 0.1).subtract(getPos());
            Vec3d vel = getVelocity();

            addVelocity(
                (Math.signum(motion.getX()) * 0.5 - vel.x) * 0.1,
                (Math.signum(motion.getY()) * 0.7 - vel.y) * 0.1,
                (Math.signum(motion.getZ()) * 0.5 - vel.z) * 0.1
            );

            float direction = (float)(MathHelper.atan2(vel.z, vel.x) * (180 / Math.PI)) - 90;

            forwardSpeed = 0.5F;
            headYaw += MathHelper.wrapDegrees(direction - headYaw);

            if (random.nextInt(100) == 0 && world.getBlockState(below).isOpaque()) {
                setResting(true);
            }

            if (!world.isClient && age % 20 == 0 && world.random.nextInt(200) == 0) {
                for (Entity i : world.getOtherEntities(this, getBoundingBox().expand(20))) {
                    if (i.getType() == getType()) {
                        return;
                    }
                }

                ButterflyEntity copy = (ButterflyEntity)getType().create(world);
                copy.copyPositionAndRotation(this);
                world.spawnEntity(copy);
            }
        }
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = 64 * getRenderDistanceMultiplier();
        return distance < d * d;
    }

    @Override
    public boolean handleFallDamage(float distance, float damageMultiplier, DamageSource cause) {
        return false;
    }

    @Override
    protected void fall(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        return reason != SpawnReason.NATURAL || (getY() >= world.getSeaLevel() && world.getLightLevel(getBlockPos()) > 3);
    }

    @Override
    public float getEyeHeight(EntityPose pose) {
        return getHeight() / 2;
    }

    public enum Variant {
        BUTTERFLY,
        YELLOW,
        LIME,
        RED,
        GREEN,
        BLUE,
        PURPLE,
        MAGENTA,
        PINK,
        HEDYLIDAE,
        LYCAENIDAE,
        NYMPHALIDAE,
        MONARCH,
        WHITE_MONARCH,
        BRIMSTONE;

        private static final Variant[] VALUES = Variant.values();

        private final Identifier skin = new Identifier("unicopia", "textures/entity/butterfly/" + name().toLowerCase() + ".png");

        public Identifier getSkin() {
            return skin;
        }

        static Variant byId(int index) {
            return VALUES[Math.max(0, index) % VALUES.length];
        }

        static Variant random(Random rand) {
            return VALUES[rand.nextInt(VALUES.length)];
        }
    }
}
