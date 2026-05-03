package com.minelittlepony.unicopia.container.spellbook;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3i;

public interface ChapterPageElement {
    byte IMAGE = 0;
    byte RECIPE = 1;
    byte STACK = 2;
    byte TEXT_BLOCK = 3;
    byte INGREDIENTS = 4;
    byte STRUCTURE = 5;

    Codec<Bounds> BOUNDS_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("x", 0).forGetter(o -> o.left),
            Codec.INT.optionalFieldOf("y", 0).forGetter(o -> o.top),
            Codec.INT.optionalFieldOf("width", 0).forGetter(o -> o.width),
            Codec.INT.optionalFieldOf("height", 0).forGetter(o -> o.height)
    ).apply(i, Bounds::new));
    PacketCodec<PacketByteBuf, Bounds> BOUNDS_PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, o -> o.left,
            PacketCodecs.INTEGER, o -> o.top,
            PacketCodecs.INTEGER, o -> o.width,
            PacketCodecs.INTEGER, o -> o.height,
            Bounds::new
    );
    Codec<ChapterPageElement> CODEC = Codecs.JSON_ELEMENT.xmap(json -> {
        if (!json.isJsonPrimitive()) {
            JsonObject el = JsonHelper.asObject(json, "element");
            if (el.has("texture")) return Image.CODEC.decode(JsonOps.INSTANCE, el).getOrThrow().getFirst();
            if (el.has("recipe")) return Recipe.CODEC.decode(JsonOps.INSTANCE, el.get("recipe")).getOrThrow().getFirst();
            if (el.has("item")) return Stack.CODEC.decode(JsonOps.INSTANCE, el).getOrThrow().getFirst();
            if (el.has("ingredients")) return Ingredients.CODEC.decode(JsonOps.INSTANCE, el.get("ingredients")).getOrThrow().getFirst();
            if (el.has("structure")) return Structure.CODEC.decode(JsonOps.INSTANCE, el.get("structure")).getOrThrow().getFirst();
        }
        return TextBlock.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
    }, page -> {
        throw new RuntimeException();
    });
    PacketCodec<RegistryByteBuf, ChapterPageElement> PACKET_CODEC = PacketCodecUtils.dispatch(ChapterPageElement::getType, PacketCodecs.BYTE, Map.of(
            IMAGE, Image.PACKET_CODEC,
            STACK, Stack.PACKET_CODEC,
            TEXT_BLOCK, TextBlock.PACKET_CODEC,
            STRUCTURE, Structure.PACKET_CODEC,
            INGREDIENTS, Ingredients.PACKET_CODEC
    ));

    byte getType();

    record Image (Identifier texture, Bounds bounds, Flow flow) implements ChapterPageElement {
        public static final Codec<Image> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Image::texture),
                BOUNDS_CODEC.fieldOf("bounds").forGetter(Image::bounds),
                Flow.CODEC.fieldOf("flow").forGetter(Image::flow)
        ).apply(i, Image::new));
        public static final PacketCodec<RegistryByteBuf, Image> PACKET_CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, Image::texture,
                BOUNDS_PACKET_CODEC, Image::bounds,
                Flow.PACKET_CODEC, Image::flow,
                Image::new
        );

        @Override
        public byte getType() {
            return IMAGE;
        }
    }

    record Recipe(RegistryEntry<net.minecraft.recipe.Recipe<?>> recipe, List<RecipeDisplay> recipeDisplays) implements ChapterPageElement {
        public static final Codec<Recipe> CODEC = RegistryElementCodec.of(RegistryKeys.RECIPE, net.minecraft.recipe.Recipe.CODEC, false)
                .xmap(entry -> new Recipe(entry, entry.value().getDisplays()), recipe -> recipe.recipe());
        public static final PacketCodec<RegistryByteBuf, Recipe> PACKET_CODEC = RecipeDisplay.STREAM_CODEC.collect(PacketCodecs.toList()).xmap(displays -> new Recipe(null, displays), Recipe::recipeDisplays);

        @Override
        public byte getType() {
            return RECIPE;
        }
    }

    record Stack (IngredientWithSpell ingredient, Bounds bounds) implements ChapterPageElement {
        public static final Codec<Stack> CODEC = RecordCodecBuilder.create(i -> i.group(
                IngredientWithSpell.FLEXIBLE_CODEC.fieldOf("item").forGetter(Stack::ingredient),
                MapCodec.assumeMapUnsafe(BOUNDS_CODEC).forGetter(Stack::bounds)
        ).apply(i, Stack::new));
        public static final PacketCodec<RegistryByteBuf, Stack> PACKET_CODEC = PacketCodec.tuple(
                IngredientWithSpell.PACKET_CODEC, Stack::ingredient,
                BOUNDS_PACKET_CODEC, Stack::bounds,
                Stack::new
        );

        @Override
        public byte getType() {
            return STACK;
        }
    }

    record TextBlock (Text text) implements ChapterPageElement {
        public static final Codec<TextBlock> CODEC = Codec.xor(
                Codec.STRING.flatXmap(s -> DataResult.success((Text)Text.translatable(s)), text -> DataResult.error(() -> "Cannot Serialize text to a plain string")),
                TextCodecs.CODEC
        ).xmap(Either::unwrap, Either::right).xmap(TextBlock::new, TextBlock::text);
        public static final PacketCodec<ByteBuf, TextBlock> PACKET_CODEC = TextCodecs.PACKET_CODEC.xmap(TextBlock::new, TextBlock::text);

        @Override
        public byte getType() {
            return TEXT_BLOCK;
        }
    }

    record Ingredients(List<Multi> entries) implements ChapterPageElement {
        public static final Codec<Ingredients> CODEC = Multi.CODEC.listOf().xmap(Ingredients::new, Ingredients::entries);
        public static final PacketCodec<ByteBuf, Ingredients> PACKET_CODEC = Multi.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(Ingredients::new, Ingredients::entries);

        @Override
        public byte getType() {
            return INGREDIENTS;
        }

        public record Multi(ChapterPageElement element, int count) {
            private static final MapCodec<ChapterPageElement> ELEMENT_CODEC = CodecUtils.<ChapterPageElement>dispatched(element -> switch(element.getType()) {
                case 1 -> "item";
                case 2 -> "trait";
                case 3 -> "text";
                case 4 -> "spell";
                default -> "text";
            }, Map.of(
                "item", Identifier.CODEC.xmap(id -> new Id((byte)1, id), Id::value),
                "trait", Trait.CODEC.xmap(trait -> trait.getId(), id -> Trait.fromId(id).get()).xmap(id -> new Id((byte)2, id), Id::value),
                "text", TextBlock.CODEC,
                "spell", Identifier.CODEC.xmap(id -> new Id((byte)1, id), Id::value)
            ));
            public static final Codec<Multi> CODEC = RecordCodecBuilder.create(i -> i.group(
                    ELEMENT_CODEC.forGetter(Multi::element),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(Multi::count)
            ).apply(i, Multi::new));
            private static final PacketCodec<ByteBuf, ChapterPageElement> ELEMENT_PACKET_CODEC = PacketCodecUtils.dispatch(ChapterPageElement::getType, PacketCodecs.BYTE, Map.of(
                    (byte)1, Id.packetCodec((byte)1),
                    (byte)2, Id.packetCodec((byte)2),
                    (byte)3, TextBlock.PACKET_CODEC,
                    (byte)4, Id.packetCodec((byte)4)
            ));
            public static final PacketCodec<ByteBuf, Multi> PACKET_CODEC = PacketCodec.tuple(
                    ELEMENT_PACKET_CODEC, Multi::element,
                    PacketCodecs.INTEGER, Multi::count,
                    Multi::new
            );

            record Id(byte id, Identifier value) implements ChapterPageElement {
                public static PacketCodec<ByteBuf, Id> packetCodec(byte type) {
                    return Identifier.PACKET_CODEC.xmap(id -> new Id(type, id), Id::value);
                }

                @Override
                public byte getType() {
                    return id;
                }
            }
        }

    }

    record Structure(List<ChapterPageElement> commands) implements ChapterPageElement {
        public static final Codec<Structure> CODEC = Codec.xor(Set.CODEC, Fill.CODEC)
                .xmap(either -> (ChapterPageElement)Either.unwrap(either), element -> element instanceof Set s ? Either.left(s) : Either.right((Fill)element))
                .listOf().xmap(Structure::new, Structure::commands);

        private static final PacketCodec<ByteBuf, ChapterPageElement> COMMAND_PACKET_CODEC = PacketCodecUtils.dispatch(ChapterPageElement::getType, PacketCodecs.BYTE, Map.of(
                (byte)1, Set.PACKET_CODEC,
                (byte)2, Fill.PACKET_CODEC
        ));
        public static final PacketCodec<ByteBuf, Structure> PACKET_CODEC = COMMAND_PACKET_CODEC.collect(PacketCodecs.toList()).xmap(Structure::new, Structure::commands);

        @Override
        public byte getType() {
            return STRUCTURE;
        }

        record Set(Vec3i pos, BlockState state) implements ChapterPageElement {
            public static final Codec<Set> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Vec3i.CODEC.fieldOf("pos").forGetter(Set::pos),
                    BlockState.CODEC.fieldOf("state").forGetter(Set::state)
            ).apply(i, Set::new));
            public static final PacketCodec<ByteBuf, Set> PACKET_CODEC = PacketCodec.tuple(
                    Vec3i.PACKET_CODEC, Set::pos,
                    PacketCodecUtils.BLOCK_STATE, Set::state,
                    Set::new
            );

            @Override
            public byte getType() {
                return 1;
            }
        }

        record Fill(Vec3i min, Vec3i max, BlockState state) implements ChapterPageElement {
            public static final Codec<Fill> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Vec3i.CODEC.fieldOf("min").forGetter(Fill::min),
                    Vec3i.CODEC.fieldOf("max").forGetter(Fill::max),
                    BlockState.CODEC.fieldOf("state").forGetter(Fill::state)
            ).apply(i, Fill::new));
            public static final PacketCodec<ByteBuf, Fill> PACKET_CODEC = PacketCodec.tuple(
                    Vec3i.PACKET_CODEC, Fill::min,
                    Vec3i.PACKET_CODEC, Fill::max,
                    PacketCodecUtils.BLOCK_STATE, Fill::state,
                    Fill::new
            );

            @Override
            public byte getType() {
                return 2;
            }
        }
    }
}
