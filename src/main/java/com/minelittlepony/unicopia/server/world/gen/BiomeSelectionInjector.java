package com.minelittlepony.unicopia.server.world.gen;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import com.minelittlepony.unicopia.server.world.gen.BiomeSelectionContext.SplittableBiomeCoordinate;
import com.mojang.datafixers.util.Pair;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.NoiseHypercube;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.ParameterRange;

public final class BiomeSelectionInjector implements Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> {

    private final Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameterConsumer;

    public BiomeSelectionInjector(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameterConsumer) {
        this.parameterConsumer = parameterConsumer;
    }

    @Override
    public void accept(Pair<NoiseHypercube, RegistryKey<Biome>> parameter) {
        Context context = new Context(parameter);
        OverworldBiomeSelectionCallback.EVENT.invoker().onSelectingBiome(context);
        if (!context.overrides.isEmpty()) {
            List<Pair<NoiseHypercube, RegistryKey<Biome>>> subSections = List.of(parameter);
            // sort splits from largest to smallest
            var divisions = context.overrides.entrySet()
                .stream()
                .sorted(Comparator.<Map.Entry<SplittableBiomeCoordinate, RegistryKey<Biome>>, Float>comparing(entry -> Context.getVolume(entry.getKey())).reversed())
                .toList();
            // recursively sub-divide into parts so every split gets a (kinda) fair share
            for (Map.Entry<SplittableBiomeCoordinate, RegistryKey<Biome>> division : divisions) {
                subSections = subSections.stream().flatMap(par -> Stream.of(
                        Pair.of(Context.write(division.getKey(), par.getFirst()), division.getValue()),
                        par
                )).toList();
            }
            // output the result
            subSections.forEach(parameterConsumer);
        } else {
            parameterConsumer.accept(parameter);
        }
    }

    class Context implements BiomeSelectionContext {
        private final Pair<NoiseHypercube, RegistryKey<Biome>> parameter;
        private final Map<SplittableBiomeCoordinate, RegistryKey<Biome>> overrides = new LinkedHashMap<>();

        public Context(Pair<NoiseHypercube, RegistryKey<Biome>> parameter) {
            this.parameter = parameter;
        }

        @Override
        public RegistryKey<Biome> biomeKey() {
            return parameter.getSecond();
        }

        @Override
        public SplittableBiomeCoordinate referenceFrame() {
            return createStart(parameter.getFirst());
        }

        @Override
        @Nullable
        public RegistryKey<Biome> addOverride(SplittableBiomeCoordinate coordinate, RegistryKey<Biome> biome) {
            return overrides.put(coordinate, biome);
        }

        static float getVolume(SplittableBiomeCoordinate coordinate) {
            return ((SplitableParameterRangeImpl)coordinate.temperature()).length()
                    * ((SplitableParameterRangeImpl)coordinate.humidity()).length()
                    * ((SplitableParameterRangeImpl)coordinate.continentalness()).length()
                    * ((SplitableParameterRangeImpl)coordinate.erosion()).length()
                    * ((SplitableParameterRangeImpl)coordinate.depth()).length()
                    * ((SplitableParameterRangeImpl)coordinate.weirdness()).length();
        }

        static NoiseHypercube write(SplittableBiomeCoordinate coordinate, NoiseHypercube referenceFrame) {
            return new NoiseHypercube(
                    ((SplitableParameterRangeImpl)coordinate.temperature()).write(referenceFrame.temperature()),
                    ((SplitableParameterRangeImpl)coordinate.humidity()).write(referenceFrame.humidity()),
                    ((SplitableParameterRangeImpl)coordinate.continentalness()).write(referenceFrame.continentalness()),
                    ((SplitableParameterRangeImpl)coordinate.erosion()).write(referenceFrame.erosion()),
                    ((SplitableParameterRangeImpl)coordinate.depth()).write(referenceFrame.depth()),
                    ((SplitableParameterRangeImpl)coordinate.weirdness()).write(referenceFrame.weirdness()),
                    coordinate.offset()
            );
        }

        static SplittableBiomeCoordinate createStart(NoiseHypercube referenceFrame) {
            final SplittableBiomeCoordinate[] self = new SplittableBiomeCoordinate[1];
            return self[0] = new SplittableBiomeCoordinate(
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::temperature, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::humidity, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::continentalness, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::erosion, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::depth, 0, 1),
                    new SplitableParameterRangeImpl(self, SplittableBiomeCoordinate::weirdness, 0, 1),
                    referenceFrame.offset()
            );
        }

        static SplittableBiomeCoordinate createCopy(SplittableBiomeCoordinate old) {
            final SplittableBiomeCoordinate[] self = new SplittableBiomeCoordinate[1];
            return self[0] = new SplittableBiomeCoordinate(
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.temperature()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.humidity()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.continentalness()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.erosion()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.depth()),
                    new SplitableParameterRangeImpl(self, (SplitableParameterRangeImpl)old.weirdness()),
                    old.offset()
            );
        }

        private static final class SplitableParameterRangeImpl implements SplitableParameterRange {
            private final SplittableBiomeCoordinate[] coordinate;
            private final Function<SplittableBiomeCoordinate, SplitableParameterRange> dimension;
            private float min;
            private float max;

            SplitableParameterRangeImpl(SplittableBiomeCoordinate[] coordinate, SplitableParameterRangeImpl original) {
                this(coordinate, original.dimension, original.min, original.max);
            }

            SplitableParameterRangeImpl(SplittableBiomeCoordinate[] coordinate, Function<SplittableBiomeCoordinate, SplitableParameterRange> dimension, float min, float max) {
                this.coordinate = coordinate;
                this.dimension = dimension;
                this.min = min;
                this.max = max;
            }

            @Override
            public SplittableBiomeCoordinate splitAbove(float midpoint) {
                return copyWithDifference(o -> o.min = MathHelper.lerp(midpoint, o.min, o.max));
            }

            @Override
            public SplittableBiomeCoordinate splitBelow(float midpoint) {
                return copyWithDifference(o -> o.max = MathHelper.lerp(midpoint, o.min, o.max));
            }

            private SplittableBiomeCoordinate copyWithDifference(Consumer<SplitableParameterRangeImpl> mutator) {
                SplittableBiomeCoordinate copy = createCopy(coordinate[0]);
                mutator.accept((SplitableParameterRangeImpl)dimension.apply(copy));
                return copy;
            }

            public ParameterRange write(ParameterRange range) {
                return ParameterRange.of(
                        MultiNoiseUtil.toFloat(range.min()) * (1 + min),
                        MultiNoiseUtil.toFloat(range.max()) * max
                );
            }

            public float length() {
                return max - min;
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof SplitableParameterRangeImpl i
                        && MathHelper.approximatelyEquals(i.min, min)
                        && MathHelper.approximatelyEquals(i.max, max);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(min, max);
            }
        }
    }
}
