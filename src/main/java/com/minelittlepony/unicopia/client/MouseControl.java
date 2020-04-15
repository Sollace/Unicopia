package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.SpeciesList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;

// TODO: we need mixins for this
public class MouseControl extends Mouse {
    public MouseControl(MinecraftClient client) {
        super(client);
    }

    @Override
    public void updateMouse() {
        if (SpeciesList.instance().getPlayer(MinecraftClient.getInstance().player).getGravity().getGravitationConstant() < 0) {
            //cursorDeltaX = -cursorDeltaX;
            //cursorDeltaY = -cursorDeltaY;
        }

        super.updateMouse();
    }
}
