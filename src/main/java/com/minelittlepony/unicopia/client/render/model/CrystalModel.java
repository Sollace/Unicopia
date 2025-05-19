package com.minelittlepony.unicopia.client.render.model;

public class CrystalModel extends BakedModel {
    public CrystalModel(int x, int z, int height) {
        // top
        buildFace((xx, xz) -> {
            addVertex(
                x * xx,
                xx * 2 + xz * 2,
                z * xz,
                xx, xz
            );
        });
        // north
        buildFace((xx, xy) -> {
            addVertex(
                x * xx,
                height * xy - (xx * (xy - 1)) * 2F,
                0,
                xx, xy
            );
        });
        // south
        buildFace((xx, xy) -> {
            addVertex(
                x * xx,
                height * xy - (xy - 1) * 2 - (xx * (xy - 1)) * 2F,
                z,
                xx, xy
            );
        });
        // east
        buildFace((zz, xy) -> {
            addVertex(
                0,
                height * xy - (zz * (xy - 1)) * 2F,
                z * zz,
                zz, xy
            );
        });
        // west
        buildFace((zz, xy) -> {
            addVertex(
                x,
                height * xy - (xy - 1) * 2 - (zz * (xy - 1)) * 2F,
                z * zz,
                zz, xy
            );
        });
        // bottom
        buildFace((xx, xz) -> {
            addVertex(
                x * xx,
                height,
                z * xz,
                xx, xz
            );
        });
    }

    static void buildFace(BiFloatConsumer consumer) {
        consumer.accept(0, 0);
        consumer.accept(1, 0);
        consumer.accept(1, 1);
        consumer.accept(0, 1);
    }

    interface BiFloatConsumer {
        void accept(float a, float b);
    }
}
