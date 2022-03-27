package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AnimalModel;

@Mixin(AnimalModel.class)
public interface MixinAnimalModel {
    @Invoker("getHeadParts")
    Iterable<ModelPart> invokeGetHeadParts();
}
