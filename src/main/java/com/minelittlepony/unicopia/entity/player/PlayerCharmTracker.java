package com.minelittlepony.unicopia.entity.player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Hand;

public class PlayerCharmTracker implements NbtSerialisable, Copyable<PlayerCharmTracker> {
    private static final Codec<List<CustomisedSpellType<?>>> HAND_SPELLS_CODEC = CustomisedSpellType.CODEC.listOf();

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

    public Hand getHand() {
        return pony.asEntity().isSneaking() ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    public TypedActionResult<CustomisedSpellType<?>> getSpellInHand(boolean consume) {
        return getSpellInHand(getHand(), consume);
    }

    public TypedActionResult<CustomisedSpellType<?>> getSpellInHand(Hand hand, boolean consume) {
        return Stream.of(pony.asEntity().getMainHandStack(), pony.asEntity().getOffHandStack())
                .filter(EnchantableItem::isEnchanted)
                .map(stack -> EnchantableItem.consumeSpell(stack, pony.asEntity(), null, consume))
                .findFirst()
                .orElse(getEquippedSpell(hand).toAction());
    }

    public CustomisedSpellType<?> equipSpell(Hand hand, CustomisedSpellType<?> spell) {
        CustomisedSpellType<?> previous = handSpells[hand.ordinal()];
        handSpells[hand.ordinal()] = spell;
        if (!Objects.equals(previous, spell)) {
            pony.asEntity().playSound(USounds.GUI_SPELL_EQUIP.value(), 0.25F, 1.75F);
        }
        pony.sendUpdatePacket();
        return previous;
    }

    @Override
    public void copyFrom(PlayerCharmTracker old, boolean alive) {
        for (int i = 0; i < handSpells.length; i++) {
            handSpells[i] = old.handSpells[i];
        }
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.put("handSpells", HAND_SPELLS_CODEC, Arrays.asList(handSpells));
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.get("handSpells", HAND_SPELLS_CODEC).ifPresent(list -> {
            for (int i = 0; i < handSpells.length && i < list.size(); i++) {
                handSpells[i] = list.get(i);
            }
        });
    }
}
