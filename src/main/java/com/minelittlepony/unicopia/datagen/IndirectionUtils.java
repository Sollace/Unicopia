package com.minelittlepony.unicopia.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateSupplier;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.When;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;

public interface IndirectionUtils {

    static <T> RegistryEntry<T> entryOf(RegistryKey<Registry<T>> registry, Identifier value, T dummyValue) {
        final Registry<T> registryRef = Untyped.cast(Registries.REGISTRIES.get(registry.getValue()));
        class Entry extends RegistryEntry.Reference<T> {
            Entry() {
                super(
                    RegistryEntry.Reference.Type.STAND_ALONE,
                    registryRef.getEntryOwner(),
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

    public class IndirectMultipartBlockStateSupplier implements BlockStateSupplier, DataCollector.Identifiable {
        private final List<IndirectMultipartBlockStateSupplier.Multipart> multiparts = new ArrayList<>();

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

        public IndirectMultipartBlockStateSupplier with(List<BlockStateVariant> variants) {
            this.multiparts.add(new IndirectMultipartBlockStateSupplier.Multipart(variants));
            return this;
        }

        public IndirectMultipartBlockStateSupplier with(BlockStateVariant variant) {
            return this.with(ImmutableList.of(variant));
        }

        public IndirectMultipartBlockStateSupplier with(When condition, List<BlockStateVariant> variants) {
            this.multiparts.add(new IndirectMultipartBlockStateSupplier.ConditionalMultipart(condition, variants));
            return this;
        }

        public IndirectMultipartBlockStateSupplier with(When condition, BlockStateVariant... variants) {
            return this.with(condition, ImmutableList.copyOf(variants));
        }

        public IndirectMultipartBlockStateSupplier with(When condition, BlockStateVariant variant) {
            return this.with(condition, ImmutableList.of(variant));
        }

        @Override
        public JsonElement get() {
            JsonArray jsonArray = new JsonArray();
            this.multiparts.stream().map(IndirectMultipartBlockStateSupplier.Multipart::get).forEach(jsonArray::add);
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("multipart", jsonArray);
            return jsonObject;
        }

        static class ConditionalMultipart extends IndirectMultipartBlockStateSupplier.Multipart {
            private final When when;

            ConditionalMultipart(When when, List<BlockStateVariant> variants) {
                super(variants);
                this.when = when;
            }

            @Override
            public void validate(StateManager<?, ?> stateManager) {
                this.when.validate(stateManager);
            }

            @Override
            public void extraToJson(JsonObject json) {
                json.add("when", this.when.get());
            }
        }

        static class Multipart implements Supplier<JsonElement> {
            private final List<BlockStateVariant> variants;

            Multipart(List<BlockStateVariant> variants) {
                this.variants = variants;
            }

            public void validate(StateManager<?, ?> stateManager) {
            }

            public void extraToJson(JsonObject json) {
            }

            @Override
            public JsonElement get() {
                JsonObject jsonObject = new JsonObject();
                this.extraToJson(jsonObject);
                jsonObject.add("apply", BlockStateVariant.toJson(this.variants));
                return jsonObject;
            }
        }
    }
}
