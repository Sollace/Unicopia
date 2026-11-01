package com.minelittlepony.unicopia.ability;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.RegistryUtils;

import net.minecraft.util.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public interface Abilities {
    Registry<Ability<?>> REGISTRY = RegistryUtils.createSynced(Unicopia.key("abilities"));
    PacketCodec<RegistryByteBuf, Ability<?>> PACKET_CODEC = PacketCodecs.registryValue(REGISTRY.getKey());
    Map<AbilitySlot, Set<Ability<?>>> BY_SLOT = new EnumMap<>(AbilitySlot.class);
    BiFunction<AbilitySlot, Race.Composite, List<Ability<?>>> BY_SLOT_AND_COMPOSITE_RACE = Util.memoize((slot, race) -> {
        return BY_SLOT.computeIfAbsent(slot, s -> new LinkedHashSet<>())
                .stream()
                .filter(a -> a.canUse(race))
                .toList();
    });

    // all races
    RegistryEntry<Ability<?>> CHANGE_FORM = register(new ChangeFormAbility(), "change_form", AbilitySlot.PRIMARY);

    // unicorn / alicorn
    RegistryEntry<Ability<?>> CAST = register(new UnicornCastingAbility(), "cast", AbilitySlot.PRIMARY);
    RegistryEntry<Ability<?>> SHOOT = register(new UnicornProjectileAbility(), "shoot", AbilitySlot.PRIMARY);
    RegistryEntry<Ability<?>> TIME = register(new TimeChangeAbility(), "time_control", AbilitySlot.SECONDARY);
    RegistryEntry<Ability<?>> TELEPORT = register(new UnicornTeleportAbility(), "teleport", AbilitySlot.SECONDARY);
    RegistryEntry<Ability<?>> GROUP_TELEPORT = register(new UnicornGroupTeleportAbility(), "teleport_group", AbilitySlot.SECONDARY);
    RegistryEntry<Ability<?>> TELEKINESIS = register(new UnicornTelekinesisAbility(), "telekinesis", AbilitySlot.TERTIARY);
    RegistryEntry<Ability<?>> DISPELL = register(new UnicornDispellAbility(), "dispell", AbilitySlot.TERTIARY);

    // earth / alicorn
    RegistryEntry<Ability<?>> KICK = register(new EarthPonyKickAbility(), "kick", AbilitySlot.PRIMARY);
    RegistryEntry<Ability<?>> GROW = register(new EarthPonyGrowAbility(), "grow", AbilitySlot.SECONDARY);
    RegistryEntry<Ability<?>> STOMP = register(new EarthPonyStompAbility(), "stomp", AbilitySlot.TERTIARY);
    RegistryEntry<Ability<?>> HUG = register(new HugAbility(), "hug", AbilitySlot.TERTIARY);

    // pegasus
    RegistryEntry<Ability<?>> RAINBOOM = register(new PegasusRainboomAbility(), "rainboom", AbilitySlot.PRIMARY);
    RegistryEntry<Ability<?>> CAPTURE_CLOUD = register(new PegasusCaptureStormAbility(), "capture_cloud", AbilitySlot.SECONDARY);

    // hippogriff
    RegistryEntry<Ability<?>> DASH = register(new FlyingDashAbility(), "dash", AbilitySlot.PRIMARY);
    RegistryEntry<Ability<?>> SCREECH = register(new ScreechAbility(), "screech", AbilitySlot.SECONDARY);
    RegistryEntry<Ability<?>> PECK = register(new PeckAbility(), "peck", AbilitySlot.SECONDARY);

    // pegasus / bat / alicorn / changeling / hippogriff
    RegistryEntry<Ability<?>> CARRY = register(new CarryAbility(), "carry", AbilitySlot.PRIMARY);
    RegistryEntry<Ability<?>> TOGGLE_FLIGHT = register(new ToggleFlightAbility(), "toggle_flight", AbilitySlot.TERTIARY);

    // changeling
    RegistryEntry<Ability<?>> DISGUISE = register(new ChangelingDisguiseAbility(), "disguise", AbilitySlot.SECONDARY);
    RegistryEntry<Ability<?>> FEED = register(new ChangelingFeedAbility(), "feed", AbilitySlot.SECONDARY);

    // bat
    RegistryEntry<Ability<?>> HANG = register(new BatPonyHangAbility(), "hang", AbilitySlot.TERTIARY);
    RegistryEntry<Ability<?>> EEEE = register(new BatEeeeAbility(), "eee", AbilitySlot.SECONDARY);

    // kirin
    RegistryEntry<Ability<?>> RAGE = register(new KirinRageAbility(), "rage", AbilitySlot.PRIMARY);
    RegistryEntry<Ability<?>> NIRIK_BLAST = register(new NirikBlastAbility(), "nirik_blast", AbilitySlot.SECONDARY);
    RegistryEntry<Ability<?>> KIRIN_CAST = register(new KirinCastingAbility(), "kirin_cast", AbilitySlot.SECONDARY);

    // seapony
    RegistryEntry<Ability<?>> SONAR_PULSE = register(new SeaponySonarPulseAbility(), "sonar_pulse", AbilitySlot.SECONDARY);

    static <T extends Ability<?>> RegistryEntry<Ability<?>> register(T power, String name, AbilitySlot slot) {
        return register(power, Unicopia.id(name), slot);
    }

    static <T extends Ability<?>> RegistryEntry<Ability<?>> register(T power, Identifier id, AbilitySlot slot) {
        BY_SLOT.computeIfAbsent(slot, s -> new LinkedHashSet<>()).add(power);
        return Registry.registerReference(REGISTRY, id, power);
    }

    static void bootstrap() {}
}
