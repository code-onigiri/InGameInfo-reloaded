package df11zomgraves.ingameinfo.integration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Small reflection helper used by the optional mod integrations. */
public final class Reflect {
    private Reflect() {
    }

    public static Class<?> cls(final String name) {
        try {
            return Class.forName(name);
        } catch (final Throwable t) {
            return null;
        }
    }

    public static boolean isInstance(final Object value, final String className) {
        final Class<?> cls = cls(className);
        return cls != null && value != null && cls.isInstance(value);
    }

    public static Object call(final Object target, final String name, final Object... args) {
        if (target == null) {
            return null;
        }
        final Object result = invoke(target.getClass(), target, name, args);
        return result == NOT_FOUND ? null : result;
    }

    public static Object callStatic(final String className, final String name, final Object... args) {
        final Class<?> cls = cls(className);
        if (cls == null) {
            return null;
        }
        final Object result = invoke(cls, null, name, args);
        return result == NOT_FOUND ? null : result;
    }

    public static Object field(final Object target, final String name) {
        if (target == null) {
            return null;
        }
        for (Class<?> cls = target.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                final Field field = cls.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (final NoSuchFieldException ignored) {
                // try the superclass
            } catch (final Throwable t) {
                return null;
            }
        }
        return null;
    }

    public static Object staticField(final String className, final String name) {
        final Class<?> cls = cls(className);
        if (cls == null) {
            return null;
        }
        try {
            final Field field = cls.getField(name);
            return field.get(null);
        } catch (final Throwable t) {
            return null;
        }
    }

    private static final Object NOT_FOUND = new Object();

    private static Object invoke(final Class<?> cls, final Object target, final String name, final Object... args) {
        for (Class<?> current = cls; current != null; current = current.getSuperclass()) {
            final Object result = invokeDeclared(current, target, name, args);
            if (result != NOT_FOUND) {
                return result;
            }
        }
        return invokeInterfaces(cls, target, name, args);
    }

    private static Object invokeInterfaces(final Class<?> cls, final Object target, final String name,
            final Object... args) {
        for (Class<?> current = cls; current != null; current = current.getSuperclass()) {
            for (final Class<?> iface : current.getInterfaces()) {
                final Object result = invokeDeclared(iface, target, name, args);
                if (result != NOT_FOUND) {
                    return result;
                }
                final Object nested = invokeInterfaces(iface, target, name, args);
                if (nested != NOT_FOUND) {
                    return nested;
                }
            }
        }
        return NOT_FOUND;
    }

    private static Object invokeDeclared(final Class<?> cls, final Object target, final String name,
            final Object... args) {
        for (final Method method : cls.getDeclaredMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != args.length) {
                continue;
            }
            if (!matches(method.getParameterTypes(), args)) {
                continue;
            }
            try {
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (final Throwable t) {
                return null;
            }
        }
        return NOT_FOUND;
    }

    private static boolean matches(final Class<?>[] types, final Object[] args) {
        for (int i = 0; i < types.length; i++) {
            if (args[i] == null) {
                if (types[i].isPrimitive()) {
                    return false;
                }
                continue;
            }
            final Class<?> type = box(types[i]);
            if (!type.isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> box(final Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
