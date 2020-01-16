package com.minelittlepony.unicopia.forgebullshit;

import java.util.Set;

import net.minecraft.util.Identifier;

/**
 * Provides access to the built in texture locations.
 * This is needed to add things like custom backgrounds for slots.
 *
 */
@Deprecated
public final class BuildInTexturesBakery extends ModelBakery {
    private BuildInTexturesBakery() {
        super(null, null, null);
    }

    public static Set<Identifier> getBuiltInTextures() {
        return LOCATIONS_BUILTIN_TEXTURES;
    }
}
