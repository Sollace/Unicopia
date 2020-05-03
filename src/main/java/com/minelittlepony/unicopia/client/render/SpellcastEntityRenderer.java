package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.render.model.GemEntityModel;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SpellcastEntityRenderer extends LivingEntityRenderer<SpellcastEntity, GemEntityModel> {

    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/entity/gem.png");

    public SpellcastEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new GemEntityModel(), 0);
        addFeature(new TierFeature(this));
    }

    @Override
    public Identifier getTexture(SpellcastEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(SpellcastEntity entity, Frustum visibleRegion, double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    protected float getLyingAngle(SpellcastEntity entity) {

        return 0;
    }

    @Override
    public void render(SpellcastEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    protected boolean hasLabel(SpellcastEntity targetEntity) {
        return super.hasLabel(targetEntity) && (targetEntity.isCustomNameVisible()
                 || targetEntity.hasCustomName() && targetEntity == renderManager.targetedEntity);
    }

    class TierFeature extends FeatureRenderer<SpellcastEntity, GemEntityModel> {

        public TierFeature(FeatureRendererContext<SpellcastEntity, GemEntityModel> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, SpellcastEntity entity, float limbAngle, float limbDistance, float tickDelta, float customAngle, float headYaw, float headPitch) {
            matrices.push();
            int tiers = Math.min(entity.getCurrentLevel(), 5);

            for (int i = 0; i <= tiers; i++) {
                float grow = (1 + i) * 0.2F;

                matrices.scale(1 + grow, 1 + grow, 1 + grow);
                matrices.translate(0, -grow, 0);

                if (i == 5) {
                    matrices.push();
                    matrices.translate(0.6F, 0.8F, 0);
                    matrices.scale(0.4F, 0.4F, 0.4F);
                    FeatureRenderer.render(model, model, TEXTURE, matrices, vertexConsumers, light, entity, limbAngle, limbDistance, entity.age, headYaw, headPitch, 1, 1, 1, 1);
                    matrices.pop();
                }
            }

            for (int i = entity.getCurrentLevel(); i > 0; i--) {
                matrices.push();
                matrices.translate(0.6F, 0, 0);
                FeatureRenderer.render(model, model, TEXTURE, matrices, vertexConsumers, light, entity, limbAngle, limbDistance, entity.age, headYaw, headPitch, 1, 1, 1, 1);
                matrices.pop();
            }
            matrices.pop();
        }
    }
}
