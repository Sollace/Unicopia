package com.minelittlepony.unicopia;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public final class Race implements Affine {
    public static final String DEFAULT_ID = "unicopia:human";
    public static final Registry<Race> REGISTRY = Registries.createDefaulted(Unicopia.id("race"), DEFAULT_ID);
    public static final RegistryKey<? extends Registry<Race>> REGISTRY_KEY = REGISTRY.getKey();

    public static Race register(String name, boolean magic, FlightType flight, boolean earth) {
        return register(Unicopia.id(name), magic, flight, earth);
    }

    public static Race register(Identifier id, boolean magic, FlightType flight, boolean earth) {
        return Registry.register(REGISTRY, id, new Race(magic, flight, earth));
    }

    public static RegistryKeyArgumentType<Race> argument() {
        return RegistryKeyArgumentType.registryKey(REGISTRY_KEY);
    }

    /**
     * The default, unset race.
     * This is used if there are no other races.
     */
    public static final Race HUMAN = register("human", false, FlightType.NONE, false);
    public static final Race EARTH = register("earth", false, FlightType.NONE, true);
    public static final Race UNICORN = register("unicorn", true, FlightType.NONE, false);
    public static final Race PEGASUS = register("pegasus", false, FlightType.AVIAN, false);
    public static final Race BAT = register("bat", false, FlightType.AVIAN, false);
    public static final Race ALICORN = register("alicorn", true, FlightType.AVIAN, true);
    public static final Race CHANGELING = register("changeling", false, FlightType.INSECTOID, false);

    public static void bootstrap() {}

    private final boolean magic;
    private final FlightType flight;
    private final boolean earth;

    Race(boolean magic, FlightType flight, boolean earth) {
        this.magic = magic;
        this.flight = flight;
        this.earth = earth;
    }

    @Override
    public Affinity getAffinity() {
        return this == CHANGELING ? Affinity.BAD : Affinity.NEUTRAL;
    }

    public boolean hasIronGut() {
        return isUsable() && this != CHANGELING;
    }

    public boolean isUsable() {
        return !isDefault();
    }

    public boolean isDefault() {
        return this == HUMAN;
    }

    public boolean isOp() {
        return this == ALICORN;
    }

    public FlightType getFlightType() {
        return flight;
    }

    public boolean canFly() {
        return !getFlightType().isGrounded();
    }

    public boolean canCast() {
        return magic;
    }

    public boolean canUseEarth() {
        return earth;
    }

    public boolean canInteractWithClouds() {
        return canFly() && this != CHANGELING && this != BAT;
    }

    public Text getDisplayName() {
        return Text.translatable(getTranslationKey());
    }

    public Text getAltDisplayName() {
        return Text.translatable(getTranslationKey() + ".alt");
    }

    public String getTranslationKey() {
        Identifier id = REGISTRY.getId(this);
        return String.format("%s.race.%s", id.getNamespace(), id.getPath().toLowerCase());
    }

    public boolean isPermitted(@Nullable PlayerEntity sender) {
        if (isOp() && (sender == null || !sender.getAbilities().creativeMode)) {
            return false;
        }

        Set<Race> whitelist = Unicopia.getConfig().speciesWhiteList.get();

        return isDefault()
                || whitelist.isEmpty()
                || whitelist.contains(this);
    }

    public Race validate(PlayerEntity sender) {
        if (!isPermitted(sender)) {
            if (this == EARTH) {
                return HUMAN;
            }

            return EARTH.validate(sender);
        }

        return this;
    }

    public boolean equals(String s) {
        return REGISTRY.getId(this).toString().equalsIgnoreCase(s)
                || getTranslationKey().equalsIgnoreCase(s);
    }

    public static Race fromName(String s, Race def) {
        if (!Strings.isNullOrEmpty(s)) {
            Identifier id = Identifier.tryParse(s);
            if (id != null) {
                if (id.getNamespace() == Identifier.DEFAULT_NAMESPACE) {
                    id = new Identifier(Unicopia.DEFAULT_NAMESPACE, id.getPath());
                }
                return REGISTRY.getOrEmpty(id).orElse(def);
            }
        }

        return def;
    }

    public static Race fromName(String name) {
        return fromName(name, EARTH);
    }
}
