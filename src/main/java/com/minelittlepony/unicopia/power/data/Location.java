package com.minelittlepony.unicopia.power.data;

import com.google.gson.annotations.Expose;
import com.minelittlepony.unicopia.power.IData;

import net.minecraft.util.math.BlockPos;

public class Location implements IData {

    @Expose
    public int x;

    @Expose
    public int y;

    @Expose
    public int z;

    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location(BlockPos pos) {
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
    }

    public BlockPos pos() {
        return new BlockPos(x, y, z);
    }
}
