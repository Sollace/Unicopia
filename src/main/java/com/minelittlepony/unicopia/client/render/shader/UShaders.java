package com.minelittlepony.unicopia.client.render.shader;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public final class UShaders {
    @Nullable
    private static Supplier<ShaderProgram> renderTypePortalSurfaceProgram = register("rendertype_portal_surface", VertexFormats.POSITION_COLOR);

    public static ShaderProgram getRenderTypePortalSurfaceProgram() {
        return renderTypePortalSurfaceProgram.get();
    }

    public static void bootstrap() { }

    static Supplier<ShaderProgram> register(String name, VertexFormat format) {
        AtomicReference<ShaderProgram> holder = new AtomicReference<>();
        CoreShaderRegistrationCallback.EVENT.register(context -> context.register(Unicopia.id(name), format, holder::set));
        return holder::get;
    }
}
