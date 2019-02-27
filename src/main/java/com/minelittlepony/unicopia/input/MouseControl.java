package com.minelittlepony.unicopia.input;

import com.minelittlepony.unicopia.UClient;

import net.minecraft.util.MouseHelper;

public class MouseControl extends MouseHelper {
    public void mouseXYChange() {
        super.mouseXYChange();

        if (UClient.instance().getIPlayer().getGravity().getGravitationConstant() < 0) {
            deltaX = -deltaX;
            deltaY = -deltaY;
        }
    }
}
