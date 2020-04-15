package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;

class FlightExperienceBar extends DrawableHelper implements HudElement {

    static final Identifier TEXTURE = new Identifier("textures/gui/bars.png");

    @Override
    public boolean shouldRender(Pony player) {
        return player.getSpecies().canFly()
                && !player.getOwner().abilities.creativeMode;
    }

    @Override
    public void renderHud(UHud context) {
        float xp = context.player.getFlight().getFlightExperience();
        float length = context.player.getFlight().getFlightDuration();

        context.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (context.width - 182) / 2;
        int y = context.height - 29;

        int xpFill = (int)Math.floor(xp * 182);
        int xpBuff = (int)Math.floor((183 - xpFill) * length);

        int baseV = 0;

        if (context.player.getFlight().isExperienceCritical()) {

            int tickCount = (int)(context.mc.getTickDelta() * 10);

            baseV += (tickCount % 3) * 10;
        }

        blit(x, y, 0, baseV, 256, 5, 256, 256);
        blit(x, y, 0, baseV + 5, xpFill, 5, 256, 256);

        blit(x + xpFill, y, xpFill, baseV + 10, xpBuff, 5, 256, 256);
    }

    @Override
    public void repositionHud(UHud context) {
        int offset = 6;

        GlStateManager.translatef(0, context.begin ? -offset : offset, 0);
    }
}






















