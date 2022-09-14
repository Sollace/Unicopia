package com.minelittlepony.unicopia.container;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;

import com.google.gson.*;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.*;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgServerResources;
import com.minelittlepony.unicopia.util.Resources;
import com.mojang.logging.LogUtils;

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
    //private static final Executor EXECUTOR = CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS);
    public static boolean DEBUG = false;

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
                world.getPlayers().forEach(player -> {
                    Channel.SERVER_RESOURCES_SEND.send(player, msg);
                });
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

        /*if (DEBUG) {
            CompletableFuture.runAsync(() -> {
                try {
                    Util.waitAndApply(executor -> reload(CompletableFuture::completedFuture, manager, profiler, profiler, Util.getMainWorkerExecutor(), executor)).get();
                } catch (InterruptedException | ExecutionException e) {
                }
                dirty = true;
            }, EXECUTOR);
        }*/
    }

    public record Chapter (
        Identifier id,
        TabSide side,
        int tabY,
        int color,
        List<Page> pages) {
        public Chapter(Identifier id, JsonObject json) {
            this(id,
                TabSide.valueOf(JsonHelper.getString(json, "side")),
                JsonHelper.getInt(json, "y_position"),
                JsonHelper.getInt(json, "color", 0),
                loadContent(JsonHelper.getObject(json, "content", new JsonObject()))
            );
        }

        private static List<Page> loadContent(JsonObject json) {
            return Optional.of(JsonHelper.getArray(json, "pages", new JsonArray()))
                .filter(pages -> pages.size() > 0)
                .stream()
                .flatMap(pages -> StreamSupport.stream(pages.spliterator(), false))
                .map(Page::new)
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
        public Page(JsonElement json) {
            this(json.getAsJsonObject());
        }

        public Page(JsonObject json) {
            this(
                Text.Serializer.fromJson(json.get("title")),
                JsonHelper.getInt(json, "level", 0),
                new ArrayList<Element>()
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

    public enum Flow {
        NONE, LEFT, RIGHT
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

        record Recipe (Identifier id) implements Element {
            @Override
            public void toBuffer(PacketByteBuf buffer) {
                buffer.writeByte(1);
                buffer.writeIdentifier(id);
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

        static void write(PacketByteBuf buffer, Element element) {
            element.toBuffer(buffer);
        }

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
                    return new Recipe(new Identifier(JsonHelper.getString(el, "recipe")));
                }

                if (el.has("item")) {
                    return new Stack(IngredientWithSpell.fromJson(el.get("item")), boundsFromJson(el));
                }
            }

            return new TextBlock(Text.Serializer.fromJson(json));
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
