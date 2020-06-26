package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.StructureFeature;

@Mixin(StructuresConfig.class)
public interface MixinStructuresConfig {
    @Accessor("DEFAULT_STRUCTURES")
    static void setDefaultConfigs(ImmutableMap<StructureFeature<?>, StructureConfig> map) {

    }
}
