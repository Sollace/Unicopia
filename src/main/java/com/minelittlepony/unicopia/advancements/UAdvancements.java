package com.minelittlepony.unicopia.advancements;

import net.minecraft.advancements.CriteriaTriggers;

public class UAdvancements {
    public static final BOHDeathTrigger BOH_DEATH = new BOHDeathTrigger();

    public static void init() {
        CriteriaTriggers.register(BOH_DEATH);
    }
}
