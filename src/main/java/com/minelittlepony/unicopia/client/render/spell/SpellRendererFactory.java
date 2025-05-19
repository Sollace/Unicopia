package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;

public interface SpellRendererFactory<T extends Spell> {
    SpellRenderer<T> create();

    static void bootstrap() {

    }
}
