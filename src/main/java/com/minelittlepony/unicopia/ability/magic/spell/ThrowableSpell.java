package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
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
    public Collection<Spell> getDelegates() {
        return List.of(spell);
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    public Optional<MagicProjectileEntity> throwProjectile(Caster<?> caster) {
        return throwProjectile(caster, 1);
    }

    /**
     * Projects this spell.
     *
     * Returns the resulting projectile entity for customization (or null if on the client).
     */
    public Optional<MagicProjectileEntity> throwProjectile(Caster<?> caster, float divergance) {
        World world = caster.getReferenceWorld();

        LivingEntity entity = caster.getMaster();

        if (entity == null) {
            return Optional.empty();
        }

        caster.playSound(USounds.SPELL_CAST_SHOOT, 0.7F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!caster.isClient()) {
            MagicProjectileEntity projectile = UEntities.MAGIC_BEAM.create(world);
            projectile.setPosition(entity.getX(), entity.getEyeY() - 0.1F, entity.getZ());
            projectile.setOwner(entity);
            projectile.setItem(GemstoneItem.enchant(UItems.GEMSTONE.getDefaultStack(), spell.getType()));
            projectile.getSpellSlot().put(spell);
            projectile.setVelocity(entity, entity.getPitch(), entity.getYaw(), 0, 1.5F, divergance);
            projectile.setNoGravity(true);
            configureProjectile(projectile, caster);
            world.spawnEntity(projectile);

            return Optional.of(projectile);
        }

        return Optional.empty();
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.put("spell", Spell.writeNbt(spell));
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        spell = Spell.readNbt(compound.getCompound("spell"));
    }

    @Override
    public ThrowableSpell toThrowable() {
        return this;
    }
}
