package com.minelittlepony.unicopia.client.input;

import com.minelittlepony.unicopia.InteractionManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

// TODO: we need mixins for this
public class MouseControl extends Mouse {
    public MouseControl(MinecraftClient client) {
        super(client);
    }

    @Override
    public void updateMouse() {
        if (InteractionManager.instance().getIPlayer().getGravity().getGravitationConstant() < 0) {
            //cursorDeltaX = -cursorDeltaX;
            //cursorDeltaY = -cursorDeltaY;
        }

        super.updateMouse();
    }
}
