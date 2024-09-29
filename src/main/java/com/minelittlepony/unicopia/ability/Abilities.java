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

public interface Abilities {
    Registry<Ability<?>> REGISTRY = RegistryUtils.createSimple(Unicopia.id("abilities"));
    PacketCodec<RegistryByteBuf, Ability<?>> PACKET_CODEC = PacketCodecs.registryValue(REGISTRY.getKey());
    Map<AbilitySlot, Set<Ability<?>>> BY_SLOT = new EnumMap<>(AbilitySlot.class);
    BiFunction<AbilitySlot, Race.Composite, List<Ability<?>>> BY_SLOT_AND_COMPOSITE_RACE = Util.memoize((slot, race) -> {
        return BY_SLOT.computeIfAbsent(slot, s -> new LinkedHashSet<>())
                .stream()
                .filter(a -> a.canUse(race))
                .toList();
    });

    // all races
    Ability<?> CHANGE_FORM = register(new ChangeFormAbility(), "change_form", AbilitySlot.PRIMARY);

    // unicorn / alicorn
    Ability<?> CAST = register(new UnicornCastingAbility(), "cast", AbilitySlot.PRIMARY);
    Ability<?> SHOOT = register(new UnicornProjectileAbility(), "shoot", AbilitySlot.PRIMARY);
    Ability<?> TIME = register(new TimeChangeAbility(), "time_control", AbilitySlot.SECONDARY);
    Ability<?> TELEPORT = register(new UnicornTeleportAbility(), "teleport", AbilitySlot.SECONDARY);
    Ability<?> GROUP_TELEPORT = register(new UnicornGroupTeleportAbility(), "teleport_group", AbilitySlot.SECONDARY);
    Ability<?> DISPELL = register(new UnicornDispellAbility(), "dispell", AbilitySlot.TERTIARY);

    // earth / alicorn
    Ability<?> KICK = register(new EarthPonyKickAbility(), "kick", AbilitySlot.PRIMARY);
    Ability<?> GROW = register(new EarthPonyGrowAbility(), "grow", AbilitySlot.SECONDARY);
    Ability<?> STOMP = register(new EarthPonyStompAbility(), "stomp", AbilitySlot.TERTIARY);
    Ability<?> HUG = register(new HugAbility(), "hug", AbilitySlot.TERTIARY);

    // pegasus
    Ability<?> RAINBOOM = register(new PegasusRainboomAbility(), "rainboom", AbilitySlot.PRIMARY);
    Ability<?> CAPTURE_CLOUD = register(new PegasusCaptureStormAbility(), "capture_cloud", AbilitySlot.SECONDARY);

    // hippogriff
    Ability<?> DASH = register(new FlyingDashAbility(), "dash", AbilitySlot.PRIMARY);
    Ability<?> SCREECH = register(new ScreechAbility(), "screech", AbilitySlot.SECONDARY);
    Ability<?> PECK = register(new PeckAbility(), "peck", AbilitySlot.SECONDARY);

    // pegasus / bat / alicorn / changeling / hippogriff
    Ability<?> CARRY = register(new CarryAbility(), "carry", AbilitySlot.PRIMARY);
    Ability<?> TOGGLE_FLIGHT = register(new ToggleFlightAbility(), "toggle_flight", AbilitySlot.TERTIARY);

    // changeling
    Ability<?> DISGUISE = register(new ChangelingDisguiseAbility(), "disguise", AbilitySlot.SECONDARY);
    Ability<?> FEED = register(new ChangelingFeedAbility(), "feed", AbilitySlot.SECONDARY);

    // bat
    Ability<?> HANG = register(new BatPonyHangAbility(), "hang", AbilitySlot.TERTIARY);
    Ability<?> EEEE = register(new BatEeeeAbility(), "eee", AbilitySlot.SECONDARY);

    // kirin
    Ability<?> RAGE = register(new KirinRageAbility(), "rage", AbilitySlot.PRIMARY);
    Ability<?> NIRIK_BLAST = register(new NirikBlastAbility(), "nirik_blast", AbilitySlot.SECONDARY);
    Ability<?> KIRIN_CAST = register(new KirinCastingAbility(), "kirin_cast", AbilitySlot.SECONDARY);

    // seapony
    Ability<?> SONAR_PULSE = register(new SeaponySonarPulseAbility(), "sonar_pulse", AbilitySlot.SECONDARY);

    static <T extends Ability<?>> T register(T power, String name, AbilitySlot slot) {
        Identifier id = Unicopia.id(name);
        BY_SLOT.computeIfAbsent(slot, s -> new LinkedHashSet<>()).add(power);
        return Registry.register(REGISTRY, id, power);
    }

    static void bootstrap() {}
}
