package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.datagen.ItemFamilies;
import com.minelittlepony.unicopia.item.BedsheetsItem;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class UItemTagProvider extends FabricTagProvider.ItemTagProvider {


    private final UBlockTagProvider blockTagProvider;

    public UItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, UBlockTagProvider blockTagProvider) {
        super(output, registriesFuture, blockTagProvider);
        this.blockTagProvider = blockTagProvider;
    }

    @Override
    public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        TagBuilder blockTagBuilder = Objects.requireNonNull(blockTagProvider, "Pass Block tag provider via constructor to use copy").getTagBuilder(blockTag);
        TagBuilder itemTagBuilder = getTagBuilder(itemTag);
        blockTagBuilder.build().forEach(entry -> {
            if (entry.canAdd(Registries.ITEM::containsId, tagId -> getTagBuilder(TagKey.of(RegistryKeys.ITEM, tagId)) != null)) {
                itemTagBuilder.add(entry);
            } else {
                Datagen.LOGGER.warn("Cannot copy missing entry {} to item tag {}", entry, itemTag.id());
            }
        });
    }

    @Override
    protected void configure(WrapperLookup arg) {
        copyBlockTags();
        exportConventionalTags();
        getOrCreateTagBuilder(ItemTags.BOOKSHELF_BOOKS).add(UItems.SPELLBOOK);
        getOrCreateTagBuilder(ItemTags.BEDS).add(UItems.CLOTH_BED, UItems.CLOUD_BED);

        getOrCreateTagBuilder(ItemTags.CHEST_BOATS).add(UItems.PALM_CHEST_BOAT);
        getOrCreateTagBuilder(ItemTags.BOATS).add(UItems.PALM_BOAT);
        getOrCreateTagBuilder(ItemTags.MUSIC_DISCS).add(ItemFamilies.MUSIC_DISCS);
        getOrCreateTagBuilder(ItemTags.CREEPER_DROP_MUSIC_DISCS).add(UItems.MUSIC_DISC_CRUSADE, UItems.MUSIC_DISC_FUNK, UItems.MUSIC_DISC_PET, UItems.MUSIC_DISC_POPULAR);

        getOrCreateTagBuilder(ItemTags.SIGNS).add(UBlocks.PALM_SIGN.asItem());
        getOrCreateTagBuilder(ItemTags.HANGING_SIGNS).add(UBlocks.PALM_HANGING_SIGN.asItem());

        getOrCreateTagBuilder(UTags.Items.HORSE_SHOES).add(ItemFamilies.HORSE_SHOES);
        getOrCreateTagBuilder(UTags.Items.POLEARMS).add(ItemFamilies.POLEARMS);

        getOrCreateTagBuilder(ItemTags.TOOLS).addTag(UTags.Items.HORSE_SHOES).addTag(UTags.Items.POLEARMS);

        getOrCreateTagBuilder(UTags.Items.BASKETS).add(ItemFamilies.BASKETS);
        getOrCreateTagBuilder(UTags.Items.BADGES).add(Race.REGISTRY.stream()
                .map(race -> race.getId().withPath(p -> p + "_badge"))
                .flatMap(id -> Registries.ITEM.getOrEmpty(id).stream())
                .toArray(Item[]::new));
        getOrCreateTagBuilder(UTags.Items.WOOL_BED_SHEETS).add(BedsheetsItem.ITEMS.values().stream().filter(sheet -> sheet != UItems.KELP_BED_SHEETS).toArray(Item[]::new));
        getOrCreateTagBuilder(UTags.Items.BED_SHEETS).forceAddTag(UTags.Items.WOOL_BED_SHEETS).add(UItems.KELP_BED_SHEETS);
        getOrCreateTagBuilder(UTags.Items.APPLE_SEEDS).add(UItems.GREEN_APPLE_SEEDS, UItems.SWEET_APPLE_SEEDS, UItems.SOUR_APPLE_SEEDS);
        getOrCreateTagBuilder(UTags.Items.MAGIC_FEATHERS).add(UItems.PEGASUS_FEATHER, UItems.GRYPHON_FEATHER);
        getOrCreateTagBuilder(UTags.Items.FRESH_APPLES).add(Items.APPLE, UItems.GREEN_APPLE, UItems.SWEET_APPLE, UItems.SOUR_APPLE);
        getOrCreateTagBuilder(UTags.Items.CLOUD_JARS).add(UItems.RAIN_CLOUD_JAR, UItems.STORM_CLOUD_JAR);
        getOrCreateTagBuilder(UTags.Items.PIES).add(UItems.APPLE_PIE, UItems.APPLE_PIE_HOOF);

        // technical tags
        getOrCreateTagBuilder(ItemTags.VILLAGER_PLANTABLE_SEEDS).addTag(UTags.Items.APPLE_SEEDS);
        getOrCreateTagBuilder(UTags.Items.CAN_CUT_PIE).forceAddTag(ConventionalItemTags.SHEARS).addOptionalTag(UConventionalTags.Items.TOOL_KNIVES);
        getOrCreateTagBuilder(UTags.Items.COOLS_OFF_KIRINS).add(Items.MELON_SLICE, UItems.JUICE).forceAddTag(ConventionalItemTags.WATER_BUCKETS);
        getOrCreateTagBuilder(UTags.Items.FALLS_SLOWLY).add(Items.FEATHER, UItems.CLOUD_LUMP).forceAddTag(UTags.Items.MAGIC_FEATHERS);
        getOrCreateTagBuilder(UTags.Items.IS_DELIVERED_AGGRESSIVELY).forceAddTag(ItemTags.ANVIL);
        getOrCreateTagBuilder(UTags.Items.SPOOKED_MOB_DROPS).add(Items.BRICK);
        getOrCreateTagBuilder(UTags.Items.SHADES).add(
                Items.CARVED_PUMPKIN, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.PLAYER_HEAD,
                Items.ZOMBIE_HEAD, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD,
                UItems.SUNGLASSES
        );
        getOrCreateTagBuilder(UTags.Items.FLOATS_ON_CLOUDS)
            .forceAddTag(UTags.Items.CLOUD_BEDS)
            .forceAddTag(UTags.Items.CLOUD_SLABS)
            .forceAddTag(UTags.Items.CLOUD_STAIRS)
            .forceAddTag(UTags.Items.CLOUD_BLOCKS)
            .add(UItems.CLOUD_LUMP);
        getOrCreateTagBuilder(UTags.Items.HAS_NO_TRAITS).add(
                Items.AIR, Items.SPAWNER, Items.STRUCTURE_VOID, Items.STRUCTURE_BLOCK,
                Items.COMMAND_BLOCK, Items.CHAIN_COMMAND_BLOCK, Items.REPEATING_COMMAND_BLOCK,
                Items.LIGHT, Items.JIGSAW, Items.BARRIER, Items.BEDROCK, Items.END_PORTAL_FRAME,
                Items.DEBUG_STICK, Items.COMMAND_BLOCK_MINECART,
                UItems.PLUNDER_VINE
        ).forceAddTag(UTags.Items.BADGES);
        getOrCreateTagBuilder(UTags.Items.LOOT_BUG_HIGH_VALUE_DROPS).add(
                    Items.DIAMOND, Items.GOLDEN_APPLE, Items.GOLDEN_CARROT,
                    Items.GOLDEN_HELMET, Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE,
                    Items.GOLDEN_HORSE_ARMOR,
                    Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_SWORD, Items.GOLDEN_HOE,
                    UItems.GOLDEN_HORSE_SHOE, UItems.GOLDEN_POLEARM, UItems.GOLDEN_FEATHER, UItems.GOLDEN_WING,
                    UItems.GOLDEN_OAK_SEEDS
            ).forceAddTag(ConventionalItemTags.NUGGETS)
            .forceAddTag(ConventionalItemTags.GOLD_INGOTS).forceAddTag(ConventionalItemTags.RAW_GOLD_ORES).forceAddTag(ConventionalItemTags.RAW_GOLD_BLOCKS)
            .addOptionalTag(new Identifier("farmersdelight:golden_knife"));

        exportFarmersDelightItems();
    }

    private void copyBlockTags() {
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(BlockTags.SLABS, ItemTags.SLABS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);

        copy(UTags.Blocks.ZAP_LOGS, UTags.Items.ZAP_LOGS);
        copy(UTags.Blocks.WAXED_ZAP_LOGS, UTags.Items.WAXED_ZAP_LOGS);
        copy(UTags.Blocks.PALM_LOGS, UTags.Items.PALM_LOGS);
        copy(UTags.Blocks.CLOUD_BEDS, UTags.Items.CLOUD_BEDS);
        copy(UTags.Blocks.CLOUD_SLABS, UTags.Items.CLOUD_SLABS);
        copy(UTags.Blocks.CLOUD_STAIRS, UTags.Items.CLOUD_STAIRS);
        copy(UTags.Blocks.CLOUD_BLOCKS, UTags.Items.CLOUD_BLOCKS);
        copy(UTags.Blocks.CHITIN_BLOCKS, UTags.Items.CHITIN_BLOCKS);
    }

    private void exportConventionalTags() {
        copy(UConventionalTags.Blocks.CONCRETES, UConventionalTags.Items.CONCRETES);
        copy(UConventionalTags.Blocks.CONCRETE_POWDERS, UConventionalTags.Items.CONCRETE_POWDERS);
        getOrCreateTagBuilder(UConventionalTags.Items.ACORNS).add(UItems.ACORN);
        getOrCreateTagBuilder(UConventionalTags.Items.APPLES)
            .add(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, UItems.ROTTEN_APPLE)
            .forceAddTag(UTags.Items.FRESH_APPLES)
            .addOptionalTag(new Identifier("c", "pyrite_apples")) // no idea which mod add pyrite apples
        ;
        getOrCreateTagBuilder(UConventionalTags.Items.BANANAS).add(UItems.BANANA);
        getOrCreateTagBuilder(UConventionalTags.Items.COOKED_FISH).add(Items.COOKED_COD, Items.COOKED_SALMON);
        getOrCreateTagBuilder(UConventionalTags.Items.STICKS).add(Items.STICK);
        getOrCreateTagBuilder(UConventionalTags.Items.PINECONES).add(UItems.PINECONE);
        getOrCreateTagBuilder(UConventionalTags.Items.PINEAPPLES).add(UItems.PINEAPPLE);
        getOrCreateTagBuilder(UConventionalTags.Items.MANGOES).add(UItems.MANGO);
        getOrCreateTagBuilder(UConventionalTags.Items.MUSHROOMS).add(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM);
        getOrCreateTagBuilder(UConventionalTags.Items.MUFFINS).add(UItems.MUFFIN);
        getOrCreateTagBuilder(UConventionalTags.Items.SEEDS).add(Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS, Items.WHEAT_SEEDS)
            .add(UItems.OAT_SEEDS)
            .forceAddTag(UTags.Items.APPLE_SEEDS);
        getOrCreateTagBuilder(UConventionalTags.Items.OATMEALS).add(UItems.OATMEAL);
        getOrCreateTagBuilder(UConventionalTags.Items.GRAIN).add(Items.WHEAT, UItems.OATS);
        getOrCreateTagBuilder(UConventionalTags.Items.NUTS).addOptionalTag(UConventionalTags.Items.CROPS_PEANUTS);

        getOrCreateTagBuilder(UConventionalTags.Items.FRUITS)
            .forceAddTag(UConventionalTags.Items.MANGOES)
            .forceAddTag(UConventionalTags.Items.PINEAPPLES)
            .forceAddTag(UConventionalTags.Items.APPLES)
            .forceAddTag(UConventionalTags.Items.BANANAS);
    }

    private void exportFarmersDelightItems() {
        getOrCreateTagBuilder(UTags.Items.COOLS_OFF_KIRINS)
            .addOptional(new Identifier("farmersdelight:melon_popsicle"))
            .addOptional(new Identifier("farmersdelight:melon_juice"));
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, new Identifier("farmersdelight:cabbage_roll_ingredients"))).add(UItems.OATS, UItems.ROCK, UItems.WHEAT_WORMS);
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, new Identifier("farmersdelight:comfort_foods"))).add(UItems.OATMEAL, UItems.ROCK_STEW, UItems.MUFFIN);
    }
}
