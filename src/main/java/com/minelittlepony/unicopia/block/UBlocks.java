package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

public interface UBlocks {
    Block ROCKS = register("rocks", new RockCropBlock(FabricBlockSettings.of(
                new FabricMaterialBuilder(MapColor.STONE_GRAY).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build()
            )
            .requiresTool()
            .ticksRandomly()
            .strength(2)
            .sounds(BlockSoundGroup.STONE)));

    Block FROSTED_OBSIDIAN = register("frosted_obsidian", new FrostedObsidianBlock(FabricBlockSettings.copy(Blocks.OBSIDIAN).ticksRandomly()));

    Block ZAP_LOG = register("zap_log", new ZapAppleLogBlock(Blocks.OAK_LOG, MapColor.GRAY, MapColor.DEEPSLATE_GRAY), ItemGroup.MATERIALS);
    Block ZAP_WOOD = register("zap_wood", new ZapBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.WOOD), Blocks.OAK_WOOD), ItemGroup.MATERIALS);

    Block STRIPPED_ZAP_LOG = register("stripped_zap_log", new ZapAppleLogBlock(Blocks.STRIPPED_OAK_LOG, MapColor.LIGHT_GRAY, MapColor.GRAY), ItemGroup.MATERIALS);
    Block STRIPPED_ZAP_WOOD = register("stripped_zap_wood", new ZapBlock(AbstractBlock.Settings.of(Material.WOOD, MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.WOOD), Blocks.STRIPPED_OAK_WOOD), ItemGroup.MATERIALS);

    Block ZAP_LEAVES = register("zap_leaves", new ZapAppleLeavesBlock(), ItemGroup.DECORATIONS);
    Block ZAP_BULB = register("zap_bulb", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GRAY).strength(500, 1200).sounds(BlockSoundGroup.AZALEA_LEAVES), Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE));
    Block ZAP_APPLE = register("zap_apple", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GRAY).sounds(BlockSoundGroup.AZALEA_LEAVES), Direction.DOWN, ZAP_LEAVES, FruitBlock.DEFAULT_SHAPE));

    Block GREEN_APPLE_LEAVES = register("green_apple_leaves", new FruitBearingBlock(FabricBlockSettings.copy(Blocks.OAK_LEAVES),
            0xE5FFFF88,
            () -> UBlocks.GREEN_APPLE,
            () -> UItems.GREEN_APPLE.getDefaultStack()
    ), ItemGroup.DECORATIONS);
    Block GREEN_APPLE = register("green_apple", new FruitBlock(FabricBlockSettings.of(Material.GOURD, MapColor.GREEN).sounds(BlockSoundGroup.WOOD), Direction.DOWN, GREEN_APPLE_LEAVES, FruitBlock.DEFAULT_SHAPE));

    static <T extends Block> T register(String name, T item) {
        return register(Unicopia.id(name), item);
    }

    static <T extends Block> T register(String name, T block, ItemGroup group) {
        return register(Unicopia.id(name), block, group);
    }

    static <T extends Block> T register(Identifier id, T block, ItemGroup group) {
        UItems.register(id, new BlockItem(block, new Item.Settings().group(group)));
        return register(id, block);
    }

    static <T extends Block> T register(Identifier id, T block) {
        if (block instanceof TintedBlock) {
            TintedBlock.REGISTRY.add(block);
        }
        return Registry.register(Registry.BLOCK, id, block);
    }

    static void bootstrap() {
        StrippableBlockRegistry.register(ZAP_LOG, STRIPPED_ZAP_LOG);
        StrippableBlockRegistry.register(ZAP_WOOD, STRIPPED_ZAP_WOOD);
        FlammableBlockRegistry.getDefaultInstance().add(GREEN_APPLE_LEAVES, 30, 60);
        FlammableBlockRegistry.getDefaultInstance().add(GREEN_APPLE, 20, 50);
    }

    static boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    static Boolean canSpawnOnLeaves(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
        return type == EntityType.OCELOT || type == EntityType.PARROT;
    }
}
