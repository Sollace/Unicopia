package com.minelittlepony.unicopia.datagen.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.block.FruitBearingBlock;
import com.minelittlepony.unicopia.block.PieBlock;
import com.minelittlepony.unicopia.block.PileBlock;
import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.SlimePustuleBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesBlock;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.server.world.Tree;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateSupplier;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.MultipartBlockStateSupplier;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.TexturedModel;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.data.client.When;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

import static net.minecraft.data.client.TextureKey.*;
import static net.minecraft.data.client.VariantSettings.*;
import static net.minecraft.data.client.VariantSettings.Rotation.*;

public class UBlockStateModelGenerator extends BlockStateModelGenerator {
    static final Identifier AIR_BLOCK_ID = Identifier.ofVanilla("block/air");
    static final Identifier AIR_ITEM_ID = Identifier.ofVanilla("item/air");

    static UBlockStateModelGenerator create(BlockStateModelGenerator modelGenerator) {
        return new UBlockStateModelGenerator(modelGenerator.blockStateCollector, modelGenerator.modelCollector, modelGenerator::excludeFromSimpleItemModelGeneration);
    }

    protected UBlockStateModelGenerator(BlockStateModelGenerator modelGenerator) {
        this(modelGenerator.blockStateCollector, modelGenerator.modelCollector, modelGenerator::excludeFromSimpleItemModelGeneration);
    }

    public UBlockStateModelGenerator(
            Consumer<BlockStateSupplier> blockStateCollector,
            BiConsumer<Identifier, Supplier<JsonElement>> modelCollector,
            Consumer<Block> simpleItemModelExemptionCollector) {
        super(blockStateCollector, (id, jsonSupplier) -> {
            if (AIR_BLOCK_ID.equals(id) || AIR_ITEM_ID.equals(id)) {
                throw new IllegalStateException("Registered air id for block model: " + jsonSupplier.get().toString());
            }
            modelCollector.accept(id, jsonSupplier);
        }, item -> simpleItemModelExemptionCollector.accept(Block.getBlockFromItem(item)));
    }

    @Override
    public void register() {
        for (int i = 0; i < Models.STEM_GROWTH_STAGES.length; i++) {
            Models.STEM_GROWTH_STAGES[i].upload(Unicopia.id("block/apple_sprout_stage" + i), TextureMap.stem(Blocks.MELON_STEM), modelCollector);
        }

        // handmade
        registerAll((g, block) -> g.registerParentedItemModel(block, ModelIds.getBlockModelId(block)), UBlocks.SHAPING_BENCH, UBlocks.SURFACE_CHITIN);
        registerAll(UBlockStateModelGenerator::registerSimpleState, UBlocks.SHAPING_BENCH, UBlocks.BANANAS);
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
        registerFancyBed(UBlocks.CLOUD_BED, UBlocks.CLOUD);
        registerFancyBed(UBlocks.CLOTH_BED, Blocks.SPRUCE_PLANKS);

        // chitin blocks
        registerTopsoil(UBlocks.SURFACE_CHITIN, UBlocks.CHITIN);
        registerHollow(UBlocks.CHITIN);
        registerCubeAllModelTexturePool(UBlocks.CHISELLED_CHITIN).family(UBlockFamilies.CHISELED_CHITIN);
        registerHiveBlock(UBlocks.HIVE);
        registerRotated(UBlocks.CHITIN_SPIKES, BlockModels.SPIKES);
        registerHull(UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.CHISELLED_CHITIN);
        registerItemModel(UBlocks.SLIME_PUSTULE.asItem());
        blockStateCollector.accept(VariantsBlockStateSupplier.create(UBlocks.SLIME_PUSTULE)
                .coordinate(BlockStateVariantMap.create(SlimePustuleBlock.SHAPE)
                .register(state -> BlockStateVariant.create().put(MODEL, ModelIds.getBlockSubModelId(UBlocks.SLIME_PUSTULE, "_" + state.asString())))));
        registerPie(UBlocks.APPLE_PIE);

        // palm wood
        registerLog(UBlocks.PALM_LOG).log(UBlocks.PALM_LOG).wood(UBlocks.PALM_WOOD);
        registerLog(UBlocks.STRIPPED_PALM_LOG).log(UBlocks.STRIPPED_PALM_LOG).wood(UBlocks.STRIPPED_PALM_WOOD);
        registerCubeAllModelTexturePool(UBlocks.PALM_PLANKS).family(UBlockFamilies.PALM);
        registerHangingSign(UBlocks.STRIPPED_PALM_LOG, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_WALL_HANGING_SIGN);
        registerSingleton(UBlocks.PALM_LEAVES, TexturedModel.LEAVES);

        // zap wood
        registerLog(UBlocks.ZAP_LOG)
            .log(UBlocks.ZAP_LOG).wood(UBlocks.ZAP_WOOD)
            .log(UBlocks.WAXED_ZAP_LOG).wood(UBlocks.WAXED_ZAP_WOOD);
        registerLog(UBlocks.STRIPPED_ZAP_LOG)
            .log(UBlocks.STRIPPED_ZAP_LOG).wood(UBlocks.STRIPPED_ZAP_WOOD)
            .log(UBlocks.WAXED_STRIPPED_ZAP_LOG).wood(UBlocks.WAXED_STRIPPED_ZAP_WOOD);
        registerCubeAllModelTexturePool(UBlocks.ZAP_PLANKS).family(UBlockFamilies.ZAP).parented(UBlocks.ZAP_PLANKS, UBlocks.WAXED_ZAP_PLANKS).family(UBlockFamilies.WAXED_ZAP);
        registerZapLeaves(UBlocks.ZAP_LEAVES);
        registerSingleton(UBlocks.FLOWERING_ZAP_LEAVES, TexturedModel.LEAVES);
        registerStateWithModelReference(UBlocks.ZAP_LEAVES_PLACEHOLDER, Blocks.AIR);

        // golden oak wood
        registerSimpleCubeAll(UBlocks.GOLDEN_OAK_LEAVES);
        registerLog(UBlocks.GOLDEN_OAK_LOG).log(UBlocks.GOLDEN_OAK_LOG);

        // plants
        Tree.REGISTRY.stream().filter(tree -> tree.sapling().isPresent()).forEach(tree -> registerFlowerPotPlant(tree.sapling().get(), tree.pot().get(), TintType.NOT_TINTED));
        registerTintableCross(UBlocks.CURING_JOKE, TintType.NOT_TINTED);
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
        registerParentedItemModel(UBlocks.MANGO_LEAVES, ModelIds.getBlockModelId(Blocks.JUNGLE_LEAVES));

        // fruit
        UModelProvider.FRUITS.forEach((block, item) -> registerSingleton(block, TextureMap.cross(ModelIds.getItemModelId(item)), BlockModels.FRUIT));

        // bales
        registerAll((g, block) -> g.registerBale(Unicopia.id(block.getLeft().getPath().replace("bale", "block")), block.getLeft(), block.getRight()),
                new Pair<>(Identifier.ofVanilla("hay_block"), "_top"),
                new Pair<>(Identifier.of("farmersdelight", "rice_bale"), "_top"),
                new Pair<>(Identifier.of("farmersdelight", "straw_bale"), "_end")
        );
        // shells
        registerAll(UBlockStateModelGenerator::registerShell, UBlocks.CLAM_SHELL, UBlocks.TURRET_SHELL, UBlocks.SCALLOP_SHELL);
        // other
        registerSimpleCubeAll(UBlocks.WORM_BLOCK);
        registerBuiltinWithParticle(UBlocks.WEATHER_VANE, UBlocks.WEATHER_VANE.asItem());
        registerWithStages(UBlocks.FROSTED_OBSIDIAN, Properties.AGE_3, BlockModels.CUBE_ALL, 0, 1, 2, 3);
        registerWithStagesBuiltinModels(UBlocks.ROCKS, Properties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        registerWithStagesBuiltinModels(UBlocks.MYSTERIOUS_EGG, PileBlock.COUNT, 1, 2, 3);
        registerItemModel(UBlocks.MYSTERIOUS_EGG.asItem());
        FireModels.registerSoulFire(this, UBlocks.SPECTRAL_FIRE, Blocks.SOUL_FIRE);

        blockStateCollector.accept(createSingletonBlockState(UBlocks.JAR, BlockModels.TEMPLATE_JAR));
        registerWeatherJar(UBlocks.CLOUD_JAR);
        registerWeatherJar(UBlocks.STORM_JAR);
        registerWeatherJar(UBlocks.ZAP_JAR);
        registerWeatherJar(UBlocks.LIGHTNING_JAR);
    }

    public void registerWeatherJar(Block jar) {
        blockStateCollector.accept(MultipartBlockStateSupplier.create(jar)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, BlockModels.TEMPLATE_JAR))
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, ModelIds.getBlockSubModelId(jar, "_filling"))));
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
        Item item = block.asItem();
        if (item != Items.AIR) {
            registerParentedItemModel(item, parentModelId);
        }
    }

    public BlockTexturePool createCustomTexturePool(Block block, TexturedModel.Factory modelFactory) {
        final TexturedModel texturedModel = modelFactory.get(block);
        final TextureMap textures = texturedModel.getTextures();
        return (new BlockTexturePool(textures) {
            @Override
            public BlockTexturePool stairs(Block block) {
                TextureMap textMap = textures.copyAndAdd(BlockModels.STEP, textures.getTexture(SIDE));
                Identifier inner = BlockModels.INNER_STAIRS.upload(block, textMap, modelCollector);
                Identifier straight = BlockModels.STRAIGHT_STAIRS.upload(block, textMap, modelCollector);
                Identifier outer = BlockModels.OUTER_STAIRS.upload(block, textMap, modelCollector);
                blockStateCollector.accept(BlockStateModelGenerator.createStairsBlockState(block, inner, straight, outer));
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
                Identifier inner = BlockModels.INNER_STAIRS.upload(block, textMap, modelCollector);
                Identifier straight = BlockModels.STRAIGHT_STAIRS.upload(block, textMap, modelCollector);
                Identifier outer = BlockModels.OUTER_STAIRS.upload(block, textMap, modelCollector);
                blockStateCollector.accept(BlockStateModelGenerator.createStairsBlockState(block, inner, straight, outer));
                registerParentedItemModel(block, straight);
                return this;
            }

            @Override
            public BlockTexturePool slab(Block block) {
                TextureMap textMap = textures.copyAndAdd(SIDE, twoStepTexture);
                Identifier lower = Models.SLAB.upload(block, textMap, modelCollector);
                Identifier upper = Models.SLAB_TOP.upload(block, textMap, modelCollector);
                blockStateCollector.accept(BlockStateModelGenerator.createSlabBlockState(block, lower, upper, baseModelId));
                registerParentedItemModel(block, lower);
                return this;
            }
        }).base(block, texturedModel.getModel());
    }

    public void registerTopsoil(Block block, Block dirt) {
        TexturedModel model = TexturedModel.CUBE_BOTTOM_TOP.get(dirt);
        registerTopSoil(block,
                model.upload(block, modelCollector),
                BlockStateVariant.create().put(MODEL, Models.CUBE_BOTTOM_TOP.upload(dirt, "_snow", model.getTextures()
                        .copyAndAdd(SIDE, ModelIds.getBlockSubModelId(dirt, "_side_snow_covered")
                ), modelCollector))
        );
    }

    public void registerHollow(Block block) {
        Identifier outside = ModelIds.getBlockModelId(UBlocks.CHITIN);
        Identifier inside = ModelIds.getBlockSubModelId(UBlocks.CHITIN, "_bottom");
        registerSingleton(UBlocks.CHITIN, new TextureMap()
                .put(SIDE, outside)
                .put(TOP, outside)
                .put(BOTTOM, inside), Models.CUBE_BOTTOM_TOP);
    }

    public void registerRotated(Block block, TexturedModel.Factory modelFactory) {
        Identifier modelId = modelFactory.get(block).upload(block, modelCollector);
        blockStateCollector.accept(VariantsBlockStateSupplier.create(block, BlockStateVariant.create()
                .put(MODEL, modelId))
                .coordinate(createUpDefaultFacingVariantMap()));
    }

    public void registerPlunderVine(Block plant, Block bud) {
        var rotationVariants = BlockStateVariantMap.create(Properties.FACING);
        createDownDefaultFacingVariantMap(rotationVariants::register);
        blockStateCollector.accept(VariantsBlockStateSupplier.create(bud, BlockStateVariant.create()
                .put(MODEL, ModelIds.getBlockModelId(bud)))
                .coordinate(rotationVariants));

        var supplier = MultipartBlockStateSupplier.create(plant);
        String[] stages = { "", "_2", "_3", "_4", "_4"};
        Properties.AGE_4.getValues().forEach(age -> {
            Identifier modelId = ModelIds.getBlockSubModelId(plant, "_branch" + stages[age]);
            createDownDefaultFacingVariantMap((direction, variant) -> {
                supplier.with(When.create().set(Properties.AGE_4, age).set(ConnectingBlock.FACING_PROPERTIES.get(direction), true), variant.put(MODEL, modelId));
            });
        });

        blockStateCollector.accept(supplier);
        registerParentedItemModel(bud, ModelIds.getBlockModelId(bud));
    }

    public final void createDownDefaultFacingVariantMap(BiConsumer<Direction, BlockStateVariant> builder) {
        builder.accept(Direction.DOWN, BlockStateVariant.create());
        builder.accept(Direction.UP, BlockStateVariant.create().put(X, R180));
        builder.accept(Direction.SOUTH, BlockStateVariant.create().put(X, R90));
        builder.accept(Direction.NORTH, BlockStateVariant.create().put(X, R90).put(Y, R180));
        builder.accept(Direction.EAST, BlockStateVariant.create().put(X, R90).put(Y, R270));
        builder.accept(Direction.WEST, BlockStateVariant.create().put(X, R90).put(Y, R90));
    }

    public void registerCompactedBlock(Block block) {
        for (Model model : BlockModels.FLATTENED_MODELS) {
            model.upload(block, TextureMap.all(ModelIds.getBlockModelId(block).withPath(p -> p.replace("compacted_", ""))), modelCollector);
        }
        MultipartBlockStateSupplier supplier = MultipartBlockStateSupplier.create(block);
        for (byte i = 0; i < BlockModels.FLATTENED_MODEL_ROTATIONS.length; i++) {
            final BooleanProperty yAxis = (i & 0b100) == 0 ? Properties.DOWN : Properties.UP;
            final BooleanProperty xAxis = (i & 0b010) == 0 ? Properties.NORTH: Properties.SOUTH;
            final BooleanProperty zAxis = (i & 0b001) == 0 ? Properties.EAST : Properties.WEST;
            final Rotation xRot = yAxis == Properties.DOWN ? R0 : R180;
            final Rotation yRot = BlockModels.FLATTENED_MODEL_ROTATIONS[i];
            final String[] suffexes = yRot.ordinal() % 2 == 0 ? BlockModels.FLATTENED_MODEL_SUFFEXES : BlockModels.FLATTENED_MODEL_SUFFEXES_ROT;
            for (byte v = 0; v < suffexes.length; v++) {
                supplier.with(When.create()
                            .set(yAxis, (v & 0b100) != 0)
                            .set(xAxis, (v & 0b010) != 0)
                            .set(zAxis, (v & 0b001) != 0), BlockStateVariant.create()
                        .put(MODEL, ModelIds.getBlockSubModelId(block, "_corner_" + suffexes[v]))
                        .put(UVLOCK, true)
                        .put(X, xRot)
                        .put(Y, yRot)
                );
            }
        }
        blockStateCollector.accept(supplier);
    }

    public void registerChest(Block chest, Block particleSource) {
        registerBuiltin(ModelIds.getBlockModelId(chest), particleSource).includeWithoutItem(chest);
        ItemModels.CHEST.upload(ModelIds.getItemModelId(chest.asItem()), TextureMap.particle(particleSource), modelCollector);
    }

    public void registerFancyBed(Block bed, Block particleSource) {
        registerBuiltinWithParticle(bed, ModelIds.getBlockModelId(particleSource));
        super.registerBed(bed, particleSource);
    }

    public void registerStableDoor(Block door) {
        var variants = BlockStateVariantMap.create(Properties.HORIZONTAL_FACING, Properties.DOUBLE_BLOCK_HALF, Properties.DOOR_HINGE, Properties.OPEN);
        registerItemModel(door.asItem());
        buildDoorStateModels(door, "", variants::register);
        blockStateCollector.accept(VariantsBlockStateSupplier.create(door).coordinate(variants));
    }

    public void registerLockingDoor(Block door) {
        var variants = BlockStateVariantMap.create(Properties.HORIZONTAL_FACING, Properties.DOUBLE_BLOCK_HALF, Properties.DOOR_HINGE, Properties.OPEN, Properties.LOCKED);
        registerItemModel(door.asItem());
        buildDoorStateModels(door, "", (facing, half, hinge, open, map) -> variants.register(facing, half, hinge, open, false, map));
        buildDoorStateModels(door, "_locked", (facing, half, hinge, open, map) -> variants.register(facing, half, hinge, open, true, map));
        blockStateCollector.accept(VariantsBlockStateSupplier.create(door).coordinate(variants));
    }

    private void buildDoorStateModels(Block door, String suffex, DoorStateConsumer variants) {
        TextureMap topTextures = new TextureMap()
                .put(TextureKey.TOP, TextureMap.getSubId(door, "_top" + suffex))
                .put(TextureKey.BOTTOM, TextureMap.getSubId(door, "_bottom" + suffex));
        TextureMap bottomTextures = topTextures.copyAndAdd(TOP, topTextures.getTexture(BOTTOM));
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
            DoorHinge hinge, boolean open, Rotation rotation,
            Identifier modelId) {

        for (int i = 0; i < BlockRotation.DIRECTIONS.length; i++) {
            variantMap.register(BlockRotation.DIRECTIONS[i], targetHalf, hinge, open, BlockStateVariant.create()
                    .put(MODEL, modelId)
                    .put(Y, BlockRotation.cycle(rotation, i))
            );
        }
    }

    interface DoorStateConsumer {
        void register(Direction direction, DoubleBlockHalf half, DoorHinge hinge, boolean open, BlockStateVariant variant);
    }

    public void registerPillar(Block pillar) {
        TextureMap textures = new TextureMap()
                .put(SIDE, ModelIds.getBlockSubModelId(pillar, "_side"))
                .put(TOP, ModelIds.getBlockSubModelId(pillar, "_lip"))
                .put(BOTTOM, ModelIds.getBlockSubModelId(pillar, "_end"))
                .put(END, ModelIds.getBlockSubModelId(pillar, "_side_end"));
        Identifier middle = BlockModels.TEMPLATE_PILLAR.upload(pillar, textures, modelCollector);
        Identifier end = BlockModels.TEMPLATE_PILLAR_END.upload(pillar, textures, modelCollector);
        blockStateCollector.accept(MultipartBlockStateSupplier.create(pillar)
                .with(When.create().set(Properties.AXIS, Direction.Axis.X), BlockStateVariant.create().put(MODEL, middle).put(X, R90).put(Y, R90))
                .with(When.create().set(Properties.AXIS, Direction.Axis.X).set(Properties.NORTH, false), BlockStateVariant.create().put(MODEL, end).put(X, R270).put(Y, R90))
                .with(When.create().set(Properties.AXIS, Direction.Axis.X).set(Properties.SOUTH, false), BlockStateVariant.create().put(MODEL, end).put(X, R90).put(Y, R90))

                .with(When.create().set(Properties.AXIS, Direction.Axis.Y), BlockStateVariant.create().put(MODEL, middle))
                .with(When.create().set(Properties.AXIS, Direction.Axis.Y).set(Properties.NORTH, false), BlockStateVariant.create().put(MODEL, end).put(X, R180))
                .with(When.create().set(Properties.AXIS, Direction.Axis.Y).set(Properties.SOUTH, false), BlockStateVariant.create().put(MODEL, end))

                .with(When.create().set(Properties.AXIS, Direction.Axis.Z), BlockStateVariant.create().put(MODEL, middle).put(X, R90))
                .with(When.create().set(Properties.AXIS, Direction.Axis.Z).set(Properties.NORTH, false), BlockStateVariant.create().put(MODEL, end).put(X, R90))
                .with(When.create().set(Properties.AXIS, Direction.Axis.Z).set(Properties.SOUTH, false), BlockStateVariant.create().put(MODEL, end).put(X, R270))
        );
        ItemModels.TEMPLATE_PILLAR.upload(ModelIds.getItemModelId(pillar.asItem()), textures, modelCollector);
    }

    public void registerHiveBlock(Block hive) {
        Identifier core = ModelIds.getBlockSubModelId(hive, "_core");
        Identifier side = ModelIds.getBlockSubModelId(hive, "_side");
        blockStateCollector.accept(MultipartBlockStateSupplier.create(hive)
                .with(BlockStateVariant.create().put(MODEL, core))
                .with(When.create().set(Properties.NORTH, true), BlockStateVariant.create().put(MODEL, side).put(UVLOCK, true))
                .with(When.create().set(Properties.EAST, true), BlockStateVariant.create().put(MODEL, side).put(UVLOCK, true).put(Y, R90))
                .with(When.create().set(Properties.SOUTH, true), BlockStateVariant.create().put(MODEL, side).put(UVLOCK, true).put(Y, R180))
                .with(When.create().set(Properties.WEST, true), BlockStateVariant.create().put(MODEL, side).put(UVLOCK, true).put(Y, R270))
                .with(When.create().set(Properties.DOWN, true), BlockStateVariant.create().put(MODEL, side).put(UVLOCK, true).put(X, R90))
                .with(When.create().set(Properties.UP, true), BlockStateVariant.create().put(MODEL, side).put(UVLOCK, true).put(X, R270)));
        Models.CUBE_ALL.upload(ModelIds.getItemModelId(hive.asItem()), TextureMap.all(ModelIds.getBlockSubModelId(hive, "_side")), modelCollector);
    }

    public void registerBale(Identifier blockId, Identifier baseBlockId, String endSuffex) {
        Identifier top = baseBlockId.withPath(p -> "block/" + p + endSuffex);
        Identifier side = baseBlockId.withPath(p -> "block/" + p + "_side");
        TextureMap textures = new TextureMap().put(TOP, top).put(SIDE, side);

        MultipartBlockStateSupplier supplier = MultipartBlockStateSupplier.create(Datagen.getOrCreateBaleBlock(blockId));
        Map<Integer, Identifier> uploadedModels = new HashMap<>();

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            for (int i = 0; i < EdibleBlock.SEGMENTS.length; i++) {
                BooleanProperty segment = EdibleBlock.SEGMENTS[i];
                segment.getName();
                supplier.with(When.create().set(EdibleBlock.AXIS, axis).set(segment, true), BlockStateVariant.create()
                        .put(MODEL, uploadedModels.computeIfAbsent(i, ii -> {
                            return BlockModels.BALE_MODELS[ii].getLeft().upload(blockId.withPath(p -> "block/" + p + BlockModels.BALE_MODELS[ii].getRight()), textures, modelCollector);
                        }))
                        .put(X, axis == Direction.Axis.Y ? R0 : R90)
                        .put(Y, axis == Direction.Axis.X ? R90 : R0)
                );
            }
        }

        blockStateCollector.accept(supplier);
    }

    public void registerWithStages(Block crop, Property<Integer> ageProperty, BlockModels.Factory modelFactory, int ... stages) {
        if (ageProperty.getValues().size() != stages.length) {
            throw new IllegalArgumentException();
        }
        int offset = ageProperty.getValues().iterator().next();
        Int2ObjectOpenHashMap<Identifier> uploadedModels = new Int2ObjectOpenHashMap<>();
        blockStateCollector.accept(VariantsBlockStateSupplier.create(crop)
                .coordinate(BlockStateVariantMap.create(ageProperty)
                .register(age -> BlockStateVariant.create().put(MODEL, uploadedModels.computeIfAbsent(stages[age - offset], stage -> {
                    return modelFactory.upload(crop, "_stage" + stage, modelCollector);
                })))));
    }

    public void registerWithStagesBuiltinModels(Block crop, Property<Integer> ageProperty, int ... stages) {
        if (ageProperty.getValues().size() != stages.length) {
            throw new IllegalArgumentException();
        }
        int offset = ageProperty.getValues().iterator().next();
        blockStateCollector.accept(VariantsBlockStateSupplier.create(crop)
                .coordinate(BlockStateVariantMap.create(ageProperty)
                .register(age -> BlockStateVariant.create().put(MODEL, ModelIds.getBlockSubModelId(crop, "_stage" + stages[age - offset])))));
    }

    public <T extends Enum<T> & StringIdentifiable> void registerTallCrop(Block crop,
            Property<Integer> ageProperty,
            EnumProperty<T> partProperty,
            int[] ... ageTextureIndices) {
        Map<String, Identifier> uploadedModels = new HashMap<>();
        blockStateCollector.accept(VariantsBlockStateSupplier.create(crop).coordinate(BlockStateVariantMap.create(partProperty, ageProperty).register((part, age) -> {
            int i = ageTextureIndices[part.ordinal()][age];
            Identifier identifier = uploadedModels.computeIfAbsent("_" + part.asString() + "_stage" + i, variant -> createSubModel(crop, variant, Models.CROSS, TextureMap::cross));
            return BlockStateVariant.create().put(MODEL, identifier);
        })));
    }

    public void registerPie(Block pie) {
        TextureMap textures = new TextureMap()
                .put(TOP, ModelIds.getBlockSubModelId(pie, "_top"))
                .put(BOTTOM, ModelIds.getBlockSubModelId(pie, "_bottom"))
                .put(SIDE, ModelIds.getBlockSubModelId(pie, "_side"))
                .put(INSIDE, ModelIds.getBlockSubModelId(pie, "_inside"));
        TextureMap stompedTextures = textures.copyAndAdd(TOP, ModelIds.getBlockSubModelId(pie, "_top_stomped"));
        blockStateCollector.accept(VariantsBlockStateSupplier.create(pie).coordinate(BlockStateVariantMap.create(PieBlock.BITES, PieBlock.STOMPED).register((bites, stomped) -> {
            return BlockStateVariant.create().put(MODEL, BlockModels.PIE_MODELS[bites].upload(pie, (stomped ? "_stomped" : ""), stomped ? stompedTextures : textures, modelCollector));
        })));
    }

    public void registerFloweringLeaves(Block block) {
        Identifier baseModel = TexturedModel.LEAVES.upload(block, modelCollector);
        Identifier floweringModel = Models.CUBE_ALL.upload(block, "_flowering", TextureMap.of(ALL, ModelIds.getBlockSubModelId(block, "_flowering")), modelCollector);
        blockStateCollector.accept(MultipartBlockStateSupplier.create(block)
                .with(BlockStateVariant.create().put(MODEL, baseModel))
                .with(When.create().set(FruitBearingBlock.STAGE, FruitBearingBlock.Stage.FLOWERING), BlockStateVariant.create().put(MODEL, floweringModel)));
    }

    public void registerZapLeaves(Block block) {
        Identifier baseModel = TexturedModel.LEAVES.upload(block, modelCollector);
        Identifier floweringModel = Registries.BLOCK.getId(block).withPrefixedPath("block/flowering_");
        Identifier airModel = ModelIds.getBlockModelId(Blocks.AIR);
        blockStateCollector.accept(VariantsBlockStateSupplier.create(block)
                .coordinate(BlockStateVariantMap.create(ZapAppleLeavesBlock.STAGE)
                .register(stage -> BlockStateVariant.create()
                        .put(MODEL, switch (stage) {
                            case HIBERNATING -> airModel;
                            case FLOWERING -> floweringModel;
                            default -> baseModel;
                        }))));
    }

    public void registerSprout(Block sprout) {
        blockStateCollector.accept(VariantsBlockStateSupplier.create(sprout)
                .coordinate(BlockStateVariantMap.create(Properties.AGE_7)
                .register(age -> BlockStateVariant.create()
                        .put(MODEL, Unicopia.id("block/apple_sprout_stage" + age)))));
    }

    public void registerShell(Block shell) {
        blockStateCollector.accept(VariantsBlockStateSupplier.create(shell)
                .coordinate(BlockStateVariantMap.create(ShellsBlock.COUNT)
                .register(count -> BlockStateVariant.create()
                        .put(MODEL, BlockModels.SHELL_MODELS[count - 1].upload(shell, TextureMap.of(BlockModels.SHELL, Registries.BLOCK.getId(shell).withPrefixedPath("item/")), modelCollector)))));
    }

    public void registerHull(Block block, Block core, Block shell) {
        blockStateCollector.accept(VariantsBlockStateSupplier.create(
                block,
                BlockStateVariant.create().put(MODEL, Models.CUBE_BOTTOM_TOP.upload(block, new TextureMap()
                        .put(BOTTOM, ModelIds.getBlockModelId(core))
                        .put(TOP, ModelIds.getBlockModelId(shell))
                        .put(SIDE, ModelIds.getBlockSubModelId(shell, "_half")), modelCollector))
        ).coordinate(createUpDefaultFacingVariantMap()));
    }
}
