package com.minelittlepony.unicopia.diet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.effect.FoodPoisoningStatusEffect;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PonyDiets implements DietView {
    private final Map<Race, DietProfile> diets;
    private final Map<Identifier, FoodGroup> effects;

    private static PonyDiets INSTANCE = new PonyDiets(Map.of(), Map.of());

    public static final PacketCodec<RegistryByteBuf, PonyDiets> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(HashMap::new, PacketCodecs.registryValue(Race.REGISTRY_KEY), DietProfile.PACKET_CODEC), diets -> diets.diets,
            FoodGroup.PACKET_CODEC.collect(PacketCodecUtils.toMap(FoodGroup::id)), diets -> diets.effects,
            PonyDiets::new
    );

    public static PonyDiets getInstance() {
        return INSTANCE;
    }

    @Nullable
    static Effect getEffect(Identifier id) {
        return INSTANCE.effects.get(id);
    }

    public static void load(PonyDiets diets) {
        INSTANCE = diets;
    }

    PonyDiets(Map<Race, DietProfile> diets, Map<Identifier, FoodGroup> effects) {
        this.diets = diets;
        this.effects = effects;
    }

    public DietProfile getDiet(Pony pony) {
        return Optional.ofNullable(diets.get(pony.getObservedSpecies())).orElse(DietProfile.EMPTY);
    }

    public Effect getEffects(ItemStack stack) {
        return effects.values().stream().filter(effect -> effect.test(stack)).findFirst().map(Effect.class::cast).orElse(Effect.EMPTY);
    }

    private Effect getEffects(ItemStack stack, Pony pony) {
        return getDiet(pony).findEffect(stack).orElseGet(() -> getEffects(stack));
    }

    @Override
    public TypedActionResult<ItemStack> startUsing(ItemStack stack, World world, PlayerEntity user, Hand hand) {
        return FoodPoisoningStatusEffect.apply(stack, user);
    }

    @Override
    public void finishUsing(ItemStack stack, World world, LivingEntity entity) {
        Pony.of(entity).ifPresent(pony -> getEffects(stack, pony).ailment().effects().afflict(pony.asEntity(), stack));
    }
}
