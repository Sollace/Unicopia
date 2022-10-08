package com.minelittlepony.unicopia.util.shape;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

record TranslatedPointGenerator (
        PointGenerator source,
        Vec3d offset
    ) implements Shape {

    @Override
    public double getVolume() {
        return ((Shape)source).getVolume();
    }

    @Override
    public Vec3d computePoint(Random rand) {
        return source.computePoint(rand).add(offset);
    }

    @Override
    public Vec3d getLowerBound() {
        return ((Shape)source).getLowerBound().add(offset);
    }

    @Override
    public Vec3d getUpperBound() {
        return ((Shape)source).getUpperBound().add(offset);
    }

    @Override
    public boolean isPointInside(Vec3d point) {
        return ((Shape)source).isPointInside(point.subtract(offset));
    }

    @Override
    public Shape translate(Vec3d offset) {
        if (offset.equals(Vec3d.ZERO)) {
            return this;
        }
        return new TranslatedPointGenerator(source, this.offset.add(offset));
    }
}
