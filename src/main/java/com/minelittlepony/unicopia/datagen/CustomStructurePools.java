package com.minelittlepony.unicopia.datagen;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.gen.CaveCarvingStructureProcessor;
import com.minelittlepony.unicopia.server.world.gen.SurfaceGrowthStructureProcessor;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePool.Projection;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.structure.processor.StructureProcessorRule;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import net.minecraft.util.Identifier;

final class CustomStructurePools {
    public static final RegistryKey<StructurePool> ALTAR_PILLAR = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "altar_pillar");
    public static final RegistryKey<StructurePool> ALTAR = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "altar");
    public static final RegistryKey<StructurePool> CLOUDS_START = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "clouds/start");
    public static final RegistryKey<StructurePool> CLOUDS_PILLARS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "clouds/pillars");
    public static final RegistryKey<StructurePool> CLOUDS_DECORATIONS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "clouds/decorations");
    public static final RegistryKey<StructurePool> CLOUDS_NATURAL = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "clouds/natural");
    public static final RegistryKey<StructurePool> CLOUDS_HOUSES = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "clouds/houses");

    public static final RegistryKey<StructurePool> CHANGELING_HIVE_START = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/start");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_SHAFT_TERMINATIONS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/shaft_terminations");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_TUNNEL_TERMINATIONS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/tunnel_terminations");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_SHAFTS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/shafts");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_MAIN_ENTRANCE_SHAFTS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/main_entrance_shafts");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_TUNNELS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/tunnels");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_SIDE_PASSAGES = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/side_passages");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_CHAMBER_DECORS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/chamber_decors");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_PIT_DECORS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/pit_decors");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_ROOF_ADDITIONS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/roof_additions");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_ROOF_ADDITIONS_TERMINATION = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/roof_additions_termination");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_FLOOR_ADDITIONS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/floor_additions");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_FLOOR_ADDITIONS_TERMINATION = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/floor_additions_termination");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_HIDDEN_ENTRANCES = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/hidden_entrances");
    public static final RegistryKey<StructurePool> CHANGELING_HIVE_HIDDEN_ENTRANCE_TERMINATIONS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "changeling_hive/hidden_entrance_terminations");

    public static final RegistryKey<StructurePool> CLOUD_ISLAND_LARGE_START = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "cloud_island/large/b");
    public static final RegistryKey<StructurePool> CLOUD_ISLANDS_BLOBS = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "cloud_islands/blobs");
    public static final RegistryKey<StructurePool> CLOUD_ISLANDS_SURFACES = Unicopia.key(RegistryKeys.TEMPLATE_POOL, "cloud_islands/surfaces");

    public static final RegistryKey<StructureProcessorList> CLOUD_DECAY = Unicopia.key(RegistryKeys.PROCESSOR_LIST, "cloud_decay");
    public static final RegistryKey<StructureProcessorList> CHANGELING_HIVE_DECAY = Unicopia.key(RegistryKeys.PROCESSOR_LIST, "changeling_hive_decay");
    public static final RegistryKey<StructureProcessorList> CHANGELING_HIVE_ENTRANCE_DECAY = Unicopia.key(RegistryKeys.PROCESSOR_LIST, "changeling_hive_entrance_decay");
    public static final RegistryKey<StructureProcessorList> CHANGELING_HIVE_SURFACING = Unicopia.key(RegistryKeys.PROCESSOR_LIST, "changeling_hive_surfacing");

    static void bootstrapPools(Registerable<StructurePool> registerable) {
        var empty = registerable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL).getOrThrow(StructurePools.EMPTY);
        registerable.register(ALTAR_PILLAR, new StructurePool(empty, explode(0, 6, i -> ofSingle("altar/pillar/pillar_" + i)).toList(), Projection.RIGID));
        registerable.register(ALTAR, new StructurePool(empty, List.of(ofSingle("altar/center_1")), Projection.RIGID));
        registerCloudStructures(registerable);
        registerAbandonedChangelingHives(registerable);
    }

    private static void registerCloudStructures(Registerable<StructurePool> registerable) {
        var pools = registerable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        var empty = pools.getOrThrow(StructurePools.EMPTY);
        var processors = registerable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        registerable.register(CLOUDS_START, new StructurePool(empty, Streams.concat(
                explode(1, 3, i -> ofSingle("clouds/small_" + i)),
                explode(1, 2, i -> ofSingle("clouds/medium_" + i)),
                explode(1, 3, i -> ofSingle("clouds/large_" + i))
        ).toList(), Projection.RIGID));
        List<Pair<Function<Projection, ? extends StructurePoolElement>, Integer>> pillars = explode(1, 4, i -> ofSingle("clouds/pillars/pillar_" + i)).toList();
        registerable.register(CLOUDS_PILLARS, new StructurePool(empty, pillars, Projection.RIGID));
        registerable.register(CLOUDS_DECORATIONS, new StructurePool(empty, Stream.concat(
                pillars.stream(),
                Stream.of(ofSingle("clouds/pillars/well")
        )).toList(), Projection.RIGID));
        registerable.register(CLOUDS_HOUSES, new StructurePool(empty, explode(1, 3, i -> ofSingle("clouds/village/house_" + i, i == 1 ? 4 : i == 2 ? 6 : 1)).toList(), Projection.RIGID));

        var cloudDecay = processors.getOrThrow(CLOUD_DECAY);
        registerable.register(CLOUDS_NATURAL, new StructurePool(empty, Streams.concat(
                explode(1, 3, i -> ofSingle("clouds/small_" + i, cloudDecay)),
                explode(1, 2, i -> ofSingle("clouds/medium_" + i, cloudDecay)),
                explode(1, 3, i -> ofSingle("clouds/large_" + i, cloudDecay)),
                explode(1, 5, i -> ofSingle("clouds/junction_" + i, cloudDecay)),
                Stream.of(ofSingle("clouds/pillar_boat", cloudDecay))
        ).toList(), StructurePool.Projection.RIGID));
        registerable.register(CLOUD_ISLANDS_BLOBS, new StructurePool(empty, List.of(ofSingle("cloud_islands/blobs/blob_1", cloudDecay)), Projection.RIGID));
        registerable.register(CLOUD_ISLANDS_SURFACES, new StructurePool(empty, explode(1, 3, i -> ofSingle("cloud_islands/surfaces/surface_" + i, cloudDecay)).toList(), Projection.RIGID));
        "abcdefg".chars().forEach(i -> {
            char c = (char)i;
            registerable.register(
                    Unicopia.key(RegistryKeys.TEMPLATE_POOL, "cloud_island/large/" + c),
                    new StructurePool(empty, List.of(ofSingle("cloud_islands/continent/" + c, cloudDecay)), Projection.RIGID)
            );
        });
    }

    private static void registerAbandonedChangelingHives(Registerable<StructurePool> registerable) {
        var pools = registerable.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        var empty = pools.getOrThrow(StructurePools.EMPTY);
        var processors = registerable.getRegistryLookup(RegistryKeys.PROCESSOR_LIST);
        var hiveDecay = processors.getOrThrow(CHANGELING_HIVE_DECAY);
        var hiveSurfacing = processors.getOrThrow(CHANGELING_HIVE_SURFACING);
        var shaftTerminations = registerable.register(CHANGELING_HIVE_SHAFT_TERMINATIONS, new StructurePool(empty, List.of(
                ofSingle("changeling_hive/termination/shaft1", hiveDecay)
        ), Projection.RIGID));
        var tunnelTerminations = registerable.register(CHANGELING_HIVE_TUNNEL_TERMINATIONS, new StructurePool(empty, List.of(
                ofSingle("changeling_hive/termination/tunnel1", hiveDecay),
                ofSingle("changeling_hive/spiked/termination/tunnel1", hiveDecay),
                ofSingle("changeling_hive/termination1", hiveDecay),
                ofSingle("changeling_hive/termination/blockade1", hiveDecay),
                ofSingle("changeling_hive/slimey/termination/tunnel1", hiveDecay)
        ), Projection.RIGID));

        var hiveEntranceDecay = processors.getOrThrow(CHANGELING_HIVE_ENTRANCE_DECAY);
        registerable.register(CHANGELING_HIVE_START, new StructurePool(shaftTerminations, List.of(
                ofSingle("changeling_hive/entrance", hiveEntranceDecay),
                ofSingle("changeling_hive/spiked/entrance", hiveEntranceDecay),
                ofSingle("changeling_hive/slimey/entrance", hiveEntranceDecay),
                ofSingle("changeling_hive/entrance_large_upper", hiveEntranceDecay, 3)
        ), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_SHAFTS, new StructurePool(shaftTerminations, List.of(
                ofSingle("changeling_hive/shaft1", hiveDecay), ofSingle("changeling_hive/shaft2", hiveDecay),
                ofSingle("changeling_hive/shaft_bottom", hiveDecay, 3), ofSingle("changeling_hive/shaft_bottom2", hiveDecay, 9),
                ofSingle("changeling_hive/spiked/shaft1", hiveDecay), ofSingle("changeling_hive/spiked/shaft2", hiveDecay),
                ofSingle("changeling_hive/spiked/shaft_bottom", hiveDecay, 3), ofSingle("changeling_hive/spiked/shaft_bottom2", hiveDecay, 3)
        ), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_TUNNELS, new StructurePool(tunnelTerminations, List.of(
                ofSingle("changeling_hive/termination1", hiveDecay),
                ofSingle("changeling_hive/offshoot_transition", hiveDecay, 2),
                ofSingle("changeling_hive/descent1", hiveDecay),
                ofSingle("changeling_hive/spiked/termination1", hiveDecay),
                ofSingle("changeling_hive/spiked/offshoot_transition", hiveDecay, 2),
                ofSingle("changeling_hive/spiked/descent1", hiveDecay),
                ofSingle("changeling_hive/slimey/termination1", hiveDecay),
                ofSingle("changeling_hive/slimey/offshoot_transition", hiveDecay, 2)
        ), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_SIDE_PASSAGES, new StructurePool(tunnelTerminations, List.of(
                ofSingle("changeling_hive/offshoot1", hiveDecay),
                ofSingle("changeling_hive/offshoot2", hiveDecay, 3),
                ofSingle("changeling_hive/offshoot_elbow", hiveDecay, 2),
                ofSingle("changeling_hive/offshoot_elbow2", hiveDecay, 2),
                ofSingle("changeling_hive/chamber1", hiveDecay),
                ofSingle("changeling_hive/spiked/offshoot1", hiveDecay),
                ofSingle("changeling_hive/spiked/offshoot2", hiveDecay, 3),
                ofSingle("changeling_hive/spiked/offshoot3", hiveDecay, 2),
                ofSingle("changeling_hive/spiked/offshoot_elbow", hiveDecay, 2),
                ofSingle("changeling_hive/spiked/offshoot_elbow2", hiveDecay, 2),
                ofSingle("changeling_hive/spiked/chamber1", hiveDecay),
                ofSingle("changeling_hive/spiked/chamber2", hiveDecay),
                ofSingle("changeling_hive/slimey/offshoot2", hiveDecay, 3),
                ofSingle("changeling_hive/trapped/offshoot2", hiveDecay, 3),
                ofSingle("changeling_hive/slimey/offshoot3", hiveDecay, 2),
                ofSingle("changeling_hive/slimey/offshoot_elbow", hiveDecay, 2),
                ofSingle("changeling_hive/slimey/offshoot_elbow2", hiveDecay, 2),
                ofSingle("changeling_hive/slimey/chamber1", hiveDecay),
                ofSingle("changeling_hive/slimey/chamber2", hiveDecay)
        ), Projection.RIGID));
        var hiddenEntranceTerminations = registerable.register(CHANGELING_HIVE_HIDDEN_ENTRANCE_TERMINATIONS, new StructurePool(empty, List.of(
                ofSingle("changeling_hive/termination/blockade1", hiveDecay)
        ), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_HIDDEN_ENTRANCES, new StructurePool(hiddenEntranceTerminations, List.of(
                ofSingle("changeling_hive/chamber1", hiveDecay),
                ofSingle("changeling_hive/spiked/chamber1", hiveDecay),
                ofSingle("changeling_hive/spiked/chamber2", hiveDecay),
                ofSingle("changeling_hive/slimey/chamber1", hiveDecay),
                ofSingle("changeling_hive/slimey/chamber2", hiveDecay)
        ), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_CHAMBER_DECORS, new StructurePool(empty, Stream.of("nothing", "pillar", "spiders", "spiked", "slime")
                .map(type -> ofSingle("changeling_hive/chamber_decoration/" + type, "pillar".contentEquals(type) ? hiveDecay : hiveSurfacing, switch (type) {
                    case "nothing" -> 7;
                    case "spiders" -> 4;
                    default -> 2;
                }))
                .toList(), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_PIT_DECORS, new StructurePool(empty, Stream.of("eggs", "lava", "ruins", "spiders", "spikes", "bulb")
                .map(type -> ofSingle("changeling_hive/pit_decoration/" + type, "ruins".contentEquals(type) ? hiveDecay : hiveSurfacing))
                .toList(), Projection.RIGID));
        var roofAdditionsTermination = registerable.register(CHANGELING_HIVE_ROOF_ADDITIONS_TERMINATION, new StructurePool(empty, List.of(
                ofSingle("changeling_hive/roof_decoration/empty")
        ), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_ROOF_ADDITIONS, new StructurePool(roofAdditionsTermination, Stream.of("empty", "slime", "spikes")
                .map(type -> ofSingle("changeling_hive/roof_decoration/" + type, switch (type) {
                    case "empty" -> 6;
                    case "spikes" -> 3;
                    default -> 1;
                }))
                .toList(), Projection.RIGID));
        var floorAdditionsTermination = registerable.register(CHANGELING_HIVE_FLOOR_ADDITIONS_TERMINATION, new StructurePool(empty, List.of(
                ofSingle("changeling_hive/floor_decoration/empty")
        ), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_FLOOR_ADDITIONS, new StructurePool(floorAdditionsTermination, Stream.of("empty", "slime_1", "slime_2", "slime_3", "spikes")
                .map(type -> ofSingle("changeling_hive/floor_decoration/" + type, switch (type) {
                    case "empty" -> 6;
                    case "spikes" -> 3;
                    default -> 1;
                }))
                .toList(), Projection.RIGID));
        registerable.register(CHANGELING_HIVE_MAIN_ENTRANCE_SHAFTS, new StructurePool(empty, List.of(
                ofSingle("changeling_hive/entrance_large_lower", 6)
        ), Projection.RIGID));
    }

    private static Stream<Pair<Function<Projection, ? extends StructurePoolElement>, Integer>> explode(int from, int to, IntFunction<Pair<Function<Projection, ? extends StructurePoolElement>, Integer>> func) {
        return IntStream.rangeClosed(from, to).mapToObj(func);
    }

    private static Pair<Function<Projection, ? extends StructurePoolElement>, Integer> ofSingle(String name) {
        return ofSingle(name, 1);
    }

    private static Pair<Function<Projection, ? extends StructurePoolElement>, Integer> ofSingle(String name, int weight) {
        return Pair.of(StructurePoolElement.ofSingle(Unicopia.DEFAULT_NAMESPACE + ":" + name), weight);
    }

    private static Pair<Function<Projection, ? extends StructurePoolElement>, Integer> ofSingle(String name, RegistryEntry<StructureProcessorList> processorList) {
        return ofSingle(name, processorList, 1);
    }

    private static Pair<Function<Projection, ? extends StructurePoolElement>, Integer> ofSingle(String name, RegistryEntry<StructureProcessorList> processorList, int weight) {
        return Pair.of(StructurePoolElement.ofProcessedSingle(Unicopia.DEFAULT_NAMESPACE + ":" + name, processorList), weight);
    }

    static void bootstrapProcessors(Registerable<StructureProcessorList> registerable) {
        registerExtra(registerable, StructureProcessorLists.FARM_PLAINS, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(
                new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.2F), AlwaysTrueRuleTest.INSTANCE, UBlocks.OATS.getDefaultState())
        ))));
        registerExtra(registerable, StructureProcessorLists.FARM_SAVANNA, ImmutableList.of(new RuleStructureProcessor(ImmutableList.of(
                new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.WHEAT, 0.1F), AlwaysTrueRuleTest.INSTANCE, UBlocks.OATS.getDefaultState())
        ))));
        registerable.register(CLOUD_DECAY, new StructureProcessorList(List.of(
                new RuleStructureProcessor(List.of(
                        new StructureProcessorRule(new RandomBlockMatchRuleTest(UBlocks.CLOUD, 0.2F), AlwaysTrueRuleTest.INSTANCE, UBlocks.UNSTABLE_CLOUD.getDefaultState()),
                        new StructureProcessorRule(new RandomBlockMatchRuleTest(UBlocks.CLOUD, 0.2F), AlwaysTrueRuleTest.INSTANCE, UBlocks.DENSE_CLOUD.getDefaultState())
                ))
        )));
        var hivePopulator = new RuleStructureProcessor(List.of(
                new StructureProcessorRule(new RandomBlockMatchRuleTest(UBlocks.CHITIN, 0.2F), AlwaysTrueRuleTest.INSTANCE, UBlocks.HIVE.getDefaultState())
        ));
        var hiveSurfacer = new SurfaceGrowthStructureProcessor(new BlockMatchRuleTest(UBlocks.CHITIN), UBlocks.SURFACE_CHITIN.getDefaultState());
        registerable.register(CHANGELING_HIVE_DECAY, new StructureProcessorList(List.of(hivePopulator, new CaveCarvingStructureProcessor(), hiveSurfacer)));
        registerable.register(CHANGELING_HIVE_ENTRANCE_DECAY, new StructureProcessorList(List.of(hivePopulator, hiveSurfacer)));
        registerable.register(CHANGELING_HIVE_SURFACING, new StructureProcessorList(List.of(hiveSurfacer)));
    }

    static void registerExtra(Registerable<StructurePool> registerable, String name, StructurePool pool) {
        registerable.register(RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Identifier.of(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, name)), pool);
    }

    static void registerExtra(Registerable<StructureProcessorList> registerable, RegistryKey<StructureProcessorList> key, List<StructureProcessor> processors) {
        registerable.register(RegistryKey.of(RegistryKeys.PROCESSOR_LIST, Identifier.of(Unicopia.VANILLA_EXTENSIONS_NAMESPACE, key.getValue().getPath())), new StructureProcessorList(processors));
    }
}
