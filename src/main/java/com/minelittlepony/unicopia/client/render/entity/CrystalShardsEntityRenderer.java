package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.CrystalShardsEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class CrystalShardsEntityRenderer extends EntityRenderer<CrystalShardsEntity> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/crystal_shards/normal.png");
    private static final Identifier[] CORRUPTED = List.of("corrupt", "dark", "darker").stream()
            .map(name -> Unicopia.id("textures/entity/crystal_shards/" + name + ".png"))
            .toArray(Identifier[]::new);

    private final CrystalShardsEntityModel model;

    public CrystalShardsEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new CrystalShardsEntityModel(CrystalShardsEntityModel.getTexturedModelData().createModel());
    }

    @Override
    public void render(CrystalShardsEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.multiply(entity.getAttachmentFace().getRotationQuaternion());
        matrices.scale(-1, -1, 1);

        float scale = entity.getGrowth(tickDelta);
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));

        model.setAngles(entity, 0, 0, 0, 0, 0);
        model.render(matrices, vertices.getBuffer(model.getLayer(getTexture(entity))), light, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertices, light);
    }

    @Override
    public Identifier getTexture(CrystalShardsEntity entity) {
        if (entity.isCorrupt()) {
            return CORRUPTED[(int)(Math.abs(entity.getUuid().getMostSignificantBits()) % CORRUPTED.length)];
        }
        return TEXTURE;
    }
}