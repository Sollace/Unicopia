package com.minelittlepony.unicopia.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;

// pseudo code for things forge can't do.
// @Mixin(Entity.class)
public interface MixinEntity {

    // @Accessor("setSize")
    static void setSize(Entity self, float width, float height) {
        if (self.width != width || self.height != height) {
            float f = self.width;
            self.width = width;
            self.height = height;

            if (self.width < f) {
                double d0 = width / 2;
                self.setEntityBoundingBox(new AxisAlignedBB(
                        self.posX - d0, self.posY, self.posZ - d0,
                        self.posX + d0, self.posY + self.height, self.posZ + d0));
                return;
            }

            AxisAlignedBB axisalignedbb = self.getEntityBoundingBox();
            self.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)self.width, axisalignedbb.minY + (double)self.height, axisalignedbb.minZ + (double)self.width));

            if (self.width > f/* && !self.firstUpdate*/ && !self.world.isRemote) {
                self.move(MoverType.SELF, (double)(f - self.width), 0, (double)(f - self.width));
            }
        }
    }
}
