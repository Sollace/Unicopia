package com.minelittlepony.unicopia.client.gui.spellbook.element;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Drawable;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader.Flow;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public interface PageElement extends Drawable {
    @Override
    default void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {

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

    default void compile(int y, IViewRoot Container) {}

    static PageElement read(DynamicContent.Page page, PacketByteBuf buffer) {
        byte type = buffer.readByte();
        return switch (type) {
            case 0 -> new Image(buffer.readIdentifier(), boundsFromBuffer(buffer), buffer.readEnumConstant(Flow.class));
            case 1 -> new Recipe(page, buffer.readIdentifier(), Bounds.empty());
            case 2 -> new Stack(page, IngredientWithSpell.fromPacket(buffer), boundsFromBuffer(buffer));
            case 3 -> new TextBlock(page, List.of(Suppliers.ofInstance(buffer.readText())));
            case 4 -> new TextBlock(page, buffer.readList(b -> {
                int count = b.readVarInt();
                byte t = b.readByte();
                return switch (t) {
                    case 1 -> formatLine(capture(b.readIdentifier(), id -> {
                        return Registries.ITEM.get(id).getDefaultStack().getName();
                    }), "item", count);
                    case 2 -> formatLine(Trait.fromId(b.readIdentifier()).orElseThrow()::getShortName, "trait", count);
                    case 3 -> Suppliers.ofInstance(b.readText());
                    case 4 -> formatLine(SpellType.getKey(b.readIdentifier())::getName, "spell", count);
                    default -> throw new IllegalArgumentException("Unexpected value: " + t);
                };
            }));
            case 5 -> new Structure.Builder().fromBuffer(buffer).build();
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