package com.minelittlepony.unicopia.ability.data.tree;

import java.util.*;
import java.util.function.Supplier;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Weighted;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TreeTypeLoader extends JsonDataLoader<TreeTypeLoader.TreeTypeDef> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/tree_type");

    public static final TreeTypeLoader INSTANCE = new TreeTypeLoader();

    private Map<Identifier, TreeTypeDef> entries = new HashMap<>();

    TreeTypeLoader() {
        super(TreeTypeDef.CODEC, ResourceFinder.json("tree_types"));
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
            Set<RegistryKey<Block>> logs,
            Set<RegistryKey<Block>> leaves,
            Set<Drop> drops,
            boolean wideTrunk,
            int rarity,
            float leavesRatio
    ) {
        private static final Codec<Set<RegistryKey<Block>>> BLOCK_KEY_SET_CODEC = CodecUtils.setOf(RegistryKey.createCodec(RegistryKeys.BLOCK));
        private static final PacketCodec<ByteBuf, Set<RegistryKey<Block>>> BLOCK_KEY_SET_PACKET_CODEC = RegistryKey.createPacketCodec(RegistryKeys.BLOCK).collect(PacketCodecs.toCollection(HashSet::new));
        public static final Codec<TreeTypeDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BLOCK_KEY_SET_CODEC.fieldOf("logs").forGetter(TreeTypeDef::logs),
                BLOCK_KEY_SET_CODEC.fieldOf("leaves").forGetter(TreeTypeDef::leaves),
                CodecUtils.setOf(Drop.CODEC).fieldOf("drops").forGetter(TreeTypeDef::drops),
                Codec.BOOL.optionalFieldOf("wideTrunk", false).forGetter(TreeTypeDef::wideTrunk),
                Codec.INT.optionalFieldOf("rarity", 0).forGetter(TreeTypeDef::rarity),
                Codec.FLOAT.fieldOf("leavesRatio").forGetter(TreeTypeDef::leavesRatio)
        ).apply(instance, TreeTypeDef::new));
        public static final PacketCodec<RegistryByteBuf, TreeTypeDef> PACKET_CODEC = PacketCodec.tuple(
                BLOCK_KEY_SET_PACKET_CODEC, TreeTypeDef::logs,
                BLOCK_KEY_SET_PACKET_CODEC, TreeTypeDef::leaves,
                Drop.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), TreeTypeDef::drops,
                PacketCodecs.BOOLEAN, TreeTypeDef::wideTrunk,
                PacketCodecs.INTEGER, TreeTypeDef::rarity,
                PacketCodecs.FLOAT, TreeTypeDef::leavesRatio,
                TreeTypeDef::new
        );

        public static Builder builder() {
            return new Builder();
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

        public static class Builder {
            private final Set<RegistryKey<Block>> logs = new HashSet<>();
            private final Set<RegistryKey<Block>> leaves = new HashSet<>();
            private final Set<Drop> drops = new HashSet<>();

            private boolean wideTrunk = false;
            private int rarity;
            private float leavesRatio = 0.5F;

            public Builder drop(int weight, TagKey<Item> tag) {
                drops.add(new Drop(weight, Optional.of(tag.id()), Optional.empty()));
                return this;
            }

            public Builder drop(int weight, ItemConvertible item) {
                drops.add(new Drop(weight, Optional.empty(), Optional.of(Registries.ITEM.getId(item.asItem()))));
                return this;
            }

            @SuppressWarnings("deprecation")
            public Builder logs(Block...blocks) {
                Arrays.stream(blocks).map(block -> block.getRegistryEntry().registryKey()).forEach(logs::add);
                return this;
            }

            @SuppressWarnings("deprecation")
            public Builder leaves(Block...blocks) {
                Arrays.stream(blocks).map(block -> block.getRegistryEntry().registryKey()).forEach(leaves::add);
                return this;
            }

            public Builder wideTrunk() {
                wideTrunk = true;
                return this;
            }

            public Builder rarity(int rarity) {
                this.rarity = rarity;
                return this;
            }

            public Builder leavesRatio(float leavesRatio) {
                this.leavesRatio = leavesRatio;
                return this;
            }

            public TreeTypeDef build() {
                return new TreeTypeDef(logs, leaves, drops, wideTrunk, rarity, leavesRatio);
            }
        }

    }
}
