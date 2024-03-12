package com.minelittlepony.unicopia.entity;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellContainer;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.behaviour.Guest;
import com.minelittlepony.unicopia.entity.collision.MultiBoundingBoxEntity;
import com.minelittlepony.unicopia.entity.damage.MagicalDamageSource;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.effect.CorruptInfluenceStatusEffect;
import com.minelittlepony.unicopia.entity.effect.EffectUtils;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.input.Heuristic;
import com.minelittlepony.unicopia.input.Interactable;
import com.minelittlepony.unicopia.item.GlassesItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.datasync.EffectSync;
import com.minelittlepony.unicopia.network.datasync.Transmittable;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;
import com.minelittlepony.unicopia.server.world.DragonBreathStore;
import com.minelittlepony.unicopia.util.*;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.*;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class Living<T extends LivingEntity> implements Equine<T>, Caster<T>, Transmittable {
    private static final TrackedData<Optional<UUID>> CARRIER_ID = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    protected final T entity;

    private final EffectSync effectDelegate;

    private final Interactable sneakingHeuristic;
    private final Interactable landedHeuristic;
    private final Interactable jumpingHeuristic;

    @Nullable
    private Runnable landEvent;

    private boolean invisible = false;

    @Nullable
    private Entity supportingEntity;

    @Nullable
    private Vec3d supportPositionOffset;
    private int ticksOutsideVehicle;
    private int ticksInVehicle;

    @Nullable
    private Caster<?> attacker;
    @Nullable
    private transient Caster<?> host;

    private Optional<Living<?>> target = Optional.empty();

    private int invinsibilityTicks;

    private final List<Tickable> tickers = new ArrayList<>();

    private final Enchantments enchants = addTicker(new Enchantments(this));
    private final ItemTracker armour = addTicker(new ItemTracker(this));

    protected Living(T entity, TrackedData<NbtCompound> effect) {
        this.entity = entity;
        this.effectDelegate = new EffectSync(this, effect);
        this.sneakingHeuristic = addTicker(new Interactable(entity::isSneaking));
        this.landedHeuristic = addTicker(new Interactable(entity::isOnGround));
        this.jumpingHeuristic = addTicker(new Interactable(((LivingEntityDuck)entity)::isJumping));
    }

    @Override
    public void initDataTracker() {
        effectDelegate.initDataTracker();
        entity.getDataTracker().startTracking(Creature.GRAVITY, 1F);
        entity.getDataTracker().startTracking(CARRIER_ID, Optional.empty());
    }

    public <Q extends Tickable> Q addTicker(Q tickable) {
        tickers.add(Objects.requireNonNull(tickable, "tickable cannot be null"));
        return tickable;
    }

    public boolean isInvisible() {
        return invisible && SpellPredicate.IS_DISGUISE.isOn(this);
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public void waitForFall(Runnable action) {
        if (entity.isOnGround()) {
            action.run();
        } else {
            landEvent = action;
        }
    }

    public boolean sneakingChanged() {
        return sneakingHeuristic.hasChanged(Heuristic.ONCE);
    }

    public boolean landedChanged() {
        return landedHeuristic.hasChanged(Heuristic.ONCE);
    }

    public Interactable getJumpingHeuristic() {
        return jumpingHeuristic;
    }

    @Override
    public SpellContainer getSpellSlot() {
        return effectDelegate;
    }

    public Enchantments getEnchants() {
        return enchants;
    }

    public ItemTracker getArmour() {
        return armour;
    }

    @Override
    public final T asEntity() {
        return entity;
    }

    public Optional<UUID> getCarrierId() {
        return entity.getDataTracker().get(CARRIER_ID);
    }

    public void setCarrier(UUID carrier) {
        entity.getDataTracker().set(CARRIER_ID, Optional.ofNullable(carrier));
    }

    public void setCarrier(Entity carrier) {
        entity.getDataTracker().set(CARRIER_ID, Optional.ofNullable(carrier).map(Entity::getUuid));
    }

    @Nullable
    public Optional<Living<?>> getTarget() {
        return target;
    }

    public void setTarget(Living<?> target) {
        this.target = Optional.ofNullable(target);
    }

    public boolean isBeingCarried() {
        Entity vehicle = entity.getVehicle();
        return vehicle != null && getCarrierId().filter(vehicle.getUuid()::equals).isPresent();
    }

    public boolean setSupportingEntity(@Nullable Entity supportingEntity) {
        this.supportingEntity = supportingEntity;
        if (supportingEntity != null) {
            ticksOutsideVehicle = 0;
        }
        return true;
    }

    @Nullable
    public Entity getSupportingEntity() {
        return supportingEntity;
    }

    public int getTicksInVehicle() {
        return ticksInVehicle;
    }

    public void setPositionOffset(@Nullable Vec3d positionOffset) {
        this.supportPositionOffset = positionOffset;
    }

    public void updatePositionOffset() {
        setPositionOffset(supportingEntity == null ? null : entity.getPos().subtract(supportingEntity.getPos()));
    }

    public void updateRelativePosition(Box box) {
        if (supportingEntity == null || supportPositionOffset == null) {
            return;
        }
        if (getPhysics().isFlying()) {
            return;
        }

        Vec3d newPos = supportingEntity.getPos().add(supportPositionOffset);
        Vec3d posChange = entity.getPos().subtract(newPos);
        entity.setPosition(newPos);
        if (isClient()) {
            Vec3d newServerPos = LivingEntityDuck.serverPos(entity);
            if (newServerPos.lengthSquared() != 0) {
                newServerPos = newServerPos.subtract(posChange);
                entity.updateTrackedPositionAndAngles(
                        newServerPos.x, newServerPos.y, newServerPos.z,
                        entity.getYaw(), entity.getPitch(), 3, true);
            }
        } else {
            entity.updateTrackedPosition(newPos.x, newPos.y, newPos.z);
        }

        if (!(entity instanceof PlayerEntity)) {
            entity.lastRenderX = supportingEntity.lastRenderX + supportPositionOffset.x;
            entity.lastRenderY = supportingEntity.lastRenderY + supportPositionOffset.y;
            entity.lastRenderZ = supportingEntity.lastRenderZ + supportPositionOffset.z;

            if (entity.getVelocity().length() < 0.1) {
                LimbAnimationUtil.resetToZero(entity.limbAnimator);
            }
        }

        entity.horizontalSpeed = 0;
        entity.prevHorizontalSpeed = 0;
        entity.speed = 0;
        entity.setOnGround(true);
        entity.verticalCollision = true;
        entity.groundCollision = true;
        entity.fallDistance = 0;
    }

    @Override
    public boolean beforeUpdate() {
        if (EffectUtils.getAmplifier(entity, UEffects.PARALYSIS) > 1 && entity.getVelocity().horizontalLengthSquared() > 0) {
            entity.setVelocity(entity.getVelocity().multiply(0, 1, 0));
            updateVelocity();
        }

        updateSupportingEntity();
        return false;
    }

    public void updateSupportingEntity() {
        if (supportingEntity != null) {
            Box ownBox = entity.getBoundingBox()
                    .stretch(entity.getVelocity())
                    .expand(0.1, 0.5, 0.1)
                    .stretch(supportingEntity.getVelocity().multiply(-2));

            MultiBoundingBoxEntity.getBoundingBoxes(supportingEntity).stream()
            .filter(box -> box.stretch(supportingEntity.getVelocity()).expand(0, 0.5, 0).intersects(ownBox))
            .findFirst()
            .ifPresentOrElse(box -> {
                ticksOutsideVehicle = 0;
                if (supportPositionOffset == null) {
                    updatePositionOffset();
                } else {
                    updateRelativePosition(box);
                }
                entity.setOnGround(true);
                entity.verticalCollision = true;
                entity.groundCollision = true;
            }, () -> {
                // Rubberband passengers to try and prevent players falling out when the velocity changes suddenly
                if (ticksOutsideVehicle++ > 30) {
                    supportingEntity = null;
                    supportPositionOffset = null;
                    Unicopia.LOGGER.info("Entity left vehicle");
                } else {
                    supportPositionOffset = supportPositionOffset.multiply(0.25, 1, 0.25);
                }
            });
        }

    }

    @Override
    public void tick() {
        tickers.forEach(Tickable::tick);
        effectDelegate.tick(Situation.BODY);

        if (!(entity instanceof PlayerEntity)) {
            if (!entity.hasVehicle() && getCarrierId().isPresent() && !asWorld().isClient && entity.age % 10 == 0) {
                UUID carrierId = getCarrierId().get();
                Entity carrier = ((ServerWorld)asWorld()).getEntity(carrierId);
                if (carrier != null) {
                    asEntity().startRiding(carrier, true);
                    Living.transmitPassengers(carrier);
                } else {
                    Unicopia.LOGGER.warn("No passenger with id {]", carrierId);
                }
            }
        }

        if (invinsibilityTicks > 0) {
            invinsibilityTicks--;
        }

        if (landEvent != null && entity.isOnGround() && landedChanged()) {
            landEvent.run();
            landEvent = null;
        }

        if (entity.hasStatusEffect(UEffects.PARALYSIS) && entity.getVelocity().horizontalLengthSquared() > 0) {
            entity.setVelocity(entity.getVelocity().multiply(0, 1, 0));
            updateVelocity();
        }

        if (isBeingCarried()) {
            Pony carrier = Pony.of(entity.getVehicle()).orElse(null);
            if (!Abilities.CARRY.canUse(carrier.getCompositeRace()) && !Abilities.HUG.canUse(carrier.getCompositeRace())) {
                entity.stopRiding();
                entity.refreshPositionAfterTeleport(carrier.getOriginVector());
                Living.transmitPassengers(carrier.asEntity());
            }
            entity.setYaw(carrier.asEntity().getYaw());
        }

        updateDragonBreath();

        if (ticksOutsideVehicle == 0) {
            updatePositionOffset();

            ticksInVehicle++;
        } else {
            ticksInVehicle = 0;
        }
    }

    public void updateAttributeModifier(UUID id, EntityAttribute attribute, float desiredValue, Float2ObjectFunction<EntityAttributeModifier> modifierSupplier, boolean permanent) {
        @Nullable
        EntityAttributeInstance instance = asEntity().getAttributeInstance(attribute);
        if (instance == null) {
            return;
        }

        @Nullable
        EntityAttributeModifier modifier = instance.getModifier(id);

        if (!MathHelper.approximatelyEquals(desiredValue, modifier == null ? 0 : modifier.getValue())) {
            if (modifier != null) {
                instance.removeModifier(modifier);
            }

            if (desiredValue != 0) {
                if (permanent) {
                    instance.addPersistentModifier(modifierSupplier.get(desiredValue));
                } else {
                    instance.addTemporaryModifier(modifierSupplier.get(desiredValue));
                }
            }
        }
    }

    public boolean canBeSeenBy(Entity entity) {
        return !isInvisible()
            && getSpellSlot()
            .get(SpellPredicate.IS_DISGUISE, true)
            .filter(spell -> spell.getDisguise().getAppearance() == entity)
            .isEmpty();
    }

    public Optional<Vec3d> adjustMovementSpeedInWater(Vec3d speed) {
        return Optional.empty();
    }

    private void updateDragonBreath() {
        if (!entity.getWorld().isClient && (entity instanceof PlayerEntity || entity.hasCustomName())) {

            Vec3d targetPos = entity.getRotationVector().multiply(2).add(entity.getEyePos());

            if (entity.getWorld().isAir(BlockPos.ofFloored(targetPos))) {
                DragonBreathStore store = DragonBreathStore.get(entity.getWorld());
                String name = entity.getDisplayName().getString();
                store.popEntries(name).forEach(stack -> {
                    ItemStack payload = stack.payload();
                    Item item = payload.getItem();

                    boolean deliverAggressively = payload.isIn(UTags.IS_DELIVERED_AGGRESSIVELY);

                    Vec3d randomPos = deliverAggressively ? targetPos.add(0, 2, 0) : targetPos.add(VecHelper.supply(() -> entity.getRandom().nextTriangular(0.1, 0.5)));

                    if (deliverAggressively && item instanceof BlockItem blockItem) {
                        do {
                            ItemStack instance = payload.split(1);
                            BlockPos pos = BlockPos.ofFloored(randomPos);
                            if (!entity.getWorld().isAir(pos)) {
                                store.put(name, instance);
                            } else {

                                for (int i = 0; i < 10; i++) {
                                    ParticleUtils.spawnParticle(entity.getWorld(), ParticleTypes.FLAME, randomPos.add(
                                            VecHelper.supply(() -> entity.getRandom().nextTriangular(0.1, 0.5))
                                    ), Vec3d.ZERO);
                                }

                                ItemPlacementContext context = new ItemPlacementContext(entity.getWorld(), (PlayerEntity)null, Hand.MAIN_HAND, instance,
                                        BlockHitResult.createMissed(Vec3d.ZERO, Direction.UP, pos)
                                );

                                BlockState state = blockItem.getBlock().getPlacementState(context);
                                if (state == null) {
                                    state = blockItem.getBlock().getDefaultState();
                                }

                                entity.getWorld().setBlockState(pos, state);
                                BlockSoundGroup sound = state.getSoundGroup();
                                entity.getWorld().playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1) * 0.5F, sound.getPitch() * 0.8F);
                            }
                            randomPos = targetPos.add(VecHelper.supply(() -> entity.getRandom().nextTriangular(0.1, 0.5)));
                        } while (!payload.isEmpty());
                    } else {
                        if (!entity.getWorld().isAir(BlockPos.ofFloored(randomPos))) {
                            store.put(name, stack.payload());
                        } else {
                            for (int i = 0; i < 10; i++) {
                                ParticleUtils.spawnParticle(entity.getWorld(), ParticleTypes.FLAME, randomPos.add(
                                        VecHelper.supply(() -> entity.getRandom().nextTriangular(0.1, 0.5))
                                ), Vec3d.ZERO);
                            }

                            ItemEntity itemEntity = EntityType.ITEM.create(entity.getWorld());
                            itemEntity.setStack(payload);
                            itemEntity.setPosition(randomPos);
                            itemEntity.getWorld().spawnEntity(itemEntity);
                            entity.getWorld().playSoundFromEntity(null, entity, USounds.ITEM_DRAGON_BREATH_ARRIVE, entity.getSoundCategory(), 1, 1);
                            UCriteria.SEND_DRAGON_BREATH.triggerReceived(entity, payload.copy());
                        }
                    }
                });
            }
        }
    }

    public boolean onUpdatePassengerPosition(Entity passender, Entity.PositionUpdater positionUpdater) {
        return false;
    }

    public void onJump() {
        if (getPhysics().isGravityNegative()) {
            entity.setVelocity(entity.getVelocity().multiply(1, -1, 1));
        }
    }

    @Nullable
    public final Caster<?> getAttacker() {
        return attacker;
    }

    public Optional<Boolean> onDamage(DamageSource source, float amount) {

        if (Guest.of(source.getAttacker()).hostIs(this)
            || Guest.of(source.getSource()).hostIs(this)) {
            var type = source.getTypeRegistryEntry();
            return Optional.of(entity.damage(
                    type.matchesKey(DamageTypes.FIREBALL) ? entity.getDamageSources().create(DamageTypes.UNATTRIBUTED_FIREBALL) :
                    type.matchesKey(DamageTypes.PLAYER_EXPLOSION) ? entity.getDamageSources().create(DamageTypes.EXPLOSION) :
                    new DamageSource(type, entity, entity), amount));
        }

        if (Guest.of(entity).getHost() instanceof Living l) {
            l.asEntity().damage(source, amount);
        }

        if (source.isIn(DamageTypeTags.IS_LIGHTNING) && (invinsibilityTicks > 0 || tryCaptureLightning())) {
            return Optional.of(false);
        }

        if (source instanceof MagicalDamageSource magical) {
            Caster<?> attacker = ((MagicalDamageSource)source).getSpell();
            if (attacker != null) {
                this.attacker = attacker;
            }

            if (magical.isIn(UTags.BREAKS_SUNGLASSES)) {
                ItemStack glasses = GlassesItem.getForEntity(entity);
                if (glasses.getItem() == UItems.SUNGLASSES) {
                    ItemStack broken = UItems.BROKEN_SUNGLASSES.getDefaultStack();
                    broken.setNbt(glasses.getNbt());
                    TrinketsDelegate.getInstance(entity).setEquippedStack(entity, TrinketsDelegate.FACE, broken);
                    playSound(USounds.ITEM_SUNGLASSES_SHATTER, 1, 1);
                }
            }
        }

        if (entity instanceof HostileEntity mob && mob.hasStatusEffect(UEffects.CORRUPT_INFLUENCE) && mob.getRandom().nextInt(4) == 0) {
            CorruptInfluenceStatusEffect.reproduce(mob);
        }

        return Optional.empty();
    }

    public TriState canBeHurtByWater() {
        return TriState.DEFAULT;
    }

    public Optional<BlockPos> chooseClimbingPos() {
        return getSpellSlot().get(SpellPredicate.IS_DISGUISE, false)
                .map(AbstractDisguiseSpell::getDisguise)
                .filter(EntityAppearance::canClimbWalls)
                .map(v -> entity.getBlockPos());
    }

    private boolean tryCaptureLightning() {
        return getInventoryStacks().filter(stack -> !stack.isEmpty() && stack.getItem() == UItems.EMPTY_JAR).findFirst().map(stack -> {
            invinsibilityTicks = 20;
            stack.split(1);
            giveBackItem(UItems.LIGHTNING_JAR.getDefaultStack());
            return stack;
        }).isPresent();
    }

    protected Stream<ItemStack> getInventoryStacks() {
        return Stream.of(entity.getStackInHand(Hand.MAIN_HAND), entity.getStackInHand(Hand.OFF_HAND));
    }

    protected Stream<ItemStack> getArmourStacks() {
        if (!TrinketsDelegate.hasTrinkets()) {
            return StreamSupport.stream(entity.getArmorItems().spliterator(), false);
        }
        return Stream.concat(
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.NECKLACE),
                StreamSupport.stream(entity.getArmorItems().spliterator(), false)
        );
    }

    protected void giveBackItem(ItemStack stack) {
        entity.dropStack(stack);
    }

    @Override
    public boolean onProjectileImpact(ProjectileEntity projectile) {
        return getSpellSlot().get(true)
                .filter(effect -> !effect.isDead()
                        && effect instanceof ProjectileImpactListener
                        && ((ProjectileImpactListener)effect).onProjectileImpact(projectile))
                .isPresent();
    }

    protected void handleFall(float distance, float damageMultiplier, DamageSource cause) {
        getSpellSlot().get(SpellPredicate.IS_DISGUISE, false).ifPresent(spell -> {
            spell.getDisguise().onImpact(this, distance, damageMultiplier, cause);
        });
    }

    @Override
    public float getCloudWalkingStrength() {
        Enchantment featherFalling = net.minecraft.enchantment.Enchantments.FEATHER_FALLING;
        int maxLevel = featherFalling.getMaxLevel();
        int level = EnchantmentHelper.getEquipmentLevel(featherFalling, entity);
        return MathHelper.clamp(level / (float)maxLevel, 0, 1);
    }

    @Override
    public void setDirty() {}

    @Override
    public void toNBT(NbtCompound compound) {
        enchants.toNBT(compound);
        effectDelegate.toNBT(compound);
        toSyncronisedNbt(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        enchants.fromNBT(compound);
        effectDelegate.fromNBT(compound);
        fromSynchronizedNbt(compound);
    }

    @Override
    public void toSyncronisedNbt(NbtCompound compound) {
        compound.put("armour", armour.toNBT());
        getCarrierId().ifPresent(id -> compound.putUuid("carrier", id));
    }

    @Override
    public void fromSynchronizedNbt(NbtCompound compound) {
        armour.fromNBT(compound.getCompound("armour"));
        setCarrier(compound.containsUuid("carrier") ? compound.getUuid("carrier") : null);
    }

    public void updateVelocity() {
        updateVelocity(entity);
    }

    @Override
    public boolean canUse(Ability<?> ability) {
        return ability.canUse(getCompositeRace());
    }

    public static Optional<Living<?>> getOrEmpty(Entity entity) {
        return Equine.of(entity, a -> a instanceof Living);
    }

    public static Living<?> living(Entity entity) {
        return getOrEmpty(entity).orElse(null);
    }

    public static <E extends LivingEntity> Living<E> living(E entity) {
        return Equine.<E, Living<E>>of(entity, e -> e instanceof Living<?>).orElse(null);
    }

    public static void updateVelocity(@Nullable Entity entity) {
        if (entity != null) {
            entity.velocityModified = true;
            //if (entity instanceof ServerPlayerEntity ply) {
            //    ply.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(ply));
            //}
        }
    }

    public static void transmitPassengers(@Nullable Entity entity) {
        if (entity != null && entity.getWorld() instanceof ServerWorld sw) {
            sw.getChunkManager().sendToNearbyPlayers(entity, new EntityPassengersSetS2CPacket(entity));
        }
    }
}
