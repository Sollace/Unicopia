package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public final class ThrowableSpell extends AbstractDelegatingSpell {

    private Spell spell;

    public ThrowableSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
    }

    public ThrowableSpell setSpell(Spell spell) {
        this.spell = spell;
        return this;
    }

    @Override
    protected Collection<Spell> getDelegates() {
        return List.of(spell);
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    public Optional<MagicProjectileEntity> throwProjectile(Caster<?> caster) {
        World world = caster.getWorld();

        LivingEntity entity = caster.getMaster();

        caster.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 0.7F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!caster.isClient()) {
            MagicProjectileEntity projectile = new MagicProjectileEntity(world, entity);

            projectile.setItem(GemstoneItem.enchant(UItems.GEMSTONE.getDefaultStack(), spell.getType()));
            projectile.getSpellSlot().put(this);
            projectile.setVelocity(entity, entity.getPitch(), entity.getYaw(), 0, 1.5F, 1);
            projectile.setHydrophobic();
            configureProjectile(projectile, caster);
            world.spawnEntity(projectile);

            return Optional.of(projectile);
        }

        return Optional.empty();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.put("spell", SpellType.toNBT(spell));
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        spell = SpellType.fromNBT(compound.getCompound("spell"));
    }

    @Override
    public ThrowableSpell toThrowable() {
        return this;
    }
}
