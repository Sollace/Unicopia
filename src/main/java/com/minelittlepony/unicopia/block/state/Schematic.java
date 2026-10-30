package com.minelittlepony.unicopia.block.state;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public record Schematic(int dx, int dy, int dz, Entry[] states) {
    public static final int ALTAR_VISUAL_BASE_SIZE = amountForRange(0, 0, 0, 8, 0, 8);
    public static final Unbaked ALTAR = Unbaked.builder()
            .fill(0, 0, 0, 8, 0, 8, Blocks.SOUL_SAND.getDefaultState())
            .fill(3, 1, 3, 5, 1, 5, Blocks.OBSIDIAN.getDefaultState())
            .set(4, 1, 4, Blocks.SOUL_SAND.getDefaultState())
            .set(4, 2, 4, Blocks.SOUL_FIRE.getDefaultState())
            .set(4, 1, 6, Blocks.LODESTONE.getDefaultState())
            .fill(0, 1, 2, 0, 4, 2, Blocks.OBSIDIAN.getDefaultState()).fill(0, 1, 6, 0, 4, 6, Blocks.OBSIDIAN.getDefaultState())
            .fill(2, 1, 0, 2, 4, 0, Blocks.OBSIDIAN.getDefaultState()).fill(6, 1, 0, 6, 4, 0, Blocks.OBSIDIAN.getDefaultState())
            .fill(8, 1, 2, 8, 4, 2, Blocks.OBSIDIAN.getDefaultState()).fill(8, 1, 6, 8, 4, 6, Blocks.OBSIDIAN.getDefaultState())
            .fill(2, 1, 8, 2, 4, 8, Blocks.OBSIDIAN.getDefaultState()).fill(6, 1, 8, 6, 4, 8, Blocks.OBSIDIAN.getDefaultState())
            .build();
    public static final Unbaked CRYSTAL_PODIUM = Unbaked.builder()
            .fill(0, 0, 0, 2, 0, 2, Blocks.DIAMOND_BLOCK.getDefaultState())
            .set(1, 1, 1, Blocks.DIAMOND_BLOCK.getDefaultState())
            .set(1, 2, 1, Blocks.END_ROD.getDefaultState().with(Properties.FACING, Direction.UP))
            .set(1, 4, 1, Blocks.END_ROD.getDefaultState().with(Properties.FACING, Direction.DOWN))
            .set(1, 5, 1, Blocks.DIAMOND_BLOCK.getDefaultState())
            .fill(0, 6, 0, 2, 6, 2, Blocks.DIAMOND_BLOCK.getDefaultState())
            .build();

    public int volume() {
        return states.length;
    }

    public static final int amountForRange(int x0, int y0, int z0, int x1, int y1, int z1) {
        int dx = difference(x0, x1);
        int dy = difference(y0, y1);
        int dz = difference(z0, z1);
        return (dx + 1) * (dy + 1) * (dz + 1);
    }

    private static final int difference(int min, int max) {
        return Math.max(min, max) - Math.min(min, max);
    }

    public static final class MaterialsCounter {
        private final Object2IntMap<Item> materials = new Object2IntLinkedOpenHashMap<>();

        public MaterialsCounter count(int count, ItemConvertible material) {
            Item item = material.asItem();
            if (item != Items.AIR) {
                materials.mergeInt(item, count, Integer::sum);
            }
            return this;
        }

        public MaterialsCounter consume(int amount, ItemConvertible material) {
            return count(-amount, material);
        }

        public Object2IntMap<Item> materials() {
            return materials;
        }
    }

    private static final class Builder {
        private static final BlockState AIR = Blocks.AIR.getDefaultState();

        private int dx = -1;
        private int dy = -1;
        private int dz = -1;
        private final ExpandableList<ExpandableList<ExpandableList<BlockState>>> layers = new ExpandableList<>(0, () -> new ExpandableList<>(dz, () -> new ExpandableList<>(dx, () -> AIR)));

        @Nullable
        private final MaterialsCounter materials;

        private Builder(@Nullable MaterialsCounter materials) {
            this.materials = materials;
        }

        public Builder set(int x, int y, int z, BlockState state) {
            dx = Math.max(dx, x);
            dy = Math.max(dy, y);
            dz = Math.max(dz, z);
            layers.getOrExpand(y).getOrExpand(z).set(x, state);
            if (materials != null) {
                materials.count(1, state.getBlock());
            }
            return this;
        }

        public Builder fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState state) {
            dx = Math.max(dx, maxX);
            dy = Math.max(dy, maxY);
            dz = Math.max(dz, maxZ);
            for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
            for (int z = minZ; z <= maxZ; z++)
                set(x, y, z, state);
            return this;
        }

        public Schematic build() {
            List<Entry> states = new LinkedList<>();
            for (int y = 0; y <= dy && y < layers.size(); y++)
            for (int z = 0; z <= dz && z < layers.get(y).size(); z++)
            for (int x = 0; x <= dx && x < layers.get(y).get(z).size(); x++) {
                BlockState state = layers.get(y).get(z).get(x);
                if (!state.isAir()) {
                    states.add(new Entry(x, y, z, state));
                }
            }

            return new Schematic(dx, dy, dz, states.toArray(Entry[]::new));
        }
    }

    public record Entry(int x, int y, int z, BlockState state) {}

    public record Unbaked(List<Command> commands) {
        public static final Codec<Unbaked> CODEC = Command.CODEC.listOf().xmap(Unbaked::new, Unbaked::commands);

        public static final PacketCodec<ByteBuf, Unbaked> PACKET_CODEC = Command.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(Unbaked::new, Unbaked::commands);


        public MaterialsCounter countMaterials(MaterialsCounter materials) {
            Schematic.Builder builder = new Schematic.Builder(materials);
            commands.forEach(command -> command.apply(builder));
            return materials;
        }

        public Schematic bake() {
            Schematic.Builder builder = new Schematic.Builder(null);
            commands.forEach(command -> command.apply(builder));
            return builder.build();
        }

        public sealed interface Command permits Command.Set, Command.Fill {
            Codec<Command> CODEC = Codec.xor(Set.CODEC, Fill.CODEC).xmap(Either::unwrap, element -> element instanceof Set s ? Either.left(s) : Either.right((Fill)element));
            PacketCodec<ByteBuf, Command> PACKET_CODEC = PacketCodecs.BYTE.dispatch(element -> element instanceof Set ? (byte)1 : (byte)2, Map.of(
                    (byte)1, Set.PACKET_CODEC,
                    (byte)2, Fill.PACKET_CODEC
            )::get);
            void apply(Schematic.Builder builder);

            record Set(Vec3i pos, BlockState state) implements Command {
                public static final Codec<Set> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Vec3i.CODEC.fieldOf("pos").forGetter(Set::pos),
                        CodecUtils.BLOCK_STATE_CODEC.fieldOf("state").forGetter(Set::state)
                ).apply(i, Set::new));
                public static final PacketCodec<ByteBuf, Set> PACKET_CODEC = PacketCodec.tuple(
                        Vec3i.PACKET_CODEC, Set::pos,
                        PacketCodecUtils.BLOCK_STATE, Set::state,
                        Set::new
                );

                @Override
                public void apply(Schematic.Builder builder) {
                    builder.set(pos.getX(), pos.getY(), pos.getZ(), state);
                }
            }

            record Fill(Vec3i min, Vec3i max, BlockState state) implements Command {
                public static final Codec<Fill> CODEC = RecordCodecBuilder.create(i -> i.group(
                        Vec3i.CODEC.fieldOf("min").forGetter(Fill::min),
                        Vec3i.CODEC.fieldOf("max").forGetter(Fill::max),
                        CodecUtils.BLOCK_STATE_CODEC.fieldOf("state").forGetter(Fill::state)
                ).apply(i, Fill::new));
                public static final PacketCodec<ByteBuf, Fill> PACKET_CODEC = PacketCodec.tuple(
                        Vec3i.PACKET_CODEC, Fill::min,
                        Vec3i.PACKET_CODEC, Fill::max,
                        PacketCodecUtils.BLOCK_STATE, Fill::state,
                        Fill::new
                );

                @Override
                public void apply(Schematic.Builder builder) {
                    builder.fill(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), state);
                }
            }

        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private final List<Command> commands = new ArrayList<>();

            private Builder() {}

            public Builder set(int x, int y, int z, BlockState state) {
                commands.add(new Command.Set(new Vec3i(x, y, z), state));
                return this;
            }

            public Builder fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
                commands.add(new Command.Fill(new Vec3i(x0, y0, z0), new Vec3i(x1, y1, z1), state));
                return this;
            }

            public Unbaked build() {
                return new Unbaked(List.copyOf(commands));
            }
        }
    }
}
