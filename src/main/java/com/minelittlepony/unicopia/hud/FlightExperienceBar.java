package com.minelittlepony.unicopia.hud;

import com.minelittlepony.unicopia.player.IPlayer;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

class FlightExperienceBar implements IHudElement {

    static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/bars.png");

    @Override
    public boolean shouldRender(IPlayer player) {
        return player.getPlayerSpecies().canFly()
                && !player.getOwner().capabilities.isCreativeMode;
    }

    @Override
    public void renderHud(UHud context) {
        float xp = context.player.getGravity().getFlightExperience();
        float length = context.player.getGravity().getFlightDuration();

        context.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (context.width - 183) / 2;
        int y = context.height - 29;

        int xpFill = (int)Math.floor(xp * 183);
        int xpBuff = (int)Math.floor((183 - xpFill) * length);

        int baseV = 0;

        if (context.player.getGravity().isExperienceCritical()) {

            int tickCount = (int)(context.mc.getRenderPartialTicks() * 10);

            baseV += (tickCount % 3) * 10;
        }

        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, baseV, 256, 5, 256, 256);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, baseV + 5, xpFill, 5, 256, 256);

        Gui.drawModalRectWithCustomSizedTexture(x + xpFill, y, xpFill, baseV + 10, xpBuff, 5, 256, 256);
    }

    @Override
    public void repositionHud(UHud context) {
        int offset = 6;

        GlStateManager.translate(0, context.begin ? -offset : offset, 0);
    }
}






















