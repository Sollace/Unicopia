package com.minelittlepony.unicopia.player;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.minelittlepony.model.anim.BasicEasingInterpolator;
import com.minelittlepony.model.anim.IInterpolator;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.enchanting.PageState;
import com.minelittlepony.unicopia.init.UEffects;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.spell.IAttachedEffect;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellAffinity;
import com.minelittlepony.unicopia.spell.SpellDisguise;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;

class PlayerCapabilities implements IPlayer {

    private static final DataParameter<Integer> PLAYER_RACE = EntityDataManager
            .createKey(EntityPlayer.class, DataSerializers.VARINT);

    private static final DataParameter<Float> ENERGY = EntityDataManager
            .createKey(EntityPlayer.class, DataSerializers.FLOAT);

    private static final DataParameter<Float> EXERTION = EntityDataManager
            .createKey(EntityPlayer.class, DataSerializers.FLOAT);

    private static final DataParameter<NBTTagCompound> EFFECT = EntityDataManager
            .createKey(EntityPlayer.class, DataSerializers.COMPOUND_TAG);

    private final Map<ResourceLocation, PageState> pageStates = Maps.newHashMap();

    private final PlayerAbilityDelegate powers = new PlayerAbilityDelegate(this);

    private final PlayerGravityDelegate gravity = new PlayerGravityDelegate(this);

    private final PlayerAttributes attributes = new PlayerAttributes();

    private final PlayerView view = new PlayerView(this);

    private final PlayerFood food = new PlayerFood(this);

    private final PlayerInventory inventory = new PlayerInventory(this);

    private final EffectSync<EntityPlayer> effectDelegate = new EffectSync<>(this, EFFECT);

    private final IInterpolator interpolator = new BasicEasingInterpolator();

    private float nextStepDistance = 1;

    private EntityPlayer entity;

    private boolean dirty = false;

    private boolean invisible = false;

    PlayerCapabilities(EntityPlayer player) {
        setOwner(player);

        player.getDataManager().register(PLAYER_RACE, Race.EARTH.ordinal());
        player.getDataManager().register(EXERTION, 0F);
        player.getDataManager().register(ENERGY, 0F);
        player.getDataManager().register(EFFECT, new NBTTagCompound());
    }

    @Override
    public Race getPlayerSpecies() {
        if (getOwner() == null) {
            return Race.HUMAN;
        }

        return Race.fromId(getOwner().getDataManager().get(PLAYER_RACE));
    }

    @Override
    public void setPlayerSpecies(Race race) {
        EntityPlayer player = getOwner();

        race = PlayerSpeciesList.instance().validate(race, player);

        player.getDataManager().set(PLAYER_RACE, race.ordinal());

        player.capabilities.allowFlying = race.canFly();
        gravity.updateFlightStat(player, player.capabilities.isFlying);

        player.sendPlayerAbilities();
        sendCapabilities(false);
    }

    @Override
    public float getExertion() {
        return getOwner().getDataManager().get(EXERTION);
    }

    @Override
    public void setExertion(float exertion) {
        getOwner().getDataManager().set(EXERTION, Math.max(0, exertion));
    }

    @Override
    public float getEnergy() {
        return getOwner().getDataManager().get(ENERGY);
    }

    @Override
    public void setEnergy(float energy) {
        getOwner().getDataManager().set(ENERGY, Math.max(0, energy));
    }

    @Override
    public boolean isInvisible() {
        return invisible && hasEffect();
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.NEUTRAL;
    }

    @Override
    public void sendCapabilities(boolean full) {
        dirty = false;

        if (!getWorld().isRemote) {
            if (full) {
                Unicopia.getConnection().broadcast(new MsgPlayerCapabilities(this));
            } else {
                Unicopia.getConnection().broadcast(new MsgPlayerCapabilities(getPlayerSpecies(), getOwner()));
            }
        }
    }

    @Override
    public void onDimensionalTravel(int destinationDimension) {
        if (!getWorld().isRemote) {
            dirty = true;
        }
    }

    @Override
    public IAbilityReceiver getAbilities() {
        return powers;
    }

    @Override
    public IGravity getGravity() {
        return gravity;
    }

    @Override
    public IView getCamera() {
        return view;
    }

    @Override
    public IInterpolator getInterpolator() {
        return interpolator;
    }

    @Override
    public void beforeUpdate() {
        if (entity.world.isRemote) {
            if (entity.isRiding() && entity.isSneaking()) {

                Entity ridee = entity.getRidingEntity();

                entity.dismountRidingEntity();

                if (ridee instanceof EntityPlayerMP) {
                    ((EntityPlayerMP)ridee).getServerWorld().getEntityTracker().sendToTrackingAndSelf(ridee, new SPacketSetPassengers(ridee));
                }
            }
        }

        powers.onUpdate();
        inventory.onUpdate();
    }

    @Override
    public void onUpdate() {
        gravity.onUpdate();

        if (hasEffect()) {
            IAttachedEffect effect = getEffect(IAttachedEffect.class, true);

            if (effect != null) {
                if (entity.getEntityWorld().isRemote) {
                    effect.renderOnPerson(this);
                }

                if (!effect.updateOnPerson(this)) {
                    setEffect(null);
                }
            }
        }

        addExertion(-1);
        addEnergy(-1);

        attributes.applyAttributes(entity, getPlayerSpecies());

        if (dirty) {
            sendCapabilities(true);
        }
    }

    @Override
    public void onFall(float distance, float damageMultiplier) {
        if (!getWorld().isRemote) {
            if (getPlayerSpecies().canFly()) {
                if (entity.fallDistance > 2) {
                    entity.addStat(StatList.FALL_ONE_CM, (int)Math.round(distance * 100));
                }

                gravity.landHard(entity, distance, damageMultiplier);
            }
        }
    }

    @Override
    public boolean onProjectileImpact(Entity projectile) {

        if (hasEffect()) {
            IMagicEffect effect = getEffect();
            if (effect instanceof SpellDisguise && !effect.getDead()) {
                Entity disguise = ((SpellDisguise)effect).getDisguise();

                if (disguise == projectile) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean stepOnCloud() {
        EntityPlayer player = getOwner();

        if (player.fallDistance > 1 || player.distanceWalkedOnStepModified > nextStepDistance) {
            nextStepDistance = player.distanceWalkedOnStepModified + 2;
            player.fallDistance = 0;

            return true;
        }

        return false;
    }

    @Override
    public SleepResult trySleep(BlockPos pos) {


        if (getInventory().isWearing(UItems.alicorn_amulet)) {
            if (!getWorld().isRemote) {
                entity.sendStatusMessage(new TextComponentTranslation("tile.bed.youAreAMonster"), true);
            }
            return SleepResult.OTHER_PROBLEM;
        }

        if (findAllSpellsInRange(10).anyMatch(c -> c instanceof IPlayer && ((IPlayer)c).getInventory().isWearing(UItems.alicorn_amulet))) {
            return SleepResult.NOT_SAFE;
        }

        return null;
    }

    @Override
    public IFood getFood() {
        return food;
    }

    @Override
    public IInventory getInventory() {
        return inventory;
    }

    @Override
    public void onEat(ItemStack stack, @Nullable ItemFood food) {
        if (getPlayerSpecies() == Race.CHANGELING) {
            EntityPlayer player = getOwner();

            if (food != null) {
                int health = food.getHealAmount(stack);
                float saturation = food.getSaturationModifier(stack);

                player.getFoodStats().addStats(-health/2, -saturation/2);

                player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 3, true, true));
            } else {
                player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 3, true, true));
            }

            if (player.world.getDifficulty() != EnumDifficulty.PEACEFUL && player.world.rand.nextInt(20) == 0) {
                player.addPotionEffect(new PotionEffect(UEffects.FOOD_POISONING, 3, 2, true, true));
            }

            player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2000, 2, true, true));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setString("playerSpecies", getPlayerSpecies().name());

        compound.setTag("powers", powers.toNBT());
        compound.setTag("gravity", gravity.toNBT());

        IMagicEffect effect = getEffect();

        if (effect != null) {
            compound.setTag("effect", SpellRegistry.instance().serializeEffectToNBT(effect));
        }

        if (!pageStates.isEmpty()) {
            NBTTagCompound pages = new NBTTagCompound();
            boolean written = false;

            for (Map.Entry<ResourceLocation, PageState> entry : pageStates.entrySet()) {
                if (entry.getValue() != PageState.LOCKED) {
                    pages.setString(entry.getKey().toString(), entry.getValue().name());
                    written = true;
                }
            }

            if (written) {
                compound.setTag("pageStates", pages);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        setPlayerSpecies(Race.fromName(compound.getString("playerSpecies")));

        powers.readFromNBT(compound.getCompoundTag("powers"));
        gravity.readFromNBT(compound.getCompoundTag("gravity"));

        if (compound.hasKey("effect")) {
            setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompoundTag("effect")));
        }

        pageStates.clear();
        if (compound.hasKey("pageStates")) {
            NBTTagCompound pages = compound.getCompoundTag("pageStates");

            for (String key : pages.getKeySet()) {
                PageState state = PageState.of(pages.getString(key));

                if (state != PageState.LOCKED) {
                    pageStates.put(new ResourceLocation(key), state);
                }
            }
        }
    }

    @Override
    public void copyFrom(IPlayer oldPlayer) {
        setEffect(oldPlayer.getEffect());
        setPlayerSpecies(oldPlayer.getPlayerSpecies());
    }

    @Override
    public void setEffect(@Nullable IMagicEffect effect) {
        effectDelegate.set(effect);

        sendCapabilities(true);
    }

    @Override
    public boolean hasEffect() {
        return effectDelegate.has();
    }

    @Nullable
    @Override
    public <T extends IMagicEffect> T getEffect(@Nullable Class<T> type, boolean update) {
        return effectDelegate.get(type, update);
    }

    @Override
    public void setOwner(EntityPlayer owner) {
        entity = owner;
    }

    @Override
    public EntityPlayer getOwner() {
        return entity;
    }

    @Override
    public int getCurrentLevel() {
        return 0;
    }

    @Override
    public void setCurrentLevel(int level) {
    }

    @Override
    public Map<ResourceLocation, PageState> getPageStates() {
        return pageStates;
    }
}
