package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.entity.player.IPlayer;

public interface IHudElement {

    void repositionHud(UHud context);

    void renderHud(UHud context);

    boolean shouldRender(IPlayer player);
}
