package com.minelittlepony.unicopia.client.gui;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class LevitatingItemActionWheel {

    public void render(DrawContext context, float tickDelta) {
        var client = MinecraftClient.getInstance();

        if (client.targetedEntity instanceof LevitatingItemEntity target) {
            render(client, target, context, tickDelta);
            Pony.of(client.player).setLookedEntity(target);
        }
    }

    public void render(MinecraftClient client, LevitatingItemEntity targetEntity, DrawContext context, float tickDelta) {
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();

        Camera cam = client.gameRenderer.getCamera();
        Vec3d offset = targetEntity.getPos().add(0, targetEntity.getHeight() * 0.5F, 0).subtract(cam.getPos());
        Quaternionf rotation = cam.getRotation().conjugate(new Quaternionf());
        Matrix4f matrix = new Matrix4f().identity().rotate(rotation);
        Vector4f projectedPosition = matrix.transform(new Vector4f((float)offset.x, (float)offset.y, (float)offset.z, 0));

        int crosshairX = scaledWidth/2 - (int)(projectedPosition.x * scaledWidth);
        int crosshairY = scaledHeight/2 + (int)(projectedPosition.y * scaledHeight);


        MatrixStack matrices = context.getMatrices();

        DrawableUtil.drawLine(context.getMatrices(), crosshairX, crosshairY - 5, crosshairX, crosshairY + 5, Colors.WHITE);
        DrawableUtil.drawLine(context.getMatrices(), crosshairX - 5, crosshairY, crosshairX + 5, crosshairY, Colors.WHITE);

        matrices.push();
        matrices.translate(scaledWidth * 0.5, scaledHeight * 0.5, 0);

        int ringInnerDiameter = 30;
        int ringOuterDiameter = 55;
        int ringMarkerWidth = 3;

        DrawableUtil.drawArc(matrices, ringInnerDiameter, ringInnerDiameter + 1, 0, DrawableUtil.TAU, 0xFFFFFF22);
        DrawableUtil.drawArc(matrices, ringInnerDiameter, ringOuterDiameter, 0, DrawableUtil.TAU, 0x00000055);

        int segmenticonRadius = 48;
        int segmentCount = 6;
        double segmentAngle = DrawableUtil.TAU / segmentCount;
        double segmentsStartAngle = segmentAngle * 2.5;

        Vector2i crosshair = new Vector2i(crosshairX - scaledWidth / 2, crosshairY - scaledHeight / 2);
        double theta = Math.atan2(crosshair.x, -crosshair.y) + Math.PI;
        double rad = crosshair.y / Math.cos(theta);

        DrawableUtil.drawArc(matrices, ringOuterDiameter, ringOuterDiameter + ringMarkerWidth, theta - segmentAngle / 2, segmentAngle, 0xFFFFFF45);

        for (int i = 0; i < segmentCount; i++) {
            String label = String.valueOf(i + 1);
            double segmentMinAngle = (i * segmentAngle + segmentsStartAngle) % DrawableUtil.TAU;
            double segmentMaxAngle = segmentMinAngle + segmentAngle;

            int x = (int)(MathHelper.sin((float)(segmentMinAngle + segmentAngle * 0.5F)) * segmenticonRadius - client.textRenderer.getWidth(label) / 2);
            int y = (int)(MathHelper.cos((float)(segmentMinAngle + segmentAngle * 0.5F)) * segmenticonRadius - client.textRenderer.fontHeight / 2);

            if (rad >= 0 && (theta > segmentMinAngle || (segmentMaxAngle > DrawableUtil.TAU && theta < segmentMaxAngle % DrawableUtil.TAU)) && theta < segmentMaxAngle) {
                DrawableUtil.drawArc(matrices, ringInnerDiameter, ringOuterDiameter, segmentMinAngle, segmentAngle, 0xFFFFFF25);
            }

            context.drawText(client.textRenderer, Text.literal(label), x, y, Colors.WHITE, true);
        }

        matrices.pop();
    }
}
