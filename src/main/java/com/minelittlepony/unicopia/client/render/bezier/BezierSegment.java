package com.minelittlepony.unicopia.client.render.bezier;

import java.util.function.Consumer;

import org.joml.Vector3f;

public record BezierSegment(
            Vector3f[] corners
        ) {

    public BezierSegment(Vector3f from, Vector3f to, float height) {
        this(new Vector3f[] {
            new Vector3f(from.x, from.y - height/2F, from.z), // bottom left
            new Vector3f(from.x, from.y + height/2F, from.z), // top    left
            new Vector3f(to.x, to.y + height/2F, to.z),       // top    right
            new Vector3f(to.x, to.y - height/2F, to.z)        // bottom right
        });
    }

    public void forEachCorner(Consumer<Vector3f> transformer) {
        for (var corner : corners) {
            transformer.accept(corner);
        }
    }
}
