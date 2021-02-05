package com.minelittlepony.unicopia.entity.player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WorldTribeManager;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.ability.magic.spell.ShieldSpell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.entity.PonyContainer;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Trap;
import com.minelittlepony.unicopia.item.toxin.FoodType;
import com.minelittlepony.unicopia.item.toxin.Toxicity;
import com.minelittlepony.unicopia.item.toxin.Toxin;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.network.MsgOtherPlayerCapabilities;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import com.minelittlepony.unicopia.network.Transmittable;
import com.minelittlepony.unicopia.util.Copieable;
import com.minelittlepony.unicopia.util.MagicalDamageSource;
import com.minelittlepony.common.util.animation.LinearInterpolator;
import com.google.common.collect.Lists;
import com.minelittlepony.common.util.animation.Interpolator;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Pony implements Caster<PlayerEntity>, Equine<PlayerEntity>, Transmittable, Copieable<Pony> {

    private static final TrackedData<Integer> RACE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    static final TrackedData<Float> ENERGY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> EXERTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> MANA = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> XP = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);
    private static final TrackedData<CompoundTag> HELD_EFFECT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    private final AbilityDispatcher powers = new AbilityDispatcher(this);
    private final PlayerPhysics gravity = new PlayerPhysics(this);
    private final PlayerAttributes attributes = new PlayerAttributes(this);
    private final PlayerCamera camera = new PlayerCamera(this);
    private final ManaContainer mana;
    private final PlayerLevelStore levels;

    private final List<Tickable> tickers;

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);

    private final Interpolator interpolator = new LinearInterpolator();

    private final PlayerEntity entity;

    private boolean dirty;
    private boolean speciesSet;
    private boolean speciesPersisted;
    private boolean prevSneaking;
    private boolean prevLanded;

    private int ticksHanging;

    private float magicExhaustion = 0;

    @Nullable
    private Race clientPreferredRace;

    private boolean invisible = false;

    @Nullable
    private Runnable landEvent;

    public Pony(PlayerEntity player) {
        this.entity = player;
        this.mana = new ManaContainer(this);
        this.levels = new PlayerLevelStore(this);
        this.tickers = Lists.newArrayList(gravity, mana, attributes);

        player.getDataTracker().startTracking(RACE, Race.HUMAN.ordinal());
        player.getDataTracker().startTracking(EFFECT, new CompoundTag());
        player.getDataTracker().startTracking(HELD_EFFECT, new CompoundTag());
    }

    public static void registerAttributes(DefaultAttributeContainer.Builder builder) {
        builder.add(PlayerAttributes.EXTENDED_REACH_DISTANCE);
        builder.add(PlayerAttributes.EXTRA_MINING_SPEED);
        builder.add(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);
    }

    @Override
    public Race getSpecies() {
        if (getMaster() == null) {
            return Race.HUMAN;
        }

        return Race.fromId(getMaster().getDataTracker().get(RACE));
    }

    public boolean sneakingChanged() {
        return entity.isSneaking() != prevSneaking;
    }

    public boolean landedChanged() {
        return entity.isOnGround() != prevLanded;
    }

    @Override
    public void setSpecies(Race race) {
        race = race.validate(entity);
        speciesSet = true;

        entity.getDataTracker().set(RACE, race.ordinal());

        gravity.updateFlightStat(entity.abilities.flying);
        entity.sendAbilitiesUpdate();
    }

    public MagicReserves getMagicalReserves() {
        return mana;
    }

    @Override
    public LevelStore getLevel() {
        return levels;
    }

    @Override
    public boolean isInvisible() {
        return invisible && hasSpell();
    }

    public boolean isSpeciesPersisted() {
        return speciesPersisted;
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    @Override
    public Affinity getAffinity() {
        return getSpecies().getAffinity();
    }

    public void setDirty() {
        dirty = true;
    }

    @Override
    public void sendCapabilities(boolean full) {
        dirty = false;

        if (entity instanceof ServerPlayerEntity) {
            MsgOtherPlayerCapabilities packet = new MsgOtherPlayerCapabilities(full, this);
            Channel.SERVER_PLAYER_CAPABILITIES.send(entity, packet);
            Channel.SERVER_OTHER_PLAYER_CAPABILITIES.send(entity.world, packet);
        }
    }

    public AbilityDispatcher getAbilities() {
        return powers;
    }

    @Override
    public Physics getPhysics() {
        return gravity;
    }

    public float getExtendedReach() {
        return (float)entity.getAttributeInstance(PlayerAttributes.EXTENDED_REACH_DISTANCE).getValue();
    }

    public float getBlockBreakingSpeed() {
        return (float)entity.getAttributeInstance(PlayerAttributes.EXTRA_MINING_SPEED).getValue();
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
    public boolean beforeUpdate() {

        if (!speciesSet && getWorld() instanceof ServerWorld) {
            setSpecies(WorldTribeManager.forWorld((ServerWorld)getWorld()).getDefaultRace());
            setDirty();
        }

        if (isClientPlayer() && !speciesSet) {
            Race race = UnicopiaClient.getPreferredRace();

            if (race != clientPreferredRace) {
                clientPreferredRace = race;

                if (race != getSpecies()) {
                    Channel.CLIENT_REQUEST_CAPABILITIES.send(new MsgRequestCapabilities(race));
                }
            }
        }

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

                    if (ridee instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)ridee).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(ridee));
                    }
                }
            }
        }

        magicExhaustion = burnFood(magicExhaustion);

        powers.tick();

        return false;
    }

    public boolean isHanging() {
        return entity.getAttributeInstance(PlayerAttributes.ENTITY_GRAVTY_MODIFIER).hasModifier(PlayerAttributes.BAT_HANGING);
    }

    public boolean canHangAt(BlockPos pos) {
        BlockState state = getWorld().getBlockState(pos);

        return state.hasSolidTopSurface(getWorld(), pos, getEntity(), Direction.DOWN);
    }

    private BlockPos getHangingPos() {
        BlockPos pos = getOrigin();
        return new BlockPos(pos.getX(), pos.getY() + entity.getEyeHeight(entity.getPose()) + 2, pos.getZ());
    }

    @Override
    public void tick() {

        if (isHanging()) {
            if (ticksHanging++ > 40) {
                if (Entity.squaredHorizontalLength(entity.getVelocity()) > 0.01
                        || entity.isSneaking()
                        || !canHangAt(getHangingPos())) {


                    entity.getAttributes().getCustomInstance(PlayerAttributes.ENTITY_GRAVTY_MODIFIER).removeModifier(PlayerAttributes.BAT_HANGING);
                    entity.calculateDimensions();
                }
            }
        } else {
            ticksHanging = 0;
        }

        if (hasSpell()) {
            Attached effect = getSpell(Attached.class, true);

            if (effect != null) {
                if (entity.getEntityWorld().isClient()) {
                    effect.renderOnPerson(this);
                }

                if (!effect.updateOnPerson(this)) {
                    setSpell(null);
                }
            }
        }

        tickers.forEach(Tickable::tick);

        if (dirty) {
            sendCapabilities(true);
        }

        prevSneaking = entity.isSneaking();
        prevLanded = entity.isOnGround();

        if (gravity.isGravityNegative() && entity.getY() > entity.world.getHeight() + 64) {
           entity.damage(DamageSource.OUT_OF_WORLD, 4.0F);
        }
    }

    public void waitForFall(Runnable action) {
        landEvent = action;
    }

    public Optional<Float> onImpact(float distance, float damageMultiplier) {

        float g = gravity.getGravityModifier();

        boolean extraProtection = getSpell(ShieldSpell.class, false) != null;

        if (g != 1 || extraProtection || getSpecies().canFly() && !entity.isCreative() && !entity.isSpectator()) {

            if (extraProtection) {
                distance /= (getLevel().get() + 1);
                if (entity.isSneaking()) {
                    distance /= 2;
                }
            }

            distance = Math.max(0, (distance * g) - 5);

            handleFall(distance, damageMultiplier);
            return Optional.of(distance);
        }

        handleFall(distance, damageMultiplier);
        return Optional.empty();
    }

    private void handleFall(float distance, float damageMultiplier) {
        if (landEvent != null) {
            landEvent.run();
            landEvent = null;
        }
        getSpellOrEmpty(DisguiseSpell.class, false).ifPresent(spell -> {
            spell.getDisguise().onImpact(this, distance, damageMultiplier);
        });
    }

    @Override
    public void onJump() {
        if (gravity.isGravityNegative()) {
            entity.setVelocity(entity.getVelocity().multiply(1, -1, 1));
        }
    }

    @Override
    public boolean onProjectileImpact(ProjectileEntity projectile) {
        if (hasSpell()) {
            Spell effect = getSpell(true);
            if (!effect.isDead() && effect.handleProjectileImpact(projectile)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean subtractEnergyCost(double foodSubtract) {
        if (!entity.isCreative() && !entity.world.isClient) {

            float currentMana = mana.getMana().get();
            float foodManaRatio = 10;

            if (currentMana >= foodSubtract * foodManaRatio) {
                mana.getMana().set(currentMana - (float)foodSubtract * foodManaRatio);
            } else {
                mana.getMana().set(0);
                foodSubtract -= currentMana / foodManaRatio;

                magicExhaustion += foodSubtract;
            }
        }

        return entity.getHealth() > 0;
    }

    private float burnFood(float foodSubtract) {
        int lostLevels = (int)Math.floor(foodSubtract);
        if (lostLevels > 0) {
            int food = entity.getHungerManager().getFoodLevel() - lostLevels;

            if (food < 0) {
                entity.getHungerManager().add(-entity.getHungerManager().getFoodLevel(), 0);
                entity.damage(MagicalDamageSource.EXHAUSTION, -food/2);
            } else {
                entity.getHungerManager().add(-lostLevels, 0);
            }
        }

        return foodSubtract - lostLevels;
    }

    public Optional<Text> trySleep(BlockPos pos) {
        return findAllSpellsInRange(10)
                .filter(p -> p instanceof Pony && ((Pony)p).isEnemy(this))
                .findFirst()
                .map(p -> new TranslatableText("block.unicopia.bed.not_safe"));
    }

    public void onEat(ItemStack stack) {
        if (getSpecies() == Race.CHANGELING) {
            Toxin.POISON.afflict(getMaster(), FoodType.RAW_MEAT, Toxicity.SAFE, stack);
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putString("playerSpecies", getSpecies().name());

        compound.putFloat("magicExhaustion", magicExhaustion);

        compound.put("powers", powers.toNBT());
        compound.put("gravity", gravity.toNBT());

        Spell effect = getSpell(true);

        if (effect != null) {
            compound.put("effect", SpellRegistry.toNBT(effect));
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        speciesPersisted = true;
        setSpecies(Race.fromName(compound.getString("playerSpecies")));

        powers.fromNBT(compound.getCompound("powers"));
        gravity.fromNBT(compound.getCompound("gravity"));

        magicExhaustion = compound.getFloat("magicExhaustion");

        if (compound.contains("effect")) {
            effectDelegate.set(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }
    }

    @Override
    public void copyFrom(Pony oldPlayer) {
        speciesPersisted = oldPlayer.speciesPersisted;
        if (!oldPlayer.getEntity().removed) {
            setSpell(oldPlayer.getSpell(true));
        }
        setSpecies(oldPlayer.getSpecies());
        setDirty();
    }

    @Override
    public EffectSync getPrimarySpellSlot() {
        return effectDelegate;
    }

    @Override
    public void setSpell(@Nullable Spell effect) {
        Caster.super.setSpell(effect);
        setDirty();
    }

    @Override
    public void setMaster(PlayerEntity owner) {
    }

    @Override
    public PlayerEntity getMaster() {
        return entity;
    }

    public boolean isClientPlayer() {
        return InteractionManager.instance().isClientPlayer(getMaster());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Pony of(@Nullable PlayerEntity player) {
        return player == null ? null : ((PonyContainer<Pony>)player).get();
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
