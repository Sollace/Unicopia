package com.minelittlepony.unicopia.core.magic;

import java.util.function.Supplier;

public class GenericSpell extends AbstractSpell {

    private final String name;

    private final int tint;

    private final Affinity affinity;

    static Supplier<IMagicEffect> factory(String name, int tint, Affinity affinity) {
        return () -> new GenericSpell(name, tint, affinity);
    }

    public GenericSpell(String name, int tint, Affinity affinity) {
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
        // TODO:
        // source.spawnParticles(UParticles.UNICORN_MAGIC, 1, getTint());
    }

    @Override
    public Affinity getAffinity() {
        return affinity;
    }
}
