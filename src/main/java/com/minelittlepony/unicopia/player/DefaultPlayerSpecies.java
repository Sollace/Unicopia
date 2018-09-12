package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.unicopia.spell.IMagicEffect;

import net.minecraft.entity.player.EntityPlayer;

final class DefaultPlayerSpecies implements IPlayer, IAbilityReceiver {

    public static final IPlayer INSTANCE = new DefaultPlayerSpecies();

    private DefaultPlayerSpecies() {
    }

    @Override
    public Race getPlayerSpecies() {
        return Race.EARTH;
    }

    @Override
    public void setPlayerSpecies(Race race) {
    }

    @Override
    public void sendCapabilities(boolean full) {

    }

    @Override
    public void tryUseAbility(IPower<?> power) {

    }

    @Override
    public void tryClearAbility() {

    }

    @Override
    public int getRemainingCooldown() {
        return 0;
    }

    @Override
    public IAbilityReceiver getAbilities() {
        return this;
    }

    @Override
    public boolean isClientPlayer() {
        return false;
    }

    @Override
    public void onUpdate(EntityPlayer entity) {

    }

    @Override
    public void setEffect(IMagicEffect effect) {

    }

    @Override
    public IMagicEffect getEffect() {
        return null;
    }

    @Override
    public EntityPlayer getOwner() {
        return null;
    }

    @Override
    public void copyFrom(IPlayer oldPlayer) {

    }
}
