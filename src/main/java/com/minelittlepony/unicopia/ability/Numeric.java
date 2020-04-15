package com.minelittlepony.unicopia.ability;

import com.google.gson.annotations.Expose;

public class Numeric implements IPower.IData {

    @Expose
    public int type;

    public Numeric(int t) {
        type = t;
    }
}
