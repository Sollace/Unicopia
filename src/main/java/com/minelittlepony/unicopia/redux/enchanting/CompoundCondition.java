package com.minelittlepony.unicopia.redux.enchanting;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.core.enchanting.IPageOwner;
import com.minelittlepony.unicopia.core.enchanting.IUnlockEvent;

public class CompoundCondition implements IUnlockCondition<IUnlockEvent> {

    final Op operation;

    final List<IUnlockCondition<IUnlockEvent>> conditions = Lists.newArrayList();

    CompoundCondition(JsonObject json) {
        require(json, "operation");
        require(json, "conditions");

        operation = Op.valueOf(json.get("operation").getAsString().toUpperCase());

        json.get("conditions").getAsJsonArray().forEach(this::addElement);
    }

    void addElement(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();

        conditions.add(Pages.instance().createCondition(obj));
    }

    @Override
    public boolean accepts(IUnlockEvent event) {
        return true;
    }

    @Override
    public boolean matches(IPageOwner owner, IUnlockEvent event) {
        return operation.test.apply(conditions.stream(), condition -> condition.accepts(event) && condition.matches(owner, event));
    }

    enum Op {
        AND(Stream::allMatch),
        OR(Stream::anyMatch);

        final Test test;

        Op(Test test) {
            this.test = test;
        }

        interface Test {
            boolean apply(Stream<IUnlockCondition<IUnlockEvent>> stream, Predicate<IUnlockCondition<IUnlockEvent>> predicate);
        }
    }
}
