package baubles.api.cap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/*
 * stub
 */
public interface IBaublesItemHandler extends IItemHandlerModifiable {
	boolean isItemValidForSlot(int slot, ItemStack stack, EntityLivingBase player);
}
