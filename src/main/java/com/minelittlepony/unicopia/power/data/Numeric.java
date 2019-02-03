package com.minelittlepony.unicopia.power.data;

import com.google.gson.annotations.Expose;
import com.minelittlepony.unicopia.power.IData;

public class Numeric implements IData {

    @Expose
    public int type;

    public Numeric(int t) {
        type = t;
    }
}
