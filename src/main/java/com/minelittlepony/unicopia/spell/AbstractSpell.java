package com.minelittlepony.unicopia.spell;

public abstract class AbstractSpell implements IMagicEffect {

	protected boolean isDead = false;

	@Override
	public void setDead() {
		isDead = true;
	}

	@Override
	public boolean getDead() {
		return isDead;
	}
}
