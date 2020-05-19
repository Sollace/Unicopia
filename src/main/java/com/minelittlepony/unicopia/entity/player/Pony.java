package com.minelittlepony.unicopia.entity.player;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ducks.PonyContainer;
import com.minelittlepony.unicopia.enchanting.PageOwner;
import com.minelittlepony.unicopia.entity.Physics;
import com.minelittlepony.unicopia.entity.Ponylike;
import com.minelittlepony.unicopia.entity.Trap;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.AttachedMagicEffect;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.HeldMagicEffect;
import com.minelittlepony.unicopia.magic.MagicEffect;
import com.minelittlepony.unicopia.magic.MagicalItem;
import com.minelittlepony.unicopia.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.network.Transmittable;
import com.minelittlepony.unicopia.toxin.Toxicity;
import com.minelittlepony.unicopia.toxin.Toxin;
import com.minelittlepony.util.BasicEasingInterpolator;
import com.minelittlepony.util.IInterpolator;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.network.packet.EntityPassengersSetS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity.SleepFailureReason;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Pony implements Caster<PlayerEntity>, Ponylike<PlayerEntity>, Transmittable {

    private static final TrackedData<Integer> RACE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    static final TrackedData<Float> ENERGY = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    static final TrackedData<Float> EXERTION = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<CompoundTag> EFFECT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);
    private static final TrackedData<CompoundTag> HELD_EFFECT = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.TAG_COMPOUND);

    private final PlayerPageStats pageStates = new PlayerPageStats(this);
    private final AbilityDispatcher powers = new AbilityDispatcher(this);
    private final PlayerPhysics gravity = new PlayerPhysics(this);
    private final PlayerAttributes attributes = new PlayerAttributes();
    private final PlayerCamera camera = new PlayerCamera(this);
    private final PlayerInventory inventory = new PlayerInventory(this);
    private final MagicReserves mana;

    private final EffectSync effectDelegate = new EffectSync(this, EFFECT);
    private final EffectSync heldEffectDelegate = new EffectSync(this, HELD_EFFECT);

    private final IInterpolator interpolator = new BasicEasingInterpolator();

    private float nextStepDistance = 1;

    private final PlayerEntity entity;

    private boolean dirty = false;

    private boolean invisible = false;

    public Pony(PlayerEntity player) {
        this.entity = player;
        this.mana = new ManaContainer(this);

        player.getDataTracker().startTracking(RACE, Race.EARTH.ordinal());
        player.getDataTracker().startTracking(EFFECT, new CompoundTag());
        player.getDataTracker().startTracking(HELD_EFFECT, new CompoundTag());

        player.getAttributes().register(PlayerAttributes.EXTENDED_REACH_DISTANCE);
    }

    @Override
    public Race getSpecies() {
        if (getOwner() == null) {
            return Race.HUMAN;
        }

        return Race.fromId(getOwner().getDataTracker().get(RACE));
    }

    @Override
    public void setSpecies(Race race) {
        race = race.validate(entity);

        entity.getDataTracker().set(RACE, race.ordinal());

        gravity.updateFlightStat(entity.abilities.flying);
        entity.sendAbilitiesUpdate();
    }

    public MagicReserves getMagicalReserves() {
        return mana;
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

    public void setDirty() {
        dirty = true;
    }

    @Override
    public void sendCapabilities(boolean full) {
        dirty = false;

        if (entity instanceof ServerPlayerEntity) {
            System.out.println("Sending capabilities for player");
            Channel.BROADCAST_CAPABILITIES.send(new MsgPlayerCapabilities(full, this));
        }
    }

    public AbilityDispatcher getAbilities() {
        return powers;
    }

    public PageOwner getPages() {
        return pageStates;
    }

    @Override
    public Physics getPhysics() {
        return gravity;
    }

    public float getExtendedReach() {
        return (float)entity.getAttributeInstance(PlayerAttributes.EXTENDED_REACH_DISTANCE).getValue();
    }

    public Motion getMotion() {
        return gravity;
    }

    public PlayerCamera getCamera() {
        return camera;
    }

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

        powers.tick();
        inventory.tick();

        return false;
    }

    @Override
    public void tick() {
        gravity.tick();

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

        mana.addExertion(-10);
        mana.addEnergy(-1);

        attributes.applyAttributes(this);

        if (dirty) {
            sendCapabilities(true);
        }
    }

    public float onImpact(float distance) {
        if (getSpecies().canFly()) {
            distance = Math.max(0, distance - 5);
        }
        return distance;
    }

    @Override
    public void onJump() {
        if (gravity.isGravityNegative()) {
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

    public boolean stepOnCloud() {
        if (entity.fallDistance > 1 || entity.distanceTraveled > nextStepDistance) {
            nextStepDistance = entity.distanceTraveled + 2;
            entity.fallDistance = 0;

            return true;
        }

        return false;
    }

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

    public PlayerInventory getInventory() {
        return inventory;
    }

    public void onEat(ItemStack stack) {
        if (getSpecies() == Race.CHANGELING) {
            Toxin.POISON.afflict(getOwner(), Toxicity.SAFE, stack);
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

        if (compound.contains("effect")) {
            effectDelegate.set(SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect")));
        }

        pageStates.fromNBT(compound);
    }

    public void copyFrom(Pony oldPlayer) {
        setEffect(oldPlayer.getEffect());
        setSpecies(oldPlayer.getSpecies());
        setDirty();
    }

    @Override
    public void setEffect(@Nullable MagicEffect effect) {
        effectDelegate.set(effect);
        setDirty();
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

    public boolean isClientPlayer() {
        return InteractionManager.instance().isClientPlayer(getOwner());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Pony of(@Nullable PlayerEntity player) {
        return player == null ? null : ((PonyContainer<Pony>)player).get();
    }

    public static boolean equal(GameProfile one, GameProfile two) {
        return one == two || (one != null && two != null && one.getId().equals(two.getId()));
    }

    public static boolean equal(PlayerEntity one, PlayerEntity two) {
        return one == two || (one != null && two != null && equal(one.getGameProfile(), two.getGameProfile()));
    }
}
