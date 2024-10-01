package com.minelittlepony.unicopia.entity;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellInventory;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.behaviour.Guest;
import com.minelittlepony.unicopia.entity.damage.MagicalDamageSource;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.effect.CorruptInfluenceStatusEffect;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.input.Heuristic;
import com.minelittlepony.unicopia.input.Interactable;
import com.minelittlepony.unicopia.item.GlassesItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.component.BreaksIntoItemComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.DataTrackerManager;
import com.minelittlepony.unicopia.network.track.Trackable;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;
import com.minelittlepony.unicopia.server.world.DragonBreathStore;
import com.minelittlepony.unicopia.util.*;
import com.minelittlepony.unicopia.util.serialization.NbtMap;
import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class Living<T extends LivingEntity> implements Equine<T>, Caster<T>, AttributeContainer {
    protected final T entity;

    private final SpellInventory spells;

    private final Interactable sneakingHeuristic;
    private final Interactable landedHeuristic;
    private final Interactable jumpingHeuristic;

    private boolean invisible = false;

    @Nullable
    private Caster<?> attacker;
    @Nullable
    private transient Caster<?> host;

    private Optional<Living<?>> target = Optional.empty();

    private int invinsibilityTicks;

    private final List<Tickable> tickers = new ArrayList<>();

    private final LandingEventHandler landEvent = addTicker(new LandingEventHandler(this));
    private final NbtMap<Identifier, Float> enchants = NbtMap.of(Identifier.CODEC, Codec.FLOAT);
    private final ItemTracker armour = addTicker(new ItemTracker(this));
    private final Transportation<T> transportation = new Transportation<>(this);

    protected final DataTrackerManager trackers;
    protected final DataTracker tracker;

    protected final DataTracker.Entry<UUID> carrierId;

    protected Living(T entity) {
        this.entity = entity;
        this.trackers = Trackable.of(entity).getDataTrackers();
        this.tracker = trackers.getPrimaryTracker();
        this.spells = SpellSlots.ofUnbounded(this);
        this.sneakingHeuristic = addTicker(new Interactable(entity::isSneaking));
        this.landedHeuristic = addTicker(new Interactable(entity::isOnGround));
        this.jumpingHeuristic = addTicker(new Interactable(((LivingEntityDuck)entity)::isJumping));

        carrierId = tracker.startTracking(TrackableDataType.UUID, Util.NIL_UUID);
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

    public void waitForFall(LandingEventHandler.Callback callback) {
        landEvent.setCallback(callback);
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
    public SpellSlots getSpellSlot() {
        return spells.getSlots();
    }

    public NbtMap<Identifier, Float> getEnchants() {
        return enchants;
    }

    public ItemTracker getArmour() {
        return armour;
    }

    public Transportation<T> getTransportation() {
        return transportation;
    }

    @Override
    public final T asEntity() {
        return entity;
    }

    public Optional<UUID> getCarrierId() {
        UUID carrierId = this.carrierId.get();
        return carrierId == Util.NIL_UUID ? Optional.empty() : Optional.of(carrierId);
    }

    public void setCarrier(UUID carrier) {
        carrierId.set(carrier == null ? Util.NIL_UUID : carrier);
    }

    public void setCarrier(Entity carrier) {
        setCarrier(carrier == null ? Util.NIL_UUID : carrier.getUuid());
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

    @Override
    public boolean hasFeatherTouch() {
        return EnchantmentUtil.getLevel(UEnchantments.FEATHER_TOUCH, entity) > 0;
    }

    @Override
    public boolean beforeUpdate() {
        landEvent.beforeTick();
        if (entity.hasStatusEffect(UEffects.PARALYSIS) && entity.getVelocity().horizontalLengthSquared() > 0) {
            entity.setVelocity(entity.getVelocity().multiply(0, 1, 0));
            updateVelocity();
        }

        return false;
    }

    @Override
    public void tick() {
        tickers.forEach(Tickable::tick);
        spells.tick(Situation.BODY);

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

        if (EnchantmentUtil.getLevel(UEnchantments.GEM_FINDER, entity) > 0) {
            InteractionManager.getInstance().playLoopingSound(entity, InteractionManager.SOUND_GEM_FINDING_MAGIC_HUM, 0);
        }

        transportation.tick();
    }

    @Override
    public final @Nullable EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute) {
        return asEntity().getAttributeInstance(attribute);
    }

    public boolean canBeSeenBy(Entity entity) {
        return !isInvisible()
            && getSpellSlot()
            .get(SpellPredicate.IS_DISGUISE)
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

                    boolean deliverAggressively = payload.isIn(UTags.Items.IS_DELIVERED_AGGRESSIVELY);

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

            ItemStack glasses = GlassesItem.getForEntity(entity).stack();
            BreaksIntoItemComponent afterBroken = glasses.get(UDataComponentTypes.ITEM_AFTER_BREAKING);

            if (afterBroken != null && afterBroken.damageType().contains(magical.getTypeRegistryEntry())) {
                if (afterBroken != null) {
                    afterBroken.getItemAfterBreaking().ifPresent(b -> {
                        ItemStack broken = glasses.withItem(b);
                        TrinketsDelegate.getInstance(entity).setEquippedStack(entity, TrinketsDelegate.FACE, broken);
                        afterBroken.getBreakingSound().ifPresent(sound -> {
                            playSound(USounds.ITEM_SUNGLASSES_SHATTER, 1, 1);
                        });
                    });
                }
            }
        }

        if (entity instanceof HostileEntity mob && mob.hasStatusEffect(UEffects.CORRUPT_INFLUENCE) && mob.getRandom().nextInt(4000) == 0) {
            CorruptInfluenceStatusEffect.reproduce(mob);
        }

        return Optional.empty();
    }

    public TriState canBeHurtByWater() {
        return TriState.DEFAULT;
    }

    public Optional<BlockPos> chooseClimbingPos() {
        return getSpellSlot().get(SpellPredicate.IS_DISGUISE)
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
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.NECKLACE).map(TrinketsDelegate.EquippedStack::stack),
                StreamSupport.stream(entity.getArmorItems().spliterator(), false)
        );
    }

    protected void giveBackItem(ItemStack stack) {
        entity.dropStack(stack);
    }

    @Override
    public boolean onProjectileImpact(ProjectileEntity projectile) {
        return getSpellSlot().get()
                .filter(effect -> !effect.isDead()
                        && effect instanceof ProjectileImpactListener
                        && ((ProjectileImpactListener)effect).onProjectileImpact(projectile))
                .isPresent();
    }

    public float onImpact(float distance, float damageMultiplier, DamageSource cause) {
        float fallDistance = landEvent.fire(getEffectiveFallDistance(distance));

        getSpellSlot().get(SpellPredicate.IS_DISGUISE).ifPresent(spell -> {
            spell.getDisguise().onImpact(this, fallDistance, damageMultiplier, cause);
        });
        return fallDistance;
    }

    protected float getEffectiveFallDistance(float distance) {
        return distance;
    }

    @Override
    public float getCloudWalkingStrength() {
        return asWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(net.minecraft.enchantment.Enchantments.FEATHER_FALLING).map(featherFalling -> {
            int maxLevel = featherFalling.value().getMaxLevel();
            int level = EnchantmentHelper.getEquipmentLevel(featherFalling, entity);
            return MathHelper.clamp(level / (float)maxLevel, 0, 1);
        }).orElse(0F);
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        enchants.toNBT(compound, lookup);
        spells.getSlots().toNBT(compound, lookup);
        getCarrierId().ifPresent(id -> compound.putUuid("carrier", id));
        toSyncronisedNbt(compound, lookup);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        enchants.fromNBT(compound, lookup);
        spells.getSlots().fromNBT(compound, lookup);
        setCarrier(compound.containsUuid("carrier") ? compound.getUuid("carrier") : null);
        fromSynchronizedNbt(compound, lookup);
    }

    public void toSyncronisedNbt(NbtCompound compound, WrapperLookup lookup) {
        compound.put("armour", armour.toNBT(lookup));
    }

    public void fromSynchronizedNbt(NbtCompound compound, WrapperLookup lookup) {
        armour.fromNBT(compound.getCompound("armour"), lookup);
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
