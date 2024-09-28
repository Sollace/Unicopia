package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.item.*;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;

public class BraceletFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {

    public static final Identifier TEXTURE = Unicopia.id("textures/models/armor/bracelet.png");

    private final BraceletModel steveModel;
    private final BraceletModel alexModel;

    private final FeatureRendererContext<E, ? extends BipedEntityModel<E>> context;

    public BraceletFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        this.context = context;
        Dilation dilation = new Dilation(0.3F);
        steveModel = new BraceletModel(BraceletModel.getData(dilation, false, 0, 0, 0).createModel());
        alexModel = new BraceletModel(BraceletModel.getData(dilation, true, 0, 0, 0).createModel());
    }

    @Override
    public void render(MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch) {
        FriendshipBraceletItem.getWornBangles(entity, TrinketsDelegate.MAIN_GLOVE).findFirst().ifPresent(bangle -> {
            renderBangleThirdPerson(bangle.stack(), stack, renderContext, lightUv, entity, limbDistance, limbAngle, tickDelta, age, headYaw, headPitch, entity.getMainArm());
        });
        FriendshipBraceletItem.getWornBangles(entity, TrinketsDelegate.SECONDARY_GLOVE).findFirst().ifPresent(bangle -> {
            renderBangleThirdPerson(bangle.stack(), stack, renderContext, lightUv, entity, limbDistance, limbAngle, tickDelta, age, headYaw, headPitch, entity.getMainArm().getOpposite());
        });
    }

    private void renderBangleThirdPerson(ItemStack item, MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch, Arm mainArm) {
        int j = DyedColorComponent.getColor(item, Colors.WHITE);

        boolean alex = entity instanceof ClientPlayerEntity && ((ClientPlayerEntity)entity).getSkinTextures().model() == SkinTextures.Model.SLIM;

        BraceletModel model = alex ? alexModel : steveModel;
        boolean isLeft = mainArm == Arm.LEFT;

        if (entity instanceof ArmorStandEntity) {
            ModelPart arm = isLeft ? context.getModel().leftArm : context.getModel().rightArm;
            arm.visible = true;
            VertexConsumer consumer = renderContext.getBuffer(context.getModel().getLayer(context.getTexture(entity)));
            arm.render(stack, consumer, lightUv, OverlayTexture.DEFAULT_UV, Colors.WHITE);
        }

        VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(TEXTURE), false);

        model.setAngles(context.getModel());
        model.setVisible(mainArm);
        model.render(stack, consumer, GlowableItem.isGlowing(item) ? LightmapTextureManager.MAX_LIGHT_COORDINATE : lightUv, OverlayTexture.DEFAULT_UV, j);
    }

    @Override
    public void renderArm(MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, E entity, ModelPart armModel, Arm side) {
        FriendshipBraceletItem.getWornBangles(entity, side == entity.getMainArm() ? TrinketsDelegate.MAIN_GLOVE : TrinketsDelegate.SECONDARY_GLOVE).findFirst().ifPresent(item -> {
            int j = DyedColorComponent.getColor(item.stack(), Colors.WHITE);

            boolean alex = entity instanceof ClientPlayerEntity && ((ClientPlayerEntity)entity).getSkinTextures().model() == SkinTextures.Model.SLIM;

            BraceletModel model = alex ? alexModel : steveModel;

            if (MineLPDelegate.getInstance().getPlayerPonyRace((ClientPlayerEntity)entity).isEquine()) {
                stack.translate(side == Arm.LEFT ? 0.06 : -0.06, 0.3, 0);
            } else {
                stack.translate(0, -0.1, 0);
            }

            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(TEXTURE), false);

            model.setAngles(context.getModel());
            model.setVisible(side);
            model.render(stack, consumer, GlowableItem.isGlowing(item.stack()) ? LightmapTextureManager.MAX_LIGHT_COORDINATE : lightUv, OverlayTexture.DEFAULT_UV, j);
        });
    }

    public static class BraceletModel extends Model {

        private final ModelPart leftArm;
        private final ModelPart rightArm;

        public BraceletModel(ModelPart tree) {
            super(RenderLayer::getEntityTranslucent);
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

        @Override
        public void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j, int color) {
            leftArm.render(matrixStack, vertexConsumer, i, j, color);
            rightArm.render(matrixStack, vertexConsumer, i, j, color);
        }
    }

}
