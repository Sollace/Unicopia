package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

@Mixin(ArmorFeatureRenderer.class)
// TODO: Remove this
abstract class MixinArmorFeatureRenderer<
        T extends LivingEntity,
        M extends BipedEntityModel<T>,
        A extends BipedEntityModel<T>>
            extends FeatureRenderer<T, M>/* implements AccessoryFeatureRenderer.FeatureRoot<T, M>*/ {

    //private AccessoryFeatureRenderer<T, M> accessories;

    MixinArmorFeatureRenderer() { super(null); }

    /*@Override
    public AccessoryFeatureRenderer<T, M> getAccessories() {
        return accessories;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(FeatureRendererContext<T, M> context, A inner, A outer, BakedModelManager bakery, CallbackInfo info) {
        accessories = new AccessoryFeatureRenderer<>(context);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(MatrixStack stack, VertexConsumerProvider renderContext, int lightUv, T entity, float limbDistance, float limbAngle, float tickDelta, float age, float headYaw, float headPitch, CallbackInfo info) {
        getAccessories().render(stack, renderContext, lightUv, entity, limbDistance, limbAngle, tickDelta, age, headYaw, headPitch);
    }*/
}
