package com.minelittlepony.unicopia.client.render.shader;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.Unicopia;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public interface UShaders {
    Supplier<ShaderProgram> RENDER_TYPE_PORTAL_SURFACE = register("rendertype_portal_surface", VertexFormats.POSITION_COLOR);

    static void bootstrap() { }

    static Supplier<ShaderProgram> register(String name, VertexFormat format) {
        AtomicReference<ShaderProgram> holder = new AtomicReference<>();
        CoreShaderRegistrationCallback.EVENT.register(context -> context.register(Unicopia.id(name), format, holder::set));
        return holder::get;
    }
}
