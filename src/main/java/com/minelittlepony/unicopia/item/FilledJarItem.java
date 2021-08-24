package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.IItemEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.WorldEvent;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;

public class FilledJarItem extends JarItem implements ChameleonItem {

    public FilledJarItem(Settings settings) {
        super(settings, false, false, false);
    }

    @Override
    public Text getName(ItemStack stack) {
        return hasAppearance(stack) ? new TranslatableText(getTranslationKey(stack), getAppearanceStack(stack).getName()) : UItems.EMPTY_JAR.getName(UItems.EMPTY_JAR.getDefaultStack());
    }

    @Override
    public boolean isFullyDisguised() {
        return false;
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        return ActionResult.PASS;
    }

    @Override
    protected float getProjectileDamage(ItemStack stack) {
        stack = getAppearanceStack(stack);

        EntityAttributeInstance instance = new EntityAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE, i -> {});

        stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(EntityAttributes.GENERIC_ATTACK_DAMAGE).forEach(modifier -> {
            instance.addTemporaryModifier(modifier);
        });

        return (float)instance.getValue();
    }

    @Override
    protected void onImpact(MagicProjectileEntity projectile) {
        ItemStack stack = getAppearanceStack(projectile.getStack());
        stack.damage(1, projectile.world.random, null);
        projectile.dropStack(stack);
        WorldEvent.play(WorldEvent.DESTROY_BLOCK, projectile.world, projectile.getBlockPos(), Blocks.GLASS.getDefaultState());
    }

    public ItemStack withContents(ItemStack contents) {
        return setAppearance(getDefaultStack(), contents);
    }
}
