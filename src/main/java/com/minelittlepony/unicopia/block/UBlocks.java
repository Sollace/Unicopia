package com.minelittlepony.unicopia.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UBlocks {
    Block ROCKS = register("rocks", new RockCropBlock(FabricBlockSettings.of(
                new FabricMaterialBuilder(MapColor.STONE_GRAY).allowsMovement().lightPassesThrough().notSolid().destroyedByPiston().build()
            )
            .requiresTool()
            .ticksRandomly()
            .strength(2)
            .sounds(BlockSoundGroup.STONE)));

    private static <T extends Block> T register(String name, T item) {
        return Registry.register(Registry.BLOCK, new Identifier("unicopia", name), item);
    }

    static void bootstrap() {}
}
