package com.minelittlepony.unicopia.world.entity;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.InAnimate;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.PegasusCloudInteractionAbility.Interactable;
import com.minelittlepony.unicopia.equine.Ponylike;
import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.particles.ParticleEmitter;
import com.minelittlepony.unicopia.particles.UParticles;
import com.minelittlepony.unicopia.world.block.UBlocks;
import com.minelittlepony.unicopia.world.item.UItems;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

public class CloudEntity extends FlyingEntity implements Interactable, InAnimate {

    private static final TrackedData<Integer> RAINTIMER = DataTracker.registerData(CloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> THUNDERING = DataTracker.registerData(CloudEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SCALE = DataTracker.registerData(CloudEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Boolean> STATIONARY = DataTracker.registerData(CloudEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static final EntityDimensions BASE_DIMENSIONS = EntityDimensions.changing(0.6f, 0.6f);

    protected double targetAltitude;

    protected int directionX;
    protected int directionZ;

    public CloudEntity(EntityType<? extends CloudEntity> type, World world) {
        super(type, world);
        inanimate = true;
        ignoreCameraFrustum = true;
        targetAltitude = getRandomFlyingHeight();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(RAINTIMER, 0);
        dataTracker.startTracking(THUNDERING, false);
        dataTracker.startTracking(STATIONARY, false);
        dataTracker.startTracking(SCALE, 1);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BLOCK_WOOL_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_WOOL_BREAK;
    }

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    @Override
    public float getBrightnessAtEyes() {
        return 0xF000F0;
    }

    @Override
    public boolean cannotDespawn() {
        return hasCustomName() || getStationary() || getOpaque();
    }

    @Override
    public int getLimitPerChunk() {
        return 6;
    }

    @Override
    public boolean canInteract(Race race) {
        return race.canInteractWithClouds();
    }

    @Override
    public void onStruckByLightning(LightningEntity lightningBolt) {

    }

    @Override
    public EntityData initialize(WorldAccess world, LocalDifficulty difficulty, SpawnReason type, @Nullable EntityData data, @Nullable CompoundTag tag) {
        if (random.nextInt(20) == 0 && canRainHere()) {
            setRaining();
            if (random.nextInt(20) == 0) {
                setIsThundering(true);
            }
        }

        setCloudSize(1 + random.nextInt(4));

        return super.initialize(world, difficulty, type, data, tag);
    }

    @Override
    protected void pushAway(Entity other) {
        if (other instanceof CloudEntity || other instanceof PlayerEntity) {
            if (other.getY() > getY()) {
                return;
            }

            super.pushAway(other);
        }
    }

    @Override
    public void pushAwayFrom(Entity other) {
        if (other instanceof PlayerEntity) {
            if (EquinePredicates.PLAYER_PEGASUS.test(other)) {
                super.pushAwayFrom(other);
            }
        } else if (other instanceof CloudEntity) {
            super.pushAwayFrom(other);
        }
    }

    @Override
    public void tick() {
        Box boundingbox = getBoundingBox();

        if (getIsRaining()) {
            if (world.isClient) {
                for (int i = 0; i < 30 * getCloudSize(); i++) {
                    double x = MathHelper.nextDouble(random, boundingbox.minX, boundingbox.maxX);
                    double y = getBoundingBox().minY + getHeight()/2;
                    double z = MathHelper.nextDouble(random, boundingbox.minZ, boundingbox.maxZ);

                    ParticleEffect particleId = canSnowHere(new BlockPos(x, y, z)) ? ParticleTypes.ITEM_SNOWBALL : UParticles.RAIN_DROPS;

                    world.addParticle(particleId, x, y, z, 0, 0, 0);
                }

                Box rainedArea = boundingbox
                        .expand(1, 0, 1)
                        .expand(0, -(getY() - getGroundPosition(getBlockPos()).getY()), 0);


                for (PlayerEntity j : world.getEntities(PlayerEntity.class, rainedArea, j -> canSnowHere(j.getBlockPos()))) {
                    j.world.playSound(j, j.getBlockPos(), SoundEvents.WEATHER_RAIN, SoundCategory.AMBIENT, 0.1F, 0.6F);
                }
            }

            double width = getDimensions(getPose()).width;
            BlockPos pos = getGroundPosition(new BlockPos(
                getX() + random.nextFloat() * width,
                getY(),
                getZ() + random.nextFloat() * width
            ));

            if (getIsThundering()) {
                if (random.nextInt(3000) == 0) {
                    spawnThunderbolt(pos);
                }

                if (random.nextInt(200) == 0) {
                    setIsThundering(false);
                }
            }

            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof FireBlock) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }

            if (random.nextInt(20) == 0) {
                BlockPos below = pos.down();
                state = world.getBlockState(below);
                if (state.getBlock() != null) {
                    Biome biome = world.getBiome(below);

                    if (biome.canSetIce(world, below)) {
                        world.setBlockState(below, Blocks.ICE.getDefaultState());
                    }

                    if (biome.canSetSnow(world, pos)) {
                        world.setBlockState(pos, Blocks.SNOW.getDefaultState());
                    }

                    if (state.getBlock() instanceof FarmlandBlock) {
                        int moisture = state.get(FarmlandBlock.MOISTURE);

                        if (moisture < 7) {
                            world.setBlockState(below, state.with(FarmlandBlock.MOISTURE, moisture + 1));
                        }
                    } else if (state.getBlock() instanceof CropBlock) {
                        int age = state.get(CropBlock.AGE);

                        if (age < 7) {
                            world.setBlockState(below, state.with(CropBlock.AGE, age + 1), 2);
                        }
                    }

                    state.getBlock().rainTick(world, below);
                }
            }

            if (setRainTimer(getRainTimer() - 1) == 0) {
                if (!getStationary()) {
                    spawnHurtParticles();

                    if (getCloudSize() > 1) {
                        setIsRaining(false);
                        setCloudSize(getCloudSize() - 1);
                    } else {
                        remove();
                    }
                }
            }
        } else {
            if (random.nextInt(8000) == 0 && canRainHere()) {
                setRaining();
                if (random.nextInt(7000) == 0) {
                    setIsThundering(true);
                }
            }
        }

        pitch = 0;
        headYaw = 0;
        yaw = 0;

        for (Entity i : world.getEntities(this, boundingbox
                .expand(1 / (1 + getCloudSize())), EquinePredicates.ENTITY_INTERACT_WITH_CLOUDS)) {
            if (i.getY() > getY() + 0.5) {
                applyGravityCompensation(i);
            }
        }

        if (isOnFire() && !dead) {
            for (int i = 0; i < 5; i++) {
                world.addParticle(ParticleTypes.CLOUD,
                        MathHelper.nextDouble(random, boundingbox.minX, boundingbox.maxX),
                        MathHelper.nextDouble(random, boundingbox.minY, boundingbox.maxY),
                        MathHelper.nextDouble(random, boundingbox.minZ, boundingbox.maxZ), 0, 0.25, 0);
            }
        }

        if (getStationary()) {
            setVelocity(0, 0, 0);
        }

        super.tick();

        double motionFactor = (1 + getCloudSize() / 4);

        Vec3d vel = this.getVelocity();
        this.setVelocity(vel.x / motionFactor, vel.y, vel.z / motionFactor);


        hurtTime = 0;
    }

    @Override
    public double getMountedHeightOffset() {
        return getBoundingBox().maxY - getBoundingBox().minY - 0.25;
    }

    @Override
    public void travel(Vec3d motion) {
        if (!getStationary()) {
            super.travel(motion);
        }
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {

        Pony pony = Pony.of(player);

        if (pony.getPhysics().isGravityNegative() ? player.getY() <= getY() : player.getY() >= getY()) {
            if (applyGravityCompensation(player)) {
                double difX = player.getX() - player.prevX;
                double difZ = player.getZ() - player.prevZ;
                double difY = player.getY() - player.prevY;

                player.horizontalSpeed = (float)(player.horizontalSpeed + MathHelper.sqrt(difX * difX + difZ * difZ) * 0.6);
                player.distanceTraveled = (float)(player.distanceTraveled + MathHelper.sqrt(difX * difX + difY * difY + difZ * difZ) * 0.6);

                if (pony.stepOnCloud()) {
                    BlockSoundGroup soundtype = BlockSoundGroup.WOOL;
                    player.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
                }
            }
        }

        super.onPlayerCollision(player);
    }

    @Override
    protected void mobTick() {

        if (!getStationary()) {
            if (!hasVehicle()) {
                double distance = targetAltitude - getY();

                if (targetAltitude < getY() && !world.isAir(getBlockPos())) {
                    distance = 0;
                }

                if (Math.abs(distance) < 1 && random.nextInt(7000) == 0) {
                    targetAltitude = getRandomFlyingHeight();
                    distance = targetAltitude - getY();
                }

                if (Math.abs(distance) < 1) {
                    distance = 0;
                }

                Vec3d vel = getVelocity();

                setVelocity(vel.x, vel.y + (Math.signum(distance) * 0.699999988079071D - vel.y) * 0.10000000149011612D, vel.z);
            }
        }
    }

    protected float getRandomFlyingHeight() {
        float b = getMinimumFlyingHeight();
        float a = b + getMaximumAltitude();

        return MathHelper.lerp(world.random.nextFloat(), Math.min(a, b), Math.max(a, b));
    }

    protected float getMinimumFlyingHeight() {
        return world.getTopY(Heightmap.Type.WORLD_SURFACE, MathHelper.floor(getX()), MathHelper.floor(getZ()));
    }

    protected int getMaximumAltitude() {
        return 15;
    }

    @Override
    public void handleStatus(byte type) {
        if (type == 2 && !isOnFire()) {
            spawnHurtParticles();
        }
        super.handleStatus(type);
    }

    @Override
    public void handlePegasusInteration(int interationType) {
        if (!world.isClient) {
            switch (interationType) {
                case 1:
                    setIsRaining(!getIsRaining());
                    break;
                case 2:
                    spawnThunderbolt();
                    break;
            }
        }

        spawnHurtParticles();
    }

    public void spawnHurtParticles() {
        for (int i = 0; i < 50 * getCloudSize(); i++) {
            ParticleEmitter.instance().emitDiggingParticles(this, UBlocks.CLOUD_BLOCK);
        }
        playHurtSound(DamageSource.GENERIC);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity attacker = source.getAttacker();

        if (attacker instanceof PlayerEntity) {
            return damage(source, amount, (PlayerEntity)attacker);
        }

        return source == DamageSource.IN_WALL || super.damage(source, amount);
    }

    private boolean damage(DamageSource source, float amount, PlayerEntity player) {

        ItemStack stack = player.getMainHandStack();

        boolean canFly = EnchantmentHelper.get(stack).containsKey(Enchantments.FEATHER_FALLING)
                || EquinePredicates.PLAYER_PEGASUS.test(player);
        boolean stat = getStationary();

        if (stat || canFly) {
            if (!isOnFire()) {
                spawnHurtParticles();
            }

            if (stack != null && stack.getItem() instanceof SwordItem) {
                return super.damage(source, amount);
            } else if (stack != null && stack.getItem() instanceof ShovelItem) {
                return super.damage(source, amount * 1.5f);
            } else if (canFly) {
                if (player.getY() < getY() || !world.isAir(getBlockPos())) {
                    targetAltitude = getY() + 5;
                } else if (player.getY() > getY()) {
                    targetAltitude = getY() - 5;
                }
            }
        }
        return false;
    }

    @Override
    public void onDeath(DamageSource s) {
        if (s == DamageSource.GENERIC || (s.getSource() != null && s.getSource() instanceof PlayerEntity)) {
            remove();
        }

        super.onDeath(s);
        //clearItemFloatingState();
    }

    @Override
    public void remove() {
        super.remove();
        //clearItemFloatingState();
    }

    //@FUF(reason = "There is no TickEvent.EntityTickEvent. Waiting on mixins...")
    protected void clearItemFloatingState() {
        Box bounds = getBoundingBox().expand(1 / (1 + getCloudSize())).expand(5);

        for (Entity i : world.getEntities(this, bounds, this::entityIsFloatingItem)) {
            i.setNoGravity(false);
        }
    }

    private boolean entityIsFloatingItem(Entity e) {
        return e instanceof ItemEntity
                && EquinePredicates.ITEM_INTERACT_WITH_CLOUDS.test(e);
    }

    @Override
    protected void dropEquipment(DamageSource source, int looting, boolean hitByPlayer) {
        if (hitByPlayer) {
            int amount = 13 + world.random.nextInt(3);

            dropItem(UItems.CLOUD_MATTER, amount * (1 + looting));

            if (world.random.nextBoolean()) {
                dropItem(UItems.DEW_DROP, 3 + looting);
            }
        }
    }

    @Override
    public ItemEntity dropItem(ItemConvertible stack, int amount) {
        ItemEntity item = super.dropItem(stack, amount);

        Ponylike.of(item).setSpecies(Race.PEGASUS);
        item.setNoGravity(true);
        item.setVelocity(0, 0, 0);

        return item;
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);

        setRainTimer(tag.getInt("RainTimer"));
        setIsThundering(tag.getBoolean("IsThundering"));
        setCloudSize(tag.getByte("CloudSize"));
        setStationary(tag.getBoolean("IsStationary"));
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);

        tag.putInt("RainTimer", getRainTimer());
        tag.putBoolean("IsThundering", getIsThundering());
        tag.putByte("CloudSize", (byte)getCloudSize());
        tag.putBoolean("IsStationary", getStationary());
    }

    protected boolean applyGravityCompensation(Entity entity) {
        int floatStrength = getFloatStrength(entity);

        if (!isConnectedThroughVehicle(entity) && floatStrength > 0) {

            double bounceModifier = entity.fallDistance > 80 ? 80 : MathHelper.floor(entity.fallDistance * 10) / 10;

            entity.setOnGround(true);

            Vec3d motion = entity.getVelocity();
            double motionX = motion.x;
            double motionY = motion.y;
            double motionZ = motion.z;

            Ponylike<?> p = Ponylike.of(entity);
            boolean negativeGravity = p != null && p.getPhysics().isGravityNegative();
            float gravityConstant = negativeGravity ? -1 : 1;

            if (negativeGravity ? motionY >= 0 : motionY <= 0) {
                motionY += gravityConstant * (((floatStrength > 2 ? 1 : floatStrength/2) * 0.699999998079071D) - motionY + bounceModifier * 0.7) * 0.10000000149011612D;

                if (negativeGravity) {
                    if (motionY > 0) {
                        motionY = 0;
                    }
                } else {
                    motionY = Math.min(0.1F, motionY);
                    if (motionY < 0.002F) {
                        motionY = 0.001;
                    }
                }
            }

            if (!getStationary()) {
                motionX += ((motionX - motionX) / getCloudSize()) - 0.002F;
            }

            if (!getStationary() && motionY > 0.4 && world.random.nextInt(900) == 0) {
                spawnThunderbolt(getBlockPos());
            }

            if (getStationary() && entity instanceof ItemEntity) {
                motionX /= 8;
                motionZ /= 8;
            }
            entity.setVelocity(motionX, motionY, motionZ);

            return true;
        }

        return false;
    }

    @Override
    public void move(MovementType type, Vec3d delta) {
        setBoundingBox(getBoundingBox().offset(delta));
        moveToBoundingBoxCenter();
    }

    public int getFloatStrength(Entity entity) {
        if (EquinePredicates.ENTITY_INTERACT_WITH_CLOUDS.test(entity)) {
            return 3;
        }

        if (entity instanceof LivingEntity) {
            return getFeatherEnchantStrength((LivingEntity)entity);
        }

        return 0;
    }

    public static int getFeatherEnchantStrength(LivingEntity entity) {
        return Streams.stream(entity.getArmorItems())
                .map(CloudEntity::getFeatherEnchantStrength).filter(i -> i > 0)
                .findFirst()
                .orElse(0);
    }

    public static int getFeatherEnchantStrength(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
            if (enchantments.containsKey(Enchantments.FEATHER_FALLING)) {
                return enchantments.get(Enchantments.FEATHER_FALLING);
            }
        }
        return 0;
    }

    private boolean canRainHere() {
        return world.getBiome(getBlockPos()).getRainfall() > 0;
    }

    private boolean canSnowHere(BlockPos pos) {
        return world.getBiome(pos).canSetSnow(world, pos);
    }

    public void spawnThunderbolt() {
        spawnThunderbolt(getGroundPosition(getBlockPos()));
    }

    public void spawnThunderbolt(BlockPos pos) {
        if (world instanceof ServerWorld) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            lightning.positAfterTeleport(pos.getX(), pos.getY(), pos.getZ());
            world.spawnEntity(lightning);
        }
    }

    private BlockPos getGroundPosition(BlockPos inPos) {
        BlockPos pos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, inPos);

        if (pos.getY() >= getY()) {
            while (!World.isHeightInvalid(pos)) {
                pos = pos.down();
                if (world.getBlockState(pos).hasSolidTopSurface(world, pos, this)) {
                    return pos.up();
                }
            }

        }
        return pos;
    }

    public int getRainTimer() {
        return dataTracker.get(RAINTIMER);
    }

    public int setRainTimer(int val) {
        val = Math.max(0, val);
        dataTracker.set(RAINTIMER, val);
        return val;
    }

    private void setRaining() {
        setRainTimer(700 + random.nextInt(20));
    }

    public void setIsRaining(boolean val) {
        if (val) {
            setRaining();
        } else {
            setRainTimer(0);
        }
    }

    public boolean getIsRaining() {
        return getRainTimer() > 0;
    }

    public boolean getIsThundering() {
        return dataTracker.get(THUNDERING);
    }

    public void setIsThundering(boolean val) {
        dataTracker.set(THUNDERING, val);
    }

    public boolean getStationary() {
        return dataTracker.get(STATIONARY);
    }

    public void setStationary(boolean val) {
        dataTracker.set(STATIONARY, val);
    }

    public boolean getOpaque() {
        return false;
    }

    public int getCloudSize() {
        return dataTracker.get(SCALE);
    }

    @Override
    public float getScaleFactor() {
        return getCloudSize();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.changing(3, 0.5F).scaled(getScaleFactor()); // super.getDimensions(pose);
     }

    public void setCloudSize(int val) {
        dataTracker.set(SCALE, Math.max(1, val));
        calculateDimensions();
    }
}
