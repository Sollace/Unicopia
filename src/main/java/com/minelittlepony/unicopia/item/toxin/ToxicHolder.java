package com.minelittlepony.unicopia.item.toxin;

import java.util.Optional;

public interface ToxicHolder {
    default Optional<Toxic> getToxic() {
        return Optional.empty();
    }
}
