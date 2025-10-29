package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.common.util.render.RenderLayerUtil;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class LevitatingItemEntityRenderer extends EntityRenderer<LevitatingItemEntity> {

    private final ItemRenderer itemRenderer;

    public LevitatingItemEntityRenderer(Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public Identifier getTexture(LevitatingItemEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(LevitatingItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertices, light);

        if (!entity.getStack().isEmpty()) {
            float scale = 1.4F;
            matrices.push();
            matrices.translate(0, MathHelper.sin((entity.age + tickDelta) / 20F) * 0.02F, 0);
            matrices.scale(scale, scale, scale);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));


            BlockPos miningPos = entity.getMiningPos().orElse(null);

            if (miningPos != null) {
                float rot = (entity.age + tickDelta);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90 * MathHelper.sin(rot)));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10 * MathHelper.sin(rot)));
            }


            itemRenderer.renderItem(entity.getMaster(), entity.getStack(), ModelTransformationMode.GROUND, false, matrices, vertices, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, 0);

            matrices.scale(1.2F, 1.2F, 1.2F);

            itemRenderer.renderItem(entity.getMaster(), entity.getStack(), ModelTransformationMode.GROUND, false, matrices, layer -> {
                return vertices.getBuffer(RenderLayerUtil.getTexture(layer)
                        .map(texture -> RenderLayers.getMagicColored(texture, RenderLayers.DEFAULT_MAGIC_COLOR))
                        .orElse(RenderLayers.getMagicColored()));
            }, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, 0);

            matrices.pop();


            if (miningPos != null) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (System.currentTimeMillis() % 5 == 0 && !client.isPaused()) {
                    client.particleManager.addBlockBreakingParticles(miningPos, entity.getMiningFace());
                }
            }
        }
    }
}
