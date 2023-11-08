package com.minelittlepony.unicopia.mixin.client;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.render.ModelPartHooks;
import com.minelittlepony.unicopia.entity.duck.Hookable;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPart.Cuboid;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(ModelPart.class)
abstract class MixinModelPart implements Hookable {
    @Unique
    private boolean isHeadPart;

    @Shadow
    private boolean visible;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onModelPart(List<Cuboid> cuboids, Map<String, ModelPart> children, CallbackInfo info) {
        if (((Object)children.getOrDefault(EntityModelPartNames.HEAD, null)) instanceof Hookable hook) {
            hook.enableHooks();
        }
    }

    @Override
    public void enableHooks() {
        isHeadPart = true;
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V", at = @At("HEAD"))
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo info) {
        if (visible && isHeadPart) {
            ModelPartHooks.onHeadRendered((ModelPart)(Object)this, matrices);
        }
    }
}
