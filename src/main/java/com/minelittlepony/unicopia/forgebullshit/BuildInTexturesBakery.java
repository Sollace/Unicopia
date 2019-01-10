package com.minelittlepony.unicopia.forgebullshit;

import java.util.Set;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;


public final class BuildInTexturesBakery extends ModelBakery {
    private BuildInTexturesBakery() {
        super(null, null, null);
    }

    public static Set<ResourceLocation> getBuiltInTextures() {
        return LOCATIONS_BUILTIN_TEXTURES;
    }
}
