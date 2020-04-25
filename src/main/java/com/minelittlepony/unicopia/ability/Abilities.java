package com.minelittlepony.unicopia.ability;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.SimpleRegistry;

public interface Abilities {
    Map<Identifier, Integer> KEYS_CODES = new HashMap<>();
    MutableRegistry<Ability<?>> REGISTRY = new SimpleRegistry<>();

    // unicorn / alicorn
    Ability<?> TELEPORT = register(new UnicornTeleportAbility(), "teleport", GLFW.GLFW_KEY_O);
    Ability<?> CAST = register(new UnicornCastingAbility(), "cast", GLFW.GLFW_KEY_P);

    // earth / alicorn
    Ability<?> GROW = register(new EarthPonyGrowAbility(), "grow", GLFW.GLFW_KEY_N);
    Ability<?> STOMP = register(new EarthPonyStompAbility(), "stomp", GLFW.GLFW_KEY_M);

    // pegasus / bat / alicorn / changeling
    Ability<?> CARRY = register(new CarryAbility(), "carry", GLFW.GLFW_KEY_K);

    // pegasus / alicorn
    Ability<?> CLOUD = register(new PegasusCloudInteractionAbility(), "cloud", GLFW.GLFW_KEY_J);

    // changeling
    Ability<?> FEED = register(new ChangelingFeedAbility(), "feed", GLFW.GLFW_KEY_O);
    Ability<?> TRAP = register(new ChangelingTrapAbility(), "trap", GLFW.GLFW_KEY_L);

    Ability<?> DISGUISE = register(new ChangelingDisguiseAbility(), "disguise", GLFW.GLFW_KEY_P);

    static <T extends Ability<?>> T register(T power, String name, int keyCode) {
        Identifier id = new Identifier("unicopia", name);
        KEYS_CODES.put(id, keyCode);
        return REGISTRY.add(id, power);
    }
}
