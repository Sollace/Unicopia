package com.minelittlepony.unicopia.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;

public class KeyBindingsHandler {
    private final String KEY_CATEGORY = "unicopia.category.name";

    public static final KeyBindingsHandler INSTANCE = new KeyBindingsHandler();

    static void bootstrap() {}

    private final Map<KeyBinding, AbilitySlot> keys = new HashMap<>();
    private final Map<AbilitySlot, KeyBinding> reverse = new HashMap<>();

    private final Set<KeyBinding> pressed = new HashSet<>();

    public KeyBindingsHandler() {
        addKeybind(GLFW.GLFW_KEY_R, AbilitySlot.PRIMARY);
        addKeybind(GLFW.GLFW_KEY_G, AbilitySlot.SECONDARY);
        addKeybind(GLFW.GLFW_KEY_V, AbilitySlot.TERTIARY);
    }

    public KeyBinding getBinding(AbilitySlot slot) {
        return reverse.get(slot);
    }

    public void addKeybind(int code, AbilitySlot slot) {
        KeyBinding binding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.unicopia." + slot.name().toLowerCase(), code, KEY_CATEGORY));
        reverse.put(slot, binding);
        keys.put(binding, slot);
    }

    public void tick(MinecraftClient client) {
        if (client.currentScreen != null
            || client.player == null) {
            return;
        }
        Pony iplayer = Pony.of(client.player);

        for (KeyBinding i : keys.keySet()) {
            AbilitySlot slot = keys.get(i);
            if (slot == AbilitySlot.PRIMARY && client.options.keySneak.isPressed()) {
                slot = AbilitySlot.PASSIVE;
            }

            if (i.isPressed()) {
                if (pressed.add(i)) {
                    iplayer.getAbilities().activate(slot);
                }
            } else if (pressed.remove(i)) {
                iplayer.getAbilities().clear(slot);
            }
        }
    }
}
