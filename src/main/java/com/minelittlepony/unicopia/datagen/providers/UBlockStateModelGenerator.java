package com.minelittlepony.unicopia.datagen.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.block.FruitBearingBlock;
import com.minelittlepony.unicopia.block.PieBlock;
import com.minelittlepony.unicopia.block.PileBlock;
import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.SlimePustuleBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesBlock;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.Tree;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

public class UBlockStateModelGenerator extends BlockStateModelGenerator {
    static final Identifier AIR_BLOCK_ID = new Identifier("block/air");
    static final Identifier AIR_ITEM_ID = new Identifier("item/air");

    static UBlockStateModelGenerator create(BlockStateModelGenerator modelGenerator) {
        return new UBlockStateModelGenerator(modelGenerator);
    }

    private UBlockStateModelGenerator(BlockStateModelGenerator modelGenerator) {
        super(modelGenerator.blockStateCollector,
                (id, jsonSupplier) -> {
                    if (AIR_BLOCK_ID.equals(id) || AIR_ITEM_ID.equals(id)) {
                        throw new IllegalStateException("Registered air id for block model: " + jsonSupplier.get().toString());
                    }
                    modelGenerator.modelCollector.accept(id, jsonSupplier);
                },
                item -> modelGenerator.excludeFromSimpleItemModelGeneration(Block.getBlockFromItem(item))
        );

        for (int i = 0; i < Models.STEM_GROWTH_STAGES.length; i++) {
            Models.STEM_GROWTH_STAGES[i].upload(Unicopia.id("block/apple_sprout_stage" + i), TextureMap.stem(Blocks.MELON_STEM), modelCollector);
        }
    }

    @Override
    public void register() {
        // handmade
        registerAll((g, block) -> g.registerParentedItemModel(block, ModelIds.getBlockModelId(block)), UBlocks.SHAPING_BENCH, UBlocks.SURFACE_CHITIN);
        registerAll(UBlockStateModelGenerator::registerSimpleState, UBlocks.SHAPING_BENCH, UBlocks.BANANAS);
        // doors
        registerAll(UBlockStateModelGenerator::registerStableDoor, UBlocks.STABLE_DOOR, UBlocks.DARK_OAK_DOOR, UBlocks.CRYSTAL_DOOR, UBlocks.CLOUD_DOOR);

        // cloud blocks
        createCustomTexturePool(UBlocks.CLOUD, TexturedModel.CUBE_ALL).same(UBlocks.UNSTABLE_CLOUD).slab(UBlocks.CLOUD_SLAB).stairs(UBlocks.CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.ETCHED_CLOUD, TexturedModel.CUBE_ALL).slab(UBlocks.ETCHED_CLOUD_SLAB).stairs(UBlocks.ETCHED_CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.DENSE_CLOUD, TexturedModel.CUBE_ALL).slab(UBlocks.DENSE_CLOUD_SLAB).stairs(UBlocks.DENSE_CLOUD_STAIRS);
        createCustomTexturePool(UBlocks.CLOUD_PLANKS, TexturedModel.CUBE_ALL).slab(UBlocks.CLOUD_PLANK_SLAB).stairs(UBlocks.CLOUD_PLANK_STAIRS);
        createCustomTexturePool(UBlocks.CLOUD_BRICKS, TexturedModel.CUBE_ALL).slab(UBlocks.CLOUD_BRICK_SLAB).stairs(UBlocks.CLOUD_BRICK_STAIRS);
        createTwoStepTexturePool(UBlocks.SOGGY_CLOUD, TexturedModel.CUBE_BOTTOM_TOP.andThen(textures -> textures.put(TextureKey.BOTTOM, ModelIds.getBlockModelId(UBlocks.CLOUD)))).slab(UBlocks.SOGGY_CLOUD_SLAB).stairs(UBlocks.SOGGY_CLOUD_STAIRS);
        registerRotated(UBlocks.CARVED_CLOUD, TexturedModel.CUBE_COLUMN);

        registerAll(UBlockStateModelGenerator::registerCompactedBlock, UBlocks.COMPACTED_CLOUD, UBlocks.COMPACTED_CLOUD_BRICKS, UBlocks.COMPACTED_CLOUD_PLANKS, UBlocks.COMPACTED_DENSE_CLOUD, UBlocks.COMPACTED_ETCHED_CLOUD);
        registerChest(UBlocks.CLOUD_CHEST, UBlocks.CLOUD);
        registerFancyBed(UBlocks.CLOUD_BED, UBlocks.CLOUD);
        registerFancyBed(UBlocks.CLOTH_BED, Blocks.SPRUCE_PLANKS);

        // chitin blocks
        registerTopsoil(UBlocks.SURFACE_CHITIN, UBlocks.CHITIN);
        registerHollow(UBlocks.CHITIN);
        registerCubeAllModelTexturePool(UBlocks.CHISELLED_CHITIN).stairs(UBlocks.CHISELLED_CHITIN_STAIRS).slab(UBlocks.CHISELLED_CHITIN_SLAB);
        registerHiveBlock(UBlocks.HIVE);
        registerRotated(UBlocks.CHITIN_SPIKES, BlockModels.SPIKES);
        registerHull(UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.CHISELLED_CHITIN);
        registerParentedItemModel(UBlocks.SLIME_PUSTULE, ModelIds.getBlockSubModelId(UBlocks.SLIME_PUSTULE, "_pod"));
        blockStateCollector.accept(VariantsBlockStateSupplier.create(UBlocks.SLIME_PUSTULE)
                .coordinate(BlockStateVariantMap.create(SlimePustuleBlock.SHAPE)
                .register(state -> BlockStateVariant.create().put(VariantSettings.MODEL, ModelIds.getBlockSubModelId(UBlocks.SLIME_PUSTULE, "_" + state.asString())))));
        registerPie(UBlocks.APPLE_PIE);

        // palm wood
        registerLog(UBlocks.PALM_LOG).log(UBlocks.PALM_LOG).wood(UBlocks.PALM_WOOD);
        registerLog(UBlocks.STRIPPED_PALM_LOG).log(UBlocks.STRIPPED_PALM_LOG).wood(UBlocks.STRIPPED_PALM_WOOD);
        registerCubeAllModelTexturePool(UBlocks.PALM_PLANKS).family(new BlockFamily.Builder(UBlocks.PALM_PLANKS)
                .slab(UBlocks.PALM_SLAB).stairs(UBlocks.PALM_STAIRS).fence(UBlocks.PALM_FENCE).fenceGate(UBlocks.PALM_FENCE_GATE)
                .button(UBlocks.PALM_BUTTON).pressurePlate(UBlocks.PALM_PRESSURE_PLATE).sign(UBlocks.PALM_SIGN, UBlocks.PALM_WALL_SIGN)
                .door(UBlocks.PALM_DOOR).trapdoor(UBlocks.PALM_TRAPDOOR)
                .group("wooden").unlockCriterionName("has_planks")
                .build());
        registerHangingSign(UBlocks.STRIPPED_PALM_LOG, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_WALL_HANGING_SIGN);
        registerSimpleCubeAll(UBlocks.PALM_LEAVES);

        // zap wood
        registerLog(UBlocks.ZAP_LOG)
            .log(UBlocks.ZAP_LOG).wood(UBlocks.ZAP_WOOD)
            .log(UBlocks.WAXED_ZAP_LOG).wood(UBlocks.WAXED_ZAP_WOOD);
        registerLog(UBlocks.STRIPPED_ZAP_LOG)
            .log(UBlocks.STRIPPED_ZAP_LOG).wood(UBlocks.STRIPPED_ZAP_WOOD)
            .log(UBlocks.WAXED_STRIPPED_ZAP_LOG).wood(UBlocks.WAXED_STRIPPED_ZAP_WOOD);
        registerCubeAllModelTexturePool(UBlocks.ZAP_PLANKS)
            .family(new BlockFamily.Builder(UBlocks.ZAP_PLANKS)
                .slab(UBlocks.ZAP_SLAB).stairs(UBlocks.ZAP_STAIRS).fence(UBlocks.ZAP_FENCE).fenceGate(UBlocks.ZAP_FENCE_GATE)
                .group("wooden").unlockCriterionName("has_planks")
                .build())
            .same(UBlocks.WAXED_ZAP_PLANKS).family(new BlockFamily.Builder(UBlocks.WAXED_ZAP_PLANKS)
                .slab(UBlocks.WAXED_ZAP_SLAB).stairs(UBlocks.WAXED_ZAP_STAIRS).fence(UBlocks.WAXED_ZAP_FENCE).fenceGate(UBlocks.WAXED_ZAP_FENCE_GATE)
                .group("wooden").unlockCriterionName("has_planks")
                .build());
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
        registerWithStages(UBlocks.OATS, UBlocks.OATS.getAgeProperty(), BlockModels.CROP, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        registerWithStages(UBlocks.OATS_STEM, UBlocks.OATS_STEM.getAgeProperty(), BlockModels.CROP, 0, 1, 2, 3, 4, 5, 6);
        registerWithStages(UBlocks.OATS_CROWN, UBlocks.OATS_CROWN.getAgeProperty(), BlockModels.CROP, 0, 1);
        registerTallCrop(UBlocks.PINEAPPLE, Properties.AGE_7, Properties.BLOCK_HALF,
                new int[] { 0, 1, 2, 3, 4, 5, 5, 6 },
                new int[] { 0, 0, 1, 2, 3, 4, 5, 6 }
        );
        registerParentedItemModel(UBlocks.PLUNDER_VINE_BUD, ModelIds.getBlockModelId(UBlocks.PLUNDER_VINE_BUD));

        // leaves
        registerAll(UBlockStateModelGenerator::registerFloweringLeaves, UBlocks.GREEN_APPLE_LEAVES, UBlocks.SOUR_APPLE_LEAVES, UBlocks.SWEET_APPLE_LEAVES);
        registerAll(UBlockStateModelGenerator::registerSprout, UBlocks.GREEN_APPLE_SPROUT, UBlocks.SOUR_APPLE_SPROUT, UBlocks.SWEET_APPLE_SPROUT, UBlocks.GOLDEN_OAK_SPROUT);
        registerStateWithModelReference(UBlocks.MANGO_LEAVES, Blocks.JUNGLE_LEAVES);
        registerParentedItemModel(UBlocks.MANGO_LEAVES, ModelIds.getBlockModelId(Blocks.JUNGLE_LEAVES));

        // fruit
        Map.of(UBlocks.GREEN_APPLE, UItems.GREEN_APPLE,
                UBlocks.GOLDEN_APPLE, Items.GOLDEN_APPLE,
                UBlocks.MANGO, UItems.MANGO,
                UBlocks.SOUR_APPLE, UItems.SOUR_APPLE,
                UBlocks.SWEET_APPLE, UItems.SWEET_APPLE,
                UBlocks.ZAP_APPLE, UItems.ZAP_APPLE,
                UBlocks.ZAP_BULB, UItems.ZAP_BULB
        ).forEach((block, item) -> registerSingleton(block, TextureMap.cross(ModelIds.getItemModelId(item)), BlockModels.FRUIT));

        // bales
        registerAll((g, block) -> g.registerBale(Unicopia.id(block.getLeft().getPath().replace("bale", "block")), block.getLeft(), block.getRight()),
                new Pair<>(new Identifier("hay_block"), "_top"),
                new Pair<>(new Identifier("farmersdelight", "rice_bale"), "_top"),
                new Pair<>(new Identifier("farmersdelight", "straw_bale"), "_end")
        );
        // shells
        registerAll(UBlockStateModelGenerator::registerShell, UBlocks.CLAM_SHELL, UBlocks.TURRET_SHELL, UBlocks.SCALLOP_SHELL);
        // other
        registerBuiltinWithParticle(UBlocks.WEATHER_VANE, UBlocks.WEATHER_VANE.asItem());
        registerWithStages(UBlocks.FROSTED_OBSIDIAN, Properties.AGE_3, BlockModels.CUBE_ALL, 0, 1, 2, 3);
        registerWithStagesBuiltinModels(UBlocks.ROCKS, Properties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        registerWithStagesBuiltinModels(UBlocks.MYSTERIOUS_EGG, PileBlock.COUNT, 1, 2, 3);
        excludeFromSimpleItemModelGeneration(UBlocks.MYSTERIOUS_EGG);
        FireModels.registerSoulFire(this, UBlocks.SPECTRAL_FIRE, Blocks.SOUL_FIRE);
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
                TextureMap textMap = textures.copyAndAdd(BlockModels.STEP, textures.getTexture(TextureKey.SIDE));
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
                TextureMap textMap = textures.copyAndAdd(TextureKey.SIDE, twoStepTexture);
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
                BlockStateVariant.create().put(VariantSettings.MODEL, Models.CUBE_BOTTOM_TOP.upload(dirt, "_snow", model.getTextures()
                        .copyAndAdd(TextureKey.SIDE, ModelIds.getBlockSubModelId(dirt, "_side_snow_covered")
                ), modelCollector))
        );
    }

    public void registerHollow(Block block) {
        Identifier outside = ModelIds.getBlockModelId(UBlocks.CHITIN);
        Identifier inside = ModelIds.getBlockSubModelId(UBlocks.CHITIN, "_bottom");
        registerSingleton(UBlocks.CHITIN, new TextureMap()
                .put(TextureKey.SIDE, outside)
                .put(TextureKey.TOP, outside)
                .put(TextureKey.BOTTOM, inside), Models.CUBE_BOTTOM_TOP);
    }

    public void registerRotated(Block block, TexturedModel.Factory modelFactory) {
        Identifier modelId = modelFactory.get(block).upload(block, modelCollector);
        blockStateCollector.accept(VariantsBlockStateSupplier.create(block, BlockStateVariant.create()
                .put(VariantSettings.MODEL, modelId))
                .coordinate(createUpDefaultFacingVariantMap()));
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
            final VariantSettings.Rotation xRot = yAxis == Properties.DOWN ? VariantSettings.Rotation.R0 : VariantSettings.Rotation.R180;
            final VariantSettings.Rotation yRot = BlockModels.FLATTENED_MODEL_ROTATIONS[i];
            final String[] suffexes = yRot.ordinal() % 2 == 0 ? BlockModels.FLATTENED_MODEL_SUFFEXES : BlockModels.FLATTENED_MODEL_SUFFEXES_ROT;
            for (byte v = 0; v < suffexes.length; v++) {
                supplier.with(When.create()
                            .set(yAxis, (v & 0b100) != 0)
                            .set(xAxis, (v & 0b010) != 0)
                            .set(zAxis, (v & 0b001) != 0), BlockStateVariant.create()
                        .put(VariantSettings.MODEL, ModelIds.getBlockSubModelId(block, "_corner_" + suffexes[v]))
                        .put(VariantSettings.UVLOCK, true)
                        .put(VariantSettings.X, xRot)
                        .put(VariantSettings.Y, yRot)
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
        TextureMap topTextures = TextureMap.topBottom(door);
        TextureMap bottomTextures = topTextures.copyAndAdd(TextureKey.TOP, topTextures.getTexture(TextureKey.BOTTOM));
        registerItemModel(door.asItem());
        blockStateCollector.accept(createStableDoorBlockState(door,
                BlockModels.DOOR_LEFT.upload(door, "_bottom_left", bottomTextures, modelCollector),
                BlockModels.DOOR_RIGHT.upload(door, "_bottom_right", bottomTextures, modelCollector),
                BlockModels.DOOR_LEFT.upload(door, "_top_left", topTextures, modelCollector),
                BlockModels.DOOR_RIGHT.upload(door, "_top_right", topTextures, modelCollector)
        ));
    }

    public static BlockStateSupplier createStableDoorBlockState(Block doorBlock, Identifier bottomLeftHingeClosedModelId, Identifier bottomRightHingeClosedModelId, Identifier topLeftHingeClosedModelId, Identifier topRightHingeClosedModelId) {
        var variants = BlockStateVariantMap.create(Properties.HORIZONTAL_FACING, Properties.DOUBLE_BLOCK_HALF, Properties.DOOR_HINGE, Properties.OPEN);
        fillStableDoorVariantMap(variants, DoubleBlockHalf.LOWER, bottomLeftHingeClosedModelId, bottomRightHingeClosedModelId);
        fillStableDoorVariantMap(variants, DoubleBlockHalf.UPPER, topLeftHingeClosedModelId, topRightHingeClosedModelId);
        return VariantsBlockStateSupplier.create(doorBlock).coordinate(variants);
    }

    public static BlockStateVariantMap.QuadrupleProperty<Direction, DoubleBlockHalf, DoorHinge, Boolean> fillStableDoorVariantMap(
            BlockStateVariantMap.QuadrupleProperty<Direction, DoubleBlockHalf, DoorHinge, Boolean> variantMap,
            DoubleBlockHalf targetHalf, Identifier leftModelId, Identifier rightModelId) {
        return variantMap
                .register(Direction.EAST, targetHalf, DoorHinge.LEFT, false, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, leftModelId))
                .register(Direction.SOUTH, targetHalf, DoorHinge.LEFT, false, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, leftModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .register(Direction.WEST, targetHalf, DoorHinge.LEFT, false, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, leftModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .register(Direction.NORTH, targetHalf, DoorHinge.LEFT, false, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, leftModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R270))

                .register(Direction.EAST, targetHalf, DoorHinge.RIGHT, false, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, rightModelId))
                .register(Direction.SOUTH, targetHalf, DoorHinge.RIGHT, false, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, rightModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .register(Direction.WEST, targetHalf, DoorHinge.RIGHT, false, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, rightModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .register(Direction.NORTH, targetHalf, DoorHinge.RIGHT, false, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, rightModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R270))

                .register(Direction.EAST, targetHalf, DoorHinge.LEFT, true, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, rightModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .register(Direction.SOUTH, targetHalf, DoorHinge.LEFT, true, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, rightModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .register(Direction.WEST, targetHalf, DoorHinge.LEFT, true, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, rightModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R270))
                .register(Direction.NORTH, targetHalf, DoorHinge.LEFT, true, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, rightModelId))

                .register(Direction.EAST, targetHalf, DoorHinge.RIGHT, true, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, leftModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R270))
                .register(Direction.SOUTH, targetHalf, DoorHinge.RIGHT, true, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, leftModelId))
                .register(Direction.WEST, targetHalf, DoorHinge.RIGHT, true, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, leftModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .register(Direction.NORTH, targetHalf, DoorHinge.RIGHT, true, BlockStateVariant.create()
                        .put(VariantSettings.MODEL, leftModelId)
                        .put(VariantSettings.Y, VariantSettings.Rotation.R180));
    }

    public void registerHiveBlock(Block hive) {
        Identifier core = ModelIds.getBlockSubModelId(hive, "_core");
        Identifier side = ModelIds.getBlockSubModelId(hive, "_side");
        blockStateCollector.accept(MultipartBlockStateSupplier.create(hive)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, core))
                .with(When.create().set(Properties.NORTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, side).put(VariantSettings.UVLOCK, true))
                .with(When.create().set(Properties.EAST, true), BlockStateVariant.create().put(VariantSettings.MODEL, side).put(VariantSettings.UVLOCK, true).put(VariantSettings.Y, VariantSettings.Rotation.R90))
                .with(When.create().set(Properties.SOUTH, true), BlockStateVariant.create().put(VariantSettings.MODEL, side).put(VariantSettings.UVLOCK, true).put(VariantSettings.Y, VariantSettings.Rotation.R180))
                .with(When.create().set(Properties.WEST, true), BlockStateVariant.create().put(VariantSettings.MODEL, side).put(VariantSettings.UVLOCK, true).put(VariantSettings.Y, VariantSettings.Rotation.R270))
                .with(When.create().set(Properties.DOWN, true), BlockStateVariant.create().put(VariantSettings.MODEL, side).put(VariantSettings.UVLOCK, true).put(VariantSettings.X, VariantSettings.Rotation.R90))
                .with(When.create().set(Properties.UP, true), BlockStateVariant.create().put(VariantSettings.MODEL, side).put(VariantSettings.UVLOCK, true).put(VariantSettings.X, VariantSettings.Rotation.R270)));
        Models.CUBE_ALL.upload(ModelIds.getItemModelId(hive.asItem()), TextureMap.all(ModelIds.getBlockSubModelId(hive, "_side")), modelCollector);
    }

    public void registerBale(Identifier blockId, Identifier baseBlockId, String endSuffex) {
        Identifier top = baseBlockId.withPath(p -> "block/" + p + endSuffex);
        Identifier side = baseBlockId.withPath(p -> "block/" + p + "_side");
        TextureMap textures = new TextureMap().put(TextureKey.TOP, top).put(TextureKey.SIDE, side);

        MultipartBlockStateSupplier supplier = MultipartBlockStateSupplier.create(Registries.BLOCK.getOrEmpty(blockId).orElseGet(() -> {
            return Registry.register(Registries.BLOCK, blockId, new EdibleBlock(blockId, blockId, false));
        }));
        Map<Integer, Identifier> uploadedModels = new HashMap<>();

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            for (int i = 0; i < EdibleBlock.SEGMENTS.length; i++) {
                BooleanProperty segment = EdibleBlock.SEGMENTS[i];
                segment.getName();
                supplier.with(When.create().set(EdibleBlock.AXIS, axis).set(segment, true), BlockStateVariant.create()
                        .put(VariantSettings.MODEL, uploadedModels.computeIfAbsent(i, ii -> {
                            return BlockModels.BALE_MODELS[ii].getLeft().upload(blockId.withPath(p -> "block/" + p + BlockModels.BALE_MODELS[ii].getRight()), textures, modelCollector);
                        }))
                        .put(VariantSettings.X, axis == Direction.Axis.Y ? VariantSettings.Rotation.R0 : VariantSettings.Rotation.R90)
                        .put(VariantSettings.Y, axis == Direction.Axis.X ? VariantSettings.Rotation.R90 : VariantSettings.Rotation.R0)
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
                .register(age -> BlockStateVariant.create().put(VariantSettings.MODEL, uploadedModels.computeIfAbsent(stages[age - offset], stage -> {
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
                .register(age -> BlockStateVariant.create().put(VariantSettings.MODEL, ModelIds.getBlockSubModelId(crop, "_stage" + stages[age - offset])))));
    }

    public <T extends Enum<T> & StringIdentifiable> void registerTallCrop(Block crop,
            Property<Integer> ageProperty,
            EnumProperty<T> partProperty,
            int[] ... ageTextureIndices) {
        Map<String, Identifier> uploadedModels = new HashMap<>();
        blockStateCollector.accept(VariantsBlockStateSupplier.create(crop).coordinate(BlockStateVariantMap.create(partProperty, ageProperty).register((part, age) -> {
            int i = ageTextureIndices[part.ordinal()][age];
            Identifier identifier = uploadedModels.computeIfAbsent("_" + part.asString() + "_stage" + i, variant -> createSubModel(crop, variant, Models.CROSS, TextureMap::cross));
            return BlockStateVariant.create().put(VariantSettings.MODEL, identifier);
        })));
    }

    public void registerPie(Block pie) {
        TextureMap textures = new TextureMap()
                .put(TextureKey.TOP, ModelIds.getBlockSubModelId(pie, "_top"))
                .put(TextureKey.BOTTOM, ModelIds.getBlockSubModelId(pie, "_bottom"))
                .put(TextureKey.SIDE, ModelIds.getBlockSubModelId(pie, "_side"))
                .put(TextureKey.INSIDE, ModelIds.getBlockSubModelId(pie, "_inside"));
        TextureMap stompedTextures = textures.copyAndAdd(TextureKey.TOP, ModelIds.getBlockSubModelId(pie, "_top_stomped"));
        blockStateCollector.accept(VariantsBlockStateSupplier.create(pie).coordinate(BlockStateVariantMap.create(PieBlock.BITES, PieBlock.STOMPED).register((bites, stomped) -> {
            return BlockStateVariant.create().put(VariantSettings.MODEL, BlockModels.PIE_MODELS[bites].upload(pie, (stomped ? "_stomped" : ""), stomped ? stompedTextures : textures, modelCollector));
        })));
    }

    public void registerFloweringLeaves(Block block) {
        Identifier baseModel = TexturedModel.LEAVES.upload(block, modelCollector);
        Identifier floweringModel = Models.CUBE_ALL.upload(block, "_flowering", TextureMap.of(TextureKey.ALL, ModelIds.getBlockSubModelId(block, "_flowering")), modelCollector);
        blockStateCollector.accept(MultipartBlockStateSupplier.create(block)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, baseModel))
                .with(When.create().set(FruitBearingBlock.STAGE, FruitBearingBlock.Stage.FLOWERING), BlockStateVariant.create().put(VariantSettings.MODEL, floweringModel)));
    }

    public void registerZapLeaves(Block block) {
        Identifier baseModel = TexturedModel.LEAVES.upload(block, modelCollector);
        Identifier floweringModel = Registries.BLOCK.getId(block).withPrefixedPath("block/flowering_");
        Identifier airModel = ModelIds.getBlockModelId(Blocks.AIR);
        blockStateCollector.accept(VariantsBlockStateSupplier.create(block)
                .coordinate(BlockStateVariantMap.create(ZapAppleLeavesBlock.STAGE)
                .register(stage -> BlockStateVariant.create()
                        .put(VariantSettings.MODEL, switch (stage) {
                            case HIBERNATING -> airModel;
                            case FLOWERING -> floweringModel;
                            default -> baseModel;
                        }))));
    }

    public void registerSprout(Block sprout) {
        blockStateCollector.accept(VariantsBlockStateSupplier.create(sprout)
                .coordinate(BlockStateVariantMap.create(Properties.AGE_7)
                .register(age -> BlockStateVariant.create()
                        .put(VariantSettings.MODEL, Unicopia.id("block/apple_sprout_stage" + age)))));
    }

    public void registerShell(Block shell) {
        blockStateCollector.accept(VariantsBlockStateSupplier.create(shell)
                .coordinate(BlockStateVariantMap.create(ShellsBlock.COUNT)
                .register(count -> BlockStateVariant.create()
                        .put(VariantSettings.MODEL, BlockModels.SHELL_MODELS[count - 1].upload(shell, TextureMap.of(BlockModels.SHELL, Registries.BLOCK.getId(shell).withPrefixedPath("item/")), modelCollector)))));
    }

    public void registerHull(Block block, Block core, Block shell) {
        blockStateCollector.accept(VariantsBlockStateSupplier.create(
                block,
                BlockStateVariant.create().put(VariantSettings.MODEL, Models.CUBE_BOTTOM_TOP.upload(block, new TextureMap()
                        .put(TextureKey.BOTTOM, ModelIds.getBlockModelId(core))
                        .put(TextureKey.TOP, ModelIds.getBlockModelId(shell))
                        .put(TextureKey.SIDE, ModelIds.getBlockSubModelId(shell, "_half")), modelCollector))
        ).coordinate(createUpDefaultFacingVariantMap()));
    }
}
