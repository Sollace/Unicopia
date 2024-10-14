package com.minelittlepony.unicopia.ability.data.tree;

import java.util.*;
import java.util.function.Supplier;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Weighted;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TreeTypeLoader extends JsonDataLoader<TreeTypeLoader.TreeTypeDef> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/tree_type");

    public static final TreeTypeLoader INSTANCE = new TreeTypeLoader();

    private Map<Identifier, TreeTypeDef> entries = new HashMap<>();

    TreeTypeLoader() {
        super(TreeTypeDef.CODEC, "tree_types");
    }

    public Map<Identifier, TreeTypeDef> getEntries() {
        return entries;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, TreeTypeDef> resources, ResourceManager manager, Profiler profiler) {
        entries = resources;

        TreeTypes.load(entries);
    }

    public record TreeTypeDef (
            Set<Identifier> logs,
            Set<Identifier> leaves,
            Set<Drop> drops,
            boolean wideTrunk,
            int rarity,
            float leavesRatio
    ) {
        public static final Codec<TreeTypeDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                CodecUtils.setOf(Identifier.CODEC).fieldOf("logs").forGetter(TreeTypeDef::logs),
                CodecUtils.setOf(Identifier.CODEC).fieldOf("leaves").forGetter(TreeTypeDef::leaves),
                CodecUtils.setOf(Drop.CODEC).fieldOf("drops").forGetter(TreeTypeDef::drops),
                Codec.BOOL.fieldOf("wideTrunk").forGetter(TreeTypeDef::wideTrunk),
                Codec.INT.fieldOf("rarity").forGetter(TreeTypeDef::rarity),
                Codec.FLOAT.fieldOf("leavesRatio").forGetter(TreeTypeDef::leavesRatio)
        ).apply(instance, TreeTypeDef::new));
        public static final PacketCodec<RegistryByteBuf, TreeTypeDef> PACKET_CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), TreeTypeDef::logs,
                Identifier.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), TreeTypeDef::leaves,
                Drop.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), TreeTypeDef::drops,
                PacketCodecs.BOOL, TreeTypeDef::wideTrunk,
                PacketCodecs.INTEGER, TreeTypeDef::rarity,
                PacketCodecs.FLOAT, TreeTypeDef::leavesRatio,
                TreeTypeDef::new
        );

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

        static record Drop (
                int weight,
                Optional<Identifier> tag,
                Optional<Identifier> item
        ) implements Weighted.Buildable<Supplier<ItemStack>> {
            public static final Codec<Drop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("weight").forGetter(Drop::weight),
                    Identifier.CODEC.optionalFieldOf("tag").forGetter(Drop::tag),
                    Identifier.CODEC.optionalFieldOf("item").forGetter(Drop::item)
            ).apply(instance, Drop::new));
            public static final PacketCodec<RegistryByteBuf, Drop> PACKET_CODEC = PacketCodec.tuple(
                    PacketCodecs.INTEGER, Drop::weight,
                    PacketCodecs.optional(Identifier.PACKET_CODEC), Drop::tag,
                    PacketCodecs.optional(Identifier.PACKET_CODEC), Drop::item,
                    Drop::new
            );

            @Override
            public void appendTo(Weighted.Builder<Supplier<ItemStack>> weighted) {
                if (item.isPresent()) {
                    Registries.ITEM.getOptionalValue(item.get()).ifPresent(item -> {
                        weighted.put(weight, item::getDefaultStack);
                    });
                } else {
                    weighted.put(weight, () -> {
                        return Registries.ITEM.getRandomEntry(TagKey.of(RegistryKeys.ITEM, tag.get()), Weighted.getRng())
                                .map(entry -> entry.value().getDefaultStack())
                                .orElse(ItemStack.EMPTY);
                    });
                }
            }
        }
    }
}
