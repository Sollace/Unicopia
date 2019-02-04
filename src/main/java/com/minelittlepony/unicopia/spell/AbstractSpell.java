package com.minelittlepony.unicopia.spell;

public abstract class AbstractSpell implements IMagicEffect {

	protected boolean isDead = false;

    @Override
    public int getCurrentLevel() {
        return 0;
    }

    @Override
    public void setCurrentLevel(int level) {

    }

    @Override
    public boolean isCraftable() {
        return true;
    }

	@Override
	public void setDead() {
		isDead = true;
	}

	@Override
	public boolean getDead() {
		return isDead;
	}
}
