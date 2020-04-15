package com.minelittlepony.unicopia.entity.player;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UEffects;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.UnicopiaCore;
import com.minelittlepony.unicopia.ability.AbilityReceiver;
import com.minelittlepony.unicopia.enchanting.PageOwner;
import com.minelittlepony.unicopia.entity.FlightControl;
import com.minelittlepony.unicopia.entity.Trap;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.AttachedMagicEffect;
import com.minelittlepony.unicopia.magic.HeldMagicEffect;
import com.minelittlepony.unicopia.magic.MagicEffect;
import com.minelittlepony.unicopia.magic.MagicalItem;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.util.BasicEasingInterpolator;
import com.minelittlepony.util.IInterpolator;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.network.packet.EntityPassengersSetS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity.SleepFailureReason;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;

public class PlayerImpl implements Pony {

    private static final TrackedData<Integer> PLAYER_RACE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> ENERGY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> EXERTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);
    private static final TrackedData<CompoundTag> HELD_EFFECT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    private final PlayerPageStats pageStates = new PlayerPageStats();

    private final AbilityDelegate powers = new AbilityDelegate(this);

    private final GravityDelegate gravity = new GravityDelegate(this);

    private final PlayerAttributes attributes = new PlayerAttributes();

    private final PlayerCamera view = new PlayerCamera(this);

    private final PlayerInventory inventory = new PlayerInventory(this);

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);
    private final EffectSync heldEffectDelegate = new EffectSync(this, HELD_EFFECT);

    private final IInterpolator interpolator = new BasicEasingInterpolator();

    private float nextStepDistance = 1;

    private final PlayerEntity entity;

    private boolean dirty = false;

    private boolean invisible = false;

    public PlayerImpl(PlayerEntity player) {
        this.entity = player;

        player.getDataTracker().startTracking(PLAYER_RACE, Race.EARTH.ordinal());
        player.getDataTracker().startTracking(EXERTION, 0F);
        player.getDataTracker().startTracking(ENERGY, 0F);
        player.getDataTracker().startTracking(EFFECT, new CompoundTag());
        player.getDataTracker().startTracking(HELD_EFFECT, new CompoundTag());
    }

    @Override
    public Race getSpecies() {
        if (getOwner() == null) {
            return Race.HUMAN;
        }

        return Race.fromId(getOwner().getDataTracker().get(PLAYER_RACE));
    }

    @Override
    public void setSpecies(Race race) {
        race = race.validate(entity);

        entity.getDataTracker().set(PLAYER_RACE, race.ordinal());

        entity.abilities.allowFlying = race.canFly();
        gravity.updateFlightStat(entity, entity.abilities.flying);
        entity.sendAbilitiesUpdate();

        sendCapabilities(false);
    }

    @Override
    public float getExertion() {
        return getOwner().getDataTracker().get(EXERTION);
    }

    @Override
    public void setExertion(float exertion) {
        entity.getDataTracker().set(EXERTION, Math.max(0, exertion));
    }

    @Override
    public float getEnergy() {
        return entity.getDataTracker().get(ENERGY);
    }

    @Override
    public void setEnergy(float energy) {
        entity.getDataTracker().set(ENERGY, Math.max(0, energy));
    }

    @Override
    public boolean isInvisible() {
        return invisible && hasEffect();
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    @Nullable
    @Override
    public HeldMagicEffect getHeldEffect(ItemStack stack) {

        if (!getSpecies().canCast()) {
            heldEffectDelegate.set(null);

            return null;
        }

        HeldMagicEffect heldEffect = heldEffectDelegate.get(HeldMagicEffect.class, true);

        if (heldEffect == null || !heldEffect.getName().equals(SpellRegistry.getKeyFromStack(stack))) {
            heldEffect = SpellRegistry.instance().getHeldFrom(stack);
            heldEffectDelegate.set(heldEffect);
        }

        return heldEffect;
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public void sendCapabilities(boolean full) {
        dirty = false;

        if (!getWorld().isClient()) {
            if (full) {
                UnicopiaCore.getConnection().broadcast(new MsgPlayerCapabilities(this));
            } else {
                UnicopiaCore.getConnection().broadcast(new MsgPlayerCapabilities(getSpecies(), getOwner()));
            }
        }
    }

    @Override
    public void onDimensionalTravel(int destinationDimension) {
        if (!getWorld().isClient()) {
            dirty = true;
        }
    }

    @Override
    public AbilityReceiver getAbilities() {
        return powers;
    }

    @Override
    public PageOwner getPages() {
        return pageStates;
    }

    @Override
    public GravityDelegate getGravity() {
        return gravity;
    }

    @Override
    public FlightControl getFlight() {
        return gravity;
    }

    @Override
    public PlayerCamera getCamera() {
        return view;
    }

    @Override
    public IInterpolator getInterpolator() {
        return interpolator;
    }

    @Override
    public boolean beforeUpdate() {
        if (entity.world.isClient()) {
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

        powers.onUpdate();
        inventory.onUpdate();

        return false;
    }

    @Override
    public void onUpdate() {
        gravity.onUpdate();

        if (hasEffect()) {
            AttachedMagicEffect effect = getEffect(AttachedMagicEffect.class, true);

            if (effect != null) {
                if (entity.getEntityWorld().isClient()) {
                    effect.renderOnPerson(this);
                }

                if (!effect.updateOnPerson(this)) {
                    setEffect(null);
                }
            }
        }

        ItemStack stack = entity.getStackInHand(Hand.MAIN_HAND);

        HeldMagicEffect effect = getHeldEffect(stack);

        if (effect != null) {
            Affinity affinity = stack.getItem() instanceof MagicalItem ? ((MagicalItem)stack.getItem()).getAffinity(stack) : Affinity.NEUTRAL;

            effect.updateInHand(this, affinity);
        }

        addExertion(-1);
        addEnergy(-1);

        attributes.applyAttributes(entity, getSpecies());

        if (dirty) {
            sendCapabilities(true);
        }
    }

    @Override
    public float onImpact(float distance) {
        if (getSpecies().canFly()) {
            distance = Math.max(0, distance - 5);
        }
        return distance;
    }

    @Override
    public void onJump() {
        if (gravity.getGravitationConstant() < 0) {
            Vec3d velocity = entity.getVelocity();
            entity.setVelocity(velocity.x, velocity.y * -1,  velocity.z);
        }
    }

    @Override
    public boolean onProjectileImpact(ProjectileEntity projectile) {
        if (hasEffect()) {
            MagicEffect effect = getEffect();
            if (!effect.isDead() && effect.handleProjectileImpact(projectile)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean subtractEnergyCost(double foodSubtract) {
        if (!entity.abilities.creativeMode) {
            int food = (int)(entity.getHungerManager().getFoodLevel() - foodSubtract);

            if (food < 0) {
                entity.getHungerManager().add(-entity.getHungerManager().getFoodLevel(), 0);
                entity.damage(DamageSource.MAGIC, -food/2);
            } else {
                entity.getHungerManager().add((int)-foodSubtract, 0);
            }
        }

        return entity.getHealth() > 0;
    }

    @Override
    public boolean stepOnCloud() {
        if (entity.fallDistance > 1 || entity.distanceWalked > nextStepDistance) {
            nextStepDistance = entity.distanceWalked + 2;
            entity.fallDistance = 0;

            return true;
        }

        return false;
    }

    @Override
    public Either<SleepFailureReason, Unit> trySleep(BlockPos pos) {

        if (getInventory().matches(UTags.CURSED_ARTEFACTS)) {
            if (!isClient()) {
                entity.addChatMessage(new TranslatableText("tile.bed.youAreAMonster"), true);
            }
            return Either.left(SleepFailureReason.OTHER_PROBLEM);
        }

        if (findAllSpellsInRange(10).anyMatch(c -> c instanceof Pony && ((Pony)c).getInventory().matches(UTags.CURSED_ARTEFACTS))) {
            return Either.left(SleepFailureReason.NOT_SAFE);
        }

        return Either.right(Unit.INSTANCE);
    }

    @Override
    public PlayerInventory getInventory() {
        return inventory;
    }

    @Override
    public void onUse(ItemStack stack) {
        if (getSpecies() == Race.CHANGELING) {
            PlayerEntity player = getOwner();

            FoodComponent food = stack.getItem().getFoodComponent();

            if (food != null) {
                int health = food.getHunger();
                float saturation = food.getSaturationModifier();

                player.getHungerManager().add(-health/2, -saturation/2);

                player.addPotionEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 3, true, true));
            } else {
                player.addPotionEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 3, true, true));
            }

            if (player.world.getDifficulty() != Difficulty.PEACEFUL && player.world.random.nextInt(20) == 0) {
                player.addPotionEffect(new StatusEffectInstance(UEffects.FOOD_POISONING, 3, 2, true, true));
            }

            player.addPotionEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 2000, 2, true, true));
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putString("playerSpecies", getSpecies().name());

        compound.put("powers", powers.toNBT());
        compound.put("gravity", gravity.toNBT());

        MagicEffect effect = getEffect();

        if (effect != null) {
            compound.put("effect", SpellRegistry.instance().serializeEffectToNBT(effect));
        }

        pageStates.toNBT(compound);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        setSpecies(Race.fromName(compound.getString("playerSpecies")));

        powers.fromNBT(compound.getCompound("powers"));
        gravity.fromNBT(compound.getCompound("gravity"));

        if (compound.containsKey("effect")) {
            setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }

        pageStates.fromNBT(compound);
    }

    @Override
    public void copyFrom(Pony oldPlayer) {
        setEffect(oldPlayer.getEffect());
        setSpecies(oldPlayer.getSpecies());
    }

    @Override
    public void setEffect(@Nullable MagicEffect effect) {
        effectDelegate.set(effect);

        sendCapabilities(true);
    }

    @Override
    public boolean hasEffect() {
        return effectDelegate.has();
    }

    @Nullable
    @Override
    public <T extends MagicEffect> T getEffect(@Nullable Class<T> type, boolean update) {
        return effectDelegate.get(type, update);
    }

    @Override
    public void setOwner(PlayerEntity owner) {
    }

    @Override
    public PlayerEntity getOwner() {
        return entity;
    }

    @Override
    public int getCurrentLevel() {
        return 0;
    }

    @Override
    public void setCurrentLevel(int level) {
    }

}
