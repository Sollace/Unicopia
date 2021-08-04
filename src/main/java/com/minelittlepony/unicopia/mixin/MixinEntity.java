package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;

@Mixin(Entity.class)
public interface MixinEntity {
    @Accessor
    void setRemovalReason(RemovalReason reason);
}
