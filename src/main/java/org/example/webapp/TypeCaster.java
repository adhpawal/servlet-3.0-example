package org.example.webapp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TypeCaster {
    public static Object cast(String value, Class type) {
        if (value == null)
            return null;
        if (type.equals(String.class))
            return value;

        Method method = null;
        Constructor constructor = null;
        try {
            method = type.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException e1) {
            try {
                constructor = type.getConstructor(String.class);
            } catch (NoSuchMethodException | SecurityException e2) {
            }
        }

        if (method != null) {
            try {
                return method.invoke(null, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                if (value.isEmpty())
                    return null;
                throw new ClassCastException("Cannot convert from String to " + type.getName());
            }
        }

        if (constructor != null) {
            try {
                return constructor.newInstance(value);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                if (value.isEmpty())
                    return null;
                throw new ClassCastException("Cannot convert from String to " + type.getName());
            }
        }

        throw new ClassCastException("Cannot convert from String to " + type.getName());
    }
}
