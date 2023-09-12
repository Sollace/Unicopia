package com.minelittlepony.unicopia.ability.data.tree;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Resources;
import com.minelittlepony.unicopia.util.Weighted;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

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
        final float leavesRatio;

        public TreeTypeDef(PacketByteBuf buffer) {
            logs = new HashSet<>(buffer.readList(PacketByteBuf::readIdentifier));
            leaves = new HashSet<>(buffer.readList(PacketByteBuf::readIdentifier));
            drops = new HashSet<>(buffer.readList(Drop::new));
            wideTrunk = buffer.readBoolean();
            rarity = buffer.readInt();
            leavesRatio = buffer.readFloat();
        }

        public TreeType toTreeType(Identifier id) {
            return new TreeTypeImpl(
                    id,
                    wideTrunk,
                    Objects.requireNonNull(logs, "TreeType must have logs"),
                    Objects.requireNonNull(leaves, "TreeType must have leaves"),
                    Weighted.of(drops),
                    rarity,
                    leavesRatio
            );
        }

        public void write(PacketByteBuf buffer) {
            buffer.writeCollection(logs, PacketByteBuf::writeIdentifier);
            buffer.writeCollection(leaves, PacketByteBuf::writeIdentifier);
            buffer.writeCollection(drops, (a, b) -> b.write(a));
            buffer.writeBoolean(wideTrunk);
            buffer.writeInt(rarity);
            buffer.writeFloat(leavesRatio);
        }

        static class Drop implements Weighted.Buildable<Supplier<ItemStack>> {
            final int weight;
            final @Nullable Identifier tag;
            final @Nullable Identifier item;

            public Drop(PacketByteBuf buffer) {
                weight = buffer.readInt();
                tag = buffer.readOptional(PacketByteBuf::readIdentifier).orElse(null);
                item = buffer.readOptional(PacketByteBuf::readIdentifier).orElse(null);
            }

            @Override
            public void appendTo(Weighted.Builder<Supplier<ItemStack>> weighted) {
                if (item != null) {
                    Registries.ITEM.getOrEmpty(item).ifPresent(item -> {
                        weighted.put(weight, item::getDefaultStack);
                    });
                } else {
                    weighted.put(weight, () -> {
                        return Registries.ITEM.getOrCreateEntryList(TagKey.of(RegistryKeys.ITEM, tag))
                                .getRandom(Weighted.getRng())
                                .map(RegistryEntry::value)
                                .map(Item::getDefaultStack)
                                .orElse(ItemStack.EMPTY);
                    });
                }
            }

            public void write(PacketByteBuf buffer) {
                buffer.writeInt(weight);
                buffer.writeOptional(Optional.ofNullable(tag), PacketByteBuf::writeIdentifier);
                buffer.writeOptional(Optional.ofNullable(item), PacketByteBuf::writeIdentifier);
            }
        }
    }
}
