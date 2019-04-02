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
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

public class UHud extends Gui {

    public static final UHud instance = new UHud();

    private List<IHudElement> elements = new ArrayList<>();

    Minecraft mc = Minecraft.getMinecraft();

    FontRenderer fonts = mc.fontRenderer;

    IPlayer player;

    int width;

    int height;

    boolean begin;

    private UHud() {
        elements.add(new FlightExperienceBar());
    }

    public void renderHud(IPlayer player, ScaledResolution resolution) {
        this.width = resolution.getScaledWidth();
        this.height = resolution.getScaledHeight();
        this.player = player;
        this.begin = true;

        elements.forEach(this::renderElement);
    }

    public void repositionElements(IPlayer player, ScaledResolution resolution, ElementType type, boolean begin) {
        this.width = resolution.getScaledWidth();
        this.height = resolution.getScaledHeight();
        this.player = player;
        this.begin = begin;

        if (isSurvivalElement(type)) {
            elements.forEach(this::positionElement);
        }
    }

    protected boolean isSurvivalElement(ElementType type) {
        switch (type) {
            case ARMOR:
            case HEALTH:
            case FOOD:
            case AIR:
            case EXPERIENCE:
            case HEALTHMOUNT:
            case JUMPBAR:
                return true;
            default: return false;
        }
    }

    private void positionElement(IHudElement element) {
        if (!element.shouldRender(player)) {
            return;
        }

        element.repositionHud(this);
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
