package com.minelittlepony.unicopia.client.gui.spellbook.element;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipeDisplay;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.Schematic;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.container.spellbook.Flow;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public interface PageElement {

    default void draw(DynamicContent.Page page, DrawContext context, int mouseX, int mouseY, IViewRoot container) {

    }

    Bounds bounds();

    default Flow flow() {
        return Flow.NONE;
    }

    default boolean isInline() {
        return flow() == Flow.NONE;
    }

    default boolean isFloating() {
        return !isInline();
    }

    default void compile(DynamicContent.Page page, int y, IViewRoot Container) {}

    static PageElement read(RegistryByteBuf buffer) {
        byte type = buffer.readByte();
        return switch (type) {
            case ChapterPageElement.IMAGE -> new Image(buffer.readIdentifier(), boundsFromBuffer(buffer), buffer.readEnumConstant(Flow.class));
            case ChapterPageElement.RECIPE -> new Recipe(buffer.readIdentifier(), SpellbookRecipeDisplay.SERIALIZER.streamCodec().collect(PacketCodecs.toList()).decode(buffer), Bounds.empty());
            case ChapterPageElement.STACK -> new Stack(IngredientWithSpell.PACKET_CODEC.decode(buffer), boundsFromBuffer(buffer));
            case ChapterPageElement.TEXT_BLOCK -> new TextBlock(List.of(Suppliers.ofInstance(TextCodecs.PACKET_CODEC.decode(buffer))));
            case ChapterPageElement.INGREDIENTS -> new TextBlock(buffer.readList(b -> {
                int count = b.readVarInt();
                byte t = b.readByte();
                return switch (t) {
                    case 1 -> formatLine(capture(b.readIdentifier(), id -> Registries.ITEM.get(id).getDefaultStack().getName()), "item", count);
                    case 2 -> formatLine(Trait.fromId(b.readIdentifier()).get()::getShortName, "trait", count);
                    case 3 -> Suppliers.ofInstance(TextCodecs.PACKET_CODEC.decode(b));
                    case 4 -> formatLine(SpellType.getKey(b.readIdentifier())::getName, "spell", count);
                    default -> throw new IllegalArgumentException("Unexpected value: " + t);
                };
            }));
            case ChapterPageElement.STRUCTURE -> new Structure(Bounds.empty(), Schematic.fromPacket(buffer));
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    private static <T, V> Supplier<V> capture(T t, Function<T, V> func) {
        return () -> func.apply(t);
    }

    private static Supplier<Text> formatLine(Supplier<Text> line, String kind, int count) {
        return () -> Text.translatable("gui.unicopia.spellbook.page.requirements.entry." + kind, count, line.get());
    }

    private static Bounds boundsFromBuffer(PacketByteBuf buffer) {
        return new Bounds(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }
}