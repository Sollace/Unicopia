package com.minelittlepony.unicopia.client.render;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class DisguisedArmsFeatureRenderer<S extends BipedEntityRenderState> implements AccessoryFeatureRenderer.Feature<S> {

    private final MinecraftClient client = MinecraftClient.getInstance();

    private static final Map<EntityType<?>, Identifier> OVERLAY_TEXTURES = Map.of(
        EntityType.DROWNED, Identifier.ofVanilla("textures/entity/zombie/drowned_outer_layer.png"),
        EntityType.STRAY, Identifier.ofVanilla("textures/entity/skeleton/stray_overlay.png")
    );

    private final Function<EntityType<?>, Set<Pair<ModelPart, ModelPart>>> overlayModelCache = Util.memoize(type -> {
        return EntityModelLayers.getLayers()
                .filter(layer -> layer.id().equals(EntityType.getId(type)) && !"main".equals(layer.name()))
                .map(MinecraftClient.getInstance().getEntityModelLoader()::getModelPart)
                .map(model -> {
                    ModelPart arms = getPart(model, EntityModelPartNames.ARMS).orElse(null);
                    ModelPart leftArm = getPart(model, EntityModelPartNames.LEFT_ARM)
                            .or(() -> getPart(model, EntityModelPartNames.LEFT_FRONT_LEG))
                            .orElse(arms);
                    ModelPart rightArm = getPart(model, EntityModelPartNames.RIGHT_ARM)
                            .or(() -> getPart(model, EntityModelPartNames.RIGHT_FRONT_LEG))
                            .orElse(arms);
                    return leftArm != null && rightArm != null ? Pair.of(leftArm, rightArm) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    });

    private static Optional<ModelPart> getPart(ModelPart part, String childName) {
        return part.hasChild(childName) ? Optional.of(part.getChild(childName)) : Optional.empty();
    }

    public DisguisedArmsFeatureRenderer(FeatureRendererContext<S, ? extends BipedEntityModel<S>> context) {

    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S entity, float limbAngle, float limbDistance) {
    }

    @Override
    public boolean beforeRenderArms(ArmRenderer sender, MatrixStack matrices, VertexConsumerProvider vertexConsumers, S entity, int light) {
        if (CasterState.of(entity).appearance instanceof LivingEntity l) {
            float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
            float swingProgress = entity.handSwingProgress;

            Hand hand = entity.preferredArm == entity.mainArm ? Hand.MAIN_HAND : Hand.OFF_HAND;

            boolean bothHands = l instanceof ZombieEntity || l instanceof IronGolemEntity;

            if (bothHands || hand == Hand.MAIN_HAND) {
                if (entity.getMainHandStack().isEmpty()) {
                    matrices.push();
                    renderArmHoldingItem(l, matrices, vertexConsumers, light, 1 - sender.getEquipProgress(Hand.MAIN_HAND, tickDelta), hand == Hand.MAIN_HAND ? swingProgress : 0, entity.mainArm);
                    matrices.pop();
                }
            }

            if (bothHands || hand == Hand.OFF_HAND) {
                if ((entity.mainArm == Arm.LEFT ? entity.rightHandStack : entity.leftHandStack).isEmpty()) {
                    matrices.push();
                    renderArmHoldingItem(l, matrices, vertexConsumers, light, 1 - sender.getEquipProgress(Hand.OFF_HAND, tickDelta), hand == Hand.OFF_HAND ? swingProgress : 0, entity.mainArm.getOpposite());
                    matrices.pop();
                }
            }
        }

        return false;
    }

    @Nullable
    private ModelPart getArmModel(@Nullable EntityModel<?> model, boolean right) {
        if (model instanceof BipedEntityModel bipedModel) {
            return right ? bipedModel.rightArm : bipedModel.leftArm;
        }

        return getOptionalChild(model.getRootPart(), EntityModelPartNames.ARMS).map(arms -> {
            return getOptionalChild(arms, right ? EntityModelPartNames.RIGHT_ARM : EntityModelPartNames.LEFT_ARM)
                    .or(() -> getOptionalChild(arms, right ? EntityModelPartNames.RIGHT_FRONT_LEG : EntityModelPartNames.LEFT_FRONT_LEG))
                    .orElse(arms);
        }).orElse(null);

    }

    static Optional<ModelPart> getOptionalChild(ModelPart parent, String key) {
        return parent.hasChild(key) ? Optional.of(parent.getChild(key)) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private void renderArmHoldingItem(LivingEntity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm) {
        if (!(client.getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer renderer)) {
            return;
        }

        boolean right = arm != Arm.LEFT;
        var model = renderer.getModel();
        @Nullable
        ModelPart part = getArmModel(model, right);

        if (part == null) {
            return;
        }

        float tickDelta = client.getRenderTickCounter().getTickDelta(false);

        LivingEntityRenderState state = (LivingEntityRenderState)renderer.getAndUpdateRenderState(entity, tickDelta);

        model.setAngles(state);

        float signum = right ? 1 : -1;
        float srtSwingProgress = MathHelper.sqrt(swingProgress);
        float xOffset = -0.3F * MathHelper.sin(srtSwingProgress * MathHelper.PI);
        float yOffset = 0.4F * MathHelper.sin(srtSwingProgress * (MathHelper.TAU));
        float swingAmount = -0.4F * MathHelper.sin(swingProgress * MathHelper.PI);
        matrices.translate(signum * (xOffset + 0.64000005F), yOffset + -0.6F + equipProgress * -0.6F, swingAmount + -0.71999997f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(signum * 45));
        float zRot = MathHelper.sin(swingProgress * swingProgress * MathHelper.PI);
        float yRot = MathHelper.sin(srtSwingProgress * MathHelper.PI);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(signum * yRot * 70));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(signum * zRot * -20));
        matrices.translate(signum * -1, 3.6F, 3.5F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(signum * 120));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(200));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(signum * -135));
        matrices.translate(signum * 5.6F, 0, 0);

        if (entity instanceof IronGolemEntity golem) {
            int attackTicks = golem.getAttackTicksLeft();
            if (attackTicks > 0) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-signum * part.pitch * MathHelper.DEGREES_PER_RADIAN - 90 * signum));
            }
        }
        part.pitch = 0;

        if (MineLPDelegate.getInstance().getRace(entity).isEquine()) {
            matrices.translate(0, -part.pivotY / 16F, 0);
        }

        Identifier texture = renderer.getTexture(state);
        RenderSystem.setShaderTexture(0, texture);
        part.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(texture)), light, OverlayTexture.DEFAULT_UV);

        Identifier overlayTexture = OVERLAY_TEXTURES.get(entity.getType());
        if (overlayTexture != null) {
            overlayModelCache.apply(entity.getType()).forEach(arms -> {
                ModelPart armPart = right ? arms.getSecond() : arms.getFirst();
                armPart.copyTransform(part);

                RenderSystem.setShaderTexture(0, overlayTexture);
                part.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(overlayTexture)), light, OverlayTexture.DEFAULT_UV);
            });
        }
    }
}
