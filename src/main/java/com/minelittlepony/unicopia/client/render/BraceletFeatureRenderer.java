package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.GlowableItem;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public class BraceletFeatureRenderer<
        E extends LivingEntity,
        M extends BipedEntityModel<E>> extends FeatureRenderer<E, M> {

    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/models/armor/bracelet.png");

    private final BraceletModel steveModel = new BraceletModel(0.3F, false);
    private final BraceletModel alexModel = new BraceletModel(0.3F, true);

    public BraceletFeatureRenderer(FeatureRendererContext<E, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, E entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch) {

        ItemStack item = entity.getEquippedStack(EquipmentSlot.CHEST);

        if (item.getItem() instanceof FriendshipBraceletItem) {


            int j = ((DyeableItem)item.getItem()).getColor(item);

            boolean alex = entity instanceof ClientPlayerEntity && ((ClientPlayerEntity)entity).getModel().startsWith("slim");

            BraceletModel model = alex ? alexModel : steveModel;


            if (entity instanceof ArmorStandEntity) {
                ModelPart arm = entity.getMainArm() == Arm.LEFT ? getContextModel().leftArm : getContextModel().rightArm;
                arm.visible = true;
                VertexConsumer consumer = renderContext.getBuffer(getContextModel().getLayer(getTexture(entity)));
                arm.render(stack, consumer, lightUv, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1);
            }

            VertexConsumer consumer = ItemRenderer.getArmorGlintConsumer(renderContext, RenderLayer.getArmorCutoutNoCull(TEXTURE), false, false);
            model.setAngles(getContextModel());
            model.setVisible(entity.getMainArm());
            model.render(stack, consumer, ((GlowableItem)item.getItem()).isGlowing(item) ? 0x0F00F0 : lightUv, OverlayTexture.DEFAULT_UV, Color.r(j), Color.g(j), Color.b(j), 1);
        }
    }

    static class BraceletModel extends Model {

        private final ModelPart leftArm;
        private final ModelPart rightArm;

        private final boolean alex;

        public BraceletModel(float dilate, boolean alex) {
            super(RenderLayer::getEntityTranslucent);
            this.alex = alex;
            rightArm = new ModelPart(this, 0, alex ? 6 : 0);
            rightArm.addCuboid(-3, 7, -2, alex ? 3 : 4, 2, 4, dilate);
            leftArm = new ModelPart(this, 0, alex ? 6 : 0);
            leftArm.mirror = true;
            leftArm.addCuboid(-1, 7, -2, alex ? 3 : 4, 2, 4, dilate);
        }

        public void setAngles(BipedEntityModel<?> biped) {
            leftArm.copyPositionAndRotation(biped.leftArm);
            rightArm.copyPositionAndRotation(biped.rightArm);
            if (alex) {
                rightArm.pivotX++;
            }
        }

        public void setVisible(Arm arm) {
            leftArm.visible = arm == Arm.LEFT;
            rightArm.visible = arm == Arm.RIGHT;
        }

        @Override
        public void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
            leftArm.render(matrixStack, vertexConsumer, i, j, f, g, h, k);
            rightArm.render(matrixStack, vertexConsumer, i, j, f, g, h, k);
        }
    }

}
