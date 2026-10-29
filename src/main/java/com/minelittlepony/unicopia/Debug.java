package com.minelittlepony.unicopia;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.particle.ParticleSource;

import net.minecraft.block.WoodType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.registry.tag.TagKey;
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
            testTraitCoverage(ItemTagPool.ofRegistry(Registries.ITEM), Unicopia.LOGGER, entry -> SpellTraits.of(entry.registryKey()).isEmpty());
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

    static void testTraitCoverage(ItemTagPool<?> tagPool, Logger logger, Predicate<RegistryEntry.Reference<Item>> traitsCheck) {
        record Category(long total, List<RegistryEntry<Item>> unregistered) {}
        tagPool.streamEntries()
                .collect(Collectors.groupingBy(
                        entry -> entry.registryKey().getValue().getNamespace(),
                        Collectors.teeing(
                                Collectors.counting(),
                                Collectors.filtering(entry -> !tagPool.isIn(UTags.Items.HAS_NO_TRAITS, entry) && !traitsCheck.test(entry), Collectors.toList()),
                                Category::new
                        )
                )).forEach((namespace, entries) -> {
            if (!entries.unregistered().isEmpty()) {
                String unregistered = entries.unregistered().stream()
                        .map(entry -> entry.value() + "(" + tagPool.stream()
                                .filter(i -> i.contains(entry))
                                .map(i -> i.getTagKey().orElseThrow().id().toString())
                                .collect(Collectors.joining(", ")) +  ")")
                        .collect(Collectors.joining(",\r\n"));
                logger.warn("No traits registered for {} out of {} items in namespace {} {}", entries.unregistered().size(), entries.total(), namespace, unregistered);
            }
        });
    }

    public interface ItemTagPool<T extends RegistryEntryList<Item>> {
        static ItemTagPool<RegistryEntryList.Named<Item>> ofRegistry(Registry<Item> registry) {
            return new ItemTagPool<>() {
                @Override
                public Stream<Named<Item>> stream() {
                    return registry.getTags();
                }

                @Override
                public Optional<Named<Item>> get(TagKey<Item> key) {
                    return registry.getOptional(key);
                }

                @Override
                public Stream<RegistryEntry.Reference<Item>> streamEntries() {
                    return registry.streamEntries();
                }

                @Override
                public RegistryEntry.Reference<Item> get(RegistryKey<Item> key) {
                    return registry.getOrThrow(key);
                }
            };
        }

        Stream<RegistryEntry.Reference<Item>> streamEntries();

        Stream<T> stream();

        RegistryEntry.Reference<Item> get(RegistryKey<Item> key);

        Optional<T> get(TagKey<Item> key);

        default boolean isIn(TagKey<Item> key, RegistryEntry<Item> item) {
            return get(key).map(i -> i.contains(item)).isPresent();
        }
    }
}
