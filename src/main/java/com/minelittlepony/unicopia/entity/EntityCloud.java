package com.minelittlepony.unicopia.entity;

import java.util.Map;

import com.minelittlepony.unicopia.Predicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UBlocks;
import com.minelittlepony.unicopia.UItems;
import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.ability.powers.PowerCloudBase.ICloudEntity;

import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class EntityCloud extends FlyingEntity implements ICloudEntity, IAnimals, IInAnimate {

    private static final TrackedData<Integer> RAINTIMER = DataTracker.registerData(EntityCloud.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> THUNDERING = DataTracker.registerData(EntityCloud.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SCALE = DataTracker.registerData(EntityCloud.class, TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<Boolean> STATIONARY = DataTracker.registerData(EntityCloud.class, TrackedDataHandlerRegistry.BOOLEAN);

    protected double targetAltitude;

    protected int directionX;
    protected int directionZ;

    private final double baseWidth = 3f;
    private final double baseHeight = 0.8f;

    public EntityCloud(EntityType<EntityCloud> type, World world) {
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

    @Override
    protected Item getDropItem() {
        return UItems.cloud_matter;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

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
    public void onStruckByLightning(EntityLightningBolt lightningBolt) {

    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData pack) {
        if (world.random.nextInt(20) == 0 && canRainHere()) {
            setRaining();
            if (world.random.nextInt(20) == 0) {
                setIsThundering(true);
            }
        }

        setCloudSize(1 + random.nextInt(4));

        return super.onInitialSpawn(difficulty, pack);
    }

    @Override
    protected void collideWithEntity(Entity other) {
        if (other instanceof EntityCloud || other instanceof PlayerEntity) {
            if (other.posY > posY) {
                return;
            }

            super.collideWithEntity(other);
        }
    }

    @Override
    public void applyEntityCollision(Entity other) {
        if (other instanceof PlayerEntity) {
            if (Predicates.INTERACT_WITH_CLOUDS.test((PlayerEntity)other)) {
                super.applyEntityCollision(other);
            }
        } else if (other instanceof EntityCloud) {
            super.applyEntityCollision(other);
        }
    }

    @Override
    public void onUpdate() {
        Box boundingbox = getBoundingBox();

        if (getIsRaining()) {
            if (world.isClient) {
                for (int i = 0; i < 30 * getCloudSize(); i++) {
                    double x = MathHelper.nextDouble(random, boundingbox.minX, boundingbox.maxX);
                    double y = getBoundingBox().minY + getHeight()/2;
                    double z = MathHelper.nextDouble(rand, boundingbox.minZ, boundingbox.maxZ);

                    int particleId = canSnowHere(new BlockPos(x, y, z)) ? ParticleTypes.ITEM_SNOWBALL.getType() : UParticles.RAIN_DROPS;

                    ParticleTypeRegistry.getTnstance().spawnParticle(particleId, false, x, y, z, 0, 0, 0);
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

            if (rand.nextInt(20) == 0) {
                BlockPos below = pos.down();
                state = world.getBlockState(below);
                if (state.getBlock() != null) {
                    if (world.canBlockFreezeWater(below)) {
                        world.setBlockState(below, Blocks.ICE.getDefaultState());
                    }

                    if (world.canSnowAt(pos, false)) {
                        world.setBlockState(pos, Blocks.SNOW.getDefaultState());
                    }

                    if (state.getBlock() instanceof FarmlandBlock) {
                        int moisture = state.getValue(FarmlandBlock.MOISTURE);

                        if (moisture < 7) {
                            world.setBlockState(below, state.with(FarmlandBlock.MOISTURE, moisture + 1));
                        }
                    } else if (state.getBlock() instanceof CropBlock) {
                        int age = state.getValue(CropBlock.AGE);

                        if (age < 7) {
                            world.setBlockState(below, state.with(CropBlock.AGE, age + 1), 2);
                        }
                    }

                    state.getBlock().fillWithRain(world, below);
                }
            }

            if (setRainTimer(getRainTimer() - 1) == 0) {
                if (!getStationary()) {
                    pomf();

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

        rotationPitch = 0;
        rotationYawHead = 0;
        rotationYaw = 0;

        for (Entity i : world.getEntitiesInAABBexcluding(this, boundingbox
                .grow(1 / (1 + getCloudSize())), Predicates.ENTITY_INTERACT_WITH_CLOUDS)) {
            if (i.posY > posY + 0.5) {
                applyGravityCompensation(i);
            }
        }

        if (isBurning() && !dead) {
            for (int i = 0; i < 5; i++) {
                world.spawnParticle(ParticleTypes.CLOUD,
                        MathHelper.nextDouble(random, boundingbox.minX, boundingbox.maxX),
                        MathHelper.nextDouble(random, boundingbox.minY, boundingbox.maxY),
                        MathHelper.nextDouble(random, boundingbox.minZ, boundingbox.maxZ), 0, 0.25, 0);
            }
        }

        if (getStationary()) {
            setVelocity(0, 0, 0);
        }

        super.onUpdate();

        double motionFactor = (1 + getCloudSize() / 4);

        motionX /= motionFactor;
        motionZ /= motionFactor;

        hurtTime = 0;
    }

    @Override
    public double getMountedYOffset() {
        return getEntityBoundingBox().maxY - getEntityBoundingBox().minY - 0.25;
    }

    @Override
    public void moveRelative(float strafe, float up, float forward, float friction) {
        if (!getStationary()) {
            super.moveRelative(strafe, up, forward, friction);
        }
    }

    @Override
    public void onCollideWithPlayer(PlayerEntity player) {
        if (player.posY >= posY) {
            if (applyGravityCompensation(player)) {
                double difX = player.posX - player.lastTickPosX;
                double difZ = player.posZ - player.lastTickPosZ;
                double difY = player.posY - player.lastTickPosY;

                player.distanceWalkedModified = (float)(player.distanceWalkedModified + MathHelper.sqrt(difX * difX + difZ * difZ) * 0.6);
                player.distanceWalkedOnStepModified = (float)(player.distanceWalkedOnStepModified + MathHelper.sqrt(difX * difX + difY * difY + difZ * difZ) * 0.6);

                if (SpeciesList.instance().getPlayer(player).stepOnCloud()) {
                    SoundType soundtype = SoundType.CLOTH;
                    player.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
                }
            }
        }

        super.onCollideWithPlayer(player);
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

                getVelocity().y -= 0.002;
                getVelocity().y += (Math.signum(distance) * 0.699999988079071D - getVelocity().y) * 0.10000000149011612D;
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
        float ground = world.getDimension().getAverageGroundLevel();
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
        if (type == 2) {
            if (!isOnFire()) {
                for (int i = 0; i < 50 * getCloudSize(); i++) {
                    ParticleTypeRegistry.getTnstance().getEmitter().emitDiggingParticles(this, UBlocks.normal_cloud.getDefaultState());
                }
            }
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

        pomf();
    }

    public void pomf() {
        for (int i = 0; i < 50 * getCloudSize(); i++) {
            ParticleTypeRegistry.getTnstance().getEmitter().emitDiggingParticles(this, UBlocks.normal_cloud.getDefaultState());
        }

        playHurtSound(DamageSource.GENERIC);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity attacker = source.getAttacker();

        if (attacker instanceof PlayerEntity) {
            return onAttackByPlayer(source, amount, (PlayerEntity)attacker);
        }

        return source == DamageSource.IN_WALL || super.damage(source, amount);
    }

    private boolean onAttackByPlayer(DamageSource source, float amount, PlayerEntity player) {

        ItemStack stack = player.getMainHandStack();

        boolean canFly = EnchantmentHelper.getEnchantments(stack).containsKey(Enchantments.FEATHER_FALLING)
                || Predicates.INTERACT_WITH_CLOUDS.test(player);
        boolean stat = getStationary();

        if (stat || canFly) {
            if (!isOnFire()) {
                for (int i = 0; i < 50 * getCloudSize(); i++) {
                    ParticleTypeRegistry.getTnstance().getEmitter().emitDiggingParticles(this, UBlocks.normal_cloud.getDefaultState());
                }
            }

            if (stack != null && stack.getItem() instanceof ItemSword) {
                return super.attackEntityFrom(source, amount);
            } else if (stack != null && stack.getItem() instanceof ItemSpade) {
                return super.attackEntityFrom(source, amount * 1.5f);
            } else if (canFly) {
                if (player.y < y || !world.isAirBlock(getPosition())) {
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

    @FUF(reason = "There is no TickEvent.EntityTickEvent. Waiting on mixins...")
    protected void clearItemFloatingState() {
        Box bounds = getEntityBoundingBox().grow(1 / (1 + getCloudSize())).grow(5);

        for (Entity i : world.getEntitiesInAABBexcluding(this, bounds, this::entityIsFloatingItem)) {
            i.setNoGravity(false);
        }
    }

    private boolean entityIsFloatingItem(Entity e) {
        return e instanceof ItemEntity
                && Predicates.ITEM_INTERACT_WITH_CLOUDS.test((ItemEntity)e);
    }

    @Override
    protected void dropFewItems(boolean hitByPlayer, int looting) {
        if (hitByPlayer) {
            Item item = getDropItem();
            int amount = 13 + world.rand.nextInt(3);

            dropItem(item, amount * (1 + looting));

            if (world.rand.nextBoolean()) {
                dropItem(UItems.dew_drop, 3 + looting);
            }
        }
    }

    @Override
    public EntityItem entityDropItem(ItemStack stack, float offsetY) {
        EntityItem item = super.entityDropItem(stack, offsetY);

        SpeciesList.instance().getEntity(item).setSpecies(Race.PEGASUS);
        item.setNoGravity(true);
        item.motionY = 0;

        return item;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);

        setRainTimer(tag.getInteger("RainTimer"));
        setIsThundering(tag.getBoolean("IsThundering"));
        setCloudSize(tag.getByte("CloudSize"));
        setStationary(tag.getBoolean("IsStationary"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);

        tag.setInteger("RainTimer", getRainTimer());
        tag.setBoolean("IsThundering", getIsThundering());
        tag.setByte("CloudSize", (byte)getCloudSize());
        tag.setBoolean("IsStationary", getStationary());
    }

    protected boolean applyGravityCompensation(Entity entity) {
        int floatStrength = getFloatStrength(entity);

        if (!isRidingOrBeingRiddenBy(entity) && floatStrength > 0) {

            double boundModifier = entity.fallDistance > 80 ? 80 : MathHelper.floor(entity.fallDistance * 10) / 10;

            entity.onGround = true;
            entity.motionY += (((floatStrength > 2 ? 1 : floatStrength/2) * 0.699999998079071D) - entity.motionY + boundModifier * 0.7) * 0.10000000149011612D;
            if (!getStationary()) {
                entity.motionX += ((motionX - entity.motionX) / getCloudSize()) - 0.002F;
            }

            if (!getStationary() && entity.motionY > 0.4 && world.rand.nextInt(900) == 0) {
                spawnThunderbolt(getPosition());
            }

            // @FUF(reason = "There is no TickEvents.EntityTickEvent. Waiting on mixins...")
            if (getStationary() && entity instanceof EntityItem) {
                entity.motionX /= 8;
                entity.motionZ /= 8;
                entity.motionY /= 16;
                entity.setNoGravity(true);
            }

            return true;
        }

        return false;
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        setEntityBoundingBox(getEntityBoundingBox().offset(x, y, z));
        resetPositionToBB();
    }

    public int getFloatStrength(Entity entity) {
        if (Predicates.ENTITY_INTERACT_WITH_CLOUDS.test(entity)) {
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
        return world.getBiome(getBlockPos()).canRain();
    }

    private boolean canSnowHere(BlockPos pos) {
        return world.getBiome(pos).canSetSnow(world, pos);
    }

    public void spawnThunderbolt() {
        spawnThunderbolt(getGroundPosition(x, z));
    }

    public void spawnThunderbolt(BlockPos pos) {
        world.addWeatherEffect(new LightningEntity(world, pos.getX(), pos.getY(), pos.getZ(), false));
    }

    private BlockPos getGroundPosition(double x, double z) {
        BlockPos pos = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, y, z));

        if (pos.getY() >= posY) {
            while (world.isValid(pos)) {
                pos = pos.down();
                if (world.getBlockState(pos).isSideSolid(world, pos, Direction.UP)) {
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
            setPosition(posX, posY, posZ);
        }
    }

    public void setCloudSize(int val) {
        val = Math.max(1, val);
        updateSize(val);
        dataTracker.set(SCALE, val);
    }
}
