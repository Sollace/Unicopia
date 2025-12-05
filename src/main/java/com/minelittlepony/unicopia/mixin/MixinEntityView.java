package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.collision.EntityCollisions;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EntityView;

@Mixin(EntityView.class)
interface MixinEntityView {
    @ModifyReturnValue(
            method = "getEntityCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;",
            at = @At("RETURN")
    )
    private List<VoxelShape> onGetEntityCollisions(List<VoxelShape> original, @Nullable Entity entity, Box box) {
        if (box.getAverageSideLength() < 1.0E-7D) {
            return original;
        }

        List<VoxelShape> shapes = EntityCollisions.getColissonShapes(entity, (EntityView)this, box);
        if (!shapes.isEmpty()) {
            return Stream.concat(shapes.stream(), original.stream()).toList();
        }
        return original;
    }
}
