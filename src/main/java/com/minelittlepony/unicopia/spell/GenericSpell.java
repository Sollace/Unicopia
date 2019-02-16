package com.minelittlepony.unicopia.spell;

import java.util.function.Supplier;

import com.minelittlepony.unicopia.init.UParticles;

public class GenericSpell extends AbstractSpell {

    private final String name;

    private final int tint;

    private final SpellAffinity affinity;

    static Supplier<IMagicEffect> factory(String name, int tint, SpellAffinity affinity) {
        return () -> new GenericSpell(name, tint, affinity);
    }

    public GenericSpell(String name, int tint, SpellAffinity affinity) {
        this.name = name;
        this.tint = tint;
        this.affinity = affinity;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getTint() {
        return tint;
    }

    @Override
    public boolean update(ICaster<?> source) {
        return true;
    }

    @Override
    public void render(ICaster<?> source) {
        source.spawnParticles(UParticles.UNICORN_MAGIC, 1);
    }

    @Override
    public SpellAffinity getAffinity() {
        return affinity;
    }
}
