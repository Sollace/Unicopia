package com.minelittlepony.unicopia.entity.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.EarthPonyStompAbility;
import com.minelittlepony.unicopia.ability.magic.*;
import com.minelittlepony.unicopia.ability.magic.spell.AbstractDisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitDiscovery;
import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.*;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.LivingEntityDuck;
import com.minelittlepony.unicopia.entity.effect.SunBlindnessStatusEffect;
import com.minelittlepony.unicopia.entity.effect.UEffects;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgOtherPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgPlayerAnimationChange;
import com.minelittlepony.unicopia.util.*;
import com.minelittlepony.unicopia.network.datasync.EffectSync.UpdateCallback;
import com.minelittlepony.common.util.animation.LinearInterpolator;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.minelittlepony.common.util.animation.Interpolator;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;

public class Pony extends Living<PlayerEntity> implements Copyable<Pony>, UpdateCallback {

    private static final TrackedData<String> RACE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.STRING);

    static final TrackedData<Float> ENERGY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> EXHAUSTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> EXERTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> MANA = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> XP = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Integer> LEVEL = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    static final TrackedData<Integer> CORRUPTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private static final TrackedData<NbtCompound> EFFECT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    private final AbilityDispatcher powers = new AbilityDispatcher(this);
    private final PlayerPhysics gravity = new PlayerPhysics(this);
    private final PlayerCharmTracker charms = new PlayerCharmTracker(this);
    private final PlayerAttributes attributes = new PlayerAttributes(this);
    private final PlayerCamera camera = new PlayerCamera(this);
    private final TraitDiscovery discoveries = new TraitDiscovery(this);

    private final Map<String, Integer> advancementProgress = new HashMap<>();

    private final ManaContainer mana;
    private final PlayerLevelStore levels;
    private final PlayerLevelStore corruption;

    private final List<Tickable> tickers;

    private final Interpolator interpolator = new LinearInterpolator();

    private boolean dirty;
    private boolean speciesPersisted;

    private Optional<BlockPos> hangingPosition = Optional.empty();
    private int ticksHanging;

    private float magicExhaustion = 0;

    @Nullable
    private Race clientPreferredRace;

    private boolean invisible = false;

    private int ticksInSun;
    private boolean hasShades;
    private int ticksSunImmunity = 20;

    private Animation animation = Animation.NONE;
    private int animationMaxDuration;
    private int animationDuration;

    public Pony(PlayerEntity player) {
        super(player, EFFECT);
        this.mana = new ManaContainer(this);
        this.levels = new PlayerLevelStore(this, LEVEL, true, SoundEvents.ENTITY_PLAYER_LEVELUP);
        this.corruption = new PlayerLevelStore(this, CORRUPTION, false, SoundEvents.PARTICLE_SOUL_ESCAPE);
        this.tickers = Lists.newArrayList(gravity, mana, attributes);

        player.getDataTracker().startTracking(RACE, Race.DEFAULT_ID);
    }

    public static void registerAttributes(DefaultAttributeContainer.Builder builder) {
        builder.add(UEntityAttributes.EXTRA_MINING_SPEED);
        builder.add(UEntityAttributes.ENTITY_GRAVTY_MODIFIER);
    }

    public void setAnimation(Animation animation) {
        setAnimation(animation, animation.getDuration());
    }

    public void setAnimation(Animation animation, int duration) {
        if (animation != this.animation && duration != animationDuration) {
            this.animation = animation;
            this.animationDuration = animation == Animation.NONE ? 0 : Math.max(0, duration);
            this.animationMaxDuration = animationDuration;

            if (!isClient()) {
                Channel.SERVER_PLAYER_ANIMATION_CHANGE.send(asWorld(), new MsgPlayerAnimationChange(this, animation, animationDuration));
            }

            animation.getSound().ifPresent(sound -> {
                playSound(sound, sound == USounds.ENTITY_PLAYER_WOLOLO ? 0.1F : 0.9F, 1);
            });
        }
    }

    public Animation getAnimation() {
        return animation;
    }

    public float getAnimationProgress(float delta) {
        if (animation == Animation.NONE) {
            return 0;
        }
        return 1 - (((float)animationDuration) / animationMaxDuration);
    }

    public Map<String, Integer> getAdvancementProgress() {
        return advancementProgress;
    }

    @Override
    public Race getSpecies() {
        if (AmuletSelectors.ALICORN_AMULET.test(entity)) {
            return Race.ALICORN;
        }

        return getObservedSpecies();
    }

    public Race getObservedSpecies() {
        return getSpellSlot()
                .get(SpellPredicate.IS_MIMIC, true)
                .map(AbstractDisguiseSpell::getDisguise)
                .map(EntityAppearance::getAppearance)
                .flatMap(Pony::of)
                .map(Pony::getActualSpecies)
                .orElse(getActualSpecies());
    }

    public Race.Composite getCompositeRace() {
        Race observed = getObservedSpecies();
        return new Race.Composite(observed, AmuletSelectors.ALICORN_AMULET.test(entity) ? Race.ALICORN : observed);
    }

    public Race getActualSpecies() {
        return Race.fromName(entity.getDataTracker().get(RACE), Race.HUMAN);
    }

    @Override
    public void setSpecies(Race race) {
        race = race.validate(entity);
        ticksInSun = 0;
        entity.getDataTracker().set(RACE, Race.REGISTRY.getId(race).toString());

        gravity.updateFlightState();
        entity.sendAbilitiesUpdate();

        UCriteria.PLAYER_CHANGE_RACE.trigger(entity);
    }

    public TraitDiscovery getDiscoveries() {
        return discoveries;
    }

    public MagicReserves getMagicalReserves() {
        return mana;
    }

    public PlayerCharmTracker getCharms() {
        return charms;
    }

    @Override
    public LevelStore getLevel() {
        return levels;
    }

    @Override
    public LevelStore getCorruption() {
        return corruption;
    }

    @Override
    public boolean isInvisible() {
        return invisible && SpellPredicate.IS_DISGUISE.isOn(this);
    }

    public boolean isSpeciesPersisted() {
        return speciesPersisted;
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public boolean isSunImmune() {
        return ticksSunImmunity > 0;
    }

    @Override
    public Affinity getAffinity() {
        return getSpecies().getAffinity();
    }

    @Override
    public void setDirty() {
        dirty = true;
    }

    private void sendCapabilities() {
        if (!dirty) {
            return;
        }
        dirty = false;

        if (entity instanceof ServerPlayerEntity) {
            MsgOtherPlayerCapabilities packet = new MsgOtherPlayerCapabilities(this);
            Channel.SERVER_PLAYER_CAPABILITIES.send((ServerPlayerEntity)entity, packet);
            Channel.SERVER_OTHER_PLAYER_CAPABILITIES.send(entity.world, packet);
        }
    }

    public AbilityDispatcher getAbilities() {
        return powers;
    }

    @Override
    public PlayerPhysics getPhysics() {
        return gravity;
    }

    public float getBlockBreakingSpeed() {
        return (float)entity.getAttributeInstance(UEntityAttributes.EXTRA_MINING_SPEED).getValue();
    }

    public Motion getMotion() {
        return gravity;
    }

    public PlayerCamera getCamera() {
        return camera;
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    @Override
    public final LivingEntity getMaster() {
        return asEntity();
    }

    public void onSpawn() {
        if (entity.world instanceof ServerWorld sw
                && getObservedSpecies() == Race.BAT
                && sw.getServer().getSaveProperties().getGameMode() != GameMode.ADVENTURE
                && SunBlindnessStatusEffect.isPositionExposedToSun(sw, getOrigin())) {
            SpawnLocator.selectSpawnPosition(sw, entity);
        }
    }

    @Override
    public boolean beforeUpdate() {
        if (isClient()) {
            if (entity.hasVehicle() && entity.isSneaking()) {

                Entity ridee = entity.getVehicle();

                if (ridee instanceof Trap) {
                    if (((Trap)ridee).attemptDismount(entity)) {
                        entity.stopRiding();
                    } else {
                        entity.setSneaking(false);
                    }
                } else {
                    entity.stopRiding();
                    Living.transmitPassengers(ridee);
                }
            }
        }

        magicExhaustion = ManaConsumptionUtil.burnFood(entity, magicExhaustion);

        powers.tick();

        return false;
    }

    public boolean isHanging() {
        return entity.getAttributeInstance(UEntityAttributes.ENTITY_GRAVTY_MODIFIER).hasModifier(PlayerAttributes.BAT_HANGING);
    }

    public void stopHanging() {
        entity.getAttributeInstance(UEntityAttributes.ENTITY_GRAVTY_MODIFIER).removeModifier(PlayerAttributes.BAT_HANGING);
        entity.calculateDimensions();
        ticksHanging = 0;
        hangingPosition = Optional.empty();
    }

    public void startHanging(BlockPos pos) {
        hangingPosition = Optional.of(pos);
        EntityAttributeInstance attr = entity.getAttributeInstance(UEntityAttributes.ENTITY_GRAVTY_MODIFIER);

        if (!attr.hasModifier(PlayerAttributes.BAT_HANGING)) {
            attr.addPersistentModifier(PlayerAttributes.BAT_HANGING);
        }
        entity.teleport(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5);
        entity.setVelocity(Vec3d.ZERO);
        entity.setSneaking(false);
        entity.stopFallFlying();
        getPhysics().cancelFlight(true);

        setDirty();
    }

    public boolean canHangAt(BlockPos pos) {
        if (!asWorld().isAir(pos) || !asWorld().isAir(pos.down())) {
            return false;
        }

        pos = pos.up();
        BlockState state = asWorld().getBlockState(pos);

        return state.isSolidSurface(asWorld(), pos, entity, Direction.DOWN);
    }

    @Override
    public void tick() {
        if (ticksSunImmunity > 0) {
            ticksSunImmunity--;
        }

        if (animationDuration >= 0 && --animationDuration <= 0) {
            setAnimation(Animation.NONE);
        }

        if (isHanging()) {
            ((LivingEntityDuck)entity).setLeaningPitch(0);
            if (!isClient() && (getObservedSpecies() != Race.BAT || (ticksHanging++ > 2 && hangingPosition.filter(getOrigin().down()::equals).filter(this::canHangAt).isEmpty()))) {
                stopHanging();
            }
        } else {
            ticksHanging = 0;
        }

        if (getObservedSpecies() == Race.BAT && !entity.hasPortalCooldown()) {
            if (SunBlindnessStatusEffect.hasSunExposure(entity)) {
                if (ticksInSun < 200) {
                    ticksInSun++;
                }

                if (ticksInSun == 1) {
                    if (!isClient()) {
                        entity.addStatusEffect(new StatusEffectInstance(UEffects.SUN_BLINDNESS, SunBlindnessStatusEffect.MAX_DURATION, 1, true, false));
                        UCriteria.LOOK_INTO_SUN.trigger(entity);
                    } else if (isClientPlayer()) {
                        InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_EARS_RINGING, entity.getId());
                    }
                }
            } else if (ticksInSun > 0) {
                ticksInSun--;
            }

            boolean hasShades = entity.getEquippedStack(EquipmentSlot.HEAD).isIn(UTags.SHADES);
            if (!this.hasShades && hasShades) {
                UCriteria.WEAR_SHADES.trigger(entity);
            }
            this.hasShades = hasShades;
        }

        if (!isClient() && !UItems.ALICORN_AMULET.isApplicable(entity)) {
            if (entity.age % (10 * ItemTracker.SECONDS) == 0) {
                if (entity.world.random.nextInt(100) == 0) {
                    corruption.add(-1);
                    setDirty();
                }

                if (entity.getHealth() >= entity.getMaxHealth() - 1 && !entity.getHungerManager().isNotFull()) {
                    corruption.add(-entity.world.random.nextInt(4));
                    setDirty();
                }
            }

            if (entity.hurtTime == 1) {
                corruption.add(1);
                setDirty();
            }
        }

        tickers.forEach(Tickable::tick);

        super.tick();

        sendCapabilities();
    }

    public Optional<Float> modifyDamage(DamageSource cause, float amount) {

        if (!cause.isUnblockable() && !cause.isMagic() && !cause.isFire() && !cause.isOutOfWorld()
                && !(cause instanceof EntityDamageSource && ((EntityDamageSource)cause).isThorns())
                && cause != DamageSource.FREEZE) {

            if (getSpecies().canUseEarth() && entity.isSneaking()) {
                amount /= (cause.isProjectile() ? 3 : 2) * (entity.getHealth() < 5 ? 3 : 1);

                return Optional.of(amount);
            }
        }
        return Optional.empty();
    }

    public Optional<Float> onImpact(float distance, float damageMultiplier, DamageSource cause) {

        float originalDistance = distance;

        distance *= gravity.getGravityModifier();

        boolean extraProtection = getSpellSlot().get(SpellType.SHIELD, false).isPresent();

        if (!entity.isCreative() && !entity.isSpectator()) {

            if (extraProtection) {
                distance /= (getLevel().getScaled(3) + 1);
                if (entity.isSneaking()) {
                    distance /= 2;
                }
            }

            if (getSpecies().canFly() || (getSpecies().canUseEarth() && entity.isSneaking())) {
                distance -= 5;
            }
            distance = Math.max(0, distance);
        }

        handleFall(distance, damageMultiplier, cause);
        if (distance != originalDistance) {
            return Optional.of(distance);
        }
        return Optional.empty();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void handleFall(float distance, float damageMultiplier, DamageSource cause) {
        super.handleFall(distance, damageMultiplier, cause);

        if (getSpecies().canUseEarth() && entity.isSneaking()) {
            double radius = distance / 10;
            if (radius > 0) {
                EarthPonyStompAbility.spawnEffectAround(entity, entity.getLandingPos(), radius, radius);
            }
        }
    }

    @Override
    public boolean subtractEnergyCost(double foodSubtract) {

        List<Pony> partyMembers = FriendshipBraceletItem.getPartyMembers(this, 10).toList();

        if (!partyMembers.isEmpty()) {
            foodSubtract /= (partyMembers.size() + 1);
            for (Pony member : partyMembers) {
                member.directTakeEnergy(foodSubtract);
            }
        }

        directTakeEnergy(foodSubtract);

        return entity.getHealth() > 0;
    }

    protected void directTakeEnergy(double foodSubtract) {
        if (!entity.isCreative() && !entity.world.isClient) {
            magicExhaustion += ManaConsumptionUtil.consumeMana(mana.getMana(), foodSubtract);
        }
    }

    @Override
    protected Stream<ItemStack> getInventoryStacks() {
        return Streams.concat(
                super.getInventoryStacks(),
                entity.getInventory().main.stream()
        );
    }

    @Override
    protected void giveBackItem(ItemStack stack) {
        if (!entity.giveItemStack(stack)) {
            entity.dropItem(stack, false);
        }
    }

    public Optional<Text> trySleep(BlockPos pos) {

        if (AmuletSelectors.ALICORN_AMULET.test(entity)) {
            return Optional.of(Text.translatable("block.unicopia.bed.not_tired"));
        }

        return findAllSpellsInRange(10)
                .filter(p -> p instanceof Pony && ((Pony)p).isEnemy(this))
                .findFirst()
                .map(p -> Text.translatable("block.unicopia.bed.not_safe"));
    }

    @Override
    public boolean isEnemy(Affine other) {
        return getArmour().contains(UItems.ALICORN_AMULET) || super.isEnemy(other);
    }

    @Override
    public void toSyncronisedNbt(NbtCompound compound) {
        super.toSyncronisedNbt(compound);
        compound.putString("playerSpecies", Race.REGISTRY.getId(getActualSpecies()).toString());
        compound.putFloat("magicExhaustion", magicExhaustion);
        compound.putInt("ticksHanging", ticksHanging);
        BLOCK_POS.writeOptional("hangingPosition", compound, hangingPosition);
        compound.putInt("ticksInSun", ticksInSun);
        compound.putBoolean("hasShades", hasShades);
        compound.put("powers", powers.toNBT());
        compound.put("gravity", gravity.toNBT());
        compound.put("charms", charms.toNBT());
        compound.put("discoveries", discoveries.toNBT());
        compound.put("mana", mana.toNBT());
        compound.putInt("levels", levels.get());
        compound.putInt("corruption", corruption.get());

        NbtCompound progress = new NbtCompound();
        advancementProgress.forEach((key, count) -> {
            progress.putInt(key, count);
        });
        compound.put("advancementProgress", progress);
    }

    @Override
    public void fromSynchronizedNbt(NbtCompound compound) {
        super.fromSynchronizedNbt(compound);
        speciesPersisted = true;
        setSpecies(Race.fromName(compound.getString("playerSpecies"), Race.HUMAN));
        powers.fromNBT(compound.getCompound("powers"));
        gravity.fromNBT(compound.getCompound("gravity"));
        charms.fromNBT(compound.getCompound("charms"));
        discoveries.fromNBT(compound.getCompound("discoveries"));
        levels.set(compound.getInt("levels"));
        corruption.set(compound.getInt("corruption"));
        mana.fromNBT(compound.getCompound("mana"));

        magicExhaustion = compound.getFloat("magicExhaustion");
        ticksHanging = compound.getInt("ticksHanging");
        hangingPosition = NbtSerialisable.BLOCK_POS.readOptional("hangingPosition", compound);
        ticksInSun = compound.getInt("ticksInSun");
        hasShades = compound.getBoolean("hasShades");

        NbtCompound progress = compound.getCompound("advancementProgress");
        advancementProgress.clear();
        for (String key : progress.getKeys()) {
            advancementProgress.put(key, progress.getInt(key));
        }
    }

    @Override
    public void copyFrom(Pony oldPlayer) {
        speciesPersisted = true;
        if (!oldPlayer.asEntity().isRemoved()) {
            oldPlayer.getSpellSlot().stream(true).forEach(getSpellSlot()::put);
        } else {
            oldPlayer.getSpellSlot().stream(true).filter(SpellPredicate.IS_PLACED).forEach(getSpellSlot()::put);
        }
        oldPlayer.getSpellSlot().put(null);
        getArmour().copyFrom(oldPlayer.getArmour());
        setSpecies(oldPlayer.getActualSpecies());
        getDiscoveries().copyFrom(oldPlayer.getDiscoveries());
        getCharms().equipSpell(Hand.MAIN_HAND, oldPlayer.getCharms().getEquippedSpell(Hand.MAIN_HAND));
        getCharms().equipSpell(Hand.OFF_HAND, oldPlayer.getCharms().getEquippedSpell(Hand.OFF_HAND));
        corruption.set(oldPlayer.getCorruption().get());
        levels.set(oldPlayer.getLevel().get());
        mana.getXp().set(oldPlayer.getMagicalReserves().getXp().get());
        advancementProgress.putAll(oldPlayer.getAdvancementProgress());
        setDirty();
        onSpawn();
    }

    @Override
    public void onSpellSet(@Nullable Spell spell) {
        if (spell != null) {
            if (spell.getAffinity() == Affinity.BAD && entity.getWorld().random.nextInt(120) == 0) {
                getCorruption().add(1);
            }
            getCorruption().add((int)spell.getTraits().getCorruption());
            setDirty();
        }
    }

    public boolean isClientPlayer() {
        return InteractionManager.instance().isClientPlayer(asEntity());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Pony of(@Nullable PlayerEntity player) {
        return player == null ? null : ((PonyContainer<Pony>)player).get();
    }

    public static Stream<Pony> stream(Stream<Entity> entities) {
        return entities.flatMap(entity -> of(entity).stream());
    }

    public static Optional<Pony> of(Entity entity) {
        return entity instanceof PlayerEntity
                ? PonyContainer.of(entity).map(a -> (Pony)a.get())
                : Optional.empty();
    }

    public static boolean equal(GameProfile one, GameProfile two) {
        return one == two || (one != null && two != null && one.getId().equals(two.getId()));
    }

    public static boolean equal(Entity one, Entity two) {
        return Objects.equals(one, two) || (one instanceof PlayerEntity && two instanceof PlayerEntity && equal((PlayerEntity)one, (PlayerEntity)two));
    }

    public static boolean equal(PlayerEntity one, PlayerEntity two) {
        return one == two || (one != null && two != null && equal(one.getGameProfile(), two.getGameProfile()));
    }
}
