package com.minelittlepony.unicopia.client.render.bezier;

import java.util.function.Consumer;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.client.render.RenderUtil;

public record BezierSegment(
            RenderUtil.Vertex[] corners
        ) {
    public BezierSegment() {
        this(new RenderUtil.Vertex[] {
            new RenderUtil.Vertex(), new RenderUtil.Vertex(),
            new RenderUtil.Vertex(), new RenderUtil.Vertex()
        });
    }

    public void set(Vector3f from, Vector3f to, float height) {
        corners[0].set(from.x, from.y - height/2F, from.z, 0, 0); // bottom left
        corners[1].set(from.x, from.y + height/2F, from.z, 1, 0); // top    left
        corners[2].set(to.x, to.y + height/2F, to.z, 1, 1);       // top    right
        corners[3].set(to.x, to.y - height/2F, to.z, 0, 1);       // bottom right
    }

    public void forEachCorner(Consumer<RenderUtil.Vertex> transformer) {
        for (var corner : corners) {
            transformer.accept(corner);
        }
    }
}
