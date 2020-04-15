package com.minelittlepony.unicopia.client;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.IKeyBinding;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UnicopiaCore;
import com.minelittlepony.unicopia.ability.PowersRegistry;
import com.minelittlepony.unicopia.entity.player.IPlayer;

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;


class KeyBindingsHandler {
    private final MinecraftClient client = MinecraftClient.getInstance();

    private final Set<KeyBinding> bindings = new HashSet<>();
    private final Set<KeyBinding> removed = new HashSet<>();

    private final Set<KeyBinding> pressed = new HashSet<>();

    public void addKeybind(IKeyBinding p) {
        KeyBindingRegistry.INSTANCE.addCategory(p.getKeyCategory());

        FabricKeyBinding b = FabricKeyBinding.Builder.create(new Identifier(UnicopiaCore.MODID, p.getKeyName()), InputUtil.Type.KEYSYM, p.getKeyCode(), p.getKeyCategory()).build();
        KeyBindingRegistry.INSTANCE.register(b);

        bindings.add(b);
    }

    public void onKeyInput() {
        if (client.currentScreen != null
            || client.player == null) {
            return;
        }
        IPlayer iplayer = SpeciesList.instance().getPlayer(client.player);

        for (KeyBinding i : bindings) {
            if (i.isPressed()) {

                if (pressed.add(i)) {
                    if (!PowersRegistry.instance().hasRegisteredPower(i.getDefaultKeyCode().getKeyCode())) {
                        removed.add(i);
                        System.out.println("Error: Keybinding(" + i.getLocalizedName() + ") does not have a registered pony power. Keybinding will be removed from event.");
                    } else {
                        PowersRegistry.instance()
                            .getCapablePowerFromKey(i.getDefaultKeyCode().getKeyCode(), iplayer.getSpecies())
                            .ifPresent(iplayer.getAbilities()::tryUseAbility);
                    }
                }
            } else if (pressed.remove(i)) {
                iplayer.getAbilities().tryClearAbility();
            }
        }

        bindings.removeAll(removed);
        pressed.removeAll(removed);
        removed.clear();
    }
}
