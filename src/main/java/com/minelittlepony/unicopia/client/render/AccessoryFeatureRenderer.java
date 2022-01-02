package com.minelittlepony.unicopia.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;

public class AccessoryFeatureRenderer<
        T extends LivingEntity,
        M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {

    private static final List<FeatureFactory<?>> REGISTRY = new ArrayList<>();

    public static <T extends LivingEntity> void register(FeatureFactory<T> factory) {
        REGISTRY.add(factory);
    }

    private final Iterable<Feature<T>> features;

    @SuppressWarnings("unchecked")
    public AccessoryFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
        features = REGISTRY.stream().map(f -> ((FeatureFactory<T>)f).create(context)).toList();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        features.forEach(feature -> feature.render(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch));
    }

    public void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, ModelPart arm, Arm side) {
        features.forEach(feature -> feature.renderArm(matrices, vertexConsumers, light, entity, arm, side));
    }

    public interface FeatureFactory<T extends LivingEntity> {
        Feature<T> create(FeatureRendererContext<T, ? extends BipedEntityModel<T>> context);
    }

    public interface Feature<T extends LivingEntity> {
        void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch);

        default void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, ModelPart arm, Arm side) {

        }
    }

    public interface FeatureRoot<
            T extends LivingEntity,
            M extends BipedEntityModel<T>> {
        AccessoryFeatureRenderer<T, M> getAccessories();
    }
}
