package com.minelittlepony.unicopia.hud;

import com.minelittlepony.unicopia.player.IPlayer;

public interface IHudElement {

    void repositionHud(UHud context);

    void renderHud(UHud context);

    boolean shouldRender(IPlayer player);
}
