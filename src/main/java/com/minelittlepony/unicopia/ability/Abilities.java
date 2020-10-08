package com.minelittlepony.unicopia.ability;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface Abilities {
    Map<AbilitySlot, Set<Ability<?>>> BY_SLOT = new EnumMap<>(AbilitySlot.class);
    Registry<Ability<?>> REGISTRY = Registries.createSimple(new Identifier("unicopia", "abilities"));

    // unicorn / alicorn
    Ability<?> CAST = register(new UnicornCastingAbility(), "cast", AbilitySlot.PRIMARY);
    Ability<?> TELEPORT = register(new UnicornTeleportAbility(), "teleport", AbilitySlot.SECONDARY);
    Ability<?> SHOOT = register(new UnicornProjectileAbility(), "shoot", AbilitySlot.TERTIARY);

    // earth / alicorn
    Ability<?> GROW = register(new EarthPonyGrowAbility(), "grow", AbilitySlot.SECONDARY);
    Ability<?> STOMP = register(new EarthPonyStompAbility(), "stomp", AbilitySlot.TERTIARY);

    // pegasus / bat / alicorn / changeling
    Ability<?> CARRY = register(new CarryAbility(), "carry", AbilitySlot.PASSIVE);

    // changeling
    Ability<?> DISGUISE = register(new ChangelingDisguiseAbility(), "disguise", AbilitySlot.SECONDARY);
    Ability<?> FEED = register(new ChangelingFeedAbility(), "feed", AbilitySlot.TERTIARY);

    // bat
    Ability<?> HANG = register(new BatPonyHangAbility(), "hang", AbilitySlot.SECONDARY);
    Ability<?> EEEE = register(new BatEeeeAbility(), "eee", AbilitySlot.TERTIARY);

    static <T extends Ability<?>> T register(T power, String name, AbilitySlot slot) {
        Identifier id = new Identifier("unicopia", name);
        BY_SLOT.computeIfAbsent(slot, s -> new HashSet<>()).add(power);
        return Registry.register(REGISTRY, id, power);
    }
}
