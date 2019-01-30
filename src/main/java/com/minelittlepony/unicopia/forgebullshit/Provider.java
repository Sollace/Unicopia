package com.minelittlepony.unicopia.forgebullshit;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

@FUF(reason = "Required to provide capability containers. Why can't forge implement this themselves!?")
class Provider implements ICapabilitySerializable<NBTTagCompound> {

    private final Entity entity;

    @Nullable
    private ICapabilitiesProxyContainer<Entity> container;

    Provider(Entity entity) {
        this.entity = entity;
    }

    private ICapabilitiesProxyContainer<Entity> getContainerObject() {
        if (container == null) {
            container = DefaultEntityCapabilitiesProxyContainer.newInstance();
        }

        return container;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return DefaultEntityCapabilitiesProxyContainer.updateAndCompare(capability);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (hasCapability(capability, facing)) {
            return (T)getContainerObject().withEntity(entity);
        }

        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) DefaultEntityCapabilitiesProxyContainer.capability().getStorage()
                .writeNBT(DefaultEntityCapabilitiesProxyContainer.capability(), getContainerObject().withEntity(entity), null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        DefaultEntityCapabilitiesProxyContainer.capability().getStorage()
                .readNBT(DefaultEntityCapabilitiesProxyContainer.capability(), getContainerObject().withEntity(entity), null, nbt);
    }
}
