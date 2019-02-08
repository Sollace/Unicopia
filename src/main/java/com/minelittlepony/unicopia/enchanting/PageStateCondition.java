package com.minelittlepony.unicopia.enchanting;

import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

public class PageStateCondition implements IUnlockCondition<IUnlockEvent> {

    ResourceLocation page;

    PageState state;

    PageStateCondition(JsonObject json) {
        require(json, "page");
        require(json, "state");

        page = new ResourceLocation(json.get("page").getAsString());
        state = PageState.of(json.get("state").getAsString());
    }

    @Override
    public boolean matches(IPageOwner owner, IUnlockEvent event) {
        IPage ipage = Pages.instance().getByName(page);

        if (ipage != null) {
            return owner.getPageState(ipage) == state;
        }

        return false;
    }
}
