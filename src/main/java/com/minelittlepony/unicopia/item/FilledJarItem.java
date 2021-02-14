package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.IItemEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.WorldEvent;

import net.minecraft.block.Blocks;
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
    protected void onImpact(MagicProjectileEntity projectile) {
        projectile.dropStack(getAppearanceStack(projectile.getStack()));
        WorldEvent.play(WorldEvent.DESTROY_BLOCK, projectile.world, projectile.getBlockPos(), Blocks.GLASS.getDefaultState());
    }
}
