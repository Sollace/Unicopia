package com.minelittlepony.unicopia.player;

import java.util.UUID;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

class PlayerCapabilities implements IPlayer, ICaster<EntityPlayer> {

    private Race playerSpecies = Race.HUMAN;

    private final PlayerAbilityDelegate powers = new PlayerAbilityDelegate(this);

    private final PlayerGravityDelegate gravity = new PlayerGravityDelegate(this);

    private IMagicEffect effect;

    private EntityPlayer entity;

    PlayerCapabilities(UUID playerId) {

    }

    @Override
    public Race getPlayerSpecies() {
        return playerSpecies;
    }

    @Override
    public void setPlayerSpecies(Race race) {
        playerSpecies = race;

        EntityPlayer self = getOwner();

        self.capabilities.allowFlying = race.canFly();
        gravity.updateFlightStat(self, self.capabilities.isFlying);

        self.sendPlayerAbilities();
    }

    @Override
    public void sendCapabilities() {
        PlayerSpeciesList.instance().sendCapabilities(getOwner().getGameProfile().getId());
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
    public void onUpdate(EntityPlayer entity) {
        this.entity = entity;

        powers.onUpdate(entity);
        gravity.onUpdate(entity);

        if (!getPlayerSpecies().canCast()) {
            effect = null;
        }

        if (effect != null) {
            if (entity.getEntityWorld().isRemote && entity.getEntityWorld().getWorldTime() % 10 == 0) {
                effect.render(entity);
            }

            if (!effect.update(entity)) {
                effect = null;
            }
        }
    }

    public void onEntityEat() {
        if (getPlayerSpecies() == Race.CHANGELING) {
            EntityPlayer player = getOwner();

            player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2000, 2));
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 2000, 2));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setString("playerSpecies", playerSpecies.name());
        compound.setTag("powers", powers.toNBT());
        compound.setTag("gravity", gravity.toNBT());

        if (effect != null) {
            compound.setString("effect_id", effect.getName());
            compound.setTag("effect", effect.toNBT());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        playerSpecies = Race.fromName(compound.getString("playerSpecies"));
        powers.readFromNBT(compound.getCompoundTag("powers"));
        gravity.readFromNBT(compound.getCompoundTag("gravity"));

        if (compound.hasKey("effect_id") && compound.hasKey("effect")) {
            effect = null;
            SpellRegistry.instance().getSpellFromName(compound.getString("effect_id")).ifPresent(f -> {
                effect = f;
                effect.readFromNBT(compound.getCompoundTag("effect"));
            });
        }
    }

    @Override
    public void setEffect(IMagicEffect effect) {
        this.effect = effect;
    }

    @Override
    public IMagicEffect getEffect() {
        return effect;
    }

    @Override
    public EntityPlayer getOwner() {
        return entity;
    }
}
