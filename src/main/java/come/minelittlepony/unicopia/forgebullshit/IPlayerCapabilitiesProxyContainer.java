package come.minelittlepony.unicopia.forgebullshit;

import com.minelittlepony.unicopia.InbtSerialisable;
import com.minelittlepony.unicopia.player.IPlayer;

import net.minecraft.entity.player.EntityPlayer;

public interface IPlayerCapabilitiesProxyContainer extends InbtSerialisable {
    IPlayer getPlayer();

    void setPlayer(IPlayer player);

    IPlayerCapabilitiesProxyContainer withEntity(EntityPlayer player);
}
