package com.minelittlepony.unicopia.modmenu;

import com.minelittlepony.unicopia.client.gui.SettingsScreen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class UMenuFactory implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SettingsScreen::new;
    }
}
