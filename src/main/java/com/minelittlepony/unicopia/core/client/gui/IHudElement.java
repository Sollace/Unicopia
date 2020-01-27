package com.minelittlepony.unicopia.core.client.gui;

import com.minelittlepony.unicopia.core.entity.player.IPlayer;

public interface IHudElement {

    void repositionHud(UHud context);

    void renderHud(UHud context);

    boolean shouldRender(IPlayer player);
}
