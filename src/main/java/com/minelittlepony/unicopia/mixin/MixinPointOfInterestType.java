package com.minelittlepony.unicopia.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.block.BlockState;
import net.minecraft.world.poi.PointOfInterestType;

@Mixin(PointOfInterestType.class)
public interface MixinPointOfInterestType {
    @Mutable
    @Accessor("blockStates")
    void setStates(Set<BlockState> states);
}
