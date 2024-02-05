package com.minelittlepony.unicopia.block.state;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;

public record Schematic(int dx, int dy, int dz, Entry[] states) {
    public static Schematic fromPacket(PacketByteBuf buffer) {
        Builder builder = new Builder();
        buffer.readCollection(ArrayList::new, buf -> {
            byte op = buf.readByte();
            return switch (op) {
                case 1 -> builder.set(buf.readInt(), buf.readInt(), buf.readInt(), Block.getStateFromRawId(buf.readInt()));
                case 2 -> builder.fill(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), Block.getStateFromRawId(buf.readInt()));
                default -> builder;
            };
        });
        return builder.build();
    }

    public int volume() {
        return states.length;
    }

    public static final class Builder {
        private static final BlockState AIR = Blocks.AIR.getDefaultState();

        private int dx = -1;
        private int dy = -1;
        private int dz = -1;
        private final ExpandableList<ExpandableList<ExpandableList<BlockState>>> layers = new ExpandableList<>(0, () -> new ExpandableList<>(dz, () -> new ExpandableList<>(dx, () -> AIR)));

        public Builder set(int x, int y, int z, BlockState state) {
            dx = Math.max(dx, x);
            dy = Math.max(dy, y);
            dz = Math.max(dz, z);
            layers.getOrExpand(y).getOrExpand(z).set(x, state);
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
}
