package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ducks.IItemEntity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;

public class ItemEntityCapabilities implements RaceContainer<ItemEntity>, Owned<ItemEntity> {

    private Race race = Race.HUMAN;

    private final ItemEntity owner;

    public ItemEntityCapabilities(ItemEntity owner) {
        this.owner = owner;
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public boolean beforeUpdate() {
        ItemStack stack = owner.getStack();

        if (!stack.isEmpty() && stack.getItem() instanceof TickableItem) {
            return ((TickableItem)stack.getItem()).onGroundTick((IItemEntity)owner) == ActionResult.SUCCESS;
        }

        return false;
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

    public interface TickableItem {
        ActionResult onGroundTick(IItemEntity entity);
    }
}
