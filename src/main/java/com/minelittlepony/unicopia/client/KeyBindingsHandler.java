package com.minelittlepony.unicopia.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

class KeyBindingsHandler {
    private final String KEY_CATEGORY = "unicopia.category.name";

    private final Map<KeyBinding, List<Ability<? extends Hit>>> keyPools = new HashMap<>();

    private final Set<KeyBinding> bindings = new HashSet<>();

    private final Set<KeyBinding> pressed = new HashSet<>();

    private Collection<Ability<?>> getKeyCodePool(KeyBinding keyCode) {
        return keyPools.computeIfAbsent(keyCode, i -> new ArrayList<>());
    }

    public void addKeybind(Ability<?> p) {
        KeyBindingRegistry.INSTANCE.addCategory(KEY_CATEGORY);

        Identifier id = Abilities.REGISTRY.getId(p);
        int code = Abilities.KEYS_CODES.get(id);

        FabricKeyBinding b = FabricKeyBinding.Builder.create(id, InputUtil.Type.KEYSYM, code, KEY_CATEGORY).build();
        KeyBindingRegistry.INSTANCE.register(b);
        getKeyCodePool(b).add(p);
        bindings.add(b);
    }

    public void tick(MinecraftClient client) {
        if (client.currentScreen != null
            || client.player == null) {
            return;
        }
        Pony iplayer = Pony.of(client.player);

        for (KeyBinding i : bindings) {
            if (i.isPressed()) {

                if (pressed.add(i)) {
                    getKeyCodePool(i)
                        .stream()
                        .filter(power -> power.canUse(iplayer.getSpecies()))
                        .findFirst()
                        .ifPresent(iplayer.getAbilities()::tryUseAbility);
                }
            } else if (pressed.remove(i)) {
                iplayer.getAbilities().tryClearAbility();
            }
        }
    }
}
