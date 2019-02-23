package com.minelittlepony.unicopia.mixin;

import java.lang.reflect.Field;

public class FieldAccessor<Owner, Type> {

    private boolean init;
    private Field field;

    private final Class<Owner> ownerClass;

    private final int fieldIndex;

    FieldAccessor(Class<Owner> type, int index) {
        ownerClass = type;
        fieldIndex = index;
    }

    void set(Owner instance, Type value) {
        init = false;
        field = null;
        init();

        try {
            if (field != null) {
                field.set(instance, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        if (!init && field == null) {
            Field[] fields = ownerClass.getDeclaredFields();
            field = fields[fieldIndex < 0 ? (fields.length + fieldIndex) : fieldIndex];
            field.setAccessible(true);
        }
    }
}
