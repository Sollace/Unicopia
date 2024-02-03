package com.minelittlepony.unicopia.client.gui.spellbook.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public record Structure(Bounds bounds, List<List<List<BlockState>>> states) implements PageElement {
    static final BlockState DIAMOND = Blocks.DIAMOND_BLOCK.getDefaultState();
    static final BlockState AIR = Blocks.AIR.getDefaultState();
    static final BlockState OBS = Blocks.OBSIDIAN.getDefaultState();
    static final BlockState SOU = Blocks.SOUL_SAND.getDefaultState();
    public static final Structure CRYSTAL_HEART_ALTAR = new Structure.Builder()
            .fill(0, 0, 0, 2, 0, 2, DIAMOND)
            .set(1, 1, 1, DIAMOND)
            .set(1, 2, 1, Blocks.END_ROD.getDefaultState().with(Properties.FACING, Direction.UP))
            .set(1, 4, 1, Blocks.END_ROD.getDefaultState().with(Properties.FACING, Direction.DOWN))
            .set(1, 5, 1, DIAMOND)
            .fill(0, 6, 0, 2, 6, 2, DIAMOND)
            .build();
    public static final Structure ALTAR_STRUCTURE = new Structure.Builder()
            .fill(0, 0, 0, 8, 0, 8, SOU)
            .fill(3, 1, 3, 5, 1, 5, OBS)
            .set(4, 1, 4, SOU)
            .set(4, 1, 6, Blocks.LODESTONE.getDefaultState())
            .fill(0, 1, 2, 0, 4, 2, OBS).fill(0, 1, 6, 0, 4, 6, OBS)
            .fill(2, 1, 0, 2, 4, 0, OBS).fill(6, 1, 0, 6, 4, 0, OBS)
            .fill(8, 1, 2, 8, 4, 2, OBS).fill(8, 1, 6, 8, 4, 6, OBS)
            .fill(2, 1, 8, 2, 4, 8, OBS).fill(6, 1, 8, 6, 4, 8, OBS)
            .build();

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {
        int height = states.size();
        if (height == 0) {
            return;
        }
        int depth = states.get(0).size();
        if (depth == 0) {
            return;
        }
        int width = states.get(0).get(0).size();
        if (width == 0) {
            return;
        }
        MatrixStack matrices = context.getMatrices();
        Immediate immediate = context.getVertexConsumers();

        MinecraftClient client = MinecraftClient.getInstance();
        float tickDelta = client.player.age + client.getTickDelta();
        float age = tickDelta % 360F;

        matrices.push();
        if (container != null) {
            matrices.translate(container.getBounds().width / 2, container.getBounds().height / 2, 100);
            float minDimensions = Math.min(container.getBounds().width, container.getBounds().height) - 30;
            int minSize = Math.max(height, Math.max(width, depth)) * 16;
            float scale = minDimensions / minSize;
            matrices.scale(scale, scale, 1);
        }
        matrices.scale(-16, -16, -16);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20 + MathHelper.sin(tickDelta / 10F) * 2));
        matrices.peek().getPositionMatrix().rotate(RotationAxis.POSITIVE_Y.rotationDegrees(age));
        DiffuseLighting.enableForLevel(matrices.peek().getPositionMatrix());

        matrices.translate(-width / 2F, -height / 2F, -depth / 2F);

        for (int y = 0; y < height; y++)
        for (int x = 0; x < width; x++)
        for (int z = 0; z < depth; z++) {
            BlockState state = states.get(y).get(z).get(x);
            matrices.push();
            matrices.translate(x, y, z);
            client.getBlockRenderManager().renderBlockAsEntity(state, matrices, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }

        matrices.pop();
    }

    public static class Builder {
        private int dx = -1;
        private int dy = -1;
        private int dz = -1;
        private final List<List<List<BlockState>>> layers = new ArrayList<>();

        private void resize(int x, int y, int z) {
            int ddx = Math.max(dx, x);
            int ddy = Math.max(dy, y);
            int ddz = Math.max(dz, z);

            if (ddx <= dx && ddy <= dy && ddz <= dz) {
                return;
            }

            if (ddy > dy) {
                dy = ddy;
                while (layers.size() <= ddy) {
                    layers.add(createFilledList(ddz, () -> createFilledList(ddx, () -> AIR)));
                }
            }
            if (ddz > dz || ddx > dx) {
                layers.forEach(layer -> {
                    if (ddz > dz) {
                        while (layer.size() <= ddz) {
                            layer.add(createFilledList(ddx, () -> AIR));
                        }
                    }
                    if (ddx > dx) {
                        layer.forEach(row -> {
                            while (row.size() <= ddx) {
                                row.add(AIR);
                            }
                        });
                    }
                });
                dz = ddz;
                dx = ddx;
            }
        }

        private <T> List<T> createFilledList(int length, Supplier<T> contentSupplier) {
            List<T> list = new ArrayList<>();
            while (list.size() <= length) {
                list.add(contentSupplier.get());
            }
            return list;
        }

        public Builder set(int x, int y, int z, BlockState state) {
            resize(x, y, z);
            layers.get(y).get(z).set(x, state);
            return this;
        }

        public Builder fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState state) {
            resize(maxX, maxY, maxZ);
            for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
            for (int z = minZ; z <= maxZ; z++)
                layers.get(y).get(z).set(x, state);
            return this;
        }

        public Builder fromBuffer(PacketByteBuf buffer) {

            buffer.readCollection(ArrayList::new, buf -> {
                byte op = buf.readByte();
                return switch (op) {
                    case 1 -> set(buf.readInt(), buf.readInt(), buf.readInt(), Block.getStateFromRawId(buf.readInt()));
                    case 2 -> fill(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), Block.getStateFromRawId(buf.readInt()));
                    default -> this;
                };
            });

            return this;
        }

        public Structure build() {
            return new Structure(Bounds.empty(), layers);
        }
    }
}
