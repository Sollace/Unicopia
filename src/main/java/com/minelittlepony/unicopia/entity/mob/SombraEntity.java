package com.minelittlepony.unicopia.entity.mob;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.AmuletSelectors;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.ai.ArenaAttackGoal;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleSource;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LongDoorInteractGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.goal.WanderNearTargetGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.BirdPathNodeMaker;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.BossBar.Style;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.event.GameEvent;

public class SombraEntity extends HostileEntity implements ArenaCombatant, ParticleSource<SombraEntity> {
    static final byte BITE = 70;
    static final int MAX_BITE_TIME = 20;
    static final Predicate<Entity> EFFECT_TARGET_PREDICATE = EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
            .and(e -> e instanceof PlayerEntity)
            .and(e -> !(AbstractDisguiseSpell.getAppearance(e) instanceof SombraEntity));

    private static final TrackedData<Optional<BlockPos>> HOME_POS = DataTracker.registerData(SombraEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final TrackedData<Float> TARGET_SIZE = DataTracker.registerData(SombraEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private final ServerBossBar bossBar;
    final EntityReference<StormCloudEntity> stormCloud = new EntityReference<>();

    private int prevBiteTime;
    private int biteTime;

    private float prevSize;
    private float currentSize;

    public static void startEncounter(World world, BlockPos pos) {
        if (world.getEntitiesByClass(Entity.class, new Box(pos).expand(16), e -> {
            return e instanceof SombraEntity || e instanceof StormCloudEntity cloud && cloud.cursed;
        }).size() > 0) {
            return;
        }

        StormCloudEntity cloud = UEntities.STORM_CLOUD.create(world);
        cloud.setPosition(pos.up(10).toCenterPos());
        cloud.setSize(1);
        cloud.cursed = true;
        world.spawnEntity(cloud);
    }

    public SombraEntity(EntityType<SombraEntity> type, World world) {
        this(type, world, null);
    }

    public SombraEntity(EntityType<SombraEntity> type, World world, @Nullable ServerBossBar bossBar) {
        super(type, world);
        this.bossBar = bossBar == null ? createBossBar(getDisplayName()) : bossBar;
        this.bossBar.setName(getDisplayName());
        this.bossBar.setStyle(Style.NOTCHED_10);
    }

    public static ServerBossBar createBossBar(Text name) {
        return new SombraBossBar(name);
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return HostileEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 2000)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.5)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 22);
    }

    @Override
    public SombraEntity asEntity() {
        return this;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WARDEN_DEATH;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(HOME_POS, Optional.empty());
        dataTracker.startTracking(TARGET_SIZE, 1F);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new ArenaAttackGoal<>(this));
        goalSelector.add(2, new PounceAtTargetGoal(this, 1.3F));
        goalSelector.add(2, new LongDoorInteractGoal(this, true));
        goalSelector.add(2, new WanderNearTargetGoal(this, 1.5F, 32));
        goalSelector.add(6, new LookAtEntityGoal(this, LivingEntity.class, 32F));
        goalSelector.add(7, new LookAroundGoal(this));
        goalSelector.add(7, new WanderAroundGoal(this, 1));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
        targetSelector.add(3, new ActiveTargetGoal<>(this, MerchantEntity.class, false));
        targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        targetSelector.add(4, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        MobNavigation nav = new MobNavigation(this, world) {
            @Override
            protected PathNodeNavigator createPathNodeNavigator(int range) {
                nodeMaker = new BirdPathNodeMaker();
                nodeMaker.setCanEnterOpenDoors(true);
                return new PathNodeNavigator(nodeMaker, range);
            }

            @Override
            protected boolean canPathDirectlyThrough(Vec3d origin, Vec3d target) {
                return BirdNavigation.doesNotCollide(entity, origin, target, true);
            }

            @Override
            protected boolean isAtValidPosition() {
                return canSwim() && isInLiquid() || !entity.hasVehicle();
            }

            @Override
            protected Vec3d getPos() {
                return entity.getPos();
            }

            @Override
            @Nullable
            public Path findPathTo(BlockPos target, int distance) {
                return findPathTo(ImmutableSet.of(target), 8, false, distance);
            }
        };
        nav.setCanPathThroughDoors(true);
        nav.setCanSwim(true);
        nav.setCanEnterOpenDoors(true);
        nav.canJumpToNext(PathNodeType.UNPASSABLE_RAIL);
        return nav;
    }

    @Override
    public Optional<BlockPos> getHomePos() {
        return dataTracker.get(HOME_POS);
    }

    public void setHomePos(BlockPos pos) {
        dataTracker.set(HOME_POS, Optional.of(pos));
    }

    public float getBiteAmount(float tickDelta) {
        float progress = (MathHelper.lerp(tickDelta, prevBiteTime, biteTime) / (float)MAX_BITE_TIME);
        return 1 - Math.abs(MathHelper.sin(progress * MathHelper.PI * 3));
    }

    @Override
    public float getScaleFactor() {
        return Math.max(1, dataTracker.get(TARGET_SIZE));
    }

    public float getScaleFactor(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevSize, currentSize);
    }

    public void setScaleFactor(float targetSize) {
        dataTracker.set(TARGET_SIZE, targetSize);
        calculateDimensions();
    }

    @Override
    public void tick() {
        setPersistent();
        Optional<BlockPos> homePos = getHomePos();

        if (homePos.isEmpty()) {
            setHomePos(getWorld().getTopPosition(Type.MOTION_BLOCKING_NO_LEAVES, getBlockPos()));
            homePos = getHomePos();
        }

        if (!isRemoved()) {
            if (getNavigation().isIdle()) {
                if (getBlockPos().getSquaredDistance(homePos.get()) > MathHelper.square(getAreaRadius())) {
                    teleportTo(Vec3d.ofCenter(homePos.get()));
                    getNavigation().stop();
                }
            }

            prevBiteTime = biteTime;
            if (biteTime > 0) {
                biteTime--;
            }

            float targetSize = getScaleFactor();
            boolean sizeChanging = prevSize != currentSize;
            prevSize = currentSize;
            tickGrowth(targetSize, sizeChanging);
        }

        super.tick();

        if (getTarget() == null && getVelocity().y < -0.1F) {
            setVelocity(getVelocity().multiply(1, 0.4, 1));
        }

        addVelocity(0, 0.0442F, 0);

        LivingEntity target = getTarget();

        if (target != null && target.getY() > getY() && getVelocity().getY() < 0.5F) {

            float velocityChange = (float)MathHelper.clamp((target.getY() - getY()) * 0.05F, -0.3F, 0.3F);

            if (target instanceof WitherEntity || target instanceof FlyingEntity) {
                target.addVelocity(0, -velocityChange * 20, 0);
            } else {
                addVelocity(0, velocityChange, 0);
            }
        }

        if (isDead()) {
            return;
        }

        if (isSubmergedInWater()) {
            jump();
        }

        if (random.nextInt(1200) == 0) {
            laugh();
        }

        if (random.nextInt(340) == 0) {
            playSound(SoundEvents.AMBIENT_CAVE.value(), 1, 0.3F);
        } else if (random.nextInt(1340) == 0) {
            playSound(USounds.ENTITY_SOMBRA_AMBIENT, 1, 1);
        }

        if (getWorld().isClient) {
            generateBodyParticles();
        } else {
            if (getWorld().getGameRules().get(GameRules.DO_MOB_GRIEFING).get()) {
                for (BlockPos p : BlockPos.iterateOutwards(getBlockPos(), 2, 1, 2)) {
                    if (getWorld().getBlockState(p).getLuminance() > 13) {
                        destroyLightSource(p);
                    }
                }
            }

            float healthPercentage = 100 * (getHealth() / getMaxHealth());
            float difference = MathHelper.abs(healthPercentage - MathHelper.floor(healthPercentage));

            if (random.nextInt(healthPercentage < 90 && difference < 0.25F ? 19 : 120) == 0) {
                for (BlockPos p : BlockPos.iterateRandomly(random, 5, getBlockPos(), 20)) {
                    CrystalShardsEntity.infestBlock((ServerWorld)getWorld(), p);
                }
            }

            if (getTarget() == null && getNavigation().isIdle()) {
                getNavigation().startMovingTo(homePos.get().getX(), homePos.get().getY() + 5, homePos.get().getZ(), 2);
            }
        }

        if (getTarget() != null && getTarget().isRemoved()) {
            setTarget(null);
        }

        if (getHealth() < getMaxHealth()) {
            for (Entity shard : getWorld().getEntitiesByClass(CrystalShardsEntity.class, getBoundingBox().expand(50), EntityPredicates.VALID_ENTITY)) {

                if (age % 150 == 0) {
                    heal(2);

                    ParticleUtils.spawnParticle(getWorld(),
                            new FollowingParticleEffect(UParticles.HEALTH_DRAIN, this, 0.2F)
                            .withChild(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE),
                            shard.getPos(),
                            Vec3d.ZERO
                    );
                }
            }
        }

        getHomePos().ifPresent(this::generateArenaEffects);
    }

    protected void tickGrowth(float targetSize, boolean changing) {
        if (currentSize != targetSize) {
            float sizeDifference = (dataTracker.get(TARGET_SIZE) - currentSize);
            currentSize = Math.abs(sizeDifference) < 0.01F ? targetSize : currentSize + (sizeDifference * 0.2F);
            calculateDimensions();
        }

        if (currentSize == targetSize && changing) {
            laugh();
        }

        if (currentSize == targetSize && isDead()) {
            setScaleFactor(currentSize + 1);
        }
    }

    private void laugh() {
        if (!getWorld().isClient) {
            playSound(USounds.ENTITY_SOMBRA_LAUGH, 1, 1);
            getWorld().sendEntityStatus(this, BITE);
        }
    }

    protected void applyAreaEffects(PlayerEntity target) {
        if (this.age % 150 == 0) {
            target.playSound(
                    random.nextInt(30) == 0 ? USounds.ENTITY_SOMBRA_AMBIENT
                            : random.nextInt(10) == 0 ? USounds.ENTITY_SOMBRA_SCARY
                            : USounds.Vanilla.AMBIENT_CAVE.value(),
                    (float)random.nextTriangular(1, 0.2F),
                    (float)random.nextTriangular(0.3F, 0.2F)
            );
        }

        if (this.age % 1000 < 50) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 26, 0, true, false));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 26, 0, true, false));
        }

        if (getTarget() == null && target.distanceTo(this) < getAreaRadius() / 2F) {
            if (teleportTo(target.getPos())) {
                setPosition(getPos().add(0, 4, 0));
            }
        }
    }

    protected void destroyLightSource(BlockPos pos) {
        getWorld().breakBlock(pos, true);
        playSound(USounds.ENTITY_SOMBRA_SNICKER, 1, 1);
    }

    protected void generateBodyParticles() {
        for (int i = 0; i < 3; i++) {
            getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                    random.nextTriangular(getX(), 3),
                    random.nextTriangular(getY(), 3),
                    random.nextTriangular(getZ(), 3),
                    0,
                    0,
                    0
                );
        }
    }

    private void generateArenaEffects(BlockPos home) {
        if (getWorld().isClient()) {
            Stream.concat(
                    new Sphere(false, getAreaRadius()).translate(home).randomPoints(random).filter(this::isSurfaceBlock).limit(80),
                    new Sphere(true, getAreaRadius()).translate(home).randomPoints(random).filter(this::isSurfaceBlock).limit(30))
                .forEach(pos -> {
                    ParticleEffect type = random.nextInt(3) < 1 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SOUL_FIRE_FLAME;
                    ParticleUtils.spawnParticle(getWorld(), type, pos, Vec3d.ZERO);
                    ParticleUtils.spawnParticle(getWorld(), type, pos, pos.subtract(getPos()).add(0, 0.1, 0).multiply(-0.013));
                });
        } else {
            VecHelper.findInRange(this, getWorld(), home.toCenterPos(), getAreaRadius() - 0.2F, EFFECT_TARGET_PREDICATE).forEach(e -> {
                applyAreaEffects((PlayerEntity)e);
            });
        }
    }

    private boolean isSurfaceBlock(Vec3d pos) {
        BlockPos bPos = BlockPos.ofFloored(pos);
        return getWorld().isAir(bPos) && !getWorld().isAir(bPos.down());
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        bossBar.setPercent(getHealth() / getMaxHealth());
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
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof PlayerEntity player) {
            if (AmuletSelectors.ALICORN_AMULET.test(player)) {
                if (!getWorld().isClient) {
                    playSound(USounds.ENTITY_SOMBRA_SNICKER, 1, 1);
                    player.sendMessage(Text.translatable("entity.unicopia.sombra.taunt"));
                }
            }
            ItemStack amulet = AmuletItem.getForEntity(player);
            if (amulet.isOf(UItems.ALICORN_AMULET)) {
                amulet.decrement(1);
            }
        }
        boolean damaged = super.damage(source, amount);

        if (!getWorld().isClient) {
            if (source.getAttacker() instanceof LivingEntity attacker) {
                teleportRandomly(6);

                if (!(attacker instanceof PlayerEntity player && (player.isCreative() || player.isSpectator()))) {
                    setTarget(attacker);
                }
            }

            float targetSize = getScaleFactor();
            if (targetSize > 1) {
                setScaleFactor(Math.max(1, targetSize * 0.9F));
            }
        }

        return damaged;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        if (!dead) {
            stormCloud.ifPresent(getWorld(), cloud -> {
                cloud.setStormTicks(0);
                cloud.setDissipating(true);
            });
            stormCloud.set(null);
            getHomePos().ifPresent(home -> {
                VecHelper.findInRange(this, getWorld(), home.toCenterPos(), getAreaRadius() - 0.2F, e -> e instanceof CrystalShardsEntity).forEach(e -> {
                    ((CrystalShardsEntity)e).setDecaying(true);
                });

                VecHelper.findInRange(this, getWorld(), home.toCenterPos(), getAreaRadius() - 0.2F, EFFECT_TARGET_PREDICATE).forEach(player -> {
                    Pony.of((PlayerEntity)player).getCorruption().set(0);
                    UCriteria.DEFEAT_SOMBRA.trigger(player);
                });
            });

        }
        super.onDeath(damageSource);
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        ItemEntity itemEntity = dropItem(UItems.BROKEN_ALICORN_AMULET);
        if (itemEntity != null) {
            itemEntity.setCovetedItem();
        }
    }

    @Override
    protected void updatePostDeath() {
        if (++deathTime >= 180 && deathTime <= 200) {
            getWorld().addParticle(ParticleTypes.EXPLOSION_EMITTER,
                    random.nextTriangular(getX(), 4F),
                    random.nextTriangular(getY() + 2, 2F),
                    random.nextTriangular(getZ(), 4F), 0, 0, 0);
        }

        move(MovementType.SELF, new Vec3d(0, 0.3F, 0));

        if (getWorld() instanceof ServerWorld sw) {
            final boolean dropLoot = this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
            final int experience = 500;

            if (deathTime > 150 && deathTime % 5 == 0 && dropLoot) {
                ExperienceOrbEntity.spawn(sw, getPos(), MathHelper.floor(experience * 0.08f));
            }

            if (deathTime == 1 && !isSilent()) {
                getWorld().syncGlobalEvent(WorldEvents.ENDER_DRAGON_DIES, this.getBlockPos(), 0);
            }

            if (deathTime == 200) {
                if (dropLoot) {
                    ExperienceOrbEntity.spawn(sw, getPos(), MathHelper.floor(experience * 0.2f));
                }
                remove(RemovalReason.KILLED);
                emitGameEvent(GameEvent.ENTITY_DIE);
            }
        }
    }

    @Override
    protected void fall(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canTarget(LivingEntity target) {
        if (target instanceof SombraEntity
                || EquinePredicates.IS_MAGIC_IMMUNE.test(target)
                || !EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(target)) {
            return false;
        }

        if (target != null && !target.isRemoved() && target == getTarget()) {
            return true;
        }
        return super.canTarget(target) && getHomePos().filter(home -> target.getPos().isInRange(home.toCenterPos(), getAreaRadius())).isPresent();
    }

    @Override
    public boolean isInWalkTargetRange(BlockPos pos) {
        BlockPos center = getHomePos().orElse(getBlockPos());
        double distance = pos.getSquaredDistanceFromCenter(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);
        return distance < MathHelper.square(getAreaRadius());
    }

    @Override
    public boolean tryAttack(Entity target) {
        laugh();
        return super.tryAttack(target);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == BITE) {
            biteTime = MAX_BITE_TIME;
        } else {
            super.handleStatus(status);
        }
    }

    protected boolean teleportRandomly(int maxDistance) {
        if (getWorld().isClient() || !isAlive()) {
            return false;
        }
        return teleportTo(getPos().add(VecHelper.supply(() -> random.nextTriangular(0, maxDistance))));
    }

    @Override
    public boolean teleportTo(Vec3d destination) {
        Vec3d oldPos = getPos();
        if (canTeleportTo(destination) && teleport(destination.x, destination.y, destination.z, true)) {
            getWorld().emitGameEvent(GameEvent.TELEPORT, oldPos, GameEvent.Emitter.of(this));
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean canTeleportTo(Vec3d destination) {
        BlockPos.Mutable mutable = new BlockPos.Mutable(destination.x, destination.y, destination.z);
        while (mutable.getY() > getWorld().getBottomY() && !getWorld().getBlockState(mutable).blocksMovement()) {
            mutable.move(Direction.DOWN);
        }
        BlockState destinationState = getWorld().getBlockState(mutable);
        return destinationState.blocksMovement() && !destinationState.getFluidState().isIn(FluidTags.WATER);
    }

    @Override
    @Deprecated
    public float getBrightnessAtEyes() {
        return super.getBrightnessAtEyes() * 0.2F;
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        getHomePos().map(NbtHelper::fromBlockPos).ifPresent(pos -> {
            nbt.put("homePos", pos);
        });
        nbt.put("cloud", stormCloud.toNBT());
        nbt.putFloat("size", getScaleFactor());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("homePos", NbtElement.COMPOUND_TYPE)) {
            setHomePos(NbtHelper.toBlockPos(nbt.getCompound("homePos")));
        }
        if (hasCustomName()) {
            bossBar.setName(getDisplayName());
        }
        setScaleFactor(nbt.getFloat("size"));
        stormCloud.fromNBT(nbt.getCompound("cloud"));
    }

    private static class SombraBossBar extends ServerBossBar {
        public SombraBossBar(Text displayName) {
            super(displayName, BossBar.Color.PURPLE, BossBar.Style.PROGRESS);
            setDarkenSky(true);
            setThickenFog(true);
            setDragonMusic(true);
        }

        @Override
        public void setPercent(float percent) {
            super.setPercent(percent);
            if (percent > 0.6F && getColor() == Color.PURPLE) {
                setColor(Color.RED);
            }
        }
    }
}
