package com.minelittlepony.unicopia.hud;

import com.minelittlepony.unicopia.player.IPlayer;

public interface IHudElement {
    void renderHud(UHud context);

    boolean shouldRender(IPlayer player);
}
