package com.minelittlepony.unicopia;

import java.util.Set;

public record SyncedConfig (
    Set<String> wantItNeedItExcludeList,
    Set<String> dimensionsWithoutAtmosphere) {
}
