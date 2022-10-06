package com.minelittlepony.unicopia.ability.data.tree;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Resources;
import com.minelittlepony.unicopia.util.Weighted;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class TreeTypeLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/tree_type");

    public static final TreeTypeLoader INSTANCE = new TreeTypeLoader();

    private Map<Identifier, TreeTypeDef> entries = new HashMap<>();

    TreeTypeLoader() {
        super(Resources.GSON, "tree_types");
    }

    public Map<Identifier, TreeTypeDef> getEntries() {
        return entries;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager manager, Profiler profiler) {
        entries = resources.entrySet().stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            try {
                return Resources.GSON.fromJson(entry.getValue(), TreeTypeDef.class);
            } catch (IllegalArgumentException | JsonParseException e) {
                return null;
            }
        }));
        TreeTypes.load(entries);
    }

    public static final class TreeTypeDef {
        final Set<Identifier> logs;
        final Set<Identifier> leaves;
        final Set<Drop> drops;
        final boolean wideTrunk;
        final int rarity;

        public TreeTypeDef(PacketByteBuf buffer) {
            logs = new HashSet<>(buffer.readList(PacketByteBuf::readIdentifier));
            leaves = new HashSet<>(buffer.readList(PacketByteBuf::readIdentifier));
            drops = new HashSet<>(buffer.readList(Drop::new));
            wideTrunk = buffer.readBoolean();
            rarity = buffer.readInt();
        }

        public TreeType toTreeType(Identifier id) {
            return new TreeTypeImpl(
                    id,
                    wideTrunk,
                    Objects.requireNonNull(logs, "TreeType must have logs"),
                    Objects.requireNonNull(leaves, "TreeType must have leaves"),
                    Weighted.of(weighted -> drops.forEach(drop -> drop.appendDrop(weighted))),
                    rarity
            );
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeCollection(logs, PacketByteBuf::writeIdentifier);
            buffer.writeCollection(leaves, PacketByteBuf::writeIdentifier);
            buffer.writeCollection(drops, (a, b) -> b.write(a));
            buffer.writeBoolean(wideTrunk);
            buffer.writeInt(rarity);
        }

        static class Drop {
            final int weight;
            final Identifier item;

            public Drop(PacketByteBuf buffer) {
                weight = buffer.readInt();
                item = buffer.readIdentifier();
            }

            void appendDrop(Weighted.Builder<Supplier<ItemStack>> weighted) {
                Registry.ITEM.getOrEmpty(item).ifPresent(item -> {
                    weighted.put(weight, item::getDefaultStack);
                });
            }

            public void write(PacketByteBuf buffer) {
                buffer.writeInt(weight);
                buffer.writeIdentifier(item);
            }
        }
    }
}
