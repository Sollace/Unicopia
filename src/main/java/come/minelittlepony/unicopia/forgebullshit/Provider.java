package come.minelittlepony.unicopia.forgebullshit;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

class Provider implements ICapabilitySerializable<NBTTagCompound> {
    DefaultPlayerCapabilitiesProxyContainer instance = (DefaultPlayerCapabilitiesProxyContainer) DefaultPlayerCapabilitiesProxyContainer.CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == DefaultPlayerCapabilitiesProxyContainer.CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (hasCapability(capability, facing)) {
            return DefaultPlayerCapabilitiesProxyContainer.CAPABILITY.<T>cast(instance);
        }

        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) DefaultPlayerCapabilitiesProxyContainer.CAPABILITY.getStorage()
                .writeNBT(DefaultPlayerCapabilitiesProxyContainer.CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        DefaultPlayerCapabilitiesProxyContainer.CAPABILITY.getStorage()
                .readNBT(DefaultPlayerCapabilitiesProxyContainer.CAPABILITY, instance, null, nbt);
    }
}
