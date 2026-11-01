package com.minelittlepony.unicopia;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.magic.Affine;
import com.minelittlepony.unicopia.util.RegistryUtils;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public record Race (
        List<RegistryEntry<Ability<?>>> abilities,
        Affinity affinity,
        Availability availability,
        FlightType flightType,
        boolean canCast,
        boolean hasIronGut,
        boolean canUseEarth,
        boolean isNocturnal,
        boolean canHang,
        boolean isFish,
        boolean canInfluenceWeather,
        boolean canInteractWithClouds
    ) implements Affine {
    public static final Registry<Race> REGISTRY = RegistryUtils.createSynced(RaceKeys.REGISTRY, RaceKeys.UNSET.getValue());
    public static final Registry<Race> COMMAND_REGISTRY = RegistryUtils.createSynced(Unicopia.key("race/grantable"), RaceKeys.UNSET.getValue());
    private static final DynamicCommandExceptionType UNKNOWN_RACE_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("commands.race.fail", id));
    private static final Function<Race, Composite> COMPOSITES = Util.memoize(race -> new Composite(race, null, null));

    public static final Codec<Race> CODEC = REGISTRY.getCodec();
    public static final PacketCodec<RegistryByteBuf, Race> PACKET_CODEC = PacketCodecs.registryValue(RaceKeys.REGISTRY);
    public static final Codec<Race> INLINE_CODEC = RecordCodecBuilder.create(i -> i.group(
            Abilities.REGISTRY.getEntryCodec().listOf().fieldOf("abilities").forGetter(Race::abilities),
            Affinity.CODEC.fieldOf("affinity").forGetter(Race::affinity),
            Availability.CODEC.fieldOf("availability").forGetter(Race::availability),
            FlightType.CODEC.fieldOf("flight").forGetter(Race::flightType),
            Codec.BOOL.fieldOf("magic").forGetter(Race::canCast),
            Codec.BOOL.fieldOf("can_forage").forGetter(Race::hasIronGut),
            Codec.BOOL.fieldOf("earth_pony_strength").forGetter(Race::canUseEarth),
            Codec.BOOL.fieldOf("nocturnal").forGetter(Race::isNocturnal),
            Codec.BOOL.fieldOf("hanging").forGetter(Race::canHang),
            Codec.BOOL.fieldOf("aquatic").forGetter(Race::isFish),
            Codec.BOOL.fieldOf("weather_magic").forGetter(Race::canInfluenceWeather),
            Codec.BOOL.fieldOf("cloud_magic").forGetter(Race::canInteractWithClouds)
    ).apply(i, Race::new));

    /**
     * The default, unset race.
     * This is used if there are no other races.
     */
    public static final Race UNSET = register(RaceKeys.UNSET, new Builder().availability(Availability.COMMANDS));
    public static final Race HUMAN = register(RaceKeys.HUMAN, new Builder());
    public static final Race EARTH = register(RaceKeys.EARTH, new Builder().foraging().earth()
            .abilities(Abilities.HUG, Abilities.STOMP, Abilities.KICK, Abilities.GROW)
    );
    public static final Race UNICORN = register(RaceKeys.UNICORN, new Builder().foraging().magic()
            .abilities(Abilities.TELEPORT, Abilities.CAST, Abilities.GROUP_TELEPORT, Abilities.SHOOT, Abilities.DISPELL, Abilities.TELEKINESIS)
    );
    public static final Race PEGASUS = register(RaceKeys.PEGASUS, new Builder().foraging().flight(FlightType.AVIAN).weatherMagic().cloudMagic()
            .abilities(Abilities.TOGGLE_FLIGHT, Abilities.RAINBOOM, Abilities.CAPTURE_CLOUD, Abilities.CARRY)
    );
    public static final Race BAT = register(RaceKeys.BAT, new Builder().foraging().flight(FlightType.AVIAN).canHang().cloudMagic().nocturnal()
            .abilities(Abilities.TOGGLE_FLIGHT, Abilities.CARRY, Abilities.HANG, Abilities.EEEE)
    );
    public static final Race ALICORN = register(RaceKeys.ALICORN, new Builder().foraging().availability(Availability.COMMANDS).flight(FlightType.AVIAN).earth().magic().weatherMagic().cloudMagic()
            .abilities(
                    Abilities.TELEPORT, Abilities.GROUP_TELEPORT, Abilities.CAST, Abilities.SHOOT, Abilities.DISPELL, Abilities.TELEKINESIS,
                    Abilities.TOGGLE_FLIGHT, Abilities.RAINBOOM, Abilities.CAPTURE_CLOUD, Abilities.CARRY,
                    Abilities.HUG, Abilities.STOMP, Abilities.KICK, Abilities.GROW,
                    Abilities.TIME
            )
    );
    public static final Race CHANGELING = register(RaceKeys.CHANGELING, new Builder().foraging().affinity(Affinity.BAD).flight(FlightType.INSECTOID).canHang()
            .abilities(Abilities.DISPELL, Abilities.TOGGLE_FLIGHT, Abilities.FEED, Abilities.DISGUISE, Abilities.CARRY, Abilities.TELEKINESIS)
    );
    public static final Race KIRIN = register(RaceKeys.KIRIN, new Builder().foraging().magic()
            .abilities(Abilities.DISPELL, Abilities.RAGE, Abilities.NIRIK_BLAST, Abilities.KIRIN_CAST, Abilities.TELEKINESIS)
    );
    public static final Race HIPPOGRIFF = register(RaceKeys.HIPPOGRIFF, new Builder().foraging().flight(FlightType.AVIAN).cloudMagic()
            .abilities(Abilities.TOGGLE_FLIGHT, Abilities.SCREECH, Abilities.PECK, Abilities.DASH, Abilities.CARRY)
    );
    public static final Race SEAPONY = register(RaceKeys.SEAPONY, new Builder().availability(Availability.COMMANDS).foraging().fish()
            .abilities(Abilities.SONAR_PULSE)
    );

    public static void bootstrap() {}

    public Composite composite() {
        return COMPOSITES.apply(this);
    }

    public Composite composite(@Nullable Race pseudo, @Nullable Race potential) {
        return pseudo == null && potential == null ? composite() : new Composite(this, pseudo, potential);
    }

    @Override
    public Affinity getAffinity() {
        return affinity;
    }

    public boolean isUnset() {
        return this == UNSET;
    }

    public boolean isEquine() {
        return !isHuman();
    }

    public boolean isHuman() {
        return isUnset() || this == HUMAN;
    }

    public boolean isDayurnal() {
        return !isNocturnal();
    }

    public boolean canFly() {
        return !flightType().isGrounded();
    }

    public boolean hasPersistentWeatherMagic() {
        return canInfluenceWeather();
    }

    public boolean canUse(RegistryEntry<Ability<?>> ability) {
        return abilities.contains(ability);
    }

    public Identifier getId() {
        return REGISTRY.getId(this);
    }

    public RegistryKey<Race> getKey() {
        return REGISTRY.getKey(this).orElseThrow();
    }

    public Text getDisplayName() {
        return Text.translatable(getTranslationKey());
    }

    public Text getAltDisplayName() {
        return Text.translatable(getTranslationKey() + ".alt");
    }

    public String getTranslationKey() {
        return Util.createTranslationKey("race", getId());
    }

    public Identifier getIcon() {
        return getId().withPath(p -> "textures/gui/race/" + p + ".png");
    }

    public boolean isPermitted(@Nullable PlayerEntity sender) {
        return AllowList.INSTANCE.permits(this);
    }

    public Race validate(PlayerEntity sender) {
        if (!isPermitted(sender)) {
            Race alternative = this == EARTH ? HUMAN : EARTH.validate(sender);
            if (alternative != this && sender instanceof ServerPlayerEntity spe) {
                spe.sendMessageToClient(Text.translatable("respawn.reason.illegal_race", getDisplayName()), false);
            }
            return alternative;
        }

        return this;
    }

    public Race or(Race other) {
        return isUnset() ? other : this;
    }

    @Override
    public String toString() {
        return "Race{ " + getId().toString() + " }";
    }

    public boolean equals(String s) {
        return getId().toString().equalsIgnoreCase(s)
                || getTranslationKey().equalsIgnoreCase(s);
    }

    public static Race register(RegistryKey<Race> key, Builder builder) {
        Race race = Registry.register(REGISTRY, key, builder.build());
        if (race.availability().isGrantable()) {
            Registry.register(COMMAND_REGISTRY, key.getValue(), race);
        }
        return race;
    }

    public static RegistryKeyArgumentType<Race> argument() {
        return RegistryKeyArgumentType.registryKey(COMMAND_REGISTRY.getKey());
    }

    public static Race fromArgument(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        Identifier id = context.getArgument(name, RegistryKey.class).getValue();
        final Identifier idf = id;
        if (Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace()) && !REGISTRY.containsId(id)) {
            id = RaceKeys.REGISTRY.getValue().withPath(id.getPath());
        }
        return REGISTRY.getOptionalValue(id).orElseThrow(() -> UNKNOWN_RACE_EXCEPTION.create(idf));
    }

    public static Set<Race> allPermitted(PlayerEntity player) {
        return REGISTRY.stream().filter(r -> r.isPermitted(player)).collect(Collectors.toSet());
    }

    public record Composite (
            /**
             * The current reported race affected by illusions and transformation potions
             */
            Race physical,
            /**
             * Pseudo race granting additional abilities through an item or trinket
             */
            @Nullable Race pseudo,
            /**
             * The inactive (suppressed) race during an active transformation
             */
            @Nullable Race potential
        ) {
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

        public boolean canUse(RegistryEntry<Ability<?>> ability) {
            return any(r -> r.canUse(ability));
        }

        public boolean canInteractWithClouds() {
            return any(Race::canInteractWithClouds);
        }

        public boolean canInfluenceWeather() {
            return any(Race::canInfluenceWeather);
        }

        public boolean hasPersistentWeatherMagic() {
            return any(Race::hasPersistentWeatherMagic);
        }

        public FlightType flightType() {
            if (pseudo() == null) {
                return physical().flightType();
            }
            return physical().flightType().or(pseudo().flightType());
        }
    }

    public static final class Builder {
        private final List<RegistryEntry<Ability<?>>> abilities = new ArrayList<>();
        private Affinity affinity = Affinity.NEUTRAL;
        private Availability availability = Availability.DEFAULT;
        private boolean canCast;
        private boolean hasIronGut;
        private FlightType flightType = FlightType.NONE;
        private boolean canUseEarth;
        private boolean isNocturnal;
        private boolean canHang;
        private boolean isFish;
        private boolean canInfluenceWeather;
        private boolean canInteractWithClouds;

        @SafeVarargs
        public final Builder abilities(RegistryEntry<Ability<?>>...abilities) {
            this.abilities.addAll(List.of(abilities));
            return this;
        }

        public Builder foraging() {
            hasIronGut = true;
            return this;
        }

        public Builder affinity(Affinity affinity) {
            this.affinity = affinity;
            return this;
        }

        public Builder availability(Availability availability) {
            this.availability = availability;
            return this;
        }

        public Builder flight(FlightType flight) {
            flightType = flight;
            return this;
        }

        public Builder magic() {
            canCast = true;
            return this;
        }

        public Builder earth() {
            canUseEarth = true;
            return this;
        }

        public Builder nocturnal() {
            isNocturnal = true;
            return this;
        }

        public Builder canHang() {
            canHang = true;
            return this;
        }

        public Builder fish() {
            isFish = true;
            return this;
        }

        public Builder weatherMagic() {
            canInfluenceWeather = true;
            return this;
        }

        public Builder cloudMagic() {
            canInteractWithClouds = true;
            return this;
        }

        public Race build() {
            return new Race(List.copyOf(abilities), affinity, availability, flightType, canCast, hasIronGut, canUseEarth, isNocturnal, canHang, isFish, canInfluenceWeather, canInteractWithClouds);
        }
    }
}








