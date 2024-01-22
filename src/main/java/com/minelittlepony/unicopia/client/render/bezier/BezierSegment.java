package com.minelittlepony.unicopia.client.render.bezier;

import java.util.function.Consumer;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.client.render.RenderUtil;

public record BezierSegment(
            RenderUtil.Vertex[] corners
        ) {

    public BezierSegment(Vector3f from, Vector3f to, float height) {
        this(new RenderUtil.Vertex[] {
            new RenderUtil.Vertex(new Vector3f(from.x, from.y - height/2F, from.z), 0, 0), // bottom left
            new RenderUtil.Vertex(new Vector3f(from.x, from.y + height/2F, from.z), 1, 0), // top    left
            new RenderUtil.Vertex(new Vector3f(to.x, to.y + height/2F, to.z), 1, 1),       // top    right
            new RenderUtil.Vertex(new Vector3f(to.x, to.y - height/2F, to.z), 0, 1)        // bottom right
        });
    }

    public void forEachCorner(Consumer<RenderUtil.Vertex> transformer) {
        for (var corner : corners) {
            transformer.accept(corner);
        }
    }
}
