package com.minelittlepony.unicopia.client;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.KeyBind;
import com.minelittlepony.unicopia.UnicopiaCore;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.entity.player.Pony;

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

    public void addKeybind(KeyBind p) {
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
        Pony iplayer = Pony.of(client.player);

        for (KeyBinding i : bindings) {
            if (i.isPressed()) {

                if (pressed.add(i)) {
                    if (!Abilities.getInstance().hasRegisteredPower(i.getDefaultKeyCode().getKeyCode())) {
                        removed.add(i);
                        System.out.println("Error: Keybinding(" + i.getLocalizedName() + ") does not have a registered pony power. Keybinding will be removed from event.");
                    } else {
                        Abilities.getInstance()
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
