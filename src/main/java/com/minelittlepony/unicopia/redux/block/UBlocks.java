package com.minelittlepony.unicopia.redux.block;

import com.minelittlepony.unicopia.core.UnicopiaCore;
import com.minelittlepony.unicopia.redux.CloudType;
import com.minelittlepony.unicopia.redux.UMaterials;
import com.minelittlepony.unicopia.redux.item.AppleItem;
import com.minelittlepony.unicopia.redux.item.UItems;
import com.minelittlepony.unicopia.redux.structure.CustomSaplingGenerator;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class UBlocks {
    public static final CloudBlock normal_cloud = register(new CloudBlock(UMaterials.cloud, CloudType.NORMAL), "cloud_block");
    public static final CloudBlock enchanted_cloud = register(new CloudBlock(UMaterials.cloud, CloudType.ENCHANTED), "enchanted_cloud_block");
    public static final CloudBlock packed_cloud = register(new CloudBlock(UMaterials.cloud, CloudType.PACKED), "packed_cloud_block");

    public static final BlockCloudStairs cloud_stairs = register(new BlockCloudStairs(normal_cloud.getDefaultState()), "cloud_stairs");

    public static final CloudSlabBlock<CloudBlock> cloud_slab = register(new CloudSlabBlock<>(normal_cloud, UMaterials.cloud), "cloud_slab");
    public static final CloudSlabBlock<CloudBlock> enchanted_cloud_slab = register(new CloudSlabBlock<>(enchanted_cloud, UMaterials.cloud), "enchanted_cloud_slab");
    public static final CloudSlabBlock<CloudBlock> packed_cloud_slab = register(new CloudSlabBlock<>(enchanted_cloud, UMaterials.cloud), "packed_cloud_slab");

    public static final BlockCloudDoor mist_door = register(new BlockCloudDoor(), "mist_door");
    public static final DutchDoorBlock library_door = register(new DutchDoorBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(3).build()), "library_door");
    public static final DutchDoorBlock bakery_door = register(new DutchDoorBlock(FabricBlockSettings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).hardness(3).build()), "bakery_door");
    public static final DiamondDoorBlock diamond_door = register(new DiamondDoorBlock(), "diamond_door");

    public static final GlowingGemBlock enchanted_torch = register(new GlowingGemBlock(), "enchanted_torch");

    public static final CloudAnvilBlock anvil = register(new CloudAnvilBlock(), "anvil");

    public static final CloudFenceBlock cloud_fence = register(new CloudFenceBlock(UMaterials.cloud, CloudType.NORMAL), "cloud_fence");
    public static final CloudBanisterBlock cloud_banister = register(new CloudBanisterBlock(UMaterials.cloud), "cloud_banister");

    public static final BlockAlfalfa alfalfa = register(new BlockAlfalfa(), "alfalfa");

    public static final StickBlock stick = register(new StickBlock(), "stick");
    public static final BlockTomatoPlant tomato_plant = register(new BlockTomatoPlant(), "tomato_plant");

    public static final BlockCloudFarm cloud_farmland = register(new BlockCloudFarm(), "cloud_farmland");

    public static final HiveWallBlock hive = register(new HiveWallBlock(), "hive");
    public static final ChitinBlock chitin = register(new ChitinBlock(), "chitin_block");
    public static final Block chissled_chitin = register(new ChiselledChitinBlock(), "chissled_chitin");

    public static final BlockGrowingCuccoon cuccoon = register(new BlockGrowingCuccoon(), "cuccoon");
    public static final SlimeLayerBlock slime_layer = register(new SlimeLayerBlock(), "slime_layer");

    public static final Block sugar_block = register(new SugarBlock(), "sugar_block");
    public static final UPot flower_pot = register(new UPot(), "flower_pot");

    public static final SaplingBlock apple_tree = register(new SaplingBlock(
            new CustomSaplingGenerator(5, Blocks.OAK_LOG.getDefaultState(), UBlocks.apple_leaves.getDefaultState()),
            FabricBlockSettings.of(Material.WOOD).build()
        ) {}, "apple_sapling");
    public static final Block apple_leaves = register(new FruitLeavesBlock()
            .growthChance(1200)
            .tint(0xFFEE81)
            .fruit(AppleItem::getRandomItemStack)
            .compost(w -> new ItemStack(UItems.rotten_apple)), "apple_leaves");


    private static <T extends Block> T register(T block, String name) {
        return Registry.BLOCK.add(new Identifier(UnicopiaCore.MODID, name), block);
    }

    public static void bootstrap() { }
}
