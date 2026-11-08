package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.tag.Tag;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Base class for tags that only exist when an optional mod is loaded. */
public abstract class IntegrationTag extends Tag {
    /** Tag classes whose failure has already been logged, so a broken tag does not spam the log every frame. */
    private static final Set<Class<?>> LOGGED_FAILURES = ConcurrentHashMap.newKeySet();

    private final String category;

    protected IntegrationTag(final String category) {
        this.category = category;
    }

    @Override
    public final String getCategory() {
        return this.category;
    }

    @Override
    public final String getValue() {
        try {
            return value();
        } catch (final Exception | LinkageError e) {
            // Other Errors (OutOfMemoryError, StackOverflowError, ...) are not
            // integration failures and must keep propagating.
            if (LOGGED_FAILURES.add(getClass())) {
                InGameInfoXML.logger.warn("Integration tag {} failed; it will report its fallback value",
                        getClass().getName(), e);
            }
            return fallback();
        }
    }

    protected String fallback() {
        return "";
    }

    protected abstract String value();
}
