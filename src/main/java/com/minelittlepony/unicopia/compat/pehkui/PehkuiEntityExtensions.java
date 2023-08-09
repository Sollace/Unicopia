package com.minelittlepony.unicopia.compat.pehkui;

import java.util.Map;

public interface PehkuiEntityExtensions {
    @SuppressWarnings("rawtypes")
    default Map pehkui_getScales() {
        return Map.of();
    }
}
