package com.minelittlepony.unicopia.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class KeyBindingsHandler {
    private static final String KEY_CATEGORY = "unicopia.category.name";

    public static final KeyBindingsHandler INSTANCE = new KeyBindingsHandler();

    static void bootstrap() {}

    private final Map<KeyBinding, AbilitySlot> keys = new HashMap<>();
    private final Map<AbilitySlot, KeyBinding> reverse = new HashMap<>();

    private final KeyBinding pageDown = register(GLFW.GLFW_KEY_PAGE_DOWN, "hud_page_dn");
    private final KeyBinding pageUp = register(GLFW.GLFW_KEY_PAGE_UP, "hud_page_up");

    public long page = 0;

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
        KeyBinding binding = register(code, slot.name().toLowerCase());
        reverse.put(slot, binding);
        keys.put(binding, slot);
    }

    static KeyBinding register(int code, String name) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.unicopia." + name, code, KEY_CATEGORY));
    }

    public void tick(MinecraftClient client) {
        if (client.currentScreen != null
            || client.player == null) {
            return;
        }
        Pony iplayer = Pony.of(client.player);
        AbilityDispatcher abilities = iplayer.getAbilities();
        long maxPage = abilities.getMaxPage();

        page = MathHelper.clamp(page, 0, maxPage);

        if (page > 0 && checkPressed(pageDown) == PressedState.PRESSED) {
            changePage(client, maxPage, -1);
        } else if (page < maxPage && checkPressed(pageUp) == PressedState.PRESSED) {
            changePage(client, maxPage, 1);
        } else {
            for (KeyBinding i : keys.keySet()) {
                AbilitySlot slot = keys.get(i);
                if (slot == AbilitySlot.PRIMARY && client.options.keySneak.isPressed()) {
                    slot = AbilitySlot.PASSIVE;
                }

                PressedState state = checkPressed(i);
                if (state != PressedState.UNCHANGED) {
                    if (state == PressedState.PRESSED) {
                        abilities.activate(slot, page).map(Ability::getName).ifPresent(UHud.INSTANCE::setMessage);
                    } else {
                        abilities.clear(slot);
                    }
                }
            }
        }
    }

    private void changePage(MinecraftClient client, long max, int sigma) {
        page += sigma;
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.75F + (0.25F * sigma)));
        UHud.INSTANCE.setMessage(new TranslatableText("gui.unicopia.page_num", page, max));
    }

    private PressedState checkPressed(KeyBinding binding) {
        if (binding.isPressed()) {
            return pressed.add(binding) ? PressedState.PRESSED : PressedState.UNCHANGED;
        } else if (pressed.remove(binding)) {
            return PressedState.UNPRESSED;
        }

        return PressedState.UNCHANGED;
    }

    enum PressedState {
        UNCHANGED,
        PRESSED,
        UNPRESSED
    }
}
