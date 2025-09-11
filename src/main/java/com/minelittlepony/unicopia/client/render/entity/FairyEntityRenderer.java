package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;
import java.util.Map;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.mob.FairyEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class FairyEntityRenderer extends MobEntityRenderer<FairyEntity, FairyEntityRenderer.State, FairyEntityRenderer.Model> {
    public FairyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new Model(), 0.25F);
        this.addFeature(new LightGlowFeature(this));
        shadowRadius = 0;
        shadowOpacity = 0;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(FairyEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.radius = 0.125F + (float)Math.sin(entity.age * 0.1F) / 100F;
        state.thickness = 0.05F;
    }

    @Override
    public Identifier getTexture(State entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    public static class State extends LivingEntityRenderState {
        public float radius;
        public float thickness = 0.05F;
    }

    public static class LightGlowFeature extends FeatureRenderer<State, Model> {
        public LightGlowFeature(FeatureRendererContext<State, Model> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, State state, float limbAngle, float limbDistance) {
            matrices.push();
            matrices.translate(0, 1.5, 0);

            VertexConsumer buffer = vertices.getBuffer(RenderLayers.getMagicColored());

            SphereModel.SPHERE.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, state.radius - state.thickness, ColorHelper.withAlpha(Colors.WHITE, 127));
            SphereModel.SPHERE.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, state.radius, ColorHelper.withAlpha(Colors.WHITE, 85));

            matrices.pop();
        }
    }

    public static class Model extends EntityModel<State> {
        public Model() {
            super(new ModelPart(List.of(), Map.of()), texture -> RenderLayers.getMagicColored());
        }
    }
}
