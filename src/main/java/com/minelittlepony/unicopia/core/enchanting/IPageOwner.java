package com.minelittlepony.unicopia.core.enchanting;

import java.util.Map;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.core.network.ITransmittable;
import net.minecraft.util.Identifier;

/**
 * Interface for things that own and can unlock pages.
 *
 */
public interface IPageOwner extends ITransmittable {

    @Nonnull
    Map<Identifier, PageState> getPageStates();

    default void setPageState(IPage page, PageState state) {
        if (state == PageState.LOCKED) {
            getPageStates().remove(page.getName());
        } else {
            getPageStates().put(page.getName(), state);
        }
        sendCapabilities(true);
    }

    default PageState getPageState(IPage page) {
        return getPageStates().getOrDefault(page.getName(), page.getDefaultState());
    }
}
