package com.minelittlepony.unicopia.container.spellbook;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextColor;

public record Chapter (
    TabSide side,
    int tabY,
    int color,
    List<Page> pages
) implements SpellbookChapter {
    public static final Codec<Chapter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TabSide.CODEC.fieldOf("side").forGetter(Chapter::side),
            Codec.INT.fieldOf("y_position").forGetter(Chapter::tabY),
            Codec.INT.optionalFieldOf("color", 0).forGetter(Chapter::color),
            Contents.CODEC.xmap(Contents::pages, Contents::new).fieldOf("content").forGetter(Chapter::pages)
    ).apply(instance, Chapter::new));
    public static final PacketCodec<RegistryByteBuf, Chapter> PACKET_CODEC = PacketCodec.tuple(
            TabSide.PACKET_CODEC, Chapter::side,
            PacketCodecs.INTEGER, Chapter::tabY,
            PacketCodecs.INTEGER, Chapter::color,
            Page.PACKET_CODEC.collect(PacketCodecs.toList()), Chapter::pages,
            Chapter::new
    );

    record Contents(List<Page> pages) {
        public static final Codec<Contents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Page.CODEC.listOf().fieldOf("pages").forGetter(Contents::pages)
        ).apply(instance, Chapter.Contents::new));
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
    }
}