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
    Ability<?> TELEPORT = register(new UnicornTeleportAbility(), "teleport", AbilitySlot.SECONDARY);
    Ability<?> CAST = register(new UnicornCastingAbility(), "cast", AbilitySlot.PRIMARY);

    // earth / alicorn
    Ability<?> STOMP = register(new EarthPonyStompAbility(), "stomp", AbilitySlot.PRIMARY);
    Ability<?> GROW = register(new EarthPonyGrowAbility(), "grow", AbilitySlot.SECONDARY);

    // pegasus / bat / alicorn / changeling
    Ability<?> CARRY = register(new CarryAbility(), "carry", AbilitySlot.PASSIVE);

    // pegasus / alicorn
    Ability<?> CLOUD = register(new PegasusCloudInteractionAbility(), "cloud", AbilitySlot.TERTIARY);

    // changeling
    Ability<?> DISGUISE = register(new ChangelingDisguiseAbility(), "disguise", AbilitySlot.PRIMARY);
    Ability<?> FEED = register(new ChangelingFeedAbility(), "feed", AbilitySlot.SECONDARY);
    //Ability<?> TRAP = register(new ChangelingTrapAbility(), "trap", AbilitySlot.TERTIARY);

    static <T extends Ability<?>> T register(T power, String name, AbilitySlot slot) {
        Identifier id = new Identifier("unicopia", name);
        BY_SLOT.computeIfAbsent(slot, s -> new HashSet<>()).add(power);
        return Registry.register(REGISTRY, id, power);
    }
}
