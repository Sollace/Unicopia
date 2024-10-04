package com.minelittlepony.unicopia.container.spellbook;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.block.state.StateUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;

public interface ChapterPageElement {
    byte IMAGE = 0;
    byte RECIPE = 1;
    byte STACK = 2;
    byte TEXT_BLOCK = 3;
    byte INGREDIENTS = 4;
    byte STRUCTURE = 5;

    Codec<ChapterPageElement> CODEC = Codecs.JSON_ELEMENT.xmap(json -> {
        if (!json.isJsonPrimitive()) {
            JsonObject el = JsonHelper.asObject(json, "element");
            if (el.has("texture")) return new Image(el);
            if (el.has("recipe")) return new Recipe(el);
            if (el.has("item")) return new Stack(el);
            if (el.has("ingredients")) return new Ingredients(el);
            if (el.has("structure")) return new Structure(el);
        }
        return new TextBlock(json);
    }, page -> {
        throw new RuntimeException();
    });

    static void write(PacketByteBuf buffer, ChapterPageElement element) {
        element.toBuffer((RegistryByteBuf)buffer);
    }

    private static Bounds boundsFromJson(JsonObject el) {
        return new Bounds(
            JsonHelper.getInt(el, "y", 0),
            JsonHelper.getInt(el, "x", 0),
            JsonHelper.getInt(el, "width", 0),
            JsonHelper.getInt(el, "height", 0)
        );
    }

    private static void boundsToBuffer(Bounds bounds, PacketByteBuf buffer) {
        buffer.writeInt(bounds.top);
        buffer.writeInt(bounds.left);
        buffer.writeInt(bounds.width);
        buffer.writeInt(bounds.height);
    }

    void toBuffer(RegistryByteBuf buffer);

    record Image (Identifier texture, Bounds bounds, Flow flow) implements ChapterPageElement {
        public Image(JsonObject json) {
            this(
                Identifier.of(JsonHelper.getString(json, "texture")),
                boundsFromJson(json),
                Flow.valueOf(JsonHelper.getString(json, "flow", "RIGHT"))
            );
        }
        @Override
        public void toBuffer(RegistryByteBuf buffer) {
            buffer.writeByte(IMAGE);
            buffer.writeIdentifier(texture);
            boundsToBuffer(bounds, buffer);
            buffer.writeEnumConstant(flow);
        }
    }

    record Recipe(Identifier value) implements ChapterPageElement {
        public Recipe(JsonObject json) {
            this(Identifier.of(JsonHelper.getString(json, "recipe")));
        }
        @Override
        public void toBuffer(RegistryByteBuf buffer) {
            buffer.writeByte(RECIPE);
            buffer.writeIdentifier(value);
        }
    }

    record Stack (IngredientWithSpell ingredient, Bounds bounds) implements ChapterPageElement {
        public Stack(JsonObject json) {
            this(IngredientWithSpell.CODEC.decode(JsonOps.INSTANCE, json.get("item")).result().get().getFirst(), boundsFromJson(json));
        }
        @Override
        public void toBuffer(RegistryByteBuf buffer) {
            buffer.writeByte(STACK);
            IngredientWithSpell.PACKET_CODEC.encode(buffer, ingredient);
            boundsToBuffer(bounds, buffer);
        }
    }

    record TextBlock (Text text) implements ChapterPageElement {
        public TextBlock(JsonElement json) {
            this(json.isJsonPrimitive() ? Text.translatable(json.getAsString()) : Text.Serialization.fromJsonTree(json, DynamicRegistryManager.EMPTY));
        }

        @Override
        public void toBuffer(RegistryByteBuf buffer) {
            buffer.writeByte(TEXT_BLOCK);
            TextCodecs.PACKET_CODEC.encode(buffer, text);
        }
    }

    record Ingredients(List<ChapterPageElement> entries) implements ChapterPageElement {
        public Ingredients(JsonObject json) {
            this(JsonHelper.getArray(json, "ingredients").asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(Ingredients::loadIngredient)
                        .toList());
        }

        @Deprecated
        static ChapterPageElement loadIngredient(JsonObject json) {
            int count = JsonHelper.getInt(json, "count", 1);
            if (json.has("item")) return new Multi(count, new Id((byte)1, Identifier.tryParse(json.get("item").getAsString())));
            if (json.has("trait")) return new Multi(count, new Id((byte)2, Trait.fromId(json.get("trait").getAsString()).orElseThrow().getId()));
            if (json.has("spell")) return new Multi(count, new Id((byte)4, Identifier.tryParse(json.get("spell").getAsString())));
            return new Multi(count, new TextBlock(json.get("text")));
        }

        record Id(byte id, Identifier value) implements ChapterPageElement {
            @Override
            public void toBuffer(RegistryByteBuf buffer) {
                buffer.writeByte(id);
                buffer.writeIdentifier(value);
            }
        }

        record Multi(int count, ChapterPageElement element) implements ChapterPageElement {
            @Override
            public void toBuffer(RegistryByteBuf buffer) {
                buffer.writeVarInt(count);
                element.toBuffer(buffer);
            }
        }

        @Override
        public void toBuffer(RegistryByteBuf buffer) {
            buffer.writeByte(INGREDIENTS);
            buffer.writeCollection(entries, (b, c) -> c.toBuffer(buffer));
        }
    }

    record Structure(List<ChapterPageElement> commands) implements ChapterPageElement {
        public Structure(JsonObject json) {
            this(JsonHelper.getArray(json, "structure").asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(Structure::loadCommand)
                        .toList());
        }

        static ChapterPageElement loadCommand(JsonObject json) {
            if (json.has("pos")) {
                var pos = JsonHelper.getArray(json, "pos");
                return new Set(
                        pos.get(0).getAsInt(), pos.get(1).getAsInt(), pos.get(2).getAsInt(),
                        StateUtil.stateFromString(json.get("state").getAsString())
                );
            }

            var min = JsonHelper.getArray(json, "min");
            var max = JsonHelper.getArray(json, "max");
            return new Fill(
                    min.get(0).getAsInt(), min.get(1).getAsInt(), min.get(2).getAsInt(),
                    max.get(0).getAsInt(), max.get(1).getAsInt(), max.get(2).getAsInt(),
                    StateUtil.stateFromString(json.get("state").getAsString())
            );
        }

        @Override
        public void toBuffer(RegistryByteBuf buffer) {
            buffer.writeByte(STRUCTURE);
            buffer.writeCollection(commands, (b, c) -> c.toBuffer(buffer));
        }

        record Set(int x, int y, int z, BlockState state) implements ChapterPageElement {
            @Override
            public void toBuffer(RegistryByteBuf buffer) {
                buffer.writeByte(1);
                buffer.writeInt(x);
                buffer.writeInt(y);
                buffer.writeInt(z);
                buffer.writeInt(Block.getRawIdFromState(state));
            }

        }
        record Fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) implements ChapterPageElement {
            @Override
            public void toBuffer(RegistryByteBuf buffer) {
                buffer.writeByte(2);
                buffer.writeInt(x1);
                buffer.writeInt(y1);
                buffer.writeInt(z1);
                buffer.writeInt(x2);
                buffer.writeInt(y2);
                buffer.writeInt(z2);
                buffer.writeInt(Block.getRawIdFromState(state));
            }

        }
    }
}