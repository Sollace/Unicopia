package com.minelittlepony.unicopia.datagen.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.FancyBedBlock;
import com.minelittlepony.unicopia.block.FruitBearingBlock;
import com.minelittlepony.unicopia.block.PieBlock;
import com.minelittlepony.unicopia.block.PileBlock;
import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.SlimePustuleBlock;
import com.minelittlepony.unicopia.block.SproutBlock;
import com.minelittlepony.unicopia.block.TintedBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesBlock;
import com.minelittlepony.unicopia.client.render.item.BlockTintSource;
import com.minelittlepony.unicopia.client.render.item.CloudBedModelRenderer;
import com.minelittlepony.unicopia.client.render.item.CloudChestModelRenderer;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.server.world.Tree;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.data.BlockModelDefinitionCreator;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.BlockStateVariantMap;
import net.minecraft.client.data.ItemModelOutput;
import net.minecraft.client.data.ItemModels;
import net.minecraft.client.data.Model;
import net.minecraft.client.data.ModelIds;
import net.minecraft.client.data.ModelSupplier;
import net.minecraft.client.data.Models;
import net.minecraft.client.data.MultipartBlockModelDefinitionCreator;
import net.minecraft.client.data.TextureKey;
import net.minecraft.client.data.TextureMap;
import net.minecraft.client.data.TexturedModel;
import net.minecraft.client.data.VariantsBlockModelDefinitionCreator;
import net.minecraft.client.render.model.json.ModelVariantOperator;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;

import static net.minecraft.client.data.TextureKey.*;
import static net.minecraft.util.math.AxisRotation.*;
import static com.minelittlepony.unicopia.datagen.providers.ItemModels.*;

public class UBlockStateModelGenerator extends BlockStateModelGenerator {
    static final Identifier AIR_BLOCK_ID = Identifier.ofVanilla("block/air");
    static final Identifier AIR_ITEM_ID = Identifier.ofVanilla("item/air");

    private static final BlockStateVariantMap<ModelVariantOperator> UP_DEFAULT_ROTATION_OPERATIONS = BlockStateVariantMap.operations(Properties.FACING)
            .register(Direction.DOWN, ROTATE_X_180)
            .register(Direction.UP, NO_OP)
            .register(Direction.NORTH, ROTATE_X_90)
            .register(Direction.SOUTH, ROTATE_X_90.then(ROTATE_Y_180))
            .register(Direction.WEST, ROTATE_X_90.then(ROTATE_Y_270))
            .register(Direction.EAST, ROTATE_X_90.then(ROTATE_Y_90));
    private static final BlockStateVariantMap<ModelVariantOperator> DOWN_DEFAULT_ROTATION_OPERATIONS = Util.make(BlockStateVariantMap.operations(Properties.FACING), map -> createDownDefaultFacingVariantMap(map::register));

    public static final void createDownDefaultFacingVariantMap(BiConsumer<Direction, ModelVariantOperator> builder) {
        builder.accept(Direction.DOWN, NO_OP);
        builder.accept(Direction.UP, ROTATE_X_180);
        builder.accept(Direction.SOUTH, ROTATE_X_90);
        builder.accept(Direction.NORTH, ROTATE_X_90.then(ROTATE_Y_180));
        builder.accept(Direction.EAST, ROTATE_X_90.then(ROTATE_Y_270));
        builder.accept(Direction.WEST, ROTATE_X_90.then(ROTATE_Y_90));
    }

    static UBlockStateModelGenerator create(BlockStateModelGenerator modelGenerator) {
        return new UBlockStateModelGenerator(modelGenerator);
    }

    protected UBlockStateModelGenerator(BlockStateModelGenerator modelGenerator) {
        this(modelGenerator.blockStateCollector, modelGenerator.itemModelOutput, modelGenerator.modelCollector);
    }

    public UBlockStateModelGenerator(
            Consumer<BlockModelDefinitionCreator> blockStateCollector,
            ItemModelOutput itemModelCollector,
            BiConsumer<Identifier, ModelSupplier> modelCollector) {
        super(blockStateCollector, itemModelCollector, (id, modelSupplier) -> {
            if (AIR_BLOCK_ID.equals(id) || AIR_ITEM_ID.equals(id)) {
                throw new IllegalStateException("Registered air id for block model: " + modelSupplier.get().toString());
            }
            modelCollector.accept(id, modelSupplier);
        });
    }

    @Override
    public void register() {
        for (int i = 0; i < Models.STEM_GROWTH_STAGES.length; i++) {
            Models.STEM_GROWTH_STAGES[i].upload(Unicopia.id("block/apple_sprout_stage" + i), TextureMap.stem(Blocks.MELON_STEM), modelCollector);
        }

        // handmade
        registerAll(UBlockStateModelGenerator::registerSimpleState, UBlocks.SHAPING_BENCH, UBlocks.BANANAS);
        registerAll((g, block) -> g.registerParentedItemModel(block, ModelIds.getBlockModelId(block)), UBlocks.SHAPING_BENCH, UBlocks.SURFACE_CHITIN);
        // doors
        registerAll(UBlockStateModelGenerator::registerStableDoor, UBlocks.STABLE_DOOR, UBlocks.DARK_OAK_DOOR, UBlocks.CLOUD_DOOR);
        registerLockingDoor(UBlocks.CRYSTAL_DOOR);

        // cloud blocks
        createCustomTexturePool(UBlocks.CLOUD, TexturedModel.CUBE_ALL).parented(UBlocks.CLOUD, UBlocks.UNSTABLE_CLOUD).slab(UBlocks.CLOUD_SLAB).stairs(UBlocks.CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.ETCHED_CLOUD, TexturedModel.CUBE_ALL).slab(UBlocks.ETCHED_CLOUD_SLAB).stairs(UBlocks.ETCHED_CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.DENSE_CLOUD, TexturedModel.CUBE_ALL).slab(UBlocks.DENSE_CLOUD_SLAB).stairs(UBlocks.DENSE_CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.CLOUD_PLANKS, TexturedModel.CUBE_ALL).slab(UBlocks.CLOUD_PLANK_SLAB).stairs(UBlocks.CLOUD_PLANK_STAIRS);
        createCustomTexturePool(UBlocks.CLOUD_BRICKS, TexturedModel.CUBE_ALL).slab(UBlocks.CLOUD_BRICK_SLAB).stairs(UBlocks.CLOUD_BRICK_STAIRS);
        createTwoStepTexturePool(UBlocks.SOGGY_CLOUD, TexturedModel.CUBE_BOTTOM_TOP.andThen(textures -> textures.put(BOTTOM, ModelIds.getBlockModelId(UBlocks.CLOUD)))).slab(UBlocks.SOGGY_CLOUD_SLAB).stairs(UBlocks.SOGGY_CLOUD_STAIRS);
        registerRotated(UBlocks.CARVED_CLOUD, TexturedModel.CUBE_COLUMN);
        registerPillar(UBlocks.CLOUD_PILLAR);

        registerAll(UBlockStateModelGenerator::registerCompactedBlock, UBlocks.COMPACTED_CLOUD, UBlocks.COMPACTED_CLOUD_BRICKS, UBlocks.COMPACTED_CLOUD_PLANKS, UBlocks.COMPACTED_DENSE_CLOUD, UBlocks.COMPACTED_ETCHED_CLOUD);
        registerChest(UBlocks.CLOUD_CHEST, UBlocks.CLOUD);
        registerFancyBed(UBlocks.CLOUD_BED, UBlocks.CLOUD, true);
        registerFancyBed(UBlocks.CLOTH_BED, Blocks.SPRUCE_PLANKS, false);

        // chitin blocks
        registerTopsoil(UBlocks.SURFACE_CHITIN, UBlocks.CHITIN);
        registerSingleton(UBlocks.CHITIN, BlockModels.CUBE_BOTTOM);
        registerCubeAllModelTexturePool(UBlocks.CHISELLED_CHITIN).family(UBlockFamilies.CHISELED_CHITIN);
        registerCubeAllModelTexturePool(UBlocks.POLISHED_CHITIN).family(UBlockFamilies.POLISHED_CHITIN);
        registerHiveBlock(UBlocks.HIVE);
        registerRotated(UBlocks.CHITIN_SPIKES, BlockModels.SPIKES);
        registerHull(UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.CHISELLED_CHITIN);
        registerHull(UBlocks.POLISHED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.POLISHED_CHITIN);
        registerItemModel(UBlocks.SLIME_PUSTULE.asItem());
        registerSlimeLayers(UBlocks.SLIME);
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(UBlocks.SLIME_PUSTULE)
                .with(BlockStateVariantMap.models(SlimePustuleBlock.SHAPE)
                .generate(state -> createWeightedVariant(ModelIds.getBlockSubModelId(UBlocks.SLIME_PUSTULE, "_" + state.asString())))));
        registerPie(UBlocks.APPLE_PIE);

        // palm wood
        createLogTexturePool(UBlocks.PALM_LOG).log(UBlocks.PALM_LOG).wood(UBlocks.PALM_WOOD);
        createLogTexturePool(UBlocks.STRIPPED_PALM_LOG).log(UBlocks.STRIPPED_PALM_LOG).wood(UBlocks.STRIPPED_PALM_WOOD);
        registerCubeAllModelTexturePool(UBlocks.PALM_PLANKS).family(UBlockFamilies.PALM);
        registerHangingSign(UBlocks.STRIPPED_PALM_LOG, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_WALL_HANGING_SIGN);
        registerSingleton(UBlocks.PALM_LEAVES, TexturedModel.LEAVES);

        // zap wood
        createLogTexturePool(UBlocks.ZAP_LOG)
            .log(UBlocks.ZAP_LOG).wood(UBlocks.ZAP_WOOD)
            .log(UBlocks.WAXED_ZAP_LOG).wood(UBlocks.WAXED_ZAP_WOOD);
        createLogTexturePool(UBlocks.STRIPPED_ZAP_LOG)
            .log(UBlocks.STRIPPED_ZAP_LOG).wood(UBlocks.STRIPPED_ZAP_WOOD)
            .log(UBlocks.WAXED_STRIPPED_ZAP_LOG).wood(UBlocks.WAXED_STRIPPED_ZAP_WOOD);
        registerCubeAllModelTexturePool(UBlocks.ZAP_PLANKS).family(UBlockFamilies.ZAP).parented(UBlocks.ZAP_PLANKS, UBlocks.WAXED_ZAP_PLANKS).family(UBlockFamilies.WAXED_ZAP);
        registerZapLeaves(UBlocks.ZAP_LEAVES);
        registerSingleton(UBlocks.FLOWERING_ZAP_LEAVES, TexturedModel.LEAVES);
        registerStateWithModelReference(UBlocks.ZAP_LEAVES_PLACEHOLDER, Blocks.AIR);

        // golden oak wood
        registerSimpleCubeAll(UBlocks.GOLDEN_OAK_LEAVES);
        createLogTexturePool(UBlocks.GOLDEN_OAK_LOG).log(UBlocks.GOLDEN_OAK_LOG).wood(UBlocks.GOLDEN_OAK_WOOD);
        createLogTexturePool(UBlocks.STRIPPED_GOLDEN_OAK_LOG).log(UBlocks.STRIPPED_GOLDEN_OAK_LOG).wood(UBlocks.STRIPPED_GOLDEN_OAK_WOOD);
        registerCubeAllModelTexturePool(UBlocks.GOLDEN_OAK_PLANKS).family(UBlockFamilies.GOLDEN_OAK);

        // plants
        Tree.REGISTRY.stream().filter(tree -> tree.sapling().isPresent()).forEach(tree -> registerFlowerPotPlantAndItem(tree.sapling().get(), tree.pot().get(), CrossType.NOT_TINTED));
        registerTintableCross(UBlocks.CURING_JOKE, CrossType.NOT_TINTED);
        registerWithStages(UBlocks.GOLD_ROOT, Properties.AGE_7, BlockModels.CROP, 0, 0, 1, 1, 2, 2, 2, 3);

        registerTallCrop(UBlocks.PINEAPPLE, Properties.AGE_7, Properties.BLOCK_HALF,
                new int[] { 0, 1, 2, 3, 4, 5, 5, 6 },
                new int[] { 0, 0, 1, 2, 3, 4, 5, 6 }
        );
        registerPlunderVine(UBlocks.PLUNDER_VINE, UBlocks.PLUNDER_VINE_BUD);

        // leaves
        registerAll(UBlockStateModelGenerator::registerFloweringLeaves, UBlocks.GREEN_APPLE_LEAVES, UBlocks.SOUR_APPLE_LEAVES, UBlocks.SWEET_APPLE_LEAVES);
        registerAll(UBlockStateModelGenerator::registerSprout, UBlocks.GREEN_APPLE_SPROUT, UBlocks.SOUR_APPLE_SPROUT, UBlocks.SWEET_APPLE_SPROUT, UBlocks.GOLDEN_OAK_SPROUT);
        registerStateWithModelReference(UBlocks.MANGO_LEAVES, Blocks.JUNGLE_LEAVES);
        registerTintedItemModel(UBlocks.MANGO_LEAVES, ModelIds.getBlockModelId(Blocks.JUNGLE_LEAVES), BlockTintSource.INSTANCE);

        // fruit
        UModelProvider.FRUITS.forEach(block -> registerSingleton(block, BlockModels.FRUIT));

        // shells
        registerAll(UBlockStateModelGenerator::registerShell, UBlocks.CLAM_SHELL, UBlocks.TURRET_SHELL, UBlocks.SCALLOP_SHELL);
        // other
        registerSimpleCubeAll(UBlocks.WORM_BLOCK);
        registerBuiltinWithParticle(UBlocks.WEATHER_VANE, UBlocks.WEATHER_VANE.asItem());
        registerItemModel(UBlocks.WEATHER_VANE.asItem());
        registerWithStages(UBlocks.FROSTED_OBSIDIAN, Properties.AGE_3, BlockModels.CUBE_ALL, 0, 1, 2, 3);
        registerWithStagesBuiltinModels(UBlocks.ROCKS, Properties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        registerWithStagesBuiltinModels(UBlocks.MYSTERIOUS_EGG, PileBlock.COUNT, 1, 2, 3);
        FireModels.registerSoulFire(this, UBlocks.SPECTRAL_FIRE, Blocks.SOUL_FIRE);

        registerJar(UBlocks.JAR);
        registerWeatherJar(UBlocks.CLOUD_JAR);
        registerWeatherJar(UBlocks.STORM_JAR);
        registerWeatherJar(UBlocks.ZAP_JAR);
        registerWeatherJar(UBlocks.LIGHTNING_JAR);

        TintedBlock.REGISTRY.forEach(block -> {
            if (block == UBlocks.MANGO_LEAVES || block instanceof SproutBlock) {
                Unicopia.LOGGER.info("[Skipped] Tinted block: " + block);
                return;
            }
            Unicopia.LOGGER.info("Tinted block: " + block);
            registerTintedItemModel(block, ModelIds.getBlockModelId(block), BlockTintSource.INSTANCE);
        });
    }

    public void registerWeatherJar(Block jar) {
        blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(jar)
                .with(createWeightedVariant(BlockModels.TEMPLATE_JAR))
                .with(createWeightedVariant(ModelIds.getBlockSubModelId(jar, "_filling"))));
        registerItemModel(jar.asItem());
    }

    public void registerJar(Block jar) {
        blockStateCollector.accept(createSingletonBlockState(jar, createWeightedVariant(BlockModels.TEMPLATE_JAR)));
    }

    @SafeVarargs
    public final <T> UBlockStateModelGenerator registerAll(BiConsumer<? super UBlockStateModelGenerator, T> register, T... blocks) {
        for (T block : blocks) {
            register.accept(this, block);
        }
        return this;
    }

    @Override
    public void registerParentedItemModel(Block block, Identifier parentModelId) {
        if (block.asItem() != Items.AIR) {
            super.registerParentedItemModel(block, parentModelId);
        }
    }

    public BlockTexturePool createCustomTexturePool(Block block, TexturedModel.Factory modelFactory) {
        final TexturedModel texturedModel = modelFactory.get(block);
        final TextureMap textures = texturedModel.getTextures();
        return (new BlockTexturePool(textures) {
            @Override
            public BlockTexturePool stairs(Block block) {
                TextureMap textMap = textures.copyAndAdd(BlockModels.STEP, textures.getTexture(SIDE));
                WeightedVariant inner = createWeightedVariant(BlockModels.INNER_STAIRS.upload(block, textMap, modelCollector));
                Identifier straight = BlockModels.STRAIGHT_STAIRS.upload(block, textMap, modelCollector);
                WeightedVariant outer = createWeightedVariant(BlockModels.OUTER_STAIRS.upload(block, textMap, modelCollector));
                blockStateCollector.accept(createStairsBlockState(block, inner, createWeightedVariant(straight), outer));
                registerParentedItemModel(block, straight);
                return this;
            }
        }).base(block, texturedModel.getModel());
    }

    public BlockTexturePool createTwoStepTexturePool(Block block, TexturedModel.Factory modelFactory) {
        final TexturedModel texturedModel = modelFactory.get(block);
        final TextureMap textures = texturedModel.getTextures();
        final Identifier baseModelId = ModelIds.getBlockModelId(block);
        final Identifier twoStepTexture = ModelIds.getBlockSubModelId(block, "_slab_side");
        return (new BlockTexturePool(textures) {
            @Override
            public BlockTexturePool stairs(Block block) {
                TextureMap textMap = textures.copyAndAdd(BlockModels.STEP, twoStepTexture);
                WeightedVariant inner = createWeightedVariant(BlockModels.INNER_STAIRS.upload(block, textMap, modelCollector));
                Identifier straight = BlockModels.STRAIGHT_STAIRS.upload(block, textMap, modelCollector);
                WeightedVariant outer = createWeightedVariant(BlockModels.OUTER_STAIRS.upload(block, textMap, modelCollector));
                blockStateCollector.accept(createStairsBlockState(block, inner, createWeightedVariant(straight), outer));
                registerParentedItemModel(block, straight);
                return this;
            }

            @Override
            public BlockTexturePool slab(Block block) {
                TextureMap textMap = textures.copyAndAdd(TextureKey.SIDE, twoStepTexture);
                Identifier lower = Models.SLAB.upload(block, textMap, modelCollector);
                WeightedVariant upper = createWeightedVariant(Models.SLAB_TOP.upload(block, textMap, modelCollector));
                blockStateCollector.accept(createSlabBlockState(block, createWeightedVariant(lower), upper, createWeightedVariant(baseModelId)));
                registerParentedItemModel(block, lower);
                return this;
            }
        }).base(block, texturedModel.getModel());
    }

    public void registerTopsoil(Block block, Block dirt) {
        TexturedModel model = TexturedModel.CUBE_BOTTOM_TOP.get(dirt);
        registerTopSoil(block,
                createWeightedVariant(model.upload(block, modelCollector)),
                createWeightedVariant(Models.CUBE_BOTTOM_TOP.upload(dirt, "_snow", model.getTextures()
                        .copyAndAdd(SIDE, ModelIds.getBlockSubModelId(dirt, "_side_snow_covered")
                ), modelCollector))
        );
    }

    public void registerRotated(Block block, TexturedModel.Factory modelFactory) {
        Identifier modelId = modelFactory.get(block).upload(block, modelCollector);
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block, createWeightedVariant(modelId))
                .coordinate(UP_DEFAULT_ROTATION_OPERATIONS));
    }

    public void registerPlunderVine(Block plant, Block bud) {
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(bud, createWeightedVariant(ModelIds.getBlockModelId(bud)))
                .coordinate(DOWN_DEFAULT_ROTATION_OPERATIONS));

        var supplier = MultipartBlockModelDefinitionCreator.create(plant);
        String[] stages = { "", "_2", "_3", "_4", "_4"};
        Properties.AGE_4.getValues().forEach(age -> {
            Identifier modelId = ModelIds.getBlockSubModelId(plant, "_branch" + stages[age]);
            createDownDefaultFacingVariantMap((direction, variant) -> {
                supplier.with(createMultipartConditionBuilder().put(Properties.AGE_4, age).put(ConnectingBlock.FACING_PROPERTIES.get(direction), true),
                        createWeightedVariant(variant.apply(createModelVariant(modelId)))
                );
            });
        });

        blockStateCollector.accept(supplier);
        registerParentedItemModel(bud, ModelIds.getBlockModelId(bud));
    }

    public void registerCompactedBlock(Block block) {
        for (Model model : BlockModels.FLATTENED_MODELS) {
            model.upload(block, TextureMap.all(ModelIds.getBlockModelId(block).withPath(p -> p.replace("compacted_", ""))), modelCollector);
        }
        MultipartBlockModelDefinitionCreator supplier = MultipartBlockModelDefinitionCreator.create(block);
        for (byte i = 0; i < BlockModels.FLATTENED_MODEL_ROTATIONS.length; i++) {
            final BooleanProperty yAxis = (i & 0b100) == 0 ? Properties.DOWN : Properties.UP;
            final BooleanProperty xAxis = (i & 0b010) == 0 ? Properties.NORTH: Properties.SOUTH;
            final BooleanProperty zAxis = (i & 0b001) == 0 ? Properties.EAST : Properties.WEST;
            final AxisRotation xRot = yAxis == Properties.DOWN ? R0 : R180;
            final AxisRotation yRot = BlockModels.FLATTENED_MODEL_ROTATIONS[i];
            final String[] suffexes = yRot.ordinal() % 2 == 0 ? BlockModels.FLATTENED_MODEL_SUFFEXES : BlockModels.FLATTENED_MODEL_SUFFEXES_ROT;
            for (byte v = 0; v < suffexes.length; v++) {
                supplier.with(createMultipartConditionBuilder()
                            .put(yAxis, (v & 0b100) != 0)
                            .put(xAxis, (v & 0b010) != 0)
                            .put(zAxis, (v & 0b001) != 0), createWeightedVariant(createModelVariant(ModelIds.getBlockSubModelId(block, "_corner_" + suffexes[v]))
                        .withUVLock(true)
                        .withRotationX(xRot).withRotationY(yRot))
                );
            }
        }
        blockStateCollector.accept(supplier);
    }

    public void registerChest(Block chest, Block particleSource) {
        registerBuiltinWithParticle(chest, particleSource);
        Item item = chest.asItem();
        Identifier identifier = Models.TEMPLATE_CHEST.upload(item, TextureMap.particle(particleSource), modelCollector);
        itemModelOutput.accept(item, ItemModels.special(identifier, new CloudChestModelRenderer.Unbaked(CloudChestModelRenderer.TEXTURE, 0)));
    }

    public void registerFancyBed(Block bed, Block particleSource, boolean translucent) {
        registerBuiltinWithParticle(bed, ModelIds.getBlockModelId(particleSource));
        Item item = bed.asItem();
        Identifier itemModelId = Models.TEMPLATE_BED.upload(ModelIds.getItemModelId(item), TextureMap.particle(particleSource), modelCollector);
        this.itemModelOutput.accept(item, ItemModels.special(itemModelId, new CloudBedModelRenderer.Unbaked(Unicopia.id("textures/entity/bed/" + ((FancyBedBlock)bed).getBase() + ".png"), translucent)));
    }

    public void registerStableDoor(Block door) {
        var variants = BlockStateVariantMap.models(Properties.HORIZONTAL_FACING, Properties.DOUBLE_BLOCK_HALF, Properties.DOOR_HINGE, Properties.OPEN);
        registerItemModel(door.asItem());
        buildDoorStateModels(door, "", variants::register);
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(door).with(variants));
    }

    public void registerLockingDoor(Block door) {
        var variants = BlockStateVariantMap.models(Properties.HORIZONTAL_FACING, Properties.DOUBLE_BLOCK_HALF, Properties.DOOR_HINGE, Properties.OPEN, Properties.LOCKED);
        registerItemModel(door.asItem());
        buildDoorStateModels(door, "", (facing, half, hinge, open, map) -> variants.register(facing, half, hinge, open, false, map));
        buildDoorStateModels(door, "_locked", (facing, half, hinge, open, map) -> variants.register(facing, half, hinge, open, true, map));
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(door).with(variants));
    }

    private void buildDoorStateModels(Block door, String suffex, DoorStateConsumer variants) {
        TextureMap topTextures = new TextureMap()
                .put(TextureKey.TOP, TextureMap.getSubId(door, "_top" + suffex))
                .put(TextureKey.BOTTOM, TextureMap.getSubId(door, "_bottom" + suffex));
        TextureMap bottomTextures = topTextures.copyAndAdd(TextureKey.TOP, topTextures.getTexture(TextureKey.BOTTOM));
        fillStableDoorVariantMap(variants, DoubleBlockHalf.LOWER,
                BlockModels.DOOR_LEFT.upload(door, "_bottom_left" + suffex, bottomTextures, modelCollector),
                BlockModels.DOOR_RIGHT.upload(door, "_bottom_right" + suffex, bottomTextures, modelCollector)
        );
        fillStableDoorVariantMap(variants, DoubleBlockHalf.UPPER,
                BlockModels.DOOR_LEFT.upload(door, "_top_left" + suffex, topTextures, modelCollector),
                BlockModels.DOOR_RIGHT.upload(door, "_top_right" + suffex, topTextures, modelCollector)
        );
    }

    private static void fillStableDoorVariantMap(
            DoorStateConsumer variantMap,
            DoubleBlockHalf targetHalf, Identifier leftModelId, Identifier rightModelId) {
        fillStableDoorVariantMap(variantMap, targetHalf, DoorHinge.LEFT, false, R0, leftModelId);
        fillStableDoorVariantMap(variantMap, targetHalf, DoorHinge.RIGHT, false, R0, rightModelId);

        fillStableDoorVariantMap(variantMap, targetHalf, DoorHinge.LEFT, true, R90, rightModelId);
        fillStableDoorVariantMap(variantMap, targetHalf, DoorHinge.RIGHT, true, R270, leftModelId);
    }

    public static void fillStableDoorVariantMap(
            DoorStateConsumer variantMap,
            DoubleBlockHalf targetHalf,
            DoorHinge hinge, boolean open, AxisRotation rotation,
            Identifier modelId) {

        for (int i = 0; i < BlockRotation.DIRECTIONS.length; i++) {
            variantMap.register(BlockRotation.DIRECTIONS[i], targetHalf, hinge, open, createWeightedVariant(createModelVariant(modelId).withRotationY(BlockRotation.cycle(rotation, i))));
        }
    }

    interface DoorStateConsumer {
        void register(Direction direction, DoubleBlockHalf half, DoorHinge hinge, boolean open, WeightedVariant variant);
    }

    public void registerPillar(Block pillar) {
        TextureMap textures = new TextureMap()
                .put(SIDE, ModelIds.getBlockSubModelId(pillar, "_side"))
                .put(TOP, ModelIds.getBlockSubModelId(pillar, "_lip"))
                .put(BOTTOM, ModelIds.getBlockSubModelId(pillar, "_end"))
                .put(END, ModelIds.getBlockSubModelId(pillar, "_side_end"));
        Identifier middle = BlockModels.TEMPLATE_PILLAR.upload(pillar, textures, modelCollector);
        Identifier end = BlockModels.TEMPLATE_PILLAR_END.upload(pillar, textures, modelCollector);
        blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(pillar)
                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.X), createWeightedVariant(createModelVariant(middle).withRotationX(R90).withRotationY(R90)))
                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.X).put(Properties.NORTH, false), createWeightedVariant(createModelVariant(end).withRotationX(R270).withRotationY(R90)))
                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.X).put(Properties.SOUTH, false), createWeightedVariant(createModelVariant(end).withRotationX(R90).withRotationY(R90)))

                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.Y), createWeightedVariant(middle))
                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.Y).put(Properties.NORTH, false), createWeightedVariant(createModelVariant(end).withRotationX(R180)))
                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.Y).put(Properties.SOUTH, false), createWeightedVariant(end))

                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.Z), createWeightedVariant(createModelVariant(middle).withRotationX(R90)))
                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.Z).put(Properties.NORTH, false), createWeightedVariant(createModelVariant(end).withRotationX(R90)))
                .with(createMultipartConditionBuilder().put(Properties.AXIS, Direction.Axis.Z).put(Properties.SOUTH, false), createWeightedVariant(createModelVariant(end).withRotationX(R270)))
        );
        registerItemModel(pillar.asItem(), TEMPLATE_PILLAR.upload(ModelIds.getItemModelId(pillar.asItem()), textures, modelCollector));
    }

    public void registerHiveBlock(Block hive) {
        Identifier core = ModelIds.getBlockSubModelId(hive, "_core");
        Identifier side = ModelIds.getBlockSubModelId(hive, "_side");
        blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(hive)
                .with(createWeightedVariant(core))
                .with(createMultipartConditionBuilder().put(Properties.NORTH, true), createWeightedVariant(createModelVariant(side).withUVLock(true)))
                .with(createMultipartConditionBuilder().put(Properties.EAST, true), createWeightedVariant(createModelVariant(side).withUVLock(true).withRotationY(R90)))
                .with(createMultipartConditionBuilder().put(Properties.SOUTH, true), createWeightedVariant(createModelVariant(side).withUVLock(true).withRotationY(R180)))
                .with(createMultipartConditionBuilder().put(Properties.WEST, true), createWeightedVariant(createModelVariant(side).withUVLock(true).withRotationY(R270)))
                .with(createMultipartConditionBuilder().put(Properties.DOWN, true), createWeightedVariant(createModelVariant(side).withUVLock(true).withRotationX(R90)))
                .with(createMultipartConditionBuilder().put(Properties.UP, true), createWeightedVariant(createModelVariant(side).withUVLock(true).withRotationX(R270))));
        registerItemModel(hive.asItem(), Models.CUBE_ALL.upload(ModelIds.getItemModelId(hive.asItem()), TextureMap.all(side), modelCollector));
    }

    public void registerWithStages(Block crop, Property<Integer> ageProperty, BlockModels.Factory modelFactory, int ... stages) {
        if (ageProperty.getValues().size() != stages.length) {
            throw new IllegalArgumentException();
        }
        int offset = ageProperty.getValues().iterator().next();
        Int2ObjectOpenHashMap<Identifier> uploadedModels = new Int2ObjectOpenHashMap<>();
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(crop)
                .with(BlockStateVariantMap.models(ageProperty)
                .generate(age -> createWeightedVariant(uploadedModels.computeIfAbsent(stages[age - offset], stage -> {
                    return modelFactory.upload(crop, "_stage" + stage, modelCollector);
                })))));
    }

    public void registerWithStagesBuiltinModels(Block crop, Property<Integer> ageProperty, int ... stages) {
        if (ageProperty.getValues().size() != stages.length) {
            throw new IllegalArgumentException();
        }
        int offset = ageProperty.getValues().iterator().next();
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(crop)
                .with(BlockStateVariantMap.models(ageProperty)
                .generate(age -> createWeightedVariant(ModelIds.getBlockSubModelId(crop, "_stage" + stages[age - offset])))));
        registerItemModel(crop.asItem());
    }

    public <T extends Enum<T> & StringIdentifiable> void registerTallCrop(Block crop,
            Property<Integer> ageProperty,
            EnumProperty<T> partProperty,
            int[] ... ageTextureIndices) {
        Map<String, Identifier> uploadedModels = new HashMap<>();
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(crop).with(BlockStateVariantMap.models(partProperty, ageProperty).generate((part, age) -> {
            int i = ageTextureIndices[part.ordinal()][age];
            Identifier identifier = uploadedModels.computeIfAbsent("_" + part.asString() + "_stage" + i, variant -> createSubModel(crop, variant, Models.CROSS, TextureMap::cross));
            return createWeightedVariant(identifier);
        })));
    }

    public void registerSlimeLayers(Block block) {
        Identifier fullModel = ModelIds.getBlockModelId(Blocks.SLIME_BLOCK);
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block)
                .with(BlockStateVariantMap.models(Properties.LAYERS)
                        .generate(height -> createWeightedVariant(height < 8 ? ModelIds.getBlockSubModelId(block, "_height" + height * 2) : fullModel))));
        registerParentedItemModel(block, ModelIds.getBlockSubModelId(block, "_height2"));
    }

    public void registerPie(Block pie) {
        TextureMap textures = new TextureMap()
                .put(TOP, ModelIds.getBlockSubModelId(pie, "_top"))
                .put(BOTTOM, ModelIds.getBlockSubModelId(pie, "_bottom"))
                .put(SIDE, ModelIds.getBlockSubModelId(pie, "_side"))
                .put(INSIDE, ModelIds.getBlockSubModelId(pie, "_inside"));
        TextureMap stompedTextures = textures.copyAndAdd(TOP, ModelIds.getBlockSubModelId(pie, "_top_stomped"));
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(pie).with(BlockStateVariantMap.models(PieBlock.BITES, PieBlock.STOMPED).generate((bites, stomped) -> {
            return createWeightedVariant(BlockModels.PIE_MODELS[bites].upload(pie, (stomped ? "_stomped" : ""), stomped ? stompedTextures : textures, modelCollector));
        })));
    }

    public void registerFloweringLeaves(Block block) {
        Identifier baseModel = TexturedModel.LEAVES.upload(block, modelCollector);
        Identifier floweringModel = Models.CUBE_ALL.upload(block, "_flowering", TextureMap.of(ALL, ModelIds.getBlockSubModelId(block, "_flowering")), modelCollector);
        blockStateCollector.accept(MultipartBlockModelDefinitionCreator.create(block)
                .with(createWeightedVariant(baseModel))
                .with(createMultipartConditionBuilder().put(FruitBearingBlock.STAGE, FruitBearingBlock.Stage.FLOWERING), createWeightedVariant(floweringModel)));
    }

    public void registerZapLeaves(Block block) {
        Identifier baseModel = TexturedModel.LEAVES.upload(block, modelCollector);
        Identifier floweringModel = Registries.BLOCK.getId(block).withPrefixedPath("block/flowering_");
        Identifier airModel = ModelIds.getBlockModelId(Blocks.AIR);
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(block)
                .with(BlockStateVariantMap.models(ZapAppleLeavesBlock.STAGE)
                .generate(stage -> createWeightedVariant(switch (stage) {
                            case HIBERNATING -> airModel;
                            case FLOWERING -> floweringModel;
                            default -> baseModel;
                        }))));
    }

    public void registerSprout(Block sprout) {
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(sprout)
                .with(BlockStateVariantMap.models(Properties.AGE_7)
                .generate(age -> createWeightedVariant(Unicopia.id("block/apple_sprout_stage" + age)))));
        registerItemModel(sprout.asItem());
    }

    public void registerShell(Block shell) {
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(shell)
                .with(BlockStateVariantMap.models(ShellsBlock.COUNT)
                .generate(count -> createWeightedVariant(BlockModels.SHELL_MODELS[count - 1].upload(shell, TextureMap.of(BlockModels.SHELL, Registries.BLOCK.getId(shell).withPrefixedPath("item/")), modelCollector)))));
    }

    public void registerHull(Block block, Block core, Block shell) {
        blockStateCollector.accept(VariantsBlockModelDefinitionCreator.of(
                block,
                createWeightedVariant(Models.CUBE_BOTTOM_TOP.upload(block, new TextureMap()
                        .put(BOTTOM, ModelIds.getBlockModelId(core))
                        .put(TOP, ModelIds.getBlockModelId(shell))
                        .put(SIDE, ModelIds.getBlockSubModelId(shell, "_half")), modelCollector))
        ).coordinate(UP_DEFAULT_ROTATION_OPERATIONS));
    }
}
