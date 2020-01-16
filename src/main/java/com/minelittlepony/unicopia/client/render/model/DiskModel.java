package com.minelittlepony.unicopia.client.render.model;

public class DiskModel extends SphereModel {

    @Override
    protected void drawShape() {
        Disk sphere = new Disk();

        sphere.setDrawStyle(GLU.GLU_FILL);
        sphere.setNormals(GL.GLU_SMOOTH);
        sphere.draw(0, 1, 32, 32);
    }
}
