package com.minelittlepony.unicopia.enchanting;

import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

public class PageStateCondition implements IUnlockCondition<IUnlockEvent> {

    Identifier page;

    PageState state;

    PageStateCondition(JsonObject json) {
        require(json, "page");
        require(json, "state");

        page = new Identifier(json.get("page").getAsString());
        state = PageState.of(json.get("state").getAsString());
    }

    @Override
    public boolean matches(IPageOwner owner, IUnlockEvent event) {
        Page ipage = Pages.instance().getByName(page);

        if (ipage != null) {
            return owner.getPageState(ipage) == state;
        }

        return false;
    }
}
