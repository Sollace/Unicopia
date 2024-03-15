package com.minelittlepony.unicopia.datagen.providers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.block.FruitBearingBlock;
import com.minelittlepony.unicopia.block.zap.ZapAppleLeavesBlock;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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

public interface BlockModels {
    Model FRUIT = block("fruit", TextureKey.CROSS);

    String[] FLATTENED_MODEL_SUFFEXES =     {"xyz", "yz", "xy", "y", "xz", "z", "x", "full"};
    String[] FLATTENED_MODEL_SUFFEXES_ROT = {"xyz", "xy", "yz", "y", "xz", "x", "z", "full"};
    VariantSettings.Rotation[] FLATTENED_MODEL_ROTATIONS = {
            VariantSettings.Rotation.R0, VariantSettings.Rotation.R270, VariantSettings.Rotation.R90, VariantSettings.Rotation.R180,
            VariantSettings.Rotation.R270, VariantSettings.Rotation.R180, VariantSettings.Rotation.R0, VariantSettings.Rotation.R90
    };

    Model[] FLATTENED_MODELS = Arrays.stream(FLATTENED_MODEL_SUFFEXES)
            .map(variant -> block("flattened_corner_" + variant, "_corner_" + variant, TextureKey.ALL))
            .toArray(Model[]::new);
    @SuppressWarnings("unchecked")
    Pair<Model, String>[] BALE_MODELS = Stream.of("bnw", "bne", "bsw", "bse", "tnw", "tne", "tsw", "tse")
            .map(suffex -> new Pair<>(block("template_bale_" + suffex, "_" + suffex, TextureKey.TOP, TextureKey.SIDE), "_" + suffex))
            .toArray(Pair[]::new);

    static Model block(String parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Unicopia.id("block/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static Model block(String parent, String variant, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Unicopia.id("block/" + parent)), Optional.of(variant), requiredTextureKeys);
    }

    static Model block(Identifier parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(parent.withPrefixedPath("block/")), Optional.empty(), requiredTextureKeys);
    }

    static Model block(Identifier parent, String variant, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(parent.withPrefixedPath("block/")), Optional.of(variant), requiredTextureKeys);
    }

    static void registerCompactedBlock(BlockStateModelGenerator modelGenerator, Block block) {
        for (Model model : FLATTENED_MODELS) {
            model.upload(block, TextureMap.all(ModelIds.getBlockModelId(block).withPath(p -> p.replace("compacted_", ""))), modelGenerator.modelCollector);
        }
        modelGenerator.blockStateCollector.accept(createCompactedBlockState(block));
    }

    private static BlockStateSupplier createCompactedBlockState(Block block) {
        MultipartBlockStateSupplier supplier = MultipartBlockStateSupplier.create(block);
        for (byte i = 0; i < FLATTENED_MODEL_ROTATIONS.length; i++) {
            final BooleanProperty yAxis = (i & 0b100) == 0 ? Properties.DOWN : Properties.UP;
            final BooleanProperty xAxis = (i & 0b010) == 0 ? Properties.NORTH: Properties.SOUTH;
            final BooleanProperty zAxis = (i & 0b001) == 0 ? Properties.EAST : Properties.WEST;
            final VariantSettings.Rotation xRot = yAxis == Properties.DOWN ? VariantSettings.Rotation.R0 : VariantSettings.Rotation.R180;
            final VariantSettings.Rotation yRot = FLATTENED_MODEL_ROTATIONS[i];
            final String[] suffexes = yRot.ordinal() % 2 == 0 ? FLATTENED_MODEL_SUFFEXES : FLATTENED_MODEL_SUFFEXES_ROT;
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
        return supplier;
    }

    static void registerChest(BlockStateModelGenerator modelGenerator, Block chest, Block particleSource) {
        modelGenerator.registerBuiltin(ModelIds.getBlockModelId(chest), particleSource).includeWithoutItem(chest);
        ItemModels.CHEST.upload(ModelIds.getItemModelId(chest.asItem()), TextureMap.particle(particleSource), modelGenerator.modelCollector);
    }

    static void registerBed(BlockStateModelGenerator modelGenerator, Block bed, Block particleSource) {
        modelGenerator.registerBuiltinWithParticle(bed, ModelIds.getBlockModelId(particleSource));
        modelGenerator.registerBed(bed, particleSource);
    }

    static void registerBale(BlockStateModelGenerator modelGenerator, Identifier blockId, Identifier baseBlockId, String endSuffex) {
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
                            return BALE_MODELS[ii].getLeft().upload(blockId.withPath(p -> "block/" + p + BALE_MODELS[ii].getRight()), textures, modelGenerator.modelCollector);
                        }))
                        .put(VariantSettings.X, axis == Direction.Axis.Y ? VariantSettings.Rotation.R0 : VariantSettings.Rotation.R90)
                        .put(VariantSettings.Y, axis == Direction.Axis.X ? VariantSettings.Rotation.R90 : VariantSettings.Rotation.R0)
                );
            }
        }

        modelGenerator.blockStateCollector.accept(supplier);
    }

    static void registerCropWithoutItem(BlockStateModelGenerator modelGenerator, Block crop, Property<Integer> ageProperty, int ... stages) {
        if (ageProperty.getValues().size() != stages.length) {
            throw new IllegalArgumentException();
        }
        Int2ObjectOpenHashMap<Identifier> uploadedModels = new Int2ObjectOpenHashMap<>();
        modelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(crop).coordinate(BlockStateVariantMap.create(ageProperty).register(integer -> {
            Identifier identifier = uploadedModels.computeIfAbsent(stages[integer], stage -> modelGenerator.createSubModel(crop, "_stage" + stage, Models.CROP, TextureMap::crop));
            return BlockStateVariant.create().put(VariantSettings.MODEL, identifier);
        })));
    }

    static <T extends Enum<T> & StringIdentifiable> void registerTallCrop(BlockStateModelGenerator modelGenerator, Block crop,
            Property<Integer> ageProperty,
            EnumProperty<T> partProperty,
            int[] ... ageTextureIndices) {
        Map<String, Identifier> uploadedModels = new HashMap<>();
        modelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(crop).coordinate(BlockStateVariantMap.create(partProperty, ageProperty).register((part, age) -> {
            int i = ageTextureIndices[part.ordinal()][age];
            Identifier identifier = uploadedModels.computeIfAbsent("_" + part.asString() + "_stage" + i, variant -> modelGenerator.createSubModel(crop, variant, Models.CROSS, TextureMap::cross));
            return BlockStateVariant.create().put(VariantSettings.MODEL, identifier);
        })));
    }

    static void registerFloweringLeaves(BlockStateModelGenerator modelGenerator, Block block) {
        Identifier baseModel = TexturedModel.LEAVES.upload(block, modelGenerator.modelCollector);
        Identifier floweringModel = Models.CUBE_ALL.upload(block, "_flowering", TextureMap.of(TextureKey.ALL, ModelIds.getBlockSubModelId(block, "_flowering")), modelGenerator.modelCollector);
        modelGenerator.blockStateCollector.accept(MultipartBlockStateSupplier.create(block)
                .with(BlockStateVariant.create().put(VariantSettings.MODEL, baseModel))
                .with(When.create().set(FruitBearingBlock.STAGE, FruitBearingBlock.Stage.FLOWERING), BlockStateVariant.create().put(VariantSettings.MODEL, floweringModel)));
    }

    static void registerZapLeaves(BlockStateModelGenerator modelGenerator, Block block) {
        Identifier baseModel = TexturedModel.LEAVES.upload(block, modelGenerator.modelCollector);
        Identifier floweringModel = Registries.BLOCK.getId(block).withPrefixedPath("block/flowering_");
        modelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block)
                .coordinate(BlockStateVariantMap.create(ZapAppleLeavesBlock.STAGE)
                .register(stage -> BlockStateVariant.create()
                        .put(VariantSettings.MODEL, stage == ZapAppleStageStore.Stage.FLOWERING ? floweringModel : baseModel))));
    }

    static void createSproutStages(BlockStateModelGenerator modelGenerator) {
        for (int i = 0; i < Models.STEM_GROWTH_STAGES.length; i++) {
            Models.STEM_GROWTH_STAGES[i].upload(Unicopia.id("block/apple_sprout_stage" + i), TextureMap.stem(Blocks.MELON_STEM), modelGenerator.modelCollector);
        }
    }

    static void registerSprout(BlockStateModelGenerator modelGenerator, Block block) {
        modelGenerator.blockStateCollector.accept(VariantsBlockStateSupplier.create(block)
                .coordinate(BlockStateVariantMap.create(Properties.AGE_7)
                .register(age -> BlockStateVariant.create()
                        .put(VariantSettings.MODEL, Unicopia.id("block/apple_sprout_stage" + age)))));
    }
}
