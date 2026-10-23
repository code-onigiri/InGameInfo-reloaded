package df11zomgraves.ingameinfo.integration;

import df11zomgraves.ingameinfo.tag.Tag;

/** Base class for tags that only exist when an optional mod is loaded. */
public abstract class IntegrationTag extends Tag {
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
        } catch (final Throwable t) {
            return fallback();
        }
    }

    protected String fallback() {
        return "";
    }

    protected abstract String value();
}
