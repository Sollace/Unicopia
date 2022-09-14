package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public final class ThrowableSpell extends AbstractDelegatingSpell {

    private Spell spell;

    public ThrowableSpell(CustomisedSpellType<?> type) {
        super(type);
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
    protected void loadDelegates(NbtCompound compound) {
        spell = Spell.SERIALIZER.read(compound.getCompound("spell"));
    }

    @Override
    protected void saveDelegates(NbtCompound compound) {
        compound.put("spell", Spell.SERIALIZER.write(spell));
    }

    @Override
    public ThrowableSpell toThrowable() {
        return this;
    }
}
