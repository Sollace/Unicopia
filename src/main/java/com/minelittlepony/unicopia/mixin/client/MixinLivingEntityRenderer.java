package com.minelittlepony.unicopia.mixin.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer;
import com.minelittlepony.unicopia.client.render.AnimalPoser;
import com.minelittlepony.unicopia.client.render.PlayerPoser;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.AccessoryFeatureRenderer.FeatureRoot;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S>
        implements FeatureRendererContext<S, M>, FeatureRoot<S, M> {
    protected MixinLivingEntityRenderer(Context context) {
        super(context);
    }

    @Shadow
    private @Final List<FeatureRenderer<S, M>> features;

    @Nullable
    private AccessoryFeatureRenderer<S, M> accessories;

    @Override
    @SuppressWarnings("unchecked")
    public AccessoryFeatureRenderer<S, M> getAccessories() {
        if (accessories == null) {
            accessories = features.stream()
                .filter(a -> a instanceof FeatureRoot)
                .map(a -> ((FeatureRoot<S, M>)a).getAccessories())
                .findFirst()
                .orElseGet(() -> {
                    var feature = new AccessoryFeatureRenderer<>(this);
                    features.add(feature);
                    return feature;
                });
        }
        return accessories;
    }

    @Inject(method = "render",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/client/render/entity/model/EntityModel.setAngles(Lnet/minecraft/client/render/entity/state/EntityRenderState;)V",
                shift = Shift.AFTER))
    private void onRender(
            S state, MatrixStack matrices, VertexConsumerProvider vertices, int light,
            CallbackInfo into) {
        getAccessories();
        if (state instanceof PlayerEntityRenderState player) {
            PlayerPoser.INSTANCE.applyPosing(matrices, player, (BipedEntityModel<?>)getModel(), PlayerPoser.Context.THIRD_PERSON);
        }

        AnimalPoser.INSTANCE.applyPosing(matrices, state, getModel());
    }

    @Inject(method = "updateRenderState",
            at = @At("TAIL"))
    private void onUpdateRenderState(T entity, S state, float tickDelta, CallbackInfo info) {
        CasterState caster = CasterState.of(state);
        caster.update(Caster.of(entity).orElse(null), tickDelta);
    }
}
