package com.minelittlepony.unicopia;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

public interface Debug {
    boolean SPELLBOOK_CHAPTERS = Boolean.getBoolean("unicopia.debug.spellbookChapters");
    boolean CHECK_GAME_VALUES = Boolean.getBoolean("unicopia.debug.checkGameValues");
    boolean CHECK_TRAIT_COVERAGE = Boolean.getBoolean("unicopia.debug.checkTraitCoverage");

    AtomicReference<World> LAST_TESTED_WORLD = new AtomicReference<>(null);

    static void runTests(World world) {
        if (!CHECK_GAME_VALUES || !world.getDimensionKey().getValue().equals(DimensionTypes.OVERWORLD_ID) || (LAST_TESTED_WORLD.getAndSet(world) == world)) {
            return;
        }

        if (CHECK_TRAIT_COVERAGE) {
            testTraitCoverage();
        }

        try {
            for (var type : BoatEntity.Type.values()) {
                var balloon = UEntities.AIR_BALLOON.create(world);
                balloon.setBasketType(AirBalloonEntity.BasketType.of(type));
                balloon.asItem();
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Tests failed", t);
        }
    }

    private static void testTraitCoverage() {
        Registries.ITEM.getEntrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().getValue().getNamespace(),
                Set::of,
                Sets::union
        )).forEach((namespace, entries) -> {
            @SuppressWarnings("deprecation")
            var unregistered = entries.stream()
                .filter(entry -> !entry.getValue().getRegistryEntry().isIn(UTags.Items.HAS_NO_TRAITS) && SpellTraits.of(entry.getValue()).isEmpty())
                .map(entry -> {
                    String id = entry.getKey().getValue().toString();

                    return id + "(" + Registries.ITEM.streamTags()
                        .filter(entry.getValue().getRegistryEntry()::isIn)
                        .map(TagKey::id)
                        .map(Identifier::toString)
                        .collect(Collectors.joining(", ")) +  ")";
                })
                .toList();

            if (!unregistered.isEmpty()) {
                Unicopia.LOGGER.warn("No traits registered for {} items in namepsace {} {}", unregistered.size(), namespace, String.join(",\r\n", unregistered));
            }
        });
    }
}
