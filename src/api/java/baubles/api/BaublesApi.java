package baubles.api;

import org.apache.commons.lang3.NotImplementedException;

import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

/*
 * stub
 */
public class BaublesApi {
	public static IBaublesItemHandler getBaublesHandler(EntityPlayer player) {
	    throw new NotImplementedException("getBaublesHandler");
	}

	public static int isBaubleEquipped(EntityPlayer player, Item bauble) {
		throw new NotImplementedException("isBaubleEquipped");
	}
}
