package com.minelittlepony.unicopia.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.lwjgl.glfw.GLFW;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.ability.ActivationType;
import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class KeyBindingsHandler {
    private static final String KEY_CATEGORY = "unicopia.category.name";

    public static final KeyBindingsHandler INSTANCE = new KeyBindingsHandler();

    static void bootstrap() {}

    private final Map<Binding, AbilitySlot> keys = new HashMap<>();
    private final Map<AbilitySlot, Binding> reverse = new HashMap<>();

    private final Binding pageDown = new Binding(create(GLFW.GLFW_KEY_PAGE_DOWN, "hud_page_dn"));
    private final Binding pageUp = new Binding(create(GLFW.GLFW_KEY_PAGE_UP, "hud_page_up"));

    private final KeyBinding singleTapModifier = createSticky(InputUtil.UNKNOWN_KEY.getCode(), "ability_modifier_tap");
    private final KeyBinding doubleTapModifier = createSticky(InputUtil.UNKNOWN_KEY.getCode(), "ability_modifier_double_tap");
    private final KeyBinding tripleTapModifier = createSticky(InputUtil.UNKNOWN_KEY.getCode(), "ability_modifier_triple_tap");

    private final Set<KeyBinding> pressed = new HashSet<>();

    public KeyBindingsHandler() {
        addKeybind(GLFW.GLFW_KEY_R, AbilitySlot.PRIMARY);
        addKeybind(GLFW.GLFW_KEY_G, AbilitySlot.SECONDARY);
        addKeybind(GLFW.GLFW_KEY_V, AbilitySlot.TERTIARY);
    }

    public Binding getBinding(AbilitySlot slot) {
        return reverse.get(slot);
    }

    public boolean isToggleMode() {
        return Unicopia.getConfig().toggleAbilityKeys.get();
    }

    public ActivationType getForcedActivationType() {
        if (singleTapModifier.isPressed()) {
            return ActivationType.TAP;
        }

        if (doubleTapModifier.isPressed()) {
            return ActivationType.DOUBLE_TAP;
        }

        if (tripleTapModifier.isPressed()) {
            return ActivationType.TRIPLE_TAP;
        }

        return ActivationType.NONE;
    }

    public void addKeybind(int code, AbilitySlot slot) {
        Binding binding = new Binding(createSticky(code, slot.name().toLowerCase(Locale.ROOT)));
        reverse.put(slot, binding);
        keys.put(binding, slot);
    }

    KeyBinding create(int code, String name) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.unicopia." + name, code, KEY_CATEGORY));
    }

    KeyBinding createSticky(int code, String name) {
        return KeyBindingHelper.registerKeyBinding(new StickyKeyBinding("key.unicopia." + name, code, KEY_CATEGORY, this::isToggleMode));
    }

    public void tick(MinecraftClient client) {
        if (client.currentScreen != null
            || client.player == null) {
            return;
        }
        Pony iplayer = Pony.of(client.player);
        AbilityDispatcher abilities = iplayer.getAbilities();
        int maxPage = abilities.getMaxPage();

        int page = MathHelper.clamp(Unicopia.getConfig().hudPage.get(), 0, maxPage);

        if (page > 0 && pageDown.getState() == PressedState.PRESSED) {
            changePage(client, maxPage, -1);
        } else if (page < maxPage && pageUp.getState() == PressedState.PRESSED) {
            changePage(client, maxPage, 1);
        } else if (!client.player.isSpectator()) {
            for (Binding i : keys.keySet()) {
                AbilitySlot slot = keys.get(i);
                if (slot == AbilitySlot.PRIMARY && client.options.sneakKey.isPressed() && abilities.isFilled(AbilitySlot.PASSIVE)) {
                    slot = AbilitySlot.PASSIVE;
                }
                if (slot == AbilitySlot.PRIMARY && !abilities.isFilled(slot)) {
                    slot = AbilitySlot.PASSIVE;
                }

                PressedState state = i.getState();

                if (state != PressedState.UNCHANGED) {
                    if (state == PressedState.PRESSED) {
                        abilities.activate(slot, page).map(a -> a.getName(iplayer)).ifPresent(UHud.INSTANCE::setMessage);
                    } else {
                        abilities.clear(slot, ActivationType.NONE, page);
                    }
                } else {
                    ActivationType type = i.getType();
                    if (type.isResult()) {
                        abilities.clear(slot, type, page);
                    }
                }
            }
        }
    }

    private void changePage(MinecraftClient client, long max, int sigma) {
        int page = Unicopia.getConfig().hudPage.get();
        page += sigma;
        Unicopia.getConfig().hudPage.set(page);
        Unicopia.getConfig().save();
        client.getSoundManager().play(PositionedSoundInstance.master(USounds.Vanilla.UI_BUTTON_CLICK, 1.75F + (0.25F * sigma)));
        UHud.INSTANCE.setMessage(Text.translatable("gui.unicopia.page_num", page + 1, max + 1));
    }

    public class Binding {
        private final KeyBinding binding;

        private long nextPhaseTime;

        private final AtomicReference<ActivationType> type = new AtomicReference<>(ActivationType.NONE);

        Binding(KeyBinding binding) {
            this.binding = binding;
        }

        public Text getLabel() {
            return binding.getBoundKeyLocalizedText();
        }

        public PressedState getState() {
            PressedState state = getNewState();

            long now = System.currentTimeMillis();

            if (state == PressedState.PRESSED) {
                nextPhaseTime = now + 200;
            }

            if (state == PressedState.RELEASED && now < nextPhaseTime + 10) {
                nextPhaseTime = now + 200;
                type.set(type.get().getNext());
            }

            return state;
        }

        public ActivationType getType() {
            if (binding.isPressed()) {
                ActivationType t = getForcedActivationType();
                if (t.isResult()) {
                    KeyBinding.untoggleStickyKeys();
                    return t;
                }
            }

            if (!isToggleMode() && System.currentTimeMillis() > nextPhaseTime - 70) {
                return type.getAndSet(ActivationType.NONE);
            }

            return ActivationType.NONE;
        }

        private PressedState getNewState() {
            if (binding.isPressed()) {
                return pressed.add(binding) ? PressedState.PRESSED : PressedState.UNCHANGED;
            } else if (pressed.remove(binding)) {
                return PressedState.RELEASED;
            }

            return PressedState.UNCHANGED;
        }
    }

    enum PressedState {
        UNCHANGED,
        PRESSED,
        RELEASED
    }
}
