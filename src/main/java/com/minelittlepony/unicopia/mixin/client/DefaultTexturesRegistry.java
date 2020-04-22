package com.minelittlepony.unicopia.mixin.client;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.SpriteIdentifier;

@Mixin(ModelLoader.class)
public interface DefaultTexturesRegistry {
    @Accessor("DEFAULT_TEXTURES")
    static Set<SpriteIdentifier> getDefaultTextures() {
        return null;
    }
}
