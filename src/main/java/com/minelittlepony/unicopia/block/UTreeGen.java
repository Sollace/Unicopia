package com.minelittlepony.unicopia.block;

import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.tag.BiomeTags;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.*;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.JungleFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.UpwardsBranchingTrunkPlacer;

public interface UTreeGen {
    RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> ZAP_APPLE_TREE = ConfiguredFeatures.register("unicopia:zap_apple_tree", Feature.TREE, new TreeFeatureConfig.Builder(
            BlockStateProvider.of(UBlocks.ZAP_LOG),
            new UpwardsBranchingTrunkPlacer(7, 2, 3,
                    UniformIntProvider.create(3, 6), 0.3f,
                    UniformIntProvider.create(1, 3),
                    Registry.BLOCK.getOrCreateEntryList(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH)
            ),
            BlockStateProvider.of(UBlocks.ZAP_LEAVES),
            new JungleFoliagePlacer(
                    ConstantIntProvider.create(3),
                    ConstantIntProvider.create(2),
                    3
            ),
            new TwoLayersFeatureSize(6, 0, 16)
        ).forceDirt()
        .build()
    );
    RegistryEntry<PlacedFeature> TREES_ZAP = PlacedFeatures.register("unicopia:trees_zap", ZAP_APPLE_TREE,
        VegetationPlacedFeatures.modifiersWithWouldSurvive(PlacedFeatures.createCountExtraModifier(0, 0.01F, 1), UBlocks.ZAPLING)
    );

    static void bootstrap() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BiomeTags.IS_FOREST)), GenerationStep.Feature.VEGETAL_DECORATION, TREES_ZAP.getKey().get());
    }
}
