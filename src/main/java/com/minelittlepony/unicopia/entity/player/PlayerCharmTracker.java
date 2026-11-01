package com.minelittlepony.unicopia.entity.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.util.Copyable;
import com.minelittlepony.unicopia.util.TypedActionResult;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Hand;
import net.minecraft.util.dynamic.Codecs;

public class PlayerCharmTracker implements NbtSerialisable, Copyable<PlayerCharmTracker> {
    private static final Codec<List<Optional<CustomisedSpellType<?>>>> HAND_SPELLS_CODEC = Codecs.optional(CustomisedSpellType.CODEC.codec()).listOf(2, 2);

    private final Pony pony;

    private final List<Optional<CustomisedSpellType<?>>> handSpells = new ArrayList<>(List.of(
        Optional.<CustomisedSpellType<?>>empty(),
        Optional.<CustomisedSpellType<?>>empty()
    ));

    PlayerCharmTracker(Pony pony) {
        this.pony = pony;
    }

    public CustomisedSpellType<?> getStaffSpell(boolean consume) {
        var result = handSpells.get(0).orElseGet(() -> pony.getSpecies().staffSpell());
        if (consume) {
            handSpells.set(0, Optional.empty());
        }
        return result;
    }

    public CustomisedSpellType<?> getEquippedSpell(Hand hand) {
        return handSpells.get(hand.ordinal()).orElseGet(() -> hand == Hand.MAIN_HAND ? pony.getSpecies().mainHandSpell() : pony.getSpecies().offHandSpell());
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

    public CustomisedSpellType<?> equipSpell(Hand hand, @Nullable CustomisedSpellType<?> spell) {
        if (spell == null) {
            spell = CustomisedSpellType.empty();
        }
        CustomisedSpellType<?> previous = getEquippedSpell(hand);
        handSpells.set(hand.ordinal(), Optional.of(spell));
        if (!Objects.equals(previous, spell)) {
            pony.asEntity().playSound(USounds.GUI_SPELL_EQUIP.value(), 0.25F, 1.75F);
        }
        pony.sendUpdatePacket();
        return previous;
    }

    @Override
    public void copyFrom(PlayerCharmTracker old, boolean alive) {
        handSpells.clear();
        handSpells.addAll(old.handSpells);
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.put("handSpells", HAND_SPELLS_CODEC, handSpells);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        compound.get("handSpells", HAND_SPELLS_CODEC).ifPresent(list -> {
            handSpells.clear();
            handSpells.addAll(list);
        });
    }
}
