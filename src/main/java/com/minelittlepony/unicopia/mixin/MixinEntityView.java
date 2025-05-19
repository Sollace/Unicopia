package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.collision.EntityCollisions;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EntityView;

@Mixin(EntityView.class)
interface MixinEntityView {
    @Inject(method = "getEntityCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private void onGetEntityCollisions(@Nullable Entity entity, Box box, CallbackInfoReturnable<List<VoxelShape>> info) {
        if (box.getAverageSideLength() < 1.0E-7D) {
            return;
        }

        List<VoxelShape> shapes = EntityCollisions.getColissonShapes(entity, (EntityView)this, box);
        if (!shapes.isEmpty()) {
            info.setReturnValue(Stream.concat(shapes.stream(), info.getReturnValue().stream()).toList());
        }
    }
}
