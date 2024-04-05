package com.minelittlepony.unicopia.diet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.effect.FoodPoisoningStatusEffect;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.ItemDuck;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PonyDiets implements DietView {
    private final Map<Race, DietProfile> diets;
    private final Map<Identifier, Effect> effects;

    private static PonyDiets INSTANCE = new PonyDiets(Map.of(), Map.of());

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

    PonyDiets(Map<Race, DietProfile> diets, Map<Identifier, Effect> effects) {
        this.diets = diets;
        this.effects = effects;
    }

    public PonyDiets(PacketByteBuf buffer) {
        this(
                buffer.readMap(b -> b.readRegistryValue(Race.REGISTRY), DietProfile::new),
                buffer.readCollection(ArrayList::new, FoodGroup::new).stream().collect(Collectors.toMap(FoodGroup::id, Function.identity()))
        );
    }

    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeMap(diets, (b, r) -> b.writeRegistryValue(Race.REGISTRY, r), (b, e) -> e.toBuffer(b));
        buffer.writeCollection(effects.values(), (b, e) -> e.toBuffer(b));
    }

    private DietProfile getDiet(Pony pony) {
        return Optional.ofNullable(diets.get(pony.getObservedSpecies())).orElse(DietProfile.EMPTY);
    }

    Effect getEffects(ItemStack stack) {
        return effects.values().stream().filter(effect -> effect.test(stack)).findFirst().orElse(Effect.EMPTY);
    }

    private Effect getEffects(ItemStack stack, Pony pony) {
        return getDiet(pony).findEffect(stack).orElseGet(() -> getEffects(stack));
    }

    @Override
    public TypedActionResult<ItemStack> startUsing(ItemStack stack, World world, PlayerEntity user, Hand hand) {
        return initEdibility(stack, user)
                ? FoodPoisoningStatusEffect.apply(stack, user)
                : TypedActionResult.fail(stack);
    }

    @Override
    public void finishUsing(ItemStack stack, World world, LivingEntity entity) {
        if (initEdibility(stack, entity)) {
            Pony.of(entity).ifPresent(pony -> getEffects(stack, pony).ailment().effects().afflict(pony.asEntity(), stack));
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable PlayerEntity user, List<Text> tooltip, TooltipContext context) {

        if (initEdibility(stack, user)) {
            if (!((ItemDuck)stack.getItem()).getOriginalFoodComponent().isEmpty() || stack.getItem().getFoodComponent() != null) {
                Pony pony = Pony.of(user);

                tooltip.add(Text.translatable("unicopia.diet.information").formatted(Formatting.DARK_PURPLE));
                getEffects(stack, pony).appendTooltip(stack, tooltip, context);
                getDiet(pony).appendTooltip(stack, user, tooltip, context);
            }
        }
    }

    private boolean initEdibility(ItemStack stack, LivingEntity user) {
        ItemDuck item = (ItemDuck)stack.getItem();
        item.resetFoodComponent();
        return Pony.of(user).filter(pony -> {
            DietProfile diet = getDiet(pony);

            if (!stack.isFood() && pony.getObservedSpecies().hasIronGut()) {
                diet.findEffect(stack)
                    .flatMap(Effect::foodComponent)
                    .or(() -> getEffects(stack).foodComponent())
                    .ifPresent(item::setFoodComponent);
            }

            if (stack.isFood()) {
                item.setFoodComponent(diet.getAdjustedFoodComponent(stack));
            }

            return true;
        }).isPresent();
    }
}
