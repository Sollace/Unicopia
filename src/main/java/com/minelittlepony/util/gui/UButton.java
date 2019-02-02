package com.minelittlepony.util.gui;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class UButton extends GuiButton {

    private final Function<GuiButton, Boolean> action;

    public UButton(int id, int x, int y, int w, int h, String label, Function<GuiButton, Boolean> action) {
        super(id, x, y, w, h, label);
        this.action = action;
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            return action.apply(this);
        }

        return false;
    }
}
