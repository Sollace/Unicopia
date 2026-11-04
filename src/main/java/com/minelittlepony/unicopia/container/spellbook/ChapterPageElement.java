package com.minelittlepony.unicopia.container.spellbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.Schematic;
import com.minelittlepony.unicopia.util.Untyped;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;

public interface ChapterPageElement extends UnaryOperator<SpellbookChapter.Page.Builder> {
    record Type<T extends ChapterPageElement>(Identifier id, Codec<? extends T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec) {
        private static final Map<Identifier, Type<?>> REGISTRY = new HashMap<>();

        public static final Codec<Type<?>> CODEC = Identifier.CODEC.xmap(REGISTRY::get, Type::id);
        public static final PacketCodec<ByteBuf, Type<?>> PACKET_CODEC = Identifier.PACKET_CODEC.xmap(REGISTRY::get, Type::id);

        public static final Type<Image> IMAGE = create(Unicopia.id("image"), Image.MAP_CODEC, Image.PACKET_CODEC);
        public static final Type<Recipe> RECIPE = create(Unicopia.id("recipe"), Recipe.MAP_CODEC, Recipe.PACKET_CODEC);
        public static final Type<Stack> STACK = create(Unicopia.id("stack"), Stack.MAP_CODEC, Stack.PACKET_CODEC);
        public static final Type<TextBlock> TEXT_BLOCK = create(Unicopia.id("text_block"), TextBlock.CODEC, TextBlock.PACKET_CODEC);
        public static final Type<Ingredients> INGREDIENTS = create(Unicopia.id("ingredients"), Ingredients.MAP_CODEC, Ingredients.PACKET_CODEC);
        public static final Type<Structure> STRUCTURE = create(Unicopia.id("structure"), Structure.MAP_CODEC, Structure.PACKET_CODEC);

        public static final <T extends ChapterPageElement> Type<T> create(Identifier id, Codec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec) {
            var type = new Type<>(id, codec, packetCodec);
            REGISTRY.put(id, type);
            return type;
        }

        public static final <T extends ChapterPageElement> Type<T> create(Identifier id, MapCodec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec) {
            return create(id, codec.codec(), packetCodec);
        }
    }

    MapCodec<Bounds> BOUNDS_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("y", 0).forGetter(o -> o.top),
            Codec.INT.optionalFieldOf("x", 0).forGetter(o -> o.left),
            Codec.INT.optionalFieldOf("width", 0).forGetter(o -> o.width),
            Codec.INT.optionalFieldOf("height", 0).forGetter(o -> o.height)
    ).apply(i, Bounds::new));
    PacketCodec<PacketByteBuf, Bounds> BOUNDS_PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, o -> o.top,
            PacketCodecs.INTEGER, o -> o.left,
            PacketCodecs.INTEGER, o -> o.width,
            PacketCodecs.INTEGER, o -> o.height,
            Bounds::new
    );
    Codec<ChapterPageElement> CODEC = CodecUtils.fuzzy(Type.CODEC, () -> Type.REGISTRY.values().stream(), ChapterPageElement::type, Type::codec);
    PacketCodec<RegistryByteBuf, ChapterPageElement> PACKET_CODEC = Type.PACKET_CODEC.<RegistryByteBuf>cast().dispatch(ChapterPageElement::type, Type::packetCodec);

    Type<?> type();

    @Override
    default SpellbookChapter.Page.Builder apply(SpellbookChapter.Page.Builder builder) {
        return builder.element(this);
    }

    record Image (Identifier texture, Bounds bounds, Flow flow) implements ChapterPageElement {
        public static final MapCodec<Image> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Image::texture),
                BOUNDS_CODEC.fieldOf("bounds").forGetter(Image::bounds),
                Flow.CODEC.optionalFieldOf("flow", Flow.RIGHT).forGetter(Image::flow)
        ).apply(i, Image::new));
        public static final PacketCodec<RegistryByteBuf, Image> PACKET_CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, Image::texture,
                BOUNDS_PACKET_CODEC, Image::bounds,
                Flow.PACKET_CODEC, Image::flow,
                Image::new
        );

        @Override
        public Type<?> type() {
            return Type.IMAGE;
        }
    }

    record Recipe(RegistryKey<net.minecraft.recipe.Recipe<?>> recipe) implements ChapterPageElement {
        public static final MapCodec<Recipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                RegistryKey.createCodec(RegistryKeys.RECIPE).fieldOf("recipe").forGetter(Recipe::recipe)
        ).apply(i, Recipe::new));
        public static final PacketCodec<ByteBuf, Recipe> PACKET_CODEC = RegistryKey.createPacketCodec(RegistryKeys.RECIPE).xmap(Recipe::new, Recipe::recipe);

        @Override
        public Type<?> type() {
            return Type.RECIPE;
        }
    }

    record Stack (IngredientWithSpell ingredient, Bounds bounds) implements ChapterPageElement {
        public static final MapCodec<Stack> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                IngredientWithSpell.FLEXIBLE_CODEC.fieldOf("item").forGetter(Stack::ingredient),
                BOUNDS_CODEC.forGetter(Stack::bounds)
        ).apply(i, Stack::new));
        public static final PacketCodec<RegistryByteBuf, Stack> PACKET_CODEC = PacketCodec.tuple(
                IngredientWithSpell.PACKET_CODEC, Stack::ingredient,
                BOUNDS_PACKET_CODEC, Stack::bounds,
                Stack::new
        );

        @Override
        public Type<?> type() {
            return Type.STACK;
        }
    }

    interface TextElement extends ChapterPageElement {
        Stream<Text> lines();
    }

    record TextBlock (Text text) implements TextElement {
        public static final Codec<Text> FLEXIBLE_TEXT_CODEC = Codec.either(Codec.STRING, TextCodecs.CODEC).xmap(
                        either -> Either.unwrap(either.mapLeft(Text::translatable)),
                        text -> {
            if (text.getStyle().isEmpty()) {
                if (text.getLiteralString() != null) {
                    return Either.left(text.getLiteralString());
                }
                if (text.getContent() instanceof TranslatableTextContent c && c.getFallback() == null) {
                    return Either.left(c.getKey());
                }
            }
            return Either.right(text);
        });
        public static final Codec<TextBlock> CODEC = FLEXIBLE_TEXT_CODEC.xmap(TextBlock::new, TextBlock::text);
        public static final PacketCodec<ByteBuf, TextBlock> PACKET_CODEC = TextCodecs.PACKET_CODEC.xmap(TextBlock::new, TextBlock::text);

        @Override
        public Type<?> type() {
            return Type.TEXT_BLOCK;
        }

        @Override
        public Stream<Text> lines() {
            return Stream.of(text);
        }
    }

    record Ingredients(List<Multi<?>> entries) implements TextElement {
        public static final MapCodec<Ingredients> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Multi.CODEC.listOf().fieldOf("ingredients").forGetter(Ingredients::entries)
        ).apply(i, Ingredients::new));

        public static final PacketCodec<RegistryByteBuf, Ingredients> PACKET_CODEC = Multi.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(Ingredients::new, Ingredients::entries);

        @Override
        public Type<?> type() {
            return Type.INGREDIENTS;
        }

        @Override
        public Stream<Text> lines() {
            return entries().stream().map(Multi::text);
        }

        public record Multi<T>(T element, int count) {
            public record Type<T>(Identifier id, Codec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec, Function<Multi<T>, Text> textFunction) {
                private static final Map<Identifier, Type<?>> REGISTRY = new HashMap<>();
                public static final Codec<Type<?>> CODEC = CodecUtils.identifierWithDefaultnamespace(Unicopia.DEFAULT_NAMESPACE).xmap(REGISTRY::get, Type::id);
                public static final PacketCodec<ByteBuf, Type<?>> PACKET_CODEC = Identifier.PACKET_CODEC.xmap(REGISTRY::get, Type::id);

                private static final MapCodec<Object> ELEMENT_CODEC = CodecUtils.mapFuzzy(CODEC, () -> REGISTRY.values().stream(), Type::of, Type::codec);
                private static final PacketCodec<? super RegistryByteBuf, Object> ELEMENT_PACKET_CODEC = PACKET_CODEC.<RegistryByteBuf>cast().dispatch(Type::of, Type::packetCodec);

                public static final Type<Item> ITEM = create(Unicopia.id("item"), Registries.ITEM.getCodec(), PacketCodecs.registryValue(RegistryKeys.ITEM), i -> formatLine(i.element().getDefaultStack().getName(), "item", i.count()));
                public static final Type<Trait> TRAIT = create(Unicopia.id("trait"), Trait.CODEC, Trait.PACKET_CODEC, i -> formatLine(i.element().getShortName(), "trait", i.count()));
                public static final Type<Text> TEXT = create(Unicopia.id("text"), TextBlock.FLEXIBLE_TEXT_CODEC, TextCodecs.PACKET_CODEC, Multi::element);
                public static final Type<SpellType<?>> SPELL = create(Unicopia.id("spell"), SpellType.CODEC, SpellType.PACKET_CODEC, i -> formatLine(i.element().getName(), "spell", i.count()));

                private static Text formatLine(Text line, String kind, int count) {
                    return Text.translatable("gui.unicopia.spellbook.page.requirements.entry." + kind, count, line);
                }

                public static <T> Type<T> create(Identifier id, Codec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec, Function<Multi<T>, Text> textFunction) {
                    var type = new Type<>(id, codec, packetCodec, textFunction);
                    REGISTRY.put(id, type);
                    return type;
                }

                public static <T> Type<T> of(T element) {
                    return Untyped.cast(switch (element) {
                        case Item i -> Type.ITEM;
                        case Trait t -> Type.TRAIT;
                        case Text t -> Type.TEXT;
                        case SpellType<?> s -> Type.SPELL;
                        default -> throw new IllegalArgumentException("Don't know how to serialize " + element);
                    });
                }
            }

            public static final Codec<Multi<?>> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Type.ELEMENT_CODEC.forGetter(Multi::element),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(Multi::count)
            ).apply(i, Multi::new));

            public static final PacketCodec<RegistryByteBuf, Multi<?>> PACKET_CODEC = PacketCodec.tuple(
                    Type.ELEMENT_PACKET_CODEC, Multi::element,
                    PacketCodecs.INTEGER, Multi::count,
                    Multi::new
            );

            public Text text() {
                return Type.of(element).textFunction().apply(this);
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private final List<Multi<?>> entries = new ArrayList<>();

            private Builder() {}

            public Builder all(Map<? extends ItemConvertible, Integer> materials) {
                materials.forEach((a, b) -> item(b, a));
                return this;
            }

            public Builder item(int count, ItemConvertible item) {
                entries.add(new Multi<>(item.asItem(), count));
                return this;
            }

            public Builder trait(int count, Trait trait) {
                entries.add(new Multi<>(trait, count));
                return this;
            }

            public Builder text(int count, Text text) {
                entries.add(new Multi<>(text, count));
                return this;
            }

            public Builder spell(int count, SpellType<?> spell) {
                entries.add(new Multi<>(spell, count));
                return this;
            }

            public Ingredients build() {
                return new Ingredients(List.copyOf(entries));
            }
        }
    }

    record Structure(Schematic.Unbaked schematic) implements ChapterPageElement {
        public static final MapCodec<Structure> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Schematic.Unbaked.CODEC.fieldOf("schematic").forGetter(Structure::schematic)
        ).apply(i, Structure::new));
        public static final PacketCodec<ByteBuf, Structure> PACKET_CODEC = Schematic.Unbaked.PACKET_CODEC.xmap(Structure::new, Structure::schematic);

        @Override
        public Type<?> type() {
            return Type.STRUCTURE;
        }

        public Schematic toSchematic() {
            return schematic.bake();
        }
    }
}
