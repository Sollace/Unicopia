package com.minelittlepony.unicopia.redux.entity;

import java.util.Map;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.EquinePredicates;
import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.SpeciesList;
import com.minelittlepony.unicopia.core.UParticles;
import com.minelittlepony.unicopia.core.entity.InAnimate;
import com.minelittlepony.unicopia.core.util.particles.ParticleEmitter;
import com.minelittlepony.unicopia.redux.ability.PowerCloudBase.ICloudEntity;
import com.minelittlepony.unicopia.redux.block.UBlocks;
import com.minelittlepony.unicopia.redux.item.UItems;

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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class CloudEntity extends FlyingEntity implements ICloudEntity, InAnimate {

    private static final TrackedData<Integer> RAINTIMER = DataTracker.registerData(CloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> THUNDERING = DataTracker.registerData(CloudEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SCALE = DataTracker.registerData(CloudEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Boolean> STATIONARY = DataTracker.registerData(CloudEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected double targetAltitude;

    protected int directionX;
    protected int directionZ;

    private final double baseWidth = 3f;
    private final double baseHeight = 0.8f;

    public CloudEntity(EntityType<? extends CloudEntity> type, World world) {
        super(type, world);
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

    // TODO: loot table
    /*@Override
    protected Item getDropItem() {
        return UItems.cloud_matter;
    }*/

    @Override
    public boolean doesRenderOnFire() {
        return false;
    }

    @Override
    public int getLightmapCoordinates() {
        return 0xF000F0;
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
    public EntityData initialize(IWorld world, LocalDifficulty difficulty, SpawnType type, @Nullable EntityData data, @Nullable CompoundTag tag) {
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
            if (other.y > y) {
                return;
            }

            super.pushAway(other);
        }
    }

    @Override
    public void pushAwayFrom(Entity other) {
        if (other instanceof PlayerEntity) {
            if (EquinePredicates.INTERACT_WITH_CLOUDS.test((PlayerEntity)other)) {
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
                        .expand(0, -(y - getGroundPosition(x, z).getY()), 0);


                for (PlayerEntity j : world.getEntities(PlayerEntity.class, rainedArea)) {
                    if (!canSnowHere(j.getBlockPos())) {
                        j.world.playSound(j, j.getBlockPos(), SoundEvents.WEATHER_RAIN, SoundCategory.AMBIENT, 0.1F, 0.6F);
                    }
                }
            }

            double width = getDimensions(getPose()).width;
            BlockPos pos = getGroundPosition(
                x + random.nextFloat() * width,
                z + random.nextFloat() * width
            );

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

                    state.getBlock().onRainTick(world, below);
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
            if (i.y > y + 0.5) {
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
        if (player.y >= y) {
            if (applyGravityCompensation(player)) {
                double difX = player.x - player.prevX;
                double difZ = player.z - player.prevZ;
                double difY = player.y - player.prevY;

                player.horizontalSpeed = (float)(player.horizontalSpeed + MathHelper.sqrt(difX * difX + difZ * difZ) * 0.6);
                player.distanceWalked = (float)(player.distanceWalked + MathHelper.sqrt(difX * difX + difY * difY + difZ * difZ) * 0.6);

                if (SpeciesList.instance().getPlayer(player).stepOnCloud()) {
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
                double distance = targetAltitude - y;

                if (targetAltitude < y && !world.isAir(getBlockPos())) {
                    distance = 0;
                }

                if (Math.abs(distance) < 1 && random.nextInt(7000) == 0) {
                    targetAltitude = getRandomFlyingHeight();
                    distance = targetAltitude - y;
                }

                if (Math.abs(distance) < 1) {
                    distance = 0;
                }

                Vec3d vel = getVelocity();

                setVelocity(vel.x, vel.y - 0.002 + (Math.signum(distance) * 0.699999988079071D - vel.y) * 0.10000000149011612D, vel.z);
            }
        }
    }

    protected float getRandomFlyingHeight() {
        float a = getMaximumFlyingHeight();
        float b = getMinimumFlyingHeight();

        float min = Math.min(a, b);
        float max = Math.max(a, b);

        return min + world.random.nextFloat() * (max - min);
    }

    protected float getMinimumFlyingHeight() {
        float ground = world.getBiome(getBlockPos()).getDepth();
        float cloud = world.getDimension().getCloudHeight();

        float min = Math.min(ground, cloud);
        float max = Math.max(ground, cloud);

        return min + (max - min)/2;
    }

    protected float getMaximumFlyingHeight() {
        return world.getDimension().getCloudHeight() - 5;
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
            ParticleEmitter.instance().emitDiggingParticles(this, UBlocks.normal_cloud);
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

        boolean canFly = EnchantmentHelper.getEnchantments(stack).containsKey(Enchantments.FEATHER_FALLING)
                || EquinePredicates.INTERACT_WITH_CLOUDS.test(player);
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
                if (player.y < y || !world.isAir(getBlockPos())) {
                    targetAltitude = y + 5;
                } else if (player.y > y) {
                    targetAltitude = y - 5;
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
        clearItemFloatingState();
    }

    @Override
    public void remove() {
        super.remove();
        clearItemFloatingState();
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
                && EquinePredicates.ITEM_INTERACT_WITH_CLOUDS.test((ItemEntity)e);
    }

    @Override
    protected void dropEquipment(DamageSource source, int looting, boolean hitByPlayer) {
        if (hitByPlayer) {
            int amount = 13 + world.random.nextInt(3);

            dropItem(UItems.cloud_matter, amount * (1 + looting));

            if (world.random.nextBoolean()) {
                dropItem(UItems.dew_drop, 3 + looting);
            }
        }
    }

    @Override
    public ItemEntity dropItem(ItemConvertible stack, int amount) {
        ItemEntity item = super.dropItem(stack, amount);

        SpeciesList.instance().getEntity(item).setSpecies(Race.PEGASUS);
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

            double boundModifier = entity.fallDistance > 80 ? 80 : MathHelper.floor(entity.fallDistance * 10) / 10;

            entity.onGround = true;

            Vec3d motion = entity.getVelocity();
            double motionX = motion.x;
            double motionY = motion.y;
            double motionZ = motion.z;

            motionY += (((floatStrength > 2 ? 1 : floatStrength/2) * 0.699999998079071D) - motionY + boundModifier * 0.7) * 0.10000000149011612D;
            if (!getStationary()) {
                motionX += ((motionX - motionX) / getCloudSize()) - 0.002F;
            }

            if (!getStationary() && motionY > 0.4 && world.random.nextInt(900) == 0) {
                spawnThunderbolt(getBlockPos());
            }

            // @FUF(reason = "There is no TickEvents.EntityTickEvent. Waiting on mixins...")
            if (getStationary() && entity instanceof ItemEntity) {
                motionX /= 8;
                motionZ /= 8;
                motionY /= 16;
                entity.setNoGravity(true);
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

        if (entity instanceof PlayerEntity) {
            return getFeatherEnchantStrength((PlayerEntity)entity);
        }

        return 0;
    }

    public static int getFeatherEnchantStrength(PlayerEntity player) {
        for (ItemStack stack : player.getArmorItems()) {
            if (stack != null) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
                if (enchantments.containsKey(Enchantments.FEATHER_FALLING)) {
                    return enchantments.get(Enchantments.FEATHER_FALLING);
                }
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
        spawnThunderbolt(getGroundPosition(x, z));
    }

    public void spawnThunderbolt(BlockPos pos) {
        if (world instanceof ServerWorld) {
            ((ServerWorld)world).addLightning(new LightningEntity(world, pos.getX(), pos.getY(), pos.getZ(), false));
        }
    }

    private BlockPos getGroundPosition(double x, double z) {
        BlockPos pos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, y, z));

        if (pos.getY() >= y) {
            while (World.isValid(pos)) {
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
        int size = dataTracker.get(SCALE);
        updateSize(size);
        return size;
    }

    private void updateSize(int scale) {
        setSize((float)baseWidth * scale, (float)baseHeight * scale);
    }

    @Override
    protected void setSize(float width, float height) {
        if (width != this.width || height != this.height) {
            super.setSize(width, height);
            setPosition(x, y, z);
        }
    }

    public void setCloudSize(int val) {
        val = Math.max(1, val);
        updateSize(val);
        dataTracker.set(SCALE, val);
    }
}
