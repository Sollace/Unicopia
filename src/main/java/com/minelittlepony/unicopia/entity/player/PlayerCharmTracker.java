package com.minelittlepony.unicopia.entity.player;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public class PlayerCharmTracker implements NbtSerialisable {

    private final Pony pony;

    private CustomisedSpellType<?>[] handSpells = new CustomisedSpellType<?>[] {
        SpellType.SHIELD.withTraits(),
        SpellType.CATAPULT.withTraits()
    };

    PlayerCharmTracker(Pony pony) {
        this.pony = pony;
    }

    public CustomisedSpellType<?>[] getHandSpells() {
        return handSpells;
    }

    public CustomisedSpellType<?> getEquippedSpell(Hand hand) {
        return handSpells[hand.ordinal()] == null ? SpellType.EMPTY_KEY.withTraits() : handSpells[hand.ordinal()];
    }

    public TypedActionResult<CustomisedSpellType<?>> getSpellInHand(Hand hand) {
        return Streams.stream(pony.getMaster().getHandItems())
                .filter(GemstoneItem::isEnchanted)
                .map(stack -> GemstoneItem.consumeSpell(stack, pony.getMaster(), null))
                .findFirst()
                .orElse(getEquippedSpell(hand).toAction());
    }

    public void equipSpell(Hand hand, CustomisedSpellType<?> spell) {
        handSpells[hand.ordinal()] = spell;
        pony.getMaster().playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.25F, 1.75F);
        pony.setDirty();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        NbtList equippedSpells = new NbtList();
        for (CustomisedSpellType<?> spell : handSpells) {
            equippedSpells.add(spell.toNBT());
        }
        compound.put("handSpells", equippedSpells);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        if (compound.contains("handSpells", NbtElement.LIST_TYPE)) {
            NbtList list = compound.getList("handSpells", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < handSpells.length && i < list.size(); i++) {
                handSpells[i] = CustomisedSpellType.fromNBT(list.getCompound(i));
            }
        }
    }
}
