package df11zomgraves.ingameinfo.api;

import java.util.List;
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
		final List<ServiceLoader.Provider<InGameInfoPlugin>> providers;
		try {
			providers = ServiceLoader.load(InGameInfoPlugin.class, classLoader).stream().toList();
		} catch (final Throwable t) {
			InGameInfoXML.logger.error("Failed to enumerate InGameInfoXML integration providers.", t);
			return;
		}

		int loaded = 0;
		for (final ServiceLoader.Provider<InGameInfoPlugin> provider : providers) {
			String providerName = "unknown";
			final InGameInfoPlugin plugin;
			try {
				providerName = provider.type().getName();
				plugin = provider.get();
			} catch (final Throwable t) {
				// A broken provider must not abort the remaining providers or client setup.
				InGameInfoXML.logger.error("Failed to load InGameInfoXML integration provider '{}'.", providerName, t);
				continue;
			}

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
