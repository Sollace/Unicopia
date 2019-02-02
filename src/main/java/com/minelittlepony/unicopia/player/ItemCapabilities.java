package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.Race;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;

class ItemCapabilities implements IRaceContainer<EntityItem>, IOwned<EntityItem> {

    private Race race = Race.HUMAN;

    private EntityItem owner;

    @Override
    public void onUpdate(EntityItem entity) {

    }

    @Override
    public Race getPlayerSpecies() {
        return race;
    }

    @Override
    public void setPlayerSpecies(Race race) {
        this.race = race;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setString("owner_species", race.name());
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        race = Race.fromName(compound.getString("owner_species"));
    }

    @Override
    public void setOwner(EntityItem owner) {
        this.owner = owner;
    }

    @Override
    public void onDimensionalTravel(int destinationDimension) {

    }

    @Override
    public EntityItem getOwner() {
        return owner;
    }
}
