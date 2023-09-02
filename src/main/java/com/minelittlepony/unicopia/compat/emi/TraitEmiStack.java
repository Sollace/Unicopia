package com.minelittlepony.unicopia.compat.emi;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.client.gui.Tooltip;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.gui.ItemTraitsTooltipRenderer;

import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TraitEmiStack extends EmiStack {

    private final Trait trait;
    private final float amount;
    private final SpellTraits traits;

    public TraitEmiStack(Trait trait, float amount) {
        this.trait = trait;
        this.amount = amount;
        traits = new SpellTraits.Builder().with(trait, amount).build();
    }

    @Override
    public boolean isEmpty() {
        return amount == 0;
    }

    @Nullable
    @Override
    public NbtCompound getNbt() {
        return null;
    }

    @Override
    public Object getKey() {
        return trait;
    }

    @Override
    public Identifier getId() {
        return trait.getId();
    }

    @Override
    public List<Text> getTooltipText() {
        return trait.getTooltipLines();
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        List<TooltipComponent> list = Lists.newArrayList();
        if (!isEmpty()) {
            for (Text line : Tooltip.of(trait.getTooltip(), 200).getLines()) {
                list.add(TooltipComponent.of(line.asOrderedText()));
            }
            list.addAll(super.getTooltip());
        }
        return list;
    }

    @Override
    public Text getName() {
        return trait.getName();
    }

    @Override
    public void render(DrawContext context, int x, int y, float delta, int flags) {
        if ((flags & RENDER_ICON) != 0) {
            List<Item> knownItems = trait.getItems();
            if (knownItems.isEmpty() || MinecraftClient.getInstance().player == null) {
                ItemTraitsTooltipRenderer.renderTraitIcon(trait, amount, context, x, y, true);
            } else {
                int tick = (MinecraftClient.getInstance().player.age / 12) % knownItems.size();
                ItemStack stack = knownItems.get(tick).getDefaultStack();
                EmiStack.of(stack).render(context, x, y, delta, flags);
                ItemTraitsTooltipRenderer.renderStackTraits(traits, context, x, y, 1, delta, 0, true);
            }
        }

        if ((flags & RENDER_REMAINDER) != 0) {
            EmiRender.renderRemainderIcon(this, context, x, y);
        }
    }

    @Override
    public EmiStack copy() {
        return new TraitEmiStack(trait, amount);
    }

    @Override
    public boolean isEqual(EmiStack stack) {
        return super.isEqual(stack) && equalTo(stack);
    }

    @Override
    public boolean isEqual(EmiStack stack, Comparison comparison) {
        return super.isEqual(stack, comparison) && equalTo(stack);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof EmiStack s && equalTo(s);
    }

    private boolean equalTo(EmiStack stack) {
        return stack instanceof TraitEmiStack t && t.trait == trait && MathHelper.approximatelyEquals(t.amount, amount);
    }
}
