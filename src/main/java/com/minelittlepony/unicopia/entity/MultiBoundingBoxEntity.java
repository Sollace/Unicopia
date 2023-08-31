package com.minelittlepony.unicopia.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public interface MultiBoundingBoxEntity {
    List<Box> getBoundingBoxes();

    static List<Box> getBoundingBoxes(Entity entity) {
        return entity instanceof MultiBoundingBoxEntity multi ? multi.getBoundingBoxes() : List.of(entity.getBoundingBox());
    }
}
