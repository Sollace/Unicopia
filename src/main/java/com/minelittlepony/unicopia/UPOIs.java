package com.minelittlepony.unicopia;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.mixin.MixinPointOfInterestType;
import com.minelittlepony.unicopia.mixin.PointOfInterestTypesAccessor;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public interface UPOIs {
    Set<RegistryKey<PointOfInterestType>> CHEST_POINTS_OF_INTEREST = new HashSet<>();
    RegistryKey<PointOfInterestType> CHESTS = register(Unicopia.id("chests"), 1, 64, () -> {
        return Registries.BLOCK.getEntrySet().stream()
                .map(entry -> entry.getValue())
                .filter(b -> b instanceof AbstractChestBlock)
                .flatMap(block -> {
                    List<BlockState> states = block.getStateManager().getStates();
                    List<RegistryKey<PointOfInterestType>> existingTypes = states.stream()
                            .flatMap(state -> PointOfInterestTypes.getTypeForState(state).stream())
                            .flatMap(entry -> entry.getKey().stream())
                            .toList();

                    if (!existingTypes.isEmpty()) {
                        CHEST_POINTS_OF_INTEREST.addAll(existingTypes);
                        return Stream.empty();
                    }
                    return states.stream();
                })
                .distinct();
    });

    static RegistryKey<PointOfInterestType> register(Identifier id, int ticketCount, int searchDistance, Supplier<Stream<BlockState>> states) {
        PointOfInterestType type = PointOfInterestHelper.register(id, ticketCount, searchDistance, List.of());
        ((MixinPointOfInterestType)(Object)type).setStates(new HashSet<>());
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            if (type.blockStates().isEmpty()) {
                type.blockStates().addAll(states.get().collect(Collectors.toSet()));
                PointOfInterestTypesAccessor.registerStates(Registries.POINT_OF_INTEREST_TYPE.entryOf(CHESTS), type.blockStates());
            }
        });
        return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, id);
    }

    static boolean isChest(RegistryEntry<PointOfInterestType> type) {
        return type.getKey().filter(CHEST_POINTS_OF_INTEREST::contains).isPresent();
    }

    static void bootstrap() {
        CHEST_POINTS_OF_INTEREST.add(CHESTS);
        Registries.POINT_OF_INTEREST_TYPE.getEntrySet().forEach(poi -> {
            if (poi.getValue().blockStates().stream().anyMatch(state -> state.getBlock() instanceof ChestBlock)) {
                CHEST_POINTS_OF_INTEREST.add(poi.getKey());
            }
        });
        RegistryEntryAddedCallback.event(Registries.POINT_OF_INTEREST_TYPE).register((raw, key, value) -> {
            if (value.blockStates().stream().anyMatch(state -> state.getBlock() instanceof ChestBlock)) {
                CHEST_POINTS_OF_INTEREST.add(RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, key));
            }
        });
    }
}
