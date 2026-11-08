package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.InGameInfoXML;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Small reflection helper used by the optional mod integrations. */
public final class Reflect {
    private Reflect() {
    }

    private static final Map<String, Optional<Class<?>>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Optional<Method>> METHOD_CACHE = new ConcurrentHashMap<>();

    /** Marker used in a method-cache key in place of the type of a null argument. */
    private static final class NullArg {
        private NullArg() {
        }
    }

    private record MethodKey(Class<?> type, String name, List<Class<?>> argTypes) {
    }

    public static Class<?> cls(final String name) {
        Optional<Class<?>> cached = CLASS_CACHE.get(name);
        if (cached == null) {
            cached = loadClass(name);
            final Optional<Class<?>> raced = CLASS_CACHE.putIfAbsent(name, cached);
            if (raced != null) {
                cached = raced;
            }
        }
        return cached.orElse(null);
    }

    private static Optional<Class<?>> loadClass(final String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (final Throwable t) {
            InGameInfoXML.logger.debug("Could not load class {}", name, t);
            return Optional.empty();
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
                InGameInfoXML.logger.debug("Could not read field {} of {}", name, target.getClass().getName(), t);
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
        for (Class<?> current = cls; current != null; current = current.getSuperclass()) {
            try {
                final Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(null);
            } catch (final NoSuchFieldException ignored) {
                // try the superclass
            } catch (final Throwable t) {
                InGameInfoXML.logger.debug("Could not read static field {} of {}", name, className, t);
                return null;
            }
        }
        return null;
    }

    private static final Object NOT_FOUND = new Object();

    private static Object invoke(final Class<?> cls, final Object target, final String name, final Object... args) {
        final MethodKey key = new MethodKey(cls, name, argTypes(args));
        Optional<Method> cached = METHOD_CACHE.get(key);
        if (cached == null) {
            cached = Optional.ofNullable(findMethod(cls, name, args));
            final Optional<Method> raced = METHOD_CACHE.putIfAbsent(key, cached);
            if (raced != null) {
                cached = raced;
            }
        }
        if (cached.isEmpty()) {
            return NOT_FOUND;
        }
        return invokeResolved(cached.get(), target, args);
    }

    private static Object invokeResolved(final Method method, final Object target, final Object[] args) {
        try {
            // Production mods live in named modules whose packages are not opened to us.
            // setAccessible(true) then throws InaccessibleObjectException even for public
            // methods, so only force access when the direct invocation would not work.
            if (!method.canAccess(target)) {
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (final Throwable t) {
            InGameInfoXML.logger.debug("Reflective call to {} failed", method, t);
            return null;
        }
    }

    private static List<Class<?>> argTypes(final Object[] args) {
        final List<Class<?>> types = new ArrayList<>(args.length);
        for (final Object arg : args) {
            types.add(arg == null ? NullArg.class : arg.getClass());
        }
        return List.copyOf(types);
    }

    private static Method findMethod(final Class<?> cls, final String name, final Object[] args) {
        for (Class<?> current = cls; current != null; current = current.getSuperclass()) {
            final Method method = findDeclared(current, name, args);
            if (method != null) {
                return method;
            }
        }
        final Set<Class<?>> visited = new HashSet<>();
        for (Class<?> current = cls; current != null; current = current.getSuperclass()) {
            for (final Class<?> iface : current.getInterfaces()) {
                final Method method = findInInterface(iface, name, args, visited);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    private static Method findInInterface(final Class<?> iface, final String name, final Object[] args,
            final Set<Class<?>> visited) {
        if (!visited.add(iface)) {
            return null;
        }
        final Method method = findDeclared(iface, name, args);
        if (method != null) {
            return method;
        }
        for (final Class<?> parent : iface.getInterfaces()) {
            final Method nested = findInInterface(parent, name, args, visited);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private static Method findDeclared(final Class<?> cls, final String name, final Object[] args) {
        for (final Method method : cls.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length
                    && matches(method.getParameterTypes(), args)) {
                return method;
            }
        }
        return null;
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
