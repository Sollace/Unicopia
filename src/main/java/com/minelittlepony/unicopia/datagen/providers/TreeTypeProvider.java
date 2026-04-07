package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypeLoader.TreeTypeDef;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.UWoodTypes;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.item.UItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class TreeTypeProvider implements DataProvider {
    private final DataCollector<TreeTypeDef> collector;

    public TreeTypeProvider(FabricDataOutput output) {
        collector = new DataCollector<>(output.getResolver(DataOutput.OutputType.DATA_PACK, "tree_types"), TreeTypeDef.CODEC);
    }

    @Override
    public String getName() {
        return "Tree Types";
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        var exporter = collector.prime();
        generate((id, treeType) -> exporter.accept(id, treeType.build()));
        return collector.upload(writer);
    }

    interface Collector {
        default void accept(WoodType type, TreeTypeLoader.TreeTypeDef.Builder builder) {
            accept(Identifier.of(type.name()), builder);
        }

        void accept(Identifier id, TreeTypeLoader.TreeTypeDef.Builder builder);
    }

    private void generate(Collector exporter) {
        exporter.accept(WoodType.ACACIA, TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD).leaves(Blocks.ACACIA_LEAVES)
                .rarity(5)
                .drop(1, UItems.ROTTEN_APPLE)
                .drop(2, UItems.SWEET_APPLE)
                .drop(5, UItems.GREEN_APPLE)
        );
        exporter.accept(Identifier.ofVanilla("azalea"), TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.OAK_LOG, Blocks.OAK_WOOD).leaves(Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES)
                .rarity(5)
                .drop(1, UItems.SOUR_APPLE)
                .drop(2, UItems.GREEN_APPLE)
                .drop(3, Items.APPLE)
        );
        exporter.accept(WoodType.BIRCH, TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD).leaves(Blocks.BIRCH_LEAVES)
                .rarity(5)
                .drop(1, UItems.ROTTEN_APPLE)
                .drop(2, UItems.SWEET_APPLE)
                .drop(5, UItems.GREEN_APPLE)
        );
        exporter.accept(WoodType.CHERRY, TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.CHERRY_LOG, Blocks.CHERRY_WOOD).leaves(Blocks.CHERRY_LEAVES)
                .rarity(7)
                .drop(1, Items.PINK_PETALS)
        );
        exporter.accept(WoodType.DARK_OAK, TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD).leaves(Blocks.DARK_OAK_LEAVES)
                .rarity(7).wideTrunk()
                .drop(1, UItems.ROTTEN_APPLE)
                .drop(2, UItems.SWEET_APPLE)
                .drop(5, Items.APPLE)
        );
        exporter.accept(Unicopia.id("green_apple"), TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.OAK_LOG, Blocks.OAK_WOOD).leaves(UBlocks.GREEN_APPLE_LEAVES)
        );
        exporter.accept(WoodType.JUNGLE, TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD).leaves(Blocks.JUNGLE_LEAVES)
                .rarity(5).wideTrunk()
                .drop(1, UItems.ZAP_APPLE)
                .drop(2, UItems.SWEET_APPLE)
                .drop(5, UItems.GREEN_APPLE)
        );
        exporter.accept(Unicopia.id("mango"), TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD).leaves(UBlocks.MANGO_LEAVES)
                .rarity(5)
        );
        exporter.accept(WoodType.MANGROVE, TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.MANGROVE_LOG, Blocks.MANGROVE_WOOD).leaves(Blocks.MANGROVE_LEAVES)
                .rarity(5)
                .drop(1, UItems.ROTTEN_APPLE)
        );
        exporter.accept(WoodType.OAK, TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.OAK_LOG, Blocks.OAK_WOOD).leaves(Blocks.OAK_LEAVES)
                .rarity(3)
                .drop(1, UItems.ROTTEN_APPLE)
                .drop(2, UConventionalTags.Items.ACORNS)
                .drop(3, ConventionalItemTags.WOODEN_RODS)
        );
        exporter.accept(UWoodTypes.PALM, TreeTypeLoader.TreeTypeDef.builder()
                .logs(UBlocks.PALM_LOG, UBlocks.PALM_WOOD).leaves(UBlocks.PALM_LEAVES)
        );
        exporter.accept(Unicopia.id("sour_apple"), TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.OAK_LOG, Blocks.OAK_WOOD).leaves(UBlocks.SOUR_APPLE_LEAVES)
        );
        exporter.accept(WoodType.SPRUCE, TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD).leaves(Blocks.SPRUCE_LEAVES)
                .rarity(3).wideTrunk()
                .drop(1, UConventionalTags.Items.PINECONES)
                .drop(4, ConventionalItemTags.WOODEN_RODS)
        );
        exporter.accept(Unicopia.id("sweet_apple"), TreeTypeLoader.TreeTypeDef.builder()
                .logs(Blocks.OAK_LOG, Blocks.OAK_WOOD).leaves(UBlocks.SWEET_APPLE_LEAVES)
        );
        exporter.accept(Unicopia.id("zap_apple"), TreeTypeLoader.TreeTypeDef.builder()
                .logs(UBlocks.ZAP_LOG, UBlocks.ZAP_WOOD).leaves(UBlocks.ZAP_LEAVES, UBlocks.ZAP_LEAVES_PLACEHOLDER, UBlocks.FLOWERING_ZAP_LEAVES)
        );
    }
}
