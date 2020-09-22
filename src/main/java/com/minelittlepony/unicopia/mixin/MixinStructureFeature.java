package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;

@Mixin(StructureFeature.class)
public interface MixinStructureFeature {
    @Invoker("register")
    static <F extends StructureFeature<?>> F register(String name, F structureFeature, GenerationStep.Feature step) {
        throw new NullPointerException("mixin y u fail");
    }
}
