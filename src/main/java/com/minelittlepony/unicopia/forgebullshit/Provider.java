package com.minelittlepony.unicopia.forgebullshit;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

class Provider implements ICapabilitySerializable<NBTTagCompound> {
    @SuppressWarnings("unchecked")
    DefaultEntityCapabilitiesProxyContainer<Entity> instance = (DefaultEntityCapabilitiesProxyContainer<Entity>) DefaultEntityCapabilitiesProxyContainer.CAPABILITY.getDefaultInstance();

    private final Entity entity;

    Provider(Entity entity) {
        this.entity = entity;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == DefaultEntityCapabilitiesProxyContainer.CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (hasCapability(capability, facing)) {
            return DefaultEntityCapabilitiesProxyContainer.CAPABILITY.<T>cast(instance.withEntity(entity));
        }

        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) DefaultEntityCapabilitiesProxyContainer.CAPABILITY.getStorage()
                .writeNBT(DefaultEntityCapabilitiesProxyContainer.CAPABILITY, instance.withEntity(entity), null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        DefaultEntityCapabilitiesProxyContainer.CAPABILITY.getStorage()
                .readNBT(DefaultEntityCapabilitiesProxyContainer.CAPABILITY, instance.withEntity(entity), null, nbt);
    }
}
