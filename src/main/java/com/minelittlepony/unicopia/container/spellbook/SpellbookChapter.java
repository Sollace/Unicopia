package com.minelittlepony.unicopia.container.spellbook;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextColor;

public record SpellbookChapter (
    TabSide side,
    int tabY,
    int color,
    List<Page> pages
) {
    public static final Codec<SpellbookChapter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TabSide.CODEC.fieldOf("side").forGetter(SpellbookChapter::side),
            Codec.INT.fieldOf("y_position").forGetter(SpellbookChapter::tabY),
            Codec.INT.optionalFieldOf("color", 0).forGetter(SpellbookChapter::color),
            Contents.CODEC.xmap(Contents::pages, Contents::new).fieldOf("content").forGetter(SpellbookChapter::pages)
    ).apply(instance, SpellbookChapter::new));
    public static final PacketCodec<RegistryByteBuf, SpellbookChapter> PACKET_CODEC = PacketCodec.tuple(
            TabSide.PACKET_CODEC, SpellbookChapter::side,
            PacketCodecs.INTEGER, SpellbookChapter::tabY,
            PacketCodecs.INTEGER, SpellbookChapter::color,
            Page.PACKET_CODEC.collect(PacketCodecs.toList()), SpellbookChapter::pages,
            SpellbookChapter::new
    );

    record Contents(List<Page> pages) {
        public static final Codec<Contents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Page.CODEC.listOf().fieldOf("pages").forGetter(Contents::pages)
        ).apply(instance, SpellbookChapter.Contents::new));
    }

    public record Page (
            Text title,
            int level,
            int color,
            List<ChapterPageElement> elements
        ) {
        public static final Codec<Page> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TextCodecs.CODEC.optionalFieldOf("title", Text.empty()).forGetter(Page::title),
                Codec.INT.optionalFieldOf("level", 0).forGetter(Page::level),
                TextColor.CODEC.xmap(TextColor::getRgb, TextColor::fromRgb).optionalFieldOf("color", 0).forGetter(Page::color),
                ChapterPageElement.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(Page::elements)
        ).apply(instance, Page::new));
        public static final PacketCodec<RegistryByteBuf, Page> PACKET_CODEC = PacketCodec.tuple(
            TextCodecs.PACKET_CODEC, Page::title,
            PacketCodecs.INTEGER, Page::level,
            PacketCodecs.INTEGER, Page::color,
            ChapterPageElement.PACKET_CODEC.collect(PacketCodecs.toList()), Page::elements,
            Page::new
        );

        public static final class Builder {
            private Text title = Text.empty();
            private int level = 0;
            private int color = 0;
            private final List<ChapterPageElement> elements = new ArrayList<>();

            private Builder() {}

            public Builder title(Text title) {
                this.title = title;
                return this;
            }

            public Builder level(int level) {
                this.level = level;
                return this;
            }

            public Builder color(int color) {
                this.color = color;
                return this;
            }

            public Builder element(ChapterPageElement...elements) {
                this.elements.addAll(List.of(elements));
                return this;
            }

            public Builder apply(UnaryOperator<Builder> action) {
                return action.apply(this);
            }

            public Page build() {
                return new Page(title, level, color, List.copyOf(elements));
            }
        }
    }

    public static Page.Builder page() {
        return new Page.Builder();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TabSide side = TabSide.LEFT;
        private int color;
        private int tabY;
        private final List<Page> pages = new ArrayList<>();

        private Builder() {}

        public Builder side(TabSide side) {
            this.side = side;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder tabY(int tabY) {
            this.tabY = tabY;
            return this;
        }

        public Builder page(Page.Builder page) {
            this.pages.add(page.build());
            return this;
        }

        public Builder apply(UnaryOperator<Builder> action) {
            return action.apply(this);
        }

        public SpellbookChapter build() {
            return new SpellbookChapter(side, tabY, color, List.copyOf(pages));
        }
    }
}