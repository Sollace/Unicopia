package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;

public class HornFeatureRenderer<E extends BipedEntityRenderState> implements AccessoryFeatureRenderer.Feature<E> {

    public static final Identifier TEXTURE = Unicopia.id("textures/models/horn/unicorn.png");

    private final HornModel model;

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public HornFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
        model = new HornModel(HornModel.getData(Dilation.NONE).createModel());
    }

    protected boolean canRender(E entity) {
        return entity instanceof PlayerEntityRenderState
                && CasterState.of(entity).species.physical().canCast()
                && CasterState.of(entity).skinFeatures.showHorn();
    }

    @Override
    public void render(MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle) {
        if (canRender(entity)) {
            model.setAngles(context.getModel());
            model.setState(false);
            model.render(stack, ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(TEXTURE), false), lightUv, OverlayTexture.DEFAULT_UV, Colors.WHITE);

            CasterState state = CasterState.of(entity);
            int color = state.activeAbility.color;
            if (color == 0) {
                color = state.activeMagicColor;
            }

            if (color != 0) {
                model.setState(true);
                model.render(stack, ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayers.getMagicColored((0x99 << 24) | color), false), lightUv, OverlayTexture.DEFAULT_UV, Colors.WHITE);
            }
        }
    }

    public static class HornModel extends Model {
        public HornModel(ModelPart tree) {
            super(tree.getChild(EntityModelPartNames.HEAD), RenderLayer::getEntityTranslucent);
        }

        public static TexturedModelData getData(Dilation dilation) {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();

            ModelPartData head = root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-4, -8, -4, 8, 8, 8, dilation), ModelTransform.NONE);
            head.addChild("horn", ModelPartBuilder.create()
                        .uv(0, 3)
                        .cuboid(-0.5F, -12, -0.5F, 1, 4, 1, dilation), ModelTransform.rotation(29 * MathHelper.RADIANS_PER_DEGREE, 0, 0));
            head.addChild("magic", ModelPartBuilder.create()
                    .uv(0, 3)
                    .cuboid(-0.5F, -12, -0.5F, 1, 4, 1, dilation.add(0.5F)), ModelTransform.rotation(29 * MathHelper.RADIANS_PER_DEGREE, 0, 0));

            return TexturedModelData.of(data, 64, 64);
        }

        public void setAngles(BipedEntityModel<?> biped) {
            getRootPart().copyTransform(biped.getHead());
        }

        public void setState(boolean magic) {
            ModelPart part = getRootPart();
            part.hidden = true;
            part.getChild("horn").visible = !magic;
            part.getChild("magic").visible = magic;
        }
    }
}
