package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState.BangleState;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.*;

public class BraceletFeatureRenderer<S extends BipedEntityRenderState, E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<S> {

    public static final Identifier TEXTURE = Unicopia.id("textures/models/armor/bracelet.png");

    private final BraceletModel steveModel;
    private final BraceletModel alexModel;

    private final FeatureRendererContext<S, ? extends BipedEntityModel<S>> context;

    public BraceletFeatureRenderer(FeatureRendererContext<S, ? extends BipedEntityModel<S>> context) {
        this.context = context;
        Dilation dilation = new Dilation(0.3F);
        steveModel = new BraceletModel(BraceletModel.getData(dilation, false, 0, 0, 0).createModel());
        alexModel = new BraceletModel(BraceletModel.getData(dilation, true, 0, 0, 0).createModel());
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S entity, float limbAngle, float limbDistance) {
        CasterState caster = CasterState.of(entity);
        if (caster.mainhandBangle.present()) {
            renderBangleThirdPerson(caster.mainhandBangle, matrices, vertexConsumers, light, entity, limbDistance, limbAngle, entity.mainArm);
        }
        if (caster.offhandBangle.present()) {
            renderBangleThirdPerson(caster.offhandBangle, matrices, vertexConsumers, light, entity, limbDistance, limbAngle, entity.mainArm.getOpposite());
        }
    }

    private void renderBangleThirdPerson(BangleState state, MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, S entity, float limbDistance, float limbAngle, Arm mainArm) {
        int j = state.color;

        boolean alex = entity instanceof PlayerEntityRenderState s && s.skinTextures.model() == SkinTextures.Model.SLIM;

        BraceletModel model = alex ? alexModel : steveModel;
        boolean isLeft = mainArm == Arm.LEFT;

        if (entity instanceof ArmorStandEntityRenderState stand) {
            ModelPart arm = isLeft ? context.getModel().leftArm : context.getModel().rightArm;
            arm.visible = true;
            @SuppressWarnings("unchecked")
            VertexConsumer consumer = renderContext.getBuffer(context.getModel().getLayer(context instanceof LivingEntityRenderer renderer ? renderer.getTexture(stand) : ArmorStandEntityRenderer.TEXTURE));
            arm.render(stack, consumer, lightUv, OverlayTexture.DEFAULT_UV, Colors.WHITE);
        }

        VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(TEXTURE), false);

        model.setAngles(context.getModel());
        model.setVisible(mainArm);
        model.render(stack, consumer, state.glowing ? LightmapTextureManager.MAX_LIGHT_COORDINATE : lightUv, OverlayTexture.DEFAULT_UV, j);
    }

    @Override
    public void renderArm(MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, S entity, ModelPart armModel, Arm arm) {
        var state = CasterState.of(entity);
        var bangle = arm == entity.mainArm ? state.mainhandBangle : state.offhandBangle;
        if (bangle != null) {
            if (state.ponified) {
                stack.translate(arm == Arm.LEFT ? 0.06 : -0.06, 0.3, 0);
            } else {
                stack.translate(0, -0.1, 0);
            }

            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(TEXTURE), false);
            boolean alex = entity instanceof PlayerEntityRenderState s && s.skinTextures.model() == SkinTextures.Model.SLIM;
            BraceletModel model = alex ? alexModel : steveModel;
            model.setAngles(context.getModel());
            model.setVisible(arm);
            model.render(stack, consumer, bangle.glowing ? LightmapTextureManager.MAX_LIGHT_COORDINATE : lightUv, OverlayTexture.DEFAULT_UV, bangle.color);
        }
    }

    public static class BraceletModel extends Model {

        private final ModelPart leftArm;
        private final ModelPart rightArm;

        public BraceletModel(ModelPart tree) {
            super(tree, RenderLayer::getEntityTranslucent);
            this.leftArm = tree.getChild(EntityModelPartNames.LEFT_ARM);
            this.rightArm = tree.getChild(EntityModelPartNames.RIGHT_ARM);
        }

        public static TexturedModelData getData(Dilation dilation, boolean alex, int x, int y, int z) {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();

            root.addChild(EntityModelPartNames.RIGHT_ARM,
                    ModelPartBuilder.create()
                        .uv(0, alex ? 6 : 0)
                        .cuboid((alex ? -2 : -3) + x, 7 + y, -2 + z, alex ? 3 : 4, 2, 4, dilation), ModelTransform.NONE);
            root.addChild(EntityModelPartNames.LEFT_ARM,
                    ModelPartBuilder.create().mirrored()
                        .uv(0, alex ? 6 : 0)
                        .cuboid(-1 - x, 7 + y, -2 + z, alex ? 3 : 4, 2, 4, dilation), ModelTransform.NONE);

            return TexturedModelData.of(data, 64, 32);
        }

        public void setAngles(BipedEntityModel<?> biped) {
            leftArm.copyTransform(biped.leftArm);
            rightArm.copyTransform(biped.rightArm);
        }

        public void setVisible(Arm arm) {
            leftArm.visible = arm == Arm.LEFT;
            rightArm.visible = arm == Arm.RIGHT;
        }
    }
}
