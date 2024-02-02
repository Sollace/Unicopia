package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;

import com.google.gson.*;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.*;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgServerResources;
import com.minelittlepony.unicopia.util.Resources;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.profiler.Profiler;

public class SpellbookChapterLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ID = Unicopia.id("spellbook/chapters");
    private static final Executor EXECUTOR = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);

    public static final SpellbookChapterLoader INSTANCE = new SpellbookChapterLoader();

    private boolean dirty;
    private Map<Identifier, Chapter> chapters = new HashMap<>();

    public SpellbookChapterLoader() {
        super(Resources.GSON, ID.getPath());
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public Map<Identifier, Chapter> getChapters() {
        return chapters;
    }

    public void sendUpdate(MinecraftServer server) {
        if (dirty) {
            dirty = false;
            MsgServerResources msg = new MsgServerResources();
            server.getWorlds().forEach(world -> {
                Channel.SERVER_RESOURCES.sendToAllPlayers(msg, world);
            });
        }
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager manager, Profiler profiler) {
        try {
            chapters = data.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> new Chapter(entry.getKey(), JsonHelper.asObject(entry.getValue(), "root"))
            ));
        } catch (IllegalStateException | JsonParseException e) {
            LOGGER.error("Could not load spellbook chapters due to exception", e);
        }

        if (Debug.SPELLBOOK_CHAPTERS) {
            CompletableFuture.runAsync(() -> {
                try {
                    Util.waitAndApply(executor -> reload(CompletableFuture::completedFuture, manager, profiler, profiler, Util.getMainWorkerExecutor(), executor)).get();
                } catch (InterruptedException | ExecutionException e) {
                }
                dirty = true;
            }, EXECUTOR);
        }
    }

    private static Text readText(JsonElement json) {
        return json.isJsonPrimitive() ? Text.translatable(json.getAsString()) : Text.Serialization.fromJsonTree(json);
    }

    public enum Flow {
        NONE, LEFT, RIGHT
    }

    public record Chapter (
        Identifier id,
        TabSide side,
        int tabY,
        int color,
        List<Page> pages) {
        @Deprecated
        public Chapter(Identifier id, JsonObject json) {
            this(id,
                TabSide.valueOf(JsonHelper.getString(json, "side")),
                JsonHelper.getInt(json, "y_position"),
                JsonHelper.getInt(json, "color", 0),
                loadContent(JsonHelper.getObject(json, "content", new JsonObject()))
            );
        }

        @Deprecated
        private static List<Page> loadContent(JsonObject json) {
            return Optional.of(JsonHelper.getArray(json, "pages", new JsonArray()))
                .filter(pages -> pages.size() > 0)
                .stream()
                .flatMap(pages -> StreamSupport.stream(pages.spliterator(), false))
                .map(Page::of)
                .toList();
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeIdentifier(id);
            buffer.writeEnumConstant(side);
            buffer.writeInt(tabY);
            buffer.writeInt(color);
            buffer.writeBoolean(true);
            buffer.writeCollection(pages, Page::write);
        }
    }

    private record Page (
            Text title,
            int level,
            List<Element> elements
        ) {
        private static final Page EMPTY = new Page(Text.empty(), 0, List.of());

        public static Page of(JsonElement json) {
            return json.isJsonObject() && json.getAsJsonObject().keySet().isEmpty() ? EMPTY : new Page(json);
        }

        @Deprecated
        Page(JsonElement json) {
            this(json.getAsJsonObject());
        }

        @Deprecated
        Page(JsonObject json) {
            this(
                readText(json.get("title")),
                JsonHelper.getInt(json, "level", 0),
                new ArrayList<>()
            );
            JsonHelper.getArray(json, "elements", new JsonArray()).forEach(element -> {
                elements.add(Element.read(element));
            });
        }

        public void toBuffer(PacketByteBuf buffer) {
            buffer.writeText(title);
            buffer.writeInt(level);
            buffer.writeCollection(elements, Element::write);
        }

        public static void write(PacketByteBuf buffer, Page page) {
            page.toBuffer(buffer);
        }
    }

    private interface Element {
        void toBuffer(PacketByteBuf buffer);

        record Image (Identifier texture, Bounds bounds, Flow flow) implements Element {
            @Override
            public void toBuffer(PacketByteBuf buffer) {
                buffer.writeByte(0);
                buffer.writeIdentifier(texture);
                boundsToBuffer(bounds, buffer);
                buffer.writeEnumConstant(flow);
            }
        }

        record Multi(int count, Element element) implements Element {
            @Override
            public void toBuffer(PacketByteBuf buffer) {
                buffer.writeVarInt(count);
                element.toBuffer(buffer);
            }
        }

        record Id(byte id, Identifier value) implements Element {
            @Override
            public void toBuffer(PacketByteBuf buffer) {
                buffer.writeByte(id);
                buffer.writeIdentifier(value);
            }
        }

        record Stack (IngredientWithSpell ingredient, Bounds bounds) implements Element {
            @Override
            public void toBuffer(PacketByteBuf buffer) {
                buffer.writeByte(2);
                ingredient.write(buffer);
                boundsToBuffer(bounds, buffer);
            }
        }

        record TextBlock (Text text) implements Element {
            @Override
            public void toBuffer(PacketByteBuf buffer) {
                buffer.writeByte(3);
                buffer.writeText(text);
            }
        }

        record Ingredients(List<Element> entries) implements Element {
            @Deprecated
            static Element loadIngredient(JsonObject json) {
                int count = JsonHelper.getInt(json, "count", 1);
                if (json.has("item")) {
                    return new Multi(count, new Id((byte)1, Identifier.tryParse(json.get("item").getAsString())));
                }

                if (json.has("trait")) {
                    return new Multi(count, new Id((byte)2, Trait.fromId(json.get("trait").getAsString()).orElseThrow().getId()));
                }

                if (json.has("spell")) {
                    return new Multi(count, new Id((byte)4, Identifier.tryParse(json.get("spell").getAsString())));
                }

                return new Multi(count, new TextBlock(readText(json.get("text"))));
            }

            @Override
            public void toBuffer(PacketByteBuf buffer) {
                buffer.writeByte(4);
                buffer.writeCollection(entries, (b, c) -> c.toBuffer(b));
            }
        }

        static void write(PacketByteBuf buffer, Element element) {
            element.toBuffer(buffer);
        }

        @Deprecated
        static Element read(JsonElement json) {
            if (!json.isJsonPrimitive()) {
                JsonObject el = JsonHelper.asObject(json, "element");
                if (el.has("texture")) {
                    return new Image(
                        new Identifier(JsonHelper.getString(el, "texture")),
                        boundsFromJson(el),
                        Flow.valueOf(JsonHelper.getString(el, "flow", "RIGHT"))
                    );
                }

                if (el.has("recipe")) {
                    return new Id((byte)1, new Identifier(JsonHelper.getString(el, "recipe")));
                }

                if (el.has("item")) {
                    return new Stack(IngredientWithSpell.CODEC.decode(JsonOps.INSTANCE, el.get("item")).result().get().getFirst(), boundsFromJson(el));
                }

                if (el.has("ingredients")) {
                    return new Ingredients(JsonHelper.getArray(el, "ingredients").asList().stream()
                            .map(JsonElement::getAsJsonObject)
                            .map(Ingredients::loadIngredient)
                            .toList()
                    );
                }
            }

            return new TextBlock(readText(json));
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
    }
}
