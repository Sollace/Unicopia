package com.minelittlepony.unicopia.compat.tla;

import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;
import com.minelittlepony.unicopia.container.inventory.HexagonalCraftingGrid;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.mattidragon.tlaapi.api.gui.GuiBuilder;
import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import net.minecraft.client.MinecraftClient;

public record TraitedTlaIngredient(Optional<TraitEntry> trait, TlaIngredient ingredient) {

    public static TraitedTlaIngredient of(Trait trait, float amount) {
        return of(List.of(trait), amount);
    }

    public static TraitedTlaIngredient of(List<Trait> traits, float amount) {
        return new TraitedTlaIngredient(Optional.of(new TraitEntry(traits, amount)),
                TlaIngredient.ofItems(traits.stream().flatMap(SpellTraits::getItems).distinct().toList())
        );
    }

    public static TraitedTlaIngredient of(TlaIngredient ingredient) {
        return new TraitedTlaIngredient(Optional.empty(), ingredient);
    }

    public void buildGui(HexagonalCraftingGrid.Slot slot, GuiBuilder builder) {
        buildGui(ingredient(), slot, builder);
    }

    public void buildGui(TlaIngredient ingredientOverride, HexagonalCraftingGrid.Slot slot, GuiBuilder builder) {
        builder.addCustomWidget(slot.left() - 7, slot.top() - 7, 32, 32, (context, mouseX, mouseY, delta) -> {
            RenderSystem.enableBlend();
            context.drawTexture(SpellbookScreen.SLOT, 0, 0, 0, 0, 0, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        });
        builder.addSlot(ingredientOverride, slot.left(), slot.top()).disableBackground();
        trait.ifPresent(traitEntry -> {
            builder.addCustomWidget(slot.left(), slot.top(), 16, 16, (context, mouseX, mouseY, delta) -> {
                int tick = (MinecraftClient.getInstance().player.age / 12) % traitEntry.traits().size();
                Trait currentDisplayedTrait = traitEntry.traits().get(tick);
                if (currentDisplayedTrait.getItems().isEmpty() || MinecraftClient.getInstance().player == null) {
                    ItemTraitsTooltipRenderer.renderTraitIcon(currentDisplayedTrait, traitEntry.amount(), context, 0, 0, true);
                } else {
                    ItemTraitsTooltipRenderer.renderStackSingleTrait(currentDisplayedTrait, traitEntry.amount(), context, 0, 0, 1, delta, 0, true);
                }
            });
        });
    }

    record TraitEntry(List<Trait> traits, float amount) { }
}
