package come.minelittlepony.unicopia.forgebullshit;

import com.minelittlepony.unicopia.InbtSerialisable;
import com.minelittlepony.unicopia.player.IPlayer;

public interface IPlayerCapabilitiesProxyContainer extends InbtSerialisable {
    IPlayer getPlayer();

    void setPlayer(IPlayer player);
}
