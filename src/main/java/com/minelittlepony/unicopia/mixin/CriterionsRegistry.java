package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.Criterions;

@Mixin(Criterions.class)
public interface CriterionsRegistry {
    @Invoker("register")
    static <T extends Criterion<?>> T register(T object) {
        throw new NullPointerException("mixin y u fail");
    }
}
