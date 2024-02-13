package com.minelittlepony.unicopia.client.render;

import com.google.common.base.MoreObjects;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class DisguisedArmsFeatureRenderer<E extends LivingEntity> implements AccessoryFeatureRenderer.Feature<E> {

    private final MinecraftClient client = MinecraftClient.getInstance();

    public DisguisedArmsFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, E entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public boolean beforeRenderArms(ArmRenderer sender, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, E entity, int light) {
        Entity appearance = getAppearance(entity);
        if (appearance instanceof LivingEntity l) {
            float swingProgress = entity.getHandSwingProgress(MinecraftClient.getInstance().getTickDelta());

            Hand hand = MoreObjects.firstNonNull(entity.preferredHand, Hand.MAIN_HAND);

            //renderArmHoldingItem(l, matrices, vertexConsumers, light, 0, swingProgress, Arm.RIGHT);

            boolean bothHands = l instanceof ZombieEntity;

            if (bothHands || hand == Hand.MAIN_HAND) {
                if (entity.getMainHandStack().isEmpty()) {
                    matrices.push();
                    renderArmHoldingItem(l, matrices, vertexConsumers, light, 1 - sender.getEquipProgress(Hand.MAIN_HAND, tickDelta), hand == Hand.MAIN_HAND ? swingProgress : 0, entity.getMainArm());
                    matrices.pop();
                }
            }

            if (bothHands || hand == Hand.OFF_HAND) {
                if (entity.getOffHandStack().isEmpty()) {
                    matrices.push();
                    renderArmHoldingItem(l, matrices, vertexConsumers, light, 1 - sender.getEquipProgress(Hand.OFF_HAND, tickDelta), hand == Hand.OFF_HAND ? swingProgress : 0, entity.getMainArm().getOpposite());
                    matrices.pop();
                }
            }
        }

        return false;
    }

    private Entity getAppearance(E entity) {
        return Caster.of(entity).flatMap(caster -> caster.getSpellSlot().get(SpellPredicate.IS_DISGUISE, false)).map(Disguise.class::cast)
                .flatMap(Disguise::getAppearance)
                .map(EntityAppearance::getAppearance)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private void renderArmHoldingItem(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm) {
        if (!(client.getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer renderer)) {
            return;
        }
        if (!(renderer.getModel() instanceof BipedEntityModel bipedModel)) {
            return;
        }

        boolean bl = arm != Arm.LEFT;
        float f = bl ? 1.0f : -1.0f;
        float g = MathHelper.sqrt(swingProgress);
        float h = -0.3f * MathHelper.sin(g * (float)Math.PI);
        float i = 0.4f * MathHelper.sin(g * ((float)Math.PI * 2));
        float j = -0.4f * MathHelper.sin(swingProgress * (float)Math.PI);
        matrices.translate(f * (h + 0.64000005f), i + -0.6f + equipProgress * -0.6f, j + -0.71999997f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 45.0f));
        float k = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float l = MathHelper.sin(g * (float)Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * l * 70.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * k * -20.0f));

        Identifier texture = renderer.getTexture(entity);
        RenderSystem.setShaderTexture(0, texture);
        matrices.translate(f * -1.0f, 3.6f, 3.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 120.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -135.0f));
        matrices.translate(f * 5.6f, 0.0f, 0.0f);

        bipedModel.animateModel(entity, 0, 0, 0);
        bipedModel.setAngles(entity, 0, 0, 0, 0, 0);
        ModelPart part = bl ? bipedModel.rightArm : bipedModel.leftArm;
        part.pitch = 0;

        if (MineLPDelegate.getInstance().getRace(entity).isEquine()) {
            matrices.translate(0, -part.pivotY / 16F, 0);
        }

        part.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture)), light, OverlayTexture.DEFAULT_UV);
    }
}
