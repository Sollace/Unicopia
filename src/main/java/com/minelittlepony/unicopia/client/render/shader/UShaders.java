package com.minelittlepony.unicopia.client.render.shader;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public interface UShaders {
    ShaderProgramKey RENDER_TYPE_PORTAL_SURFACE = register("rendertype_portal_surface", VertexFormats.POSITION_COLOR, Defines.EMPTY);

    static void bootstrap() { }

    private static ShaderProgramKey register(String name, VertexFormat format, Defines defines) {
        ShaderProgramKey key = new ShaderProgramKey(Unicopia.id("core/" + name), format, defines);
        ShaderProgramKeys.getAll().add(key);
        return key;
    }
}
