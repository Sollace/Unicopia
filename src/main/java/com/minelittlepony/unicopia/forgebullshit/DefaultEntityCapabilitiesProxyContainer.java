package com.minelittlepony.unicopia.forgebullshit;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.player.IOwned;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.IRaceContainer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

class DefaultEntityCapabilitiesProxyContainer<T extends Entity> implements ICapabilitiesProxyContainer<T> {

    @Nullable
    @CapabilityInject(ICapabilitiesProxyContainer.class)
    public static Capability<ICapabilitiesProxyContainer<?>> CAPABILITY = null;

    @SuppressWarnings("unchecked")
    static boolean updateAndCompare(Capability<?> capability) {
        if (CAPABILITY == null && capability != null) {
            if (capability.getDefaultInstance() instanceof ICapabilitiesProxyContainer) {
                CAPABILITY = (Capability<ICapabilitiesProxyContainer<?>>)capability;
            }
        }

        return capability() == capability;
    }

    static <T extends Entity> ICapabilitiesProxyContainer<T> newInstance() {
        if (capability() == null) {
            return null;
        }
        return capability().cast(capability().getDefaultInstance());
    }

    @Nullable
    static Capability<ICapabilitiesProxyContainer<?>> capability() {
        if (CAPABILITY == null) {
            new RuntimeException("Warning: Capability is null").printStackTrace();
        }
        return CAPABILITY;
    }

    private IRaceContainer<T> container;


    @Override
    public IRaceContainer<T> getRaceContainer() {
        return container;
    }

    @Override
    public IPlayer getPlayer() {
        return (IPlayer)container;
    }

    public void writeToNBT(NBTTagCompound compound) {
        container.writeToNBT(compound);
    }

    public void readFromNBT(NBTTagCompound compound) {
        container.readFromNBT(compound);
    }

    @SuppressWarnings("unchecked")
    public ICapabilitiesProxyContainer<T> withEntity(T entity) {
        if (this.container == null) {
            this.container = (IRaceContainer<T>)PlayerSpeciesList.instance().emptyContainer(entity);
        } else if (container instanceof IOwned<?>) {
            ((IOwned<T>)container).setOwner(entity);
        }

        return this;
    }
}
