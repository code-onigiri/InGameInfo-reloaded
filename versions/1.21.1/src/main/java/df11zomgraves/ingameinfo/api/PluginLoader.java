package df11zomgraves.ingameinfo.api;

import java.util.ServiceLoader;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.tag.TagRegistry;
import df11zomgraves.ingameinfo.value.ValueRegistry;

/** Loads {@link InGameInfoPlugin} implementations without hard dependencies. */
public final class PluginLoader {
	private PluginLoader() {
	}

	public static void load() {
		load(PluginLoader.class.getClassLoader());
	}

	public static void load(final ClassLoader classLoader) {
		int loaded = 0;
		for (final InGameInfoPlugin plugin : ServiceLoader.load(InGameInfoPlugin.class, classLoader)) {
			try {
				plugin.registerValues(ValueRegistry.INSTANCE);
				plugin.registerTags(TagRegistry.INSTANCE);
				loaded++;
				InGameInfoXML.logger.info("Loaded InGameInfoXML integration '{}'.", plugin.getModId());
			} catch (final Throwable t) {
				InGameInfoXML.logger.error("Failed to load InGameInfoXML integration.", t);
			}
		}

		if (loaded > 0) {
			InGameInfoXML.logger.info("Loaded {} InGameInfoXML integration(s).", loaded);
		}
	}
}
