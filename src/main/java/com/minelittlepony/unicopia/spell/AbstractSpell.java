package com.minelittlepony.unicopia.spell;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

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

	@Override
	public boolean update(Entity source) {
		return false;
	}

	@Override
	public boolean updateAt(ICaster<?> source, World w, double x, double y, double z, int level) {
		return false;
	}
}
