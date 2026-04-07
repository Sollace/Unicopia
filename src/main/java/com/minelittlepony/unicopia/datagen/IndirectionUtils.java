package com.minelittlepony.unicopia.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.block.Block;
import net.minecraft.client.data.BlockModelDefinitionCreator;
import net.minecraft.client.render.model.json.BlockModelDefinition;
import net.minecraft.client.render.model.json.MultipartModelComponent;
import net.minecraft.client.render.model.json.MultipartModelCondition;
import net.minecraft.client.render.model.json.MultipartModelConditionBuilder;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public interface IndirectionUtils {

    static <T> RegistryEntry<T> entryOf(RegistryKey<Registry<T>> registry, Identifier value, T dummyValue) {
        final Registry<T> registryRef = Untyped.cast(Registries.REGISTRIES.get(registry.getValue()));
        class Entry extends RegistryEntry.Reference<T> {
            Entry() {
                super(
                    RegistryEntry.Reference.Type.STAND_ALONE,
                    registryRef,
                    RegistryKey.of(registry, value),
                    dummyValue
                );
            }
        }
        return new Entry();
    }

    static IndirectMultipartBlockStateSupplier multipartBlockStateSupplier(Identifier block) {
        return new IndirectMultipartBlockStateSupplier(block);
    }

    public class IndirectMultipartBlockStateSupplier implements BlockModelDefinitionCreator, DataCollector.Identifiable {
        private final List<Part> parts = new ArrayList<>();

        private final Identifier block;

        private IndirectMultipartBlockStateSupplier(Identifier block) {
            this.block = block;
        }

        @Override
        public Block getBlock() {
            throw new RuntimeException("Stub");
        }

        @Override
        public Identifier getId() {
            return block;
        }

        public IndirectMultipartBlockStateSupplier with(WeightedVariant variant) {
            parts.add(new Part(Optional.empty(), variant));
            return this;
        }

        public IndirectMultipartBlockStateSupplier with(MultipartModelCondition condition, WeightedVariant variant) {
            parts.add(new Part(Optional.of(condition), variant));
            return this;
        }

        public IndirectMultipartBlockStateSupplier with(MultipartModelConditionBuilder conditionBuilder, WeightedVariant part) {
            return with(conditionBuilder.build(), part);
        }

        @Override
        public BlockModelDefinition createBlockModelDefinition() {
            return new BlockModelDefinition(
                Optional.empty(),
                Optional.of(new BlockModelDefinition.Multipart(parts.stream().map(Part::toComponent).toList()))
            );
        }

        record Part(Optional<MultipartModelCondition> condition, WeightedVariant variants) {
            public MultipartModelComponent toComponent() {
                return new MultipartModelComponent(condition, variants.toModel());
            }
        }
    }
}
