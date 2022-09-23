package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.minecraft.block.*;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.BlockView;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public interface UBlocks {
    Block ROCKS = register("rocks", new RockCropBlock(FabricBlockSettings.of(
                new FabricMaterialBuilder(MapColor.STONE_GRAY).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build()
            )
            .requiresTool()
            .ticksRandomly()
            .strength(2)
            .sounds(BlockSoundGroup.STONE)));

    Block FROSTED_OBSIDIAN = register("frosted_obsidian", new FrostedObsidianBlock(FabricBlockSettings.copy(Blocks.OBSIDIAN).ticksRandomly()));

    Block ZAPLING = register("zapling", new SaplingBlock(new SaplingGenerator() {
        @Override
        protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random rng, boolean flowersNearby) {
            return UTreeGen.ZAP_APPLE_TREE;
        }
    }, FabricBlockSettings.copy(Blocks.OAK_SAPLING)));

    Block ZAP_LOG = register("zap_log", new ZapAppleLogBlock(MapColor.GRAY, MapColor.DEEPSLATE_GRAY));
    Block ZAP_LEAVES = register("zap_leaves", new ZapAppleLeavesBlock());
    Block ZAP_BULB = register("zap_bulb", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GRAY).strength(500, 1200).sounds(BlockSoundGroup.AZALEA_LEAVES), Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE));
    Block ZAP_APPLE = register("zap_apple", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GRAY).sounds(BlockSoundGroup.AZALEA_LEAVES), Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE));

    private static <T extends Block> T register(String name, T item) {
        return Registry.register(Registry.BLOCK, Unicopia.id(name), item);
    }

    static void bootstrap() {}


    static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}
