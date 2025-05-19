package com.minelittlepony.unicopia.ability.magic;

import java.util.function.Function;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;

public class SpellInventory {

    private final Caster<?> owner;
    private final SpellSlots slots;

    public SpellInventory(Caster<?> owner, SpellSlots slots) {
        this.owner = owner;
        this.slots = slots;
    }

    public SpellSlots getSlots() {
        return slots;
    }

    public boolean tick(Situation situation) {
        return tick(spell -> {
            if (spell.isDying()) {
                spell.tickDying(owner);
                return Operation.ofBoolean(!spell.isDead());
            }
            return Operation.ofBoolean(spell.tick(owner, situation));
        });
    }

    public boolean tick(Function<Spell, Operation> tickAction) {
        try {
            return forEach(spell -> {
                try {
                    return tickAction.apply(spell);
                } catch (Throwable t) {
                    Unicopia.LOGGER.error("Error whilst ticking spell on entity {}", owner, t);
                }
                return Operation.REMOVE;
            });
        } catch (Exception e) {
            Unicopia.LOGGER.error("Error whilst ticking spell on entity {}", owner.asEntity(), e);
        }
        return false;
    }

    /**
     * Iterates active spells and optionally removes matching ones.
     *
     * @return True if any matching spells remain active
     */
    public boolean forEach(Function<Spell, Operation> test) {
        return slots.reduce((initial, spell) -> {
            Operation op = test.apply(spell);
            if (op == Operation.REMOVE) {
                slots.remove(spell.getUuid(), true);
            } else {
                initial |= op != Operation.SKIP;
            }
            return initial;
        });
    }

    public enum Operation {
        SKIP,
        KEEP,
        REMOVE;

        public static Operation ofBoolean(boolean result) {
            return result ? KEEP : REMOVE;
        }
    }
}
