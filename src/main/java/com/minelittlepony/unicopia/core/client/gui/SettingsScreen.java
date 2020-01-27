package com.minelittlepony.unicopia.core.client.gui;

import com.minelittlepony.common.client.gui.GameGui;
import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.element.Label;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

public class SettingsScreen extends GameGui {

    public SettingsScreen(Screen parent) {
        super(new TranslatableText("options.title"), parent);
    }

    @Override
    public void init() {
        addButton(new Label(width / 2, 15))
            .getStyle()
                .setColor(16777215)
                .setText(title.asString());
        addButton(new Button(width / 2 - 100, height / 6 + 168))
            .onClick(s -> finish())
            .getStyle().setText("gui.done");
    }
}
