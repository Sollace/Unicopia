package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.PlacementControlSpell.PlacementDelegate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.server.world.Ether;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.util.math.MathHelper;

/**
 * A spell that can be attached to a specific location in the world.
 * <p>
 * The spell's effects are still powered by the casting player, so if the player dies or leaves the area, their
 * spell loses affect until they return.
 * <p>
 * When cast two copies of this spell are created. One is attached to the player and is the controlling spell,
 * the other is attached to a cast spell entity and placed in the world.
 *
 */
public class PlaceableSpell extends AbstractDelegatingSpell implements OrientedSpell {
    private int prevAge;
    private int age;

    private boolean dead;
    private int prevDeathTicks;
    private int deathTicks;

    private UUID controllingEntityUuid;
    private UUID controllingSpellUuid;

    public float pitch;
    public float yaw;

    public PlaceableSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    PlaceableSpell(Caster<?> caster, PlacementControlSpell control, Spell delegate) {
        this(SpellType.PLACED_SPELL.withTraits());
        this.controllingEntityUuid = caster.asEntity().getUuid();
        this.controllingSpellUuid = control.getUuid();
        this.delegate.set(delegate);

        if (delegate instanceof PlacementDelegate s) {
            s.onPlaced(caster, control);
        }
    }

    @Override
    public boolean apply(Caster<?> caster) {
        boolean result = super.apply(caster);
        if (result && !caster.isClient()) {
            Ether.get(caster.asWorld()).getOrCreate(this, caster);
        }
        setDirty();
        return result;
    }

    public float getAge(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevAge, age);
    }

    public float getScale(float tickDelta) {
        float add = MathHelper.clamp(getAge(tickDelta) / 25F, 0, 1);
        float subtract = dead ? 1 - (MathHelper.lerp(tickDelta, prevDeathTicks, deathTicks) / 20F) : 0;
        return MathHelper.clamp(add - subtract, 0, 1);
    }

    @Override
    public boolean isDying() {
        return dead && deathTicks > 0 || super.isDying();
    }

    @Override
    public void setDead() {
        super.setDead();
        dead = true;
        deathTicks = 20;
        setDirty();
    }

    @Override
    public boolean isDead() {
        return dead && deathTicks <= 0 && super.isDead();
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!source.isClient() && !checkConnection(source)) {
            setDead();
            return false;
        }

        prevAge = age;
        if (age < 25) {
            age++;
        }

        return super.tick(source, Situation.GROUND);
    }

    private boolean checkConnection(Caster<?> source) {
        return Ether.get(source.asWorld()).get(SpellType.PLACE_CONTROL_SPELL, controllingEntityUuid, controllingSpellUuid) != null;
    }

    @Override
    public void tickDying(Caster<?> caster) {
        super.tickDying(caster);
        prevDeathTicks = deathTicks;
        deathTicks--;
    }

    @Override
    protected void onDestroyed(Caster<?> source) {
        if (!source.isClient()) {
            Ether.get(source.asWorld()).remove(this, source);
        }
        super.onDestroyed(source);
    }

    @Override
    public void setOrientation(Caster<?> caster, float pitch, float yaw) {
        this.pitch = -pitch - 90;
        this.yaw = -yaw;
        Entity entity = caster.asEntity();
        entity.updatePositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), this.yaw, this.pitch);
        entity.setYaw(this.yaw);
        entity.setPitch(this.pitch);
        setDirty();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putBoolean("dead", dead);
        compound.putInt("deathTicks", deathTicks);
        compound.putInt("age", age);
        compound.putFloat("pitch", pitch);
        compound.putFloat("yaw", yaw);
        if (controllingEntityUuid != null) {
            compound.putUuid("owningEntity", controllingEntityUuid);
        }
        if (controllingSpellUuid != null) {
            compound.putUuid("owningSpell", controllingSpellUuid);
        }
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        dead = compound.getBoolean("dead");
        deathTicks = compound.getInt("deathTicks");
        age = compound.getInt("age");
        controllingEntityUuid = compound.containsUuid("owningEntity") ? compound.getUuid("owningEntity") : null;
        controllingSpellUuid = compound.containsUuid("owningSpell") ? compound.getUuid("owningSpell") : null;
        pitch = compound.getFloat("pitch");
        yaw = compound.getFloat("yaw");
    }
}
