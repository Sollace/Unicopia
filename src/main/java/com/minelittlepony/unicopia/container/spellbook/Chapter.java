package com.minelittlepony.unicopia.container.spellbook;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextColor;

public record Chapter (
    TabSide side,
    int tabY,
    int color,
    Chapter.Contents contents) {
    public static final Codec<Chapter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TabSide.CODEC.fieldOf("side").forGetter(Chapter::side),
            Codec.INT.fieldOf("y_position").forGetter(Chapter::tabY),
            Codec.INT.optionalFieldOf("color", 0).forGetter(Chapter::color),
            Contents.CODEC.fieldOf("contents").forGetter(Chapter::contents)
    ).apply(instance, Chapter::new));

    record Contents(List<Page> pages) {
        public static final Codec<Chapter.Contents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Page.CODEC.listOf().fieldOf("pages").forGetter(Contents::pages)
        ).apply(instance, Chapter.Contents::new));

        public record Page (
                Text title,
                int level,
                int color,
                List<ChapterPageElement> elements
            ) {
            public static final Codec<Page> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    TextCodecs.CODEC.fieldOf("title").forGetter(Page::title),
                    Codec.INT.fieldOf("level").forGetter(Page::level),
                    TextColor.CODEC.fieldOf("color").xmap(TextColor::getRgb, TextColor::fromRgb).forGetter(Page::level),
                    ChapterPageElement.CODEC.listOf().fieldOf("elements").forGetter(Page::elements)
            ).apply(instance, Page::new));

            public void toBuffer(RegistryByteBuf buffer) {
                TextCodecs.PACKET_CODEC.encode(buffer, title);
                buffer.writeInt(level);
                buffer.writeInt(color);
                buffer.writeCollection(elements, ChapterPageElement::write);
            }

            public static void write(PacketByteBuf buffer, Page page) {
                page.toBuffer((RegistryByteBuf)buffer);
            }
        }
    }

    public void write(RegistryByteBuf buffer) {
        buffer.writeEnumConstant(side);
        buffer.writeInt(tabY);
        buffer.writeInt(color);
        buffer.writeCollection(contents.pages(), Contents.Page::write);
    }
}