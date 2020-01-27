package com.minelittlepony.unicopia.core.client.render;

import net.minecraft.client.model.Quad;
import net.minecraft.client.model.Vertex;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3d;

public class TexturedShape2d extends Quad {

    protected boolean invertNormal;

    public TexturedShape2d(Vertex... vertices) {
        super(vertices);
    }

    public TexturedShape2d(Vertex[] vertices, int texcoordU1, int texcoordV1, int texcoordU2, int texcoordV2, float textureWidth, float textureHeight) {
        super(vertices, texcoordU1, texcoordV1, texcoordU2, texcoordV2, textureWidth, textureHeight);
    }

    public TexturedShape2d setInvertNormal() {
        invertNormal = true;
        return this;
    }

    @Override
    public void render(BufferBuilder renderer, float scale) {
        Vec3d vec3d = vertices[1].pos.reverseSubtract(vertices[0].pos);
        Vec3d vec3d1 = vertices[1].pos.reverseSubtract(vertices[2].pos);
        Vec3d vec3d2 = vec3d1.crossProduct(vec3d).normalize();
        float f = (float)vec3d2.x;
        float f1 = (float)vec3d2.y;
        float f2 = (float)vec3d2.z;

        if (invertNormal) {
            f = -f;
            f1 = -f1;
            f2 = -f2;
        }

        renderer.begin(7, VertexFormats.POSITION_UV_NORMAL);

        for (int i = 0; i < vertexCount; ++i) {
            Vertex positiontexturevertex = vertices[i];
            renderer
                .vertex(positiontexturevertex.pos.x * scale, positiontexturevertex.pos.y * scale, positiontexturevertex.pos.z * scale)
                .texture(positiontexturevertex.u, positiontexturevertex.v)
                .normal(f, f1, f2)
                .end();
        }

        Tessellator.getInstance().draw();
    }
}
