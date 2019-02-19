package com.minelittlepony.unicopia.enchanting;

import java.util.Map;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.network.ITransmittable;

import net.minecraft.util.ResourceLocation;

/**
 * Interface for things that own and can unlock pages.
 *
 */
public interface IPageOwner extends ITransmittable {

    @Nonnull
    Map<ResourceLocation, PageState> getPageStates();

    default void setPageState(IPage page, PageState state) {
        if (state == PageState.LOCKED) {
            getPageStates().remove(page.getName());
        } else {
            getPageStates().put(page.getName(), state);
        }
        sendCapabilities(true);
    }

    default boolean hasPageStateRelative(IPage start, PageState state, int direction) {
        int pos = start.getIndex();

        do {
            if (getPageState(Pages.instance().getByIndex(pos)) == state) {
                return true;
            }

            pos += direction;

        } while (pos >= 0 && pos < Pages.instance().getTotalPages());

        return false;
    }

    default PageState getPageState(IPage page) {
        return getPageStates().getOrDefault(page.getName(), page.getDefaultState());
    }
}
