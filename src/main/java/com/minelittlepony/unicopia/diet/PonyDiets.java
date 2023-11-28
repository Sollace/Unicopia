package com.minelittlepony.unicopia.diet;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.diet.DietProfile.Multiplier;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

public class PonyDiets {
    private final Map<Race, DietProfile> diets;
    private final List<Effect> effects;

    static PonyDiets INSTANCE = new PonyDiets(Map.of(), List.of());

    public static PonyDiets getinstance() {
        return INSTANCE;
    }

    public static void load(PonyDiets diets) {
        INSTANCE = diets;
    }

    PonyDiets(Map<Race, DietProfile> diets, List<Effect> effects) {
        this.diets = diets;
        this.effects = effects;
    }

    public PonyDiets(PacketByteBuf buffer) {
        this(buffer.readMap(b -> b.readRegistryValue(Race.REGISTRY), DietProfile::new), buffer.readList(Effect::new));
    }

    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeMap(diets, (b, r) -> b.writeRegistryValue(Race.REGISTRY, r), (b, e) -> e.toBuffer(b));
        buffer.writeCollection(effects, (b, e) -> e.toBuffer(b));
    }

    public Optional<DietProfile> getDiet(Race race) {
        return Optional.ofNullable(diets.get(race));
    }

    public Optional<Effect> getEffects(ItemStack stack) {
        return effects.stream().filter(effect -> effect.test(stack)).findFirst();
    }

    public Optional<Effect> getEffects(ItemStack stack, Pony pony) {
        return getDiet(pony.getObservedSpecies()).flatMap(diet -> diet.findEffect(stack)).or(() -> getEffects(stack));
    }

    public Optional<Multiplier> getMultiplier(ItemStack stack, Pony pony) {
        return getDiet(pony.getObservedSpecies()).flatMap(diet -> diet.findMultiplier(stack));
    }
}
