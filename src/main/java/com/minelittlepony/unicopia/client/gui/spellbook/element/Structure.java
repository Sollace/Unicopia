package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.block.state.Schematic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public record Structure(Bounds bounds, Schematic schematic) implements PageElement {
    @Override
    public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {
        if (schematic.volume() == 0) {
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
            int minSize = (Math.max(schematic.dx(), Math.max(schematic.dy(), schematic.dz())) + 1) * 16;
            float scale = minDimensions / minSize;
            matrices.scale(scale, scale, 1);
        }
        matrices.scale(16, -16, 16);
        matrices.peek().getNormalMatrix().scale(1, -1, 1);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20 + MathHelper.sin(tickDelta / 10F) * 2));
        matrices.peek().getPositionMatrix().rotate(RotationAxis.POSITIVE_Y.rotationDegrees(age));
        matrices.translate((-schematic.dx() - 1) / 2F, (-schematic.dy() - 1) / 2F, (-schematic.dz() - 1) / 2F);
        DiffuseLighting.disableGuiDepthLighting();

        for (var entry : schematic.states()) {
            matrices.push();
            matrices.translate(entry.x(), entry.y(), entry.z());
            client.getBlockRenderManager().renderBlockAsEntity(entry.state(), matrices, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
            matrices.pop();
        }

        matrices.pop();
    }
}
