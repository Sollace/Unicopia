package com.minelittlepony.unicopia.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.entity.player.IPlayer;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;

public class UHud {

    public static final UHud instance = new UHud();

    private List<IHudElement> elements = new ArrayList<>();

    MinecraftClient mc = MinecraftClient.getInstance();

    TextRenderer fonts = mc.textRenderer;

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
        GlStateManager.color4f(1, 1, 1, 1);

        element.renderHud(this);

        GlStateManager.popMatrix();

        GL11.glPopAttrib();

    }

}
