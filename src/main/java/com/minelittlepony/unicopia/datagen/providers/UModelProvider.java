package com.minelittlepony.unicopia.datagen.providers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.item.BedsheetsItem;
import com.minelittlepony.unicopia.item.UItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataWriter;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;

public class UModelProvider extends FabricModelProvider {
    public static final Map<Block, Item> FRUITS = Map.of(UBlocks.GREEN_APPLE, UItems.GREEN_APPLE,
            UBlocks.GOLDEN_APPLE, Items.GOLDEN_APPLE,
            UBlocks.MANGO, UItems.MANGO,
            UBlocks.SOUR_APPLE, UItems.SOUR_APPLE,
            UBlocks.SWEET_APPLE, UItems.SWEET_APPLE,
            UBlocks.ZAP_APPLE, UItems.ZAP_APPLE,
            UBlocks.ZAP_BULB, UItems.ZAP_BULB
    );

    private final DataCollector seasonsModels;

    public UModelProvider(FabricDataOutput output) {
        super(output);
        seasonsModels = new DataCollector(output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "seasons/models"));
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator modelGenerator0) {
        UBlockStateModelGenerator.create(modelGenerator0).register();
        new SeasonsModelGenerator(modelGenerator0, seasonsModels.prime()).register();
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.allOf(
            super.run(writer),
            seasonsModels.upload(writer)
        );
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        ItemModels.register(itemModelGenerator,
                UItems.ACORN, UItems.APPLE_PIE_HOOF, UItems.APPLE_PIE_SLICE, UItems.APPLE_PIE,
                UItems.BANANA, UItems.BOTCHED_GEM, UItems.BROKEN_SUNGLASSES, UItems.BURNED_JUICE, UItems.BURNED_TOAST,
                UItems.CARAPACE, UItems.CLAM_SHELL, UItems.COOKED_ZAP_APPLE, UItems.CLOUD_LUMP, UItems.CRISPY_HAY_FRIES, UItems.CRYSTAL_HEART, UItems.CRYSTAL_SHARD,
                UItems.DAFFODIL_DAISY_SANDWICH, UItems.DRAGON_BREATH_SCROLL,
                UItems.EMPTY_JAR,
                UItems.FRIENDSHIP_BRACELET,
                UItems.GIANT_BALLOON, UItems.GOLDEN_FEATHER, UItems.GOLDEN_OAK_SEEDS, UItems.GOLDEN_WING, UItems.GREEN_APPLE_SEEDS, UItems.GREEN_APPLE, UItems.GROGARS_BELL,
                    UItems.GRYPHON_FEATHER,
                UItems.HAY_BURGER, UItems.HAY_FRIES, UItems.HORSE_SHOE_FRIES,
                UItems.IMPORTED_OATS,
                UItems.JAM_TOAST, UItems.JUICE,
                UItems.LIGHTNING_JAR,
                UItems.MANGO, UItems.MUFFIN,
                UItems.OATMEAL,
                UItems.PEBBLES, UItems.PEGASUS_FEATHER, UItems.PINECONE, UItems.PINEAPPLE_CROWN,
                UItems.RAIN_CLOUD_JAR, UItems.ROCK_STEW, UItems.ROCK, UItems.ROTTEN_APPLE,
                UItems.SALT_CUBE, UItems.SCALLOP_SHELL, UItems.SHELLY, UItems.SOUR_APPLE_SEEDS, UItems.SOUR_APPLE, UItems.SPELLBOOK, UItems.STORM_CLOUD_JAR,
                    UItems.SWEET_APPLE_SEEDS, UItems.SWEET_APPLE,
                UItems.TOAST, UItems.TOM, UItems.TURRET_SHELL,
                UItems.WEIRD_ROCK, UItems.WHEAT_WORMS, UBlocks.WEATHER_VANE.asItem(),
                UItems.ZAP_APPLE_JAM_JAR, UItems.ZAP_APPLE, UItems.ZAP_BULB,
        // discs
                UItems.MUSIC_DISC_CRUSADE, UItems.MUSIC_DISC_FUNK, UItems.MUSIC_DISC_PET, UItems.MUSIC_DISC_POPULAR,
        // baskets
                UItems.ACACIA_BASKET, UItems.BAMBOO_BASKET, UItems.BIRCH_BASKET, UItems.CHERRY_BASKET,
                UItems.DARK_OAK_BASKET, UItems.JUNGLE_BASKET, UItems.MANGROVE_BASKET, UItems.OAK_BASKET, UItems.SPRUCE_BASKET,
                UItems.PALM_BASKET,
        // boats
                UItems.PALM_BOAT, UItems.PALM_CHEST_BOAT,
        // horseshoes
                UItems.COPPER_HORSE_SHOE, UItems.GOLDEN_HORSE_SHOE, UItems.IRON_HORSE_SHOE, UItems.NETHERITE_HORSE_SHOE
        );
        // spawn eggs
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_SPAWN_EGG, UItems.BUTTERFLY_SPAWN_EGG, UItems.LOOT_BUG_SPAWN_EGG);
        // amulets
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_AMULET, UItems.ALICORN_AMULET, UItems.BROKEN_ALICORN_AMULET, UItems.PEARL_NECKLACE, UItems.PEGASUS_AMULET, UItems.UNICORN_AMULET);
        // mugs
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_MUG, UItems.CIDER, UItems.LOVE_BOTTLE, UItems.LOVE_BUCKET, UItems.LOVE_MUG, UItems.MUG);
        // jars
        ItemModels.register(itemModelGenerator, ItemModels.BUILTIN_ENTITY, UItems.FILLED_JAR);
        // eyewear
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_EYEWEAR, UItems.SUNGLASSES);
        // staffs
        ItemModels.register(itemModelGenerator, ItemModels.HANDHELD_STAFF, UItems.MEADOWBROOKS_STAFF);
        ItemModels.item("handheld_staff", TextureKey.LAYER0, TextureKey.LAYER1).upload(ModelIds.getItemModelId(UItems.MAGIC_STAFF), new TextureMap()
                .put(TextureKey.LAYER0, ModelIds.getItemSubModelId(UItems.MAGIC_STAFF, "_base"))
                .put(TextureKey.LAYER1, ModelIds.getItemSubModelId(UItems.MAGIC_STAFF, "_magic")), itemModelGenerator.writer);

        // polearms
        List.of(UItems.DIAMOND_POLEARM, UItems.GOLDEN_POLEARM, UItems.NETHERITE_POLEARM, UItems.STONE_POLEARM, UItems.WOODEN_POLEARM, UItems.IRON_POLEARM).forEach(item -> ItemModels.registerPolearm(itemModelGenerator, item));
        // sheets
        ItemModels.register(itemModelGenerator, BedsheetsItem.ITEMS.values().stream().toArray(Item[]::new));
        // badges
        ItemModels.register(itemModelGenerator, Race.REGISTRY.stream()
                .map(race -> race.getId().withPath(p -> p + "_badge"))
                .flatMap(id -> Registries.ITEM.getOrEmpty(id).stream())
                .toArray(Item[]::new));

        // butterflies
        ItemModels.registerButterfly(itemModelGenerator, UItems.BUTTERFLY);
        ItemModels.registerSpectralBlock(itemModelGenerator, UItems.SPECTRAL_CLOCK);
        ModelOverrides.of(ItemModels.GENERATED)
            .addUniform("count", 2, 16, ModelIds.getItemModelId(UItems.ROCK_CANDY))
            .upload(UItems.ROCK_CANDY, itemModelGenerator);

        List.of(UItems.PINEAPPLE, UItems.CANDIED_APPLE).forEach(item -> {
            ModelOverrides.of(ItemModels.GENERATED)
                .addOverride(ModelIds.getItemSubModelId(item, "_bite1"), "damage", 0.3F)
                .addOverride(ModelIds.getItemSubModelId(item, "_bite2"), "damage", 0.6F)
                .upload(item, itemModelGenerator);
        });

        // gemstone
        ModelOverrides.of(ItemModels.GENERATED)
                .addOverride(ModelIds.getItemSubModelId(UItems.GEMSTONE, "_pure"), "affinity", 0)
                .addOverride(ModelIds.getItemSubModelId(UItems.GEMSTONE, "_corrupted"), "affinity", 1)
                .upload(UItems.GEMSTONE, itemModelGenerator);
    }
}
