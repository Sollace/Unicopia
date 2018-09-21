package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.EffectSync;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;

class PlayerCapabilities implements IPlayer, ICaster<EntityPlayer> {

    private static final DataParameter<Integer> PLAYER_RACE = EntityDataManager
            .createKey(EntityPlayer.class, DataSerializers.VARINT);

    private static final DataParameter<Float> EXERTION = EntityDataManager
            .createKey(EntityPlayer.class, DataSerializers.FLOAT);

    private static final DataParameter<NBTTagCompound> EFFECT = EntityDataManager
            .createKey(EntityPlayer.class, DataSerializers.COMPOUND_TAG);

    private final PlayerAbilityDelegate powers = new PlayerAbilityDelegate(this);

    private final PlayerGravityDelegate gravity = new PlayerGravityDelegate(this);

    private final PlayerAttributes attributes = new PlayerAttributes();

    private final EffectSync<EntityPlayer> effectDelegate = new EffectSync<>(this, EFFECT);

    private float nextStepDistance = 1;

    private EntityPlayer entity;

    PlayerCapabilities(EntityPlayer player) {
        setOwner(player);

        player.getDataManager().register(PLAYER_RACE, Race.HUMAN.ordinal());
        player.getDataManager().register(EXERTION, 0F);
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

        if (!PlayerSpeciesList.instance().speciesPermitted(race, player)) {
            race = Race.HUMAN;
        }

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
    public void sendCapabilities(boolean full) {
        if (!getOwner().getEntityWorld().isRemote) {
            System.out.println("[SERVER] Sending player capabilities.");

            if (full) {
                Unicopia.channel.broadcast(new MsgPlayerCapabilities(this));
            } else {
                Unicopia.channel.broadcast(new MsgPlayerCapabilities(getPlayerSpecies(), getOwner()));
            }
        }
    }

    @Override
    public IAbilityReceiver getAbilities() {
        return powers;
    }

    @Override
    public boolean isClientPlayer() {
        return UClient.isClientSide() &&
                Minecraft.getMinecraft().player.getGameProfile().getId().equals(getOwner().getGameProfile().getId());
    }

    @Override
    public void beforeUpdate(EntityPlayer entity) {
        if (entity.world.isRemote) {
            if (entity.isRiding() && entity.isSneaking()) {

                Entity ridee = entity.getRidingEntity();

                entity.dismountRidingEntity();

                if (ridee instanceof EntityPlayerMP) {
                    ((EntityPlayerMP)ridee).getServerWorld().getEntityTracker().sendToTrackingAndSelf(ridee, new SPacketSetPassengers(ridee));
                }
            }
        }
    }

    @Override
    public void onUpdate(EntityPlayer entity) {
        powers.onUpdate(entity);
        gravity.onUpdate(entity);

        if (hasEffect()) {
            if (!getPlayerSpecies().canCast()) {
                setEffect(null);
            } else {
                if (entity.getEntityWorld().isRemote) { //  && entity.getEntityWorld().getWorldTime() % 10 == 0
                    getEffect().render(entity);
                }

                if (!getEffect().update(entity)) {
                    setEffect(null);
                }
            }
        }

        addExertion(-1);

        attributes.applyAttributes(entity, getPlayerSpecies());
    }

    @Override
    public void onFall(float distance, float damageMultiplier) {
        if (!entity.getEntityWorld().isRemote) {
            if (getPlayerSpecies().canFly()) {
                if (entity.fallDistance > 2) {
                    entity.addStat(StatList.FALL_ONE_CM, (int)Math.round(distance * 100));
                }

                gravity.landHard(entity, distance, damageMultiplier);
            }
        }
    }


    @Override
    public boolean stepOnCloud() {
        EntityPlayer player = getOwner();

        if ((player.fallDistance > 1) || player.distanceWalkedOnStepModified > nextStepDistance) {
            nextStepDistance = player.distanceWalkedOnStepModified + 2;
            player.fallDistance = 0;

            return true;
        }

        return false;
    }

    @Override
    public void onEntityEat() {
        if (getPlayerSpecies() == Race.CHANGELING) {
            EntityPlayer player = getOwner();

            player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2000, 2));
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 2000, 2));
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
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        setPlayerSpecies(Race.fromName(compound.getString("playerSpecies"), Race.HUMAN));

        powers.readFromNBT(compound.getCompoundTag("powers"));
        gravity.readFromNBT(compound.getCompoundTag("gravity"));

        if (compound.hasKey("effect")) {
            setEffect(SpellRegistry.instance().createEffectFromNBT(compound.getCompoundTag("effect")));
        }
    }

    @Override
    public void copyFrom(IPlayer oldPlayer) {
        setEffect(oldPlayer.getEffect());
        setPlayerSpecies(oldPlayer.getPlayerSpecies());
    }

    @Override
    public void setEffect(IMagicEffect effect) {
        effectDelegate.set(effect);

        sendCapabilities(true);
    }

    @Override
    public IMagicEffect getEffect() {
        return effectDelegate.get();
    }

    @Override
    public void setOwner(EntityPlayer owner) {
        entity = owner;
    }

    @Override
    public EntityPlayer getOwner() {
        return entity;
    }
}
