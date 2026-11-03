package com.minelittlepony.unicopia.client.render.model;

import org.joml.Quaternionf;

import com.minelittlepony.unicopia.client.render.RenderUtil;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class FanModel extends BakedModel {

    public FanModel(Sprite sprite) {
        RenderUtil.Vertex[] dorito = createDorito(sprite);
        MatrixStack matrices = new MatrixStack();
        Quaternionf rotation = new Quaternionf();
        float baseAngle = 15 * MathHelper.RADIANS_PER_DEGREE;
        for (int d = 0; d < 12; d++) {
            matrices.push();
            matrices.multiply(rotation.rotationZ(30 * d * MathHelper.RADIANS_PER_DEGREE).rotateXYZ(baseAngle, baseAngle * d, 0));
            matrices.translate(2.9F, 0, 0);
            for (RenderUtil.Vertex corner : dorito) {
                var position = corner.position(matrices.peek().getPositionMatrix());
                addVertex(position.x, position.y(), position.z(), corner.texture().x, corner.texture().y);
            }
            matrices.pop();
        }
    }

    static RenderUtil.Vertex[] createDorito(Sprite sprite) {
        float chunkSize = 1F;
        float baseLength = 0.8F;
        float uLength = sprite.getMaxU() - sprite.getMinU();
        return new RenderUtil.Vertex[]{
                new RenderUtil.Vertex(-chunkSize, -chunkSize * baseLength, 0, sprite.getMinU() + uLength * baseLength, sprite.getMinV()),
                new RenderUtil.Vertex( chunkSize,  0, 0, sprite.getMaxU(), sprite.getMaxV()),
                new RenderUtil.Vertex(-chunkSize,  chunkSize * baseLength, 0, sprite.getMinU(), sprite.getMinV()),
                new RenderUtil.Vertex(-chunkSize * 3, 0, 0, sprite.getMinU(), sprite.getMaxV())
        };
    }
}
