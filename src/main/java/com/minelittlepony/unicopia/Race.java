package com.minelittlepony.unicopia;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public record Race (Supplier<Composite> compositeSupplier, boolean canCast, FlightType flightType, boolean canUseEarth, boolean isNocturnal, boolean canHang) implements Affine {
    public static final String DEFAULT_ID = "unicopia:unset";
    public static final Registry<Race> REGISTRY = RegistryUtils.createDefaulted(Unicopia.id("race"), DEFAULT_ID);
    public static final RegistryKey<? extends Registry<Race>> REGISTRY_KEY = REGISTRY.getKey();
    private static final DynamicCommandExceptionType UNKNOWN_RACE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("race.unknown", id));

    public static Race register(String name, boolean magic, FlightType flight, boolean earth, boolean nocturnal, boolean canHang) {
        return register(Unicopia.id(name), magic, flight, earth, nocturnal, canHang);
    }

    public static Race register(Identifier id, boolean magic, FlightType flight, boolean earth, boolean nocturnal, boolean canHang) {
        return Registry.register(REGISTRY, id, new Race(Suppliers.memoize(() -> new Composite(REGISTRY.get(id), null)), magic, flight, earth, nocturnal, canHang));
    }

    public static RegistryKeyArgumentType<Race> argument() {
        return RegistryKeyArgumentType.registryKey(REGISTRY_KEY);
    }

    /**
     * The default, unset race.
     * This is used if there are no other races.
     */
    public static final Race UNSET = register("unset", false, FlightType.NONE, false, false, false);
    public static final Race HUMAN = register("human", false, FlightType.NONE, false, false, false);
    public static final Race EARTH = register("earth", false, FlightType.NONE, true, false, false);
    public static final Race UNICORN = register("unicorn", true, FlightType.NONE, false, false, false);
    public static final Race PEGASUS = register("pegasus", false, FlightType.AVIAN, false, false, false);
    public static final Race BAT = register("bat", false, FlightType.AVIAN, false, true, true);
    public static final Race ALICORN = register("alicorn", true, FlightType.AVIAN, true, false, false);
    public static final Race CHANGELING = register("changeling", false, FlightType.INSECTOID, false, false, true);

    public static void bootstrap() {}

    public Composite composite() {
        return compositeSupplier.get();
    }

    public Composite composite(@Nullable Race pseudo) {
        return pseudo == null ? composite() : new Composite(this, pseudo);
    }

    @Override
    public Affinity getAffinity() {
        return this == CHANGELING ? Affinity.BAD : Affinity.NEUTRAL;
    }

    public boolean hasIronGut() {
        return !isHuman() && this != CHANGELING;
    }

    public boolean isUnset() {
        return this == UNSET;
    }

    public boolean isEquine() {
        return !isHuman();
    }

    public boolean isHuman() {
        return this == UNSET || this == HUMAN;
    }

    public boolean isDayurnal() {
        return !isNocturnal();
    }

    public boolean isOp() {
        return this == ALICORN;
    }

    public boolean canFly() {
        return !flightType().isGrounded();
    }

    public boolean canInteractWithClouds() {
        return canFly() && this != CHANGELING && this != BAT;
    }

    public Identifier getId() {
        Identifier id = REGISTRY.getId(this);
        return id;
    }

    public Text getDisplayName() {
        return Text.translatable(getTranslationKey());
    }

    public Text getAltDisplayName() {
        return Text.translatable(getTranslationKey() + ".alt");
    }

    public String getTranslationKey() {
        Identifier id = getId();
        return String.format("%s.race.%s", id.getNamespace(), id.getPath().toLowerCase());
    }

    public Identifier getIcon() {
        Identifier id = getId();
        return new Identifier(id.getNamespace(), "textures/gui/race/" + id.getPath() + ".png");
    }

    public boolean isPermitted(@Nullable PlayerEntity sender) {
        if (isOp() && (sender == null || !sender.getAbilities().creativeMode)) {
            return false;
        }

        Set<String> whitelist = Unicopia.getConfig().speciesWhiteList.get();

        return isUnset()
                || whitelist.isEmpty()
                || whitelist.contains(getId().toString());
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

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Race race && Objects.equal(race.getId(), getId());
    }

    @Override
    public String toString() {
        return "Race{ " + getId().toString() + " }";
    }

    public boolean equals(String s) {
        return getId().toString().equalsIgnoreCase(s)
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

    public static Race fromArgument(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier id = context.getArgument(name, RegistryKey.class).getValue();
        return REGISTRY.getOrEmpty(id).orElseThrow(() -> UNKNOWN_RACE_EXCEPTION.create(id));
    }

    public static Set<Race> allPermitted(PlayerEntity player) {
        return REGISTRY.stream().filter(r -> r.isPermitted(player)).collect(Collectors.toSet());
    }

    public record Composite (Race physical, @Nullable Race pseudo) {
        public Race collapsed() {
            return pseudo == null ? physical : pseudo;
        }

        public boolean includes(Race race) {
            return physical == race || pseudo == race;
        }

        public boolean any(Predicate<Race> test) {
            return test.test(physical) || (pseudo != null && test.test(pseudo));
        }

        public boolean canUseEarth() {
            return any(Race::canUseEarth);
        }

        public boolean canFly() {
            return any(Race::canFly);
        }

        public boolean canCast() {
            return any(Race::canCast);
        }
    }
}








