package com.minelittlepony.unicopia.redux.enchanting;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface IConditionFactory {
    IUnlockCondition<?> create(JsonObject json);
}
