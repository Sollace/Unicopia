package com.minelittlepony.unicopia.model;

import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.GLU;

public class ModelDisk extends ModelSphere {

    @Override
    protected void drawShape() {
        Disk sphere = new Disk();

        sphere.setDrawStyle(GLU.GLU_FILL);
        sphere.setNormals(GLU.GLU_SMOOTH);
        sphere.draw(0, 1, 32, 32);
    }
}
