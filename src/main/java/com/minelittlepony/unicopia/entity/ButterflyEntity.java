package com.minelittlepony.unicopia.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.NbtSerialisable;

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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ButterflyEntity extends AmbientEntity {
    private static final int MAX_BREEDING_COOLDOWN = 300;
    private static final int MAX_REST_TICKS = 40;
    private static final int BREEDING_INTERVAL = 20;
    private static final int FLOWER_DETECTION_RANGE = 10;
    private static final int FLOWER_UPDATE_INTERVAL = 100;

    private static final TrackedData<Boolean> RESTING = DataTracker.registerData(ButterflyEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(ButterflyEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private Optional<BlockPos> hoveringPosition = Optional.empty();
    private Optional<BlockPos> flowerPosition = Optional.empty();

    private final Map<BlockPos, Long> visited = new HashMap<>();

    private int ticksResting;
    private int breedingCooldown;

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
        return USounds.ENTITY_BUTTERFLY_HURT;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
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
            hoveringPosition = Optional.empty();
            flowerPosition = Optional.empty();
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

        if (breedingCooldown > 0) {
            breedingCooldown--;
        }

        BlockPos below = new BlockPos(getPos().add(0, -0.5, 0));

        visited.entrySet().removeIf(e -> e.getValue() < age - 500);

        if (isResting()) {
            if (!flowerPosition.isPresent()) {
                setResting(false);
                return;
            }

            if (world.getBlockState(below).isAir()
                || !world.getOtherEntities(this, getBoundingBox().expand(3), this::isAggressor).isEmpty()
                || (ticksResting++ > MAX_REST_TICKS || world.random.nextInt(500) == 0)
                || world.hasRain(below)) {
                setResting(false);
                return;
            }

            if (!world.isClient
                    && age % BREEDING_INTERVAL == 0
                    && world.random.nextInt(200) == 0
                    && canBreed()) {
                breed();
            }
        } else {
            ticksResting = 0;

            updateFlowerPosition().map(flower -> {
                if (flower.isWithinDistance(getPos(), 1)) {
                    setResting(true);
                    visited.put(flower, (long)age);
                    if (breedingCooldown <= 0) {
                        breedingCooldown = MAX_BREEDING_COOLDOWN / 10;
                    }
                }

                return flower;
            }).or(this::findNextHoverPosition).ifPresent(this::moveTowards);

            if (random.nextInt(100) == 0 && world.getBlockState(below).isOpaque()) {
                setResting(true);
            }
        }
    }

    private boolean canBreed() {
        return age > BREEDING_INTERVAL && breedingCooldown <= 0 && isResting() && world.getOtherEntities(this, getBoundingBox().expand(2), i -> {
            return i instanceof ButterflyEntity && i.getType() == getType() && ((ButterflyEntity)i).isResting();
        }).size() == 1;
    }

    private boolean breed() {
        breedingCooldown = MAX_BREEDING_COOLDOWN;

        ButterflyEntity copy = (ButterflyEntity)getType().create(world);
        copy.copyPositionAndRotation(this);
        world.spawnEntity(copy);
        setResting(false);
        return true;
    }

    private Optional<BlockPos> findNextHoverPosition() {
        // invalidate the hovering position
        BlockPos pos = getBlockPos();

        return hoveringPosition = hoveringPosition.filter(p -> world.isAir(p)
                && p.getY() >= 1
                && random.nextInt(30) != 0
                && p.getSquaredDistance(pos) >= 4).or(() -> {
            return Optional.of(pos.add(
                    random.nextInt(7) - random.nextInt(7),
                    random.nextInt(6) - 2,
                    random.nextInt(7) - random.nextInt(7)
            ));
        });
    }

    private Optional<BlockPos> updateFlowerPosition() {

        if (age > 0 && age % FLOWER_UPDATE_INTERVAL != 0) {
            return flowerPosition;
        }

        flowerPosition = flowerPosition.filter(p -> world.getBlockState(p).isIn(BlockTags.FLOWERS)).or(() -> {
            return BlockPos.streamOutwards(getBlockPos(), FLOWER_DETECTION_RANGE, FLOWER_DETECTION_RANGE, FLOWER_DETECTION_RANGE)
                    .filter(p -> !visited.containsKey(p) && world.getBlockState(p).isIn(BlockTags.FLOWERS))
                    .findFirst()
                    .map(p -> {
                visited.put(p, (long)age - 900);
                return p;
            });
        });

        return flowerPosition;
    }

    private void moveTowards(BlockPos pos) {
        Vec3d motion = Vec3d.ofCenter(pos, 0.1).subtract(getPos());
        Vec3d vel = getVelocity();

        addVelocity(
            (Math.signum(motion.getX()) * 0.5 - vel.x) * 0.1,
            (Math.signum(motion.getY()) * 0.7 - vel.y) * 0.1,
            (Math.signum(motion.getZ()) * 0.5 - vel.z) * 0.1
        );

        float direction = (float)(MathHelper.atan2(vel.z, vel.x) * (180 / Math.PI)) - 90;

        forwardSpeed = 0.5F;
        headYaw += MathHelper.wrapDegrees(direction - headYaw);
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

    @SuppressWarnings("deprecation")
    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        return reason != SpawnReason.NATURAL || (getY() >= world.getSeaLevel() && world.getLightLevel(getBlockPos()) > 3);
    }

    @Override
    public float getEyeHeight(EntityPose pose) {
        return getHeight() / 2;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("ticksResting", ticksResting);
        nbt.putInt("breedingCooldown", breedingCooldown);
        NbtSerialisable.BLOCK_POS.writeOptional("hoveringPosition", nbt, hoveringPosition);
        NbtSerialisable.BLOCK_POS.writeOptional("flowerPosition", nbt, flowerPosition);
        NbtCompound visited = new NbtCompound();
        this.visited.forEach((pos, time) -> {
            visited.putLong(String.valueOf(pos.asLong()), time);
        });
        nbt.put("visited", visited);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        ticksResting = nbt.getInt("ticksResting");
        breedingCooldown = nbt.getInt("breedingCooldown");
        hoveringPosition = NbtSerialisable.BLOCK_POS.readOptional("hoveringPosition", nbt);
        flowerPosition = NbtSerialisable.BLOCK_POS.readOptional("flowerPosition", nbt);
        NbtCompound visited = nbt.getCompound("visited");
        this.visited.clear();
        visited.getKeys().forEach(key -> {
            try {
                this.visited.put(BlockPos.fromLong(Long.valueOf(key)), visited.getLong(key));
            } catch (NumberFormatException ignore) {}
        });
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

        private final Identifier skin = Unicopia.id("textures/entity/butterfly/" + name().toLowerCase() + ".png");

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
