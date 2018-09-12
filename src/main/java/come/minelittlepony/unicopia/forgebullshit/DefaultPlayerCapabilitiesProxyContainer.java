package come.minelittlepony.unicopia.forgebullshit;

import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

class DefaultPlayerCapabilitiesProxyContainer implements IPlayerCapabilitiesProxyContainer {

    @CapabilityInject(IPlayerCapabilitiesProxyContainer.class)
    public static final Capability<IPlayerCapabilitiesProxyContainer> CAPABILITY = null;

    private IPlayer player;

    @Override
    public IPlayer getPlayer() {
        if (player == null) {
            player = PlayerSpeciesList.instance().emptyPlayer(null);
        }

        return player;
    }

    @Override
    public void setPlayer(IPlayer player) {
        this.player = player;
    }

    public void writeToNBT(NBTTagCompound compound) {
        getPlayer().writeToNBT(compound);
    }

    public void readFromNBT(NBTTagCompound compound) {
        getPlayer().readFromNBT(compound);
    }

    public IPlayerCapabilitiesProxyContainer withEntity(EntityPlayer player) {
        if (this.player == null) {
            this.player = PlayerSpeciesList.instance().emptyPlayer(player);
        } else {
            getPlayer().setOwner(player);
        }

        return this;
    }
}
