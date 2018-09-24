package com.minelittlepony.unicopia.hud;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.player.IPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class UHud extends Gui {

    public static final UHud instance = new UHud();

    private List<IHudElement> elements = new ArrayList<>();

    Minecraft mc = Minecraft.getMinecraft();

    FontRenderer fonts = mc.fontRenderer;

    IPlayer player;

    int width;

    int height;

    private UHud() {
        elements.add(new FlightExperienceBar());
    }

    public void renderHud(IPlayer player, ScaledResolution resolution) {
        this.width = resolution.getScaledWidth();
        this.height = resolution.getScaledHeight();
        this.player = player;

        elements.forEach(this::renderElement);
    }

    private void renderElement(IHudElement element) {
        if (!element.shouldRender(player)) {
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);

        element.renderHud(this);

        GlStateManager.popMatrix();

        GL11.glPopAttrib();

    }

}
