package com.minelittlepony.unicopia.entity.item;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.IOwned;
import com.minelittlepony.unicopia.entity.IRaceContainer;

import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.CompoundTag;

public class ItemEntityCapabilities implements IRaceContainer<ItemEntity>, IOwned<ItemEntity> {

    private Race race = Race.HUMAN;

    private final ItemEntity owner;

    public ItemEntityCapabilities(ItemEntity owner) {
        this.owner = owner;
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void beforeUpdate() {

    }

    @Override
    public Race getSpecies() {
        return race;
    }

    @Override
    public void setSpecies(Race race) {
        this.race = race;
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putString("owner_species", race.name());
    }


    @Override
    public void fromNBT(CompoundTag compound) {
        race = Race.fromName(compound.getString("owner_species"));
    }

    @Override
    public void setOwner(ItemEntity owner) {

    }

    @Override
    public void onDimensionalTravel(int destinationDimension) {

    }

    @Override
    public ItemEntity getOwner() {
        return owner;
    }
}
