package come.minelittlepony.unicopia.forgebullshit;

import com.minelittlepony.unicopia.player.IOwned;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.IRaceContainer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

class DefaultEntityCapabilitiesProxyContainer<T extends Entity> implements ICapabilitiesProxyContainer<T> {

    @CapabilityInject(ICapabilitiesProxyContainer.class)
    public static final Capability<ICapabilitiesProxyContainer<?>> CAPABILITY = null;

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
