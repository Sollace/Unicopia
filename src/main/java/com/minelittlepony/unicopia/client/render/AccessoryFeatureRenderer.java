package com.minelittlepony.unicopia.client.render;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.client.FirstPersonRendererOverrides.ArmRenderer;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;

public class AccessoryFeatureRenderer<
        S extends BipedEntityRenderState,
        M extends EntityModel<S>> extends FeatureRenderer<S, M> {

    private static final List<FeatureFactory<?, ?>> REGISTRY = new ArrayList<>();

    @SafeVarargs
    public static <S extends BipedEntityRenderState> void register(FeatureFactory<S, BipedEntityModel<S>>...factories) {
        for (var factory : factories) {
            REGISTRY.add(factory);
        }
    }

    private final Iterable<Feature<S>> features;

    @SuppressWarnings("unchecked")
    public AccessoryFeatureRenderer(FeatureRendererContext<S, M> context) {
        super(context);
        features = REGISTRY.stream().map(f -> ((FeatureFactory<S, M>)f).create(context)).toList();
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S state, float limbAngle, float limbDistance) {
        if (MineLPDelegate.getInstance().getRace(entity).isEquine()) {
            return;
        }

        features.forEach(feature -> feature.render(matrices, vertexConsumers, light, state, limbAngle, limbDistance));

        Caster.of(entity).ifPresent(caster -> {
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertexConsumers, light, caster, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
        });
    }

    public void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S entity, ModelPart arm, Arm side) {
        features.forEach(feature -> feature.renderArm(matrices, vertexConsumers, light, entity, arm, side));
    }

    public boolean beforeRenderArms(ArmRenderer sender, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, S entity, int light) {
        Caster<?> caster = Caster.of(entity).orElse(null);
        if (caster != null) {
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertexConsumers, light, caster, 0, 0, tickDelta, entity.age + tickDelta, 0, 0);
        }
        boolean cancelled = false;
        for (var feature : features) {
            cancelled |= feature.beforeRenderArms(sender, tickDelta, matrices, vertexConsumers, entity, light);
        }
        return cancelled;
    }

    public interface FeatureFactory<S extends BipedEntityRenderState, M extends EntityModel<S>> {
        Feature<S> create(FeatureRendererContext<S, M> context);
    }

    public interface Feature<S extends BipedEntityRenderState> {
        void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S entity, float limbAngle, float limbDistance);

        default void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S entity, ModelPart arm, Arm side) {}

        default boolean beforeRenderArms(ArmRenderer sender, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, S entity, int light) {
            return false;
        }
    }

    public interface FeatureRoot<
            S extends BipedEntityRenderState,
            M extends EntityModel<S>> {
        AccessoryFeatureRenderer<S, M> getAccessories();
        @SuppressWarnings("unchecked")
        @Nullable
        static <T extends LivingEntity, M extends EntityModel<?>> FeatureRoot<?, M> of(T entity) {
            var renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity);
            if (renderer instanceof FeatureRoot) {
                return (FeatureRoot<?, M>)renderer;
            }
            return null;
        }
    }
}
