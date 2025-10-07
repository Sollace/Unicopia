package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class LevitatingItemActionWheel {

    public void render(DrawContext context, float tickDelta) {
        var client = MinecraftClient.getInstance();

        if (client.targetedEntity instanceof LevitatingItemEntity target) {
            render(client, target, context, tickDelta);
        }
    }

    public void render(MinecraftClient client, LevitatingItemEntity targetEntity, DrawContext context, float tickDelta) {

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        /*Camera cam = client.gameRenderer.getCamera();
        Vec3d offset = targetEntity.getPos().add(0, targetEntity.getHeight() * 0.5F, 0).subtract(cam.getPos());
        Quaternionf rotation = cam.getRotation().conjugate(new Quaternionf());
        Matrix4f matrix = new Matrix4f().identity().rotate(rotation);
        Vector4f projectedPosition = matrix.transform(new Vector4f((float)offset.x, (float)offset.y, (float)offset.z, 0));*/


        //int x = scaledWidth/2 + (int)(projectedPosition.x * scaledWidth * offset.z);
        //int y = scaledHeight/2 - (int)(projectedPosition.y * scaledHeight * offset.z);

        MatrixStack matrices = context.getMatrices();

        //DrawableUtil.drawLine(context.getMatrices(), 0, 0, x, y, Colors.WHITE);
        //DrawableUtil.drawLine(context.getMatrices(), scaledWidth, 0, x, y, Colors.WHITE);

        //DrawableUtil.drawLine(context.getMatrices(), 0, scaledHeight, x, y, Colors.WHITE);
        //DrawableUtil.drawLine(context.getMatrices(), scaledWidth, scaledHeight, x, y, Colors.WHITE);

        matrices.push();
        matrices.translate(scaledWidth * 0.5, scaledHeight * 0.5, 0);

        DrawableUtil.drawArc(matrices, 20, 21, 0, DrawableUtil.TAU, 0xFFFFFF22);
        DrawableUtil.drawArc(matrices, 20, 35, 0, DrawableUtil.TAU, 0x00000055);

        /*int radius = 28;
        int options = 6;
        double angle = DrawableUtil.TAU / options;
        double angleOffset = angle * 3;

        for (int i = 0; i < 6; i++) {
            String label = String.valueOf(i + 1);
            int x = (int)(MathHelper.sin((float)(-i * angle + angleOffset)) * radius) - client.textRenderer.getWidth(label) / 2;
            int y = (int)(MathHelper.cos((float)(-i * angle + angleOffset)) * radius) - client.textRenderer.fontHeight/2;
            context.drawText(client.textRenderer, Text.literal(label), x, y, Colors.WHITE, true);
        }*/

        matrices.pop();


    }
}
