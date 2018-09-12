package come.minelittlepony.unicopia.forgebullshit;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

class Storage implements IStorage<IPlayerCapabilitiesProxyContainer> {

    @Override
    public NBTBase writeNBT(Capability<IPlayerCapabilitiesProxyContainer> capability, IPlayerCapabilitiesProxyContainer instance, EnumFacing side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<IPlayerCapabilitiesProxyContainer> capability, IPlayerCapabilitiesProxyContainer instance, EnumFacing side, NBTBase nbt) {
        instance.readFromNBT((NBTTagCompound)nbt);
    }
}
