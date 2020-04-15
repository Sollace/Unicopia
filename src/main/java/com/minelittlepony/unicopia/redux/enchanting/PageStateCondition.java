package com.minelittlepony.unicopia.redux.enchanting;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.core.enchanting.Page;
import com.minelittlepony.unicopia.core.enchanting.IPageOwner;
import com.minelittlepony.unicopia.core.enchanting.IUnlockEvent;
import com.minelittlepony.unicopia.core.enchanting.PageState;

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
