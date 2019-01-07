package com.minelittlepony.unicopia.forgebullshit;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

class Storage<T extends Entity> implements IStorage<ICapabilitiesProxyContainer<T>> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilitiesProxyContainer<T>> capability, ICapabilitiesProxyContainer<T> instance, EnumFacing side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<ICapabilitiesProxyContainer<T>> capability, ICapabilitiesProxyContainer<T> instance, EnumFacing side, NBTBase nbt) {
        instance.readFromNBT((NBTTagCompound)nbt);
    }
}
