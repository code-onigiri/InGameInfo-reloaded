package df11zomgraves.ingameinfo.api;

import df11zomgraves.ingameinfo.tag.TagRegistry;
import df11zomgraves.ingameinfo.value.ValueRegistry;

/**
 * Optional integration entry point for other mods.
 *
 * <p>Implementations are discovered with {@link java.util.ServiceLoader}; add a
 * {@code META-INF/services/df11zomgraves.ingameinfo.api.InGameInfoPlugin} file
 * containing the implementation class name. The plugin is loaded once, after
 * the built-in tags and values have been registered and before the HUD config
 * is parsed.</p>
 *
 * <p>The API intentionally exposes the same registry objects the built-in
 * content uses, so an integration can reuse base tags and only add the pieces
 * it needs. Implementations must not assume that the world or player exists
 * while registering.</p>
 */
public interface InGameInfoPlugin {
	/** Mod id that owns this integration, used for logging. */
	String getModId();

	/** Register extra value types (XML elements) for this mod. */
	default void registerValues(final ValueRegistry registry) {
	}

	/** Register extra tags (for example {@code thaumcraftaspect}). */
	default void registerTags(final TagRegistry registry) {
	}
}
