package baubles.api;

import org.apache.commons.lang3.NotImplementedException;

/*
 * stub
 */
public enum BaubleType {
	AMULET, RING, BELT, TRINKET, HEAD, BODY, CHARM;

	public int[] getValidSlots() {
	    throw new NotImplementedException("getValidSlots");
	}
}
