package com.minelittlepony.unicopia.hud;

import com.minelittlepony.unicopia.player.IPlayer;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

class FlightExperienceBar implements IHudElement {

    static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/bars.png");

    @Override
    public boolean shouldRender(IPlayer player) {
        return player.getPlayerSpecies().canFly()
                && player.getGravity().isFlying();
    }

    @Override
    public void renderHud(UHud context) {
        float xp = context.player.getGravity().getFlightExperience();
        float length = context.player.getGravity().getFlightDuration();

        context.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (context.width - 184) / 2;
        int y = context.height - 29;

        int xpFill = (int)Math.floor(xp * 184);
        int xpBuff = (int)Math.floor((184 - xpFill) * length);

        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 256, 5, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 5, xpFill, 5, 256, 256);


        Gui.drawModalRectWithCustomSizedTexture(x + xpFill, y, xpFill, 10, xpBuff, 5, 256, 256);

       // context.fonts.drawStringWithShadow("Flight experience: " + context.player.getFlightExperience(), 0, 0, 0xFFFFFF);
    }


}






















