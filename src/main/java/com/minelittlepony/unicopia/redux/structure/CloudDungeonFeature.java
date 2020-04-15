package com.minelittlepony.unicopia.redux.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.core.UnicopiaCore;
import com.mojang.datafixers.Dynamic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableIntBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class CloudDungeonFeature extends BiomeWhitelistedFeature<DefaultFeatureConfig> {

    private static final BlockPos POS = new BlockPos(4, 0, 15);

    private static final Identifier[] VARIANTS = new Identifier[] {
            new Identifier(UnicopiaCore.MODID, "cloud/temple_small"),
            new Identifier(UnicopiaCore.MODID, "cloud/house_small"),
            new Identifier(UnicopiaCore.MODID, "cloud/island_small")
    };

    private static final List<Biome> ALLOWED_BIOMES = Arrays.<Biome>asList(
            Biomes.OCEAN,
            Biomes.WOODED_BADLANDS_PLATEAU,
            Biomes.DESERT,
            Biomes.DESERT_HILLS,
            Biomes.JUNGLE,
            Biomes.JUNGLE_HILLS,
            Biomes.SWAMP,
            Biomes.SWAMP_HILLS,
            Biomes.ICE_SPIKES,
            Biomes.TAIGA
    );

    public CloudDungeonFeature(Function<Dynamic<?>, ? extends DefaultFeatureConfig> func) {
        super(func);
    }

    @Override
    protected int getSeedModifier() {
        return 143592;
    }

    @Override
    public String getName() {
        return "unicopia:clouds";
    }

    @Override
    public int getRadius() {
        return 12;
    }

    @Override
    protected boolean canSpawnInBiome(@Nonnull Biome biome) {
        return ALLOWED_BIOMES.contains(biome);
    }

    @Override
    public StructureStartFactory getStructureStartFactory() {
        return Start::new;
    }

    public static class Start extends StructureStart {
        public Start(StructureFeature<?> feature, int x, int z, Biome biome, MutableIntBoundingBox bound, int var6, long var7) {
            super(feature, x, z, biome, bound, var6, var7);
        }

        @Override
        public void initialize(ChunkGenerator<?> generator, StructureManager manager, int x, int z, Biome biome) {

             BlockRotation rotation = BlockRotation.values()[random.nextInt(BlockRotation.values().length)];
             BlockPos pos = new BlockPos(x * 16, 150, z * 16);

             Identifier template = VARIANTS[random.nextInt(VARIANTS.length) % VARIANTS.length];
             children.add(new Piece(manager, template, pos, rotation));
             setBoundingBoxFromChildren();
        }
    }

    public static class Piece extends SimpleStructurePiece {

        private final BlockRotation rotation;
        private final Identifier template;

        public Piece(StructureManager manager, Identifier template, BlockPos pos, BlockRotation rotation) {
            super(UStructures.CLOUD_HOUSE, 0);
            this.pos = pos;
            this.rotation = rotation;
            this.template = template;
            init(manager);

        }

        public Piece(StructureManager manager, CompoundTag tag) {
           super(UStructures.CLOUD_HOUSE, tag);
           this.template = new Identifier(tag.getString("Template"));
           this.rotation = BlockRotation.valueOf(tag.getString("Rot"));
           init(manager);
        }

        @Override
        protected void toNbt(CompoundTag compoundTag_1) {
           super.toNbt(compoundTag_1);
           compoundTag_1.putString("Template", template.toString());
           compoundTag_1.putString("Rot", rotation.name());
        }

        private void init(StructureManager manager) {
           setStructureData(manager.getStructureOrBlank(template), pos, new StructurePlacementData()
                   .setRotation(rotation)
                   .setMirrored(BlockMirror.NONE)
                   .setPosition(POS)
                   .addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR_AND_STRUCTURE_BLOCKS));
        }

        @Override
        protected void handleMetadata(String var1, BlockPos var2, IWorld var3, Random var4, MutableIntBoundingBox var5) {
        }
    }
}
