package com.minelittlepony.unicopia.forgebullshit;

import java.util.Set;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;

/**
 * Provides access the the built in texture locations.
 * This is needed to add things like custom backgrounds for slots.
 * @author Chris Albers
 *
 */
@FUF(reason = "Forge doesn't provide this, for some unknown reason...")
public final class BuildInTexturesBakery extends ModelBakery {
    private BuildInTexturesBakery() {
        super(null, null, null);
    }

    public static Set<ResourceLocation> getBuiltInTextures() {
        return LOCATIONS_BUILTIN_TEXTURES;
    }
}
