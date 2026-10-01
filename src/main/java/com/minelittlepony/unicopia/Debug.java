package com.minelittlepony.unicopia;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.particle.ParticleSource;

import net.minecraft.block.WoodType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

public interface Debug {
    boolean SPELLBOOK_CHAPTERS = Boolean.getBoolean("unicopia.debug.spellbookChapters");
    boolean CHECK_GAME_VALUES = Boolean.getBoolean("unicopia.debug.checkGameValues");
    boolean CHECK_TRAIT_COVERAGE = Boolean.getBoolean("unicopia.debug.checkTraitCoverage");

    AtomicReference<World> LAST_TESTED_WORLD = new AtomicReference<>(null);

    static void runTests(World world) {
        if (!CHECK_GAME_VALUES || !world.getDimensionEntry().matchesKey(DimensionTypes.OVERWORLD) || (LAST_TESTED_WORLD.getAndSet(world) == world)) {
            return;
        }

        if (CHECK_TRAIT_COVERAGE) {
            testTraitCoverage(Registries.ITEM, Unicopia.LOGGER, entry -> SpellTraits.of(entry.registryKey()).isEmpty());
        }

        try {
            for (var type : WoodType.stream().toList()) {
                var balloon = UEntities.AIR_BALLOON.create(world, SpawnReason.SPAWN_ITEM_USE);
                balloon.setBasketType(AirBalloonEntity.BasketType.of(type));
                balloon.asItem();
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Tests failed", t);
        }
    }

    static void drawBoxSelection(ParticleSource<?> pony, ParticleEffect particle, BlockPos pos) {
        if (CHECK_GAME_VALUES) {
            float amount = pony.asWorld().random.nextFloat();

            pony.addParticle(particle, new Vec3d(pos.getX() + amount, pos.getY(), pos.getZ()), Vec3d.ZERO);
            pony.addParticle(particle, new Vec3d(pos.getX(), pos.getY(), pos.getZ() + amount), Vec3d.ZERO);

            pony.addParticle(particle, new Vec3d(pos.getX() + 1, pos.getY(), pos.getZ() + amount), Vec3d.ZERO);
            pony.addParticle(particle, new Vec3d(pos.getX() + amount, pos.getY(), pos.getZ() + 1), Vec3d.ZERO);
        }
    }

    static void testTraitCoverage(RegistryWrapper<Item> registry, Logger logger, Predicate<RegistryEntry.Reference<Item>> traitsCheck) {
        registry.streamEntries().collect(Collectors.toMap(
                entry -> entry.registryKey().getValue().getNamespace(),
                Set::of,
                Sets::union
        )).forEach((namespace, entries) -> {
            List<String> unregistered = entries.stream()
                .filter(entry -> !entry.isIn(UTags.Items.HAS_NO_TRAITS) && !traitsCheck.test(entry))
                .map(entry -> {
                    String id = entry.value().toString();

                    return id + "(" + registry.getTags()
                        .filter(i -> i.contains(entry))
                        .map(i -> i.getTag().id().toString())
                        .collect(Collectors.joining(", ")) +  ")";
                })
                .toList();

            if (!unregistered.isEmpty()) {
                logger.warn("No traits registered for {} items in namepsace {} {}", unregistered.size(), namespace, String.join(",\r\n", unregistered));
            }
        });
    }
}
