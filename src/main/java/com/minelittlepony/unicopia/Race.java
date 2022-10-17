package com.minelittlepony.unicopia;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import com.google.common.base.Strings;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.util.Registries;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public final class Race implements Affine {
    public static final String DEFAULT_ID = "unicopia:human";
    public static final Registry<Race> REGISTRY = Registries.createDefaulted(Unicopia.id("race"), DEFAULT_ID);
    public static final RegistryKey<? extends Registry<Race>> REGISTRY_KEY = REGISTRY.getKey();
    private static final DynamicCommandExceptionType UNKNOWN_RACE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("race.unknown", id));

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

        return isDefault()
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
}








