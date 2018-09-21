package com.minelittlepony.render.model;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

public class TexturedShape2d extends TexturedQuad {

	protected boolean invertNormal;

	public TexturedShape2d(PositionTextureVertex... vertices) {
		super(vertices);
	}

	public TexturedShape2d(PositionTextureVertex[] vertices, int texcoordU1, int texcoordV1, int texcoordU2, int texcoordV2, float textureWidth, float textureHeight) {
		super(vertices, texcoordU1, texcoordV1, texcoordU2, texcoordV2, textureWidth, textureHeight);
	}

	public TexturedShape2d setInvertNormal() {
		invertNormal = true;
		return this;
	}

	public void drawQuad(BufferBuilder renderer, float scale) {
		Vec3d vec3d = vertexPositions[1].vector3D.subtractReverse(vertexPositions[0].vector3D);
        Vec3d vec3d1 = vertexPositions[1].vector3D.subtractReverse(vertexPositions[2].vector3D);
        Vec3d vec3d2 = vec3d1.crossProduct(vec3d).normalize();
        float f = (float)vec3d2.x;
        float f1 = (float)vec3d2.y;
        float f2 = (float)vec3d2.z;

        if (invertNormal) {
            f = -f;
            f1 = -f1;
            f2 = -f2;
        }

        renderer.begin(7, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);

        for (int i = 0; i < nVertices; ++i) {
            PositionTextureVertex positiontexturevertex = vertexPositions[i];
            renderer.pos(positiontexturevertex.vector3D.x * (double)scale, positiontexturevertex.vector3D.y * (double)scale, positiontexturevertex.vector3D.z * (double)scale).tex((double)positiontexturevertex.texturePositionX, (double)positiontexturevertex.texturePositionY).normal(f, f1, f2).endVertex();
        }

        Tessellator.getInstance().draw();
	}
}
