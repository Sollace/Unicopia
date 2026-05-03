package com.minelittlepony.unicopia.datagen.providers;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import com.google.gson.JsonElement;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.item.BedsheetsItem;
import com.minelittlepony.unicopia.item.UItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.render.model.json.BlockModelDefinition;
import net.minecraft.component.type.DyedColorComponent;

public class UModelProvider extends FabricModelProvider {
    public static final Set<Block> FRUITS = Set.of(
            UBlocks.GREEN_APPLE,
            UBlocks.GOLDEN_APPLE,
            UBlocks.MANGO,
            UBlocks.SOUR_APPLE,
            UBlocks.SWEET_APPLE,
            UBlocks.ZAP_APPLE,
            UBlocks.ZAP_BULB
    );

    private final DataCollector<Supplier<JsonElement>> seasonsModels;
    private final DataCollector<BlockModelDefinition> indirectBlockStatesDefinitions;

    public UModelProvider(FabricDataOutput output) {
        super(output);
        seasonsModels = new DataCollector<>(output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "seasons/models"), Supplier::get);
        indirectBlockStatesDefinitions = new DataCollector<>(output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "blockstates"), BlockModelDefinition.CODEC);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator modelGenerator0) {
        UBlockStateModelGenerator.create(modelGenerator0).register();
        new UExternalBlockStateModelGenerator(modelGenerator0, indirectBlockStatesDefinitions.prime((states, consumer) -> {
            if (states instanceof DataCollector.Identifiable i) {
                consumer.accept(i.getId(), states.createBlockModelDefinition());
            }
        })).register();
        new SeasonsModelGenerator(modelGenerator0, seasonsModels.prime()).register();
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.allOf(
            super.run(writer),
            indirectBlockStatesDefinitions.upload(writer),
            seasonsModels.upload(writer)
        );
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        ItemModels.register(itemModelGenerator,
                UItems.ACORN, UItems.APPLE_PIE_HOOF, UItems.APPLE_PIE_SLICE, UItems.APPLE_PIE,
                UItems.BANANA, UItems.BOTCHED_GEM, UItems.BOWL_OF_NUTS, UItems.BROKEN_SUNGLASSES, UItems.BURNED_JUICE, UItems.BURNED_TOAST,
                UItems.CARAPACE, UItems.CLAM_SHELL, UItems.COOKED_ZAP_APPLE, UItems.CHOCOLATE_OATMEAL_COOKIE,
                    UItems.CLOUD_LUMP, UItems.CRISPY_HAY_FRIES, UItems.CRYSTAL_HEART, UItems.CRYSTAL_SHARD,
                    UItems.COOKED_TROPICAL_FISH, UItems.COOKED_PUFFERFISH, UItems.COOKED_FROG_LEGS,
                UItems.DAFFODIL_DAISY_SANDWICH, UItems.DRAGON_BREATH_SCROLL, UItems.TOTEM_OF_DYING,
                UItems.FRIED_AXOLOTL, UItems.FROG_LEGS,
                UItems.GOLDEN_FEATHER, UItems.GOLDEN_WING, UItems.GREEN_APPLE, UItems.GROGARS_BELL,
                    UItems.GRYPHON_FEATHER, UItems.GREEN_FRIED_EGG,
                UItems.HAY_BURGER, UItems.HAY_FRIES, UItems.HORSE_SHOE_FRIES,
                UItems.IMPORTED_OATS,
                UItems.JAM_TOAST, UItems.JUICE,
                UItems.MANGO, UItems.MUFFIN,
                UItems.OATMEAL, UItems.OATMEAL_COOKIE, UItems.SCONE,
                UItems.PEGASUS_FEATHER, UItems.PINECONE, UItems.PINECONE_COOKIE, UItems.PINEAPPLE_CROWN,
                UItems.ROCK_STEW, UItems.ROCK,
                    UItems.ROTTEN_APPLE, UItems.ROTTEN_COD, UItems.ROTTEN_TROPICAL_FISH, UItems.ROTTEN_SALMON, UItems.ROTTEN_PUFFERFISH,
                UItems.SALT_CUBE, UItems.SCALLOP_SHELL, UItems.SHELLY, UItems.SOUR_APPLE, UItems.SPELLBOOK,
                    UItems.SWEET_APPLE,
                UItems.TOAST, UItems.TOM, UItems.TURRET_SHELL,
                UItems.WEIRD_ROCK, UItems.WHEAT_WORMS,
                UItems.ZAP_APPLE, UItems.ZAP_BULB,
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
        ItemModels.registerDyeable(itemModelGenerator, UItems.FRIENDSHIP_BRACELET, DyedColorComponent.DEFAULT_COLOR);
        // spawn eggs
        ItemModels.register(itemModelGenerator, ItemModels.GENERATED, UItems.BUTTERFLY_SPAWN_EGG, UItems.LOOT_BUG_SPAWN_EGG);
        // amulets
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_AMULET, UItems.ALICORN_AMULET, UItems.BROKEN_ALICORN_AMULET, UItems.PEARL_NECKLACE, UItems.PEGASUS_AMULET, UItems.UNICORN_AMULET);
        // mugs
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_MUG, UItems.CIDER, UItems.LOVE_BOTTLE, UItems.LOVE_BUCKET, UItems.LOVE_MUG, UItems.MUG);
        // jars
        ItemModels.register(itemModelGenerator, ItemModels.GENERATED, UItems.EMPTY_JAR);
        ItemModels.registerFilledJar(itemModelGenerator, UItems.FILLED_JAR);
        // eyewear
        ItemModels.register(itemModelGenerator, ItemModels.TEMPLATE_EYEWEAR, UItems.SUNGLASSES);
        // staffs
        ItemModels.register(itemModelGenerator, ItemModels.HANDHELD_STAFF, UItems.MEADOWBROOKS_STAFF);
        ItemModels.registerMagicStaff(itemModelGenerator, UItems.MAGIC_STAFF);
        ItemModels.registerParented(itemModelGenerator, UItems.GOLDEN_STICK, Items.BLAZE_ROD);

        // polearms
        List.of(
                UItems.DIAMOND_POLEARM, UItems.GOLDEN_POLEARM, UItems.NETHERITE_POLEARM,
                UItems.STONE_POLEARM, UItems.WOODEN_POLEARM, UItems.IRON_POLEARM
        ).forEach(item -> ItemModels.registerPolearm(itemModelGenerator, item));
        // sheets
        ItemModels.register(itemModelGenerator, BedsheetsItem.ITEMS.values().stream().toArray(Item[]::new));
        // badges
        ItemModels.register(itemModelGenerator, Race.REGISTRY.stream()
                .map(race -> race.getId().withPath(p -> p + "_badge"))
                .flatMap(id -> Registries.ITEM.getOptionalValue(id).stream())
                .toArray(Item[]::new));

        ItemModels.registerButterfly(itemModelGenerator, UItems.BUTTERFLY);
        ItemModels.registerBalloonDesigns(itemModelGenerator, UItems.GIANT_BALLOON);
        ItemModels.registerSpectralClock(itemModelGenerator, UItems.SPECTRAL_CLOCK);
        ItemModels.registerStagedFoodItem(itemModelGenerator, UItems.ROCK_CANDY, 2, 16, "");
        ItemModels.registerStagedFoodItem(itemModelGenerator, UItems.PINEAPPLE, 1, 2, "bite");
        ItemModels.registerStagedFoodItem(itemModelGenerator, UItems.CANDIED_APPLE, 1, 2, "bite");
        ItemModels.registerGemstone(itemModelGenerator, UItems.GEMSTONE);
        ItemModels.registerCustomFishingRod(itemModelGenerator, UItems.BAITED_FISHING_ROD);

    }
}
