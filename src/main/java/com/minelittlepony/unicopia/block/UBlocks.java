package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.UnicopiaCore;
import com.minelittlepony.unicopia.gas.CloudAnvilBlock;
import com.minelittlepony.unicopia.gas.CloudBlock;
import com.minelittlepony.unicopia.gas.CloudDoorBlock;
import com.minelittlepony.unicopia.gas.CloudFarmlandBlock;
import com.minelittlepony.unicopia.gas.CloudFenceBlock;
import com.minelittlepony.unicopia.gas.CloudSlabBlock;
import com.minelittlepony.unicopia.gas.CloudStairsBlock;
import com.minelittlepony.unicopia.gas.CloudType;
import com.minelittlepony.unicopia.item.AppleItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.structure.CustomSaplingGenerator;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UBlocks {
    CloudBlock normal_cloud = register(new CloudBlock(UMaterials.CLOUD, CloudType.NORMAL), "cloud_block");
    CloudBlock enchanted_cloud = register(new CloudBlock(UMaterials.CLOUD, CloudType.ENCHANTED), "enchanted_cloud_block");
    CloudBlock packed_cloud = register(new CloudBlock(UMaterials.CLOUD, CloudType.PACKED), "packed_cloud_block");

    CloudStairsBlock cloud_stairs = register(new CloudStairsBlock(normal_cloud.getDefaultState(), FabricBlockSettings.of(UMaterials.CLOUD).build()), "cloud_stairs");

    CloudSlabBlock<CloudBlock> cloud_slab = register(new CloudSlabBlock<>(normal_cloud, UMaterials.CLOUD), "cloud_slab");
    CloudSlabBlock<CloudBlock> enchanted_cloud_slab = register(new CloudSlabBlock<>(enchanted_cloud, UMaterials.CLOUD), "enchanted_cloud_slab");
    CloudSlabBlock<CloudBlock> packed_cloud_slab = register(new CloudSlabBlock<>(enchanted_cloud, UMaterials.CLOUD), "packed_cloud_slab");

    CloudDoorBlock mist_door = register(new CloudDoorBlock(), "mist_door");
    DutchDoorBlock library_door = register(new DutchDoorBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(3).build()), "library_door");
    DutchDoorBlock bakery_door = register(new DutchDoorBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(3).build()), "bakery_door");
    DiamondDoorBlock diamond_door = register(new DiamondDoorBlock(), "diamond_door");

    GlowingGemBlock enchanted_torch = register(new GlowingGemBlock(), "enchanted_torch");

    CloudAnvilBlock anvil = register(new CloudAnvilBlock(), "anvil");

    CloudFenceBlock cloud_fence = register(new CloudFenceBlock(UMaterials.CLOUD, CloudType.NORMAL), "cloud_fence");

    TallCropBlock alfalfa = register(new TallCropBlock(FabricBlockSettings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP).build()), "alfalfa");

    StickBlock stick = register(new StickBlock(), "stick");
    TomatoPlantBlock tomato_plant = register(new TomatoPlantBlock(), "tomato_plant");

    CloudFarmlandBlock cloud_farmland = register(new CloudFarmlandBlock(FabricBlockSettings.of(UMaterials.CLOUD).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.WOOL).build()), "cloud_farmland");

    HiveWallBlock hive = register(new HiveWallBlock(), "hive");
    ChitinBlock chitin = register(new ChitinBlock(), "chitin_block");
    Block chissled_chitin = register(new ChiselledChitinBlock(), "chissled_chitin");

    BlockGrowingCuccoon cuccoon = register(new BlockGrowingCuccoon(), "cuccoon");
    SlimeLayerBlock slime_layer = register(new SlimeLayerBlock(), "slime_layer");

    Block sugar_block = register(new SugarBlock(), "sugar_block");

    SaplingBlock apple_tree = register(new SaplingBlock(
            new CustomSaplingGenerator(5, Blocks.OAK_LOG.getDefaultState(), UBlocks.apple_leaves.getDefaultState()),
            FabricBlockSettings.of(Material.WOOD).build()
        ) {}, "apple_sapling");
    Block apple_leaves = register(new FruitLeavesBlock()
            .growthChance(1200)
            .tint(0xFFEE81)
            .fruit(AppleItem::getRandomItemStack)
            .compost(w -> new ItemStack(UItems.rotten_apple)), "apple_leaves");


    static <T extends Block> T register(T block, String name) {
        return Registry.BLOCK.add(new Identifier(UnicopiaCore.MODID, name), block);
    }

    static void bootstrap() { }
}
