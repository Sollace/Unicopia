package com.minelittlepony.unicopia;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public interface RaceKeys {
    RegistryKey<Registry<Race>> REGISTRY = Unicopia.key("race");

    RegistryKey<Race> UNSET = of("unset");
    RegistryKey<Race> HUMAN = of("human");
    RegistryKey<Race> EARTH = of("earth");
    RegistryKey<Race> UNICORN = of("unicorn");
    RegistryKey<Race> PEGASUS = of("pegasus");
    RegistryKey<Race> BAT = of("bat");
    RegistryKey<Race> ALICORN = of("alicorn");
    RegistryKey<Race> CHANGELING = of("changeling");
    RegistryKey<Race> KIRIN = of("kirin");
    RegistryKey<Race> HIPPOGRIFF = of("hippogriff");
    RegistryKey<Race> SEAPONY = of("seapony");

    static RegistryKey<Race> of(String name) {
        return Unicopia.key(REGISTRY, name);
    }
}
