package com.minelittlepony.unicopia.gui;

import com.minelittlepony.gui.Button;
import com.minelittlepony.gui.GameGui;
import com.minelittlepony.util.lang.ClientLocale;

import net.minecraft.client.gui.GuiScreen;

public class GuiScreenSettings extends GameGui {

    private String title = "";

    private final GuiScreen parent;

    public GuiScreenSettings(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        title = ClientLocale.format("options.title");

        addButton(new Button(width / 2 - 100, height / 6 + 168, 200, 20, "gui.done", sender -> mc.displayGuiScreen(parent)));
    }

    @Override
    protected void drawContents(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, title, width / 2, 15, 16777215);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
