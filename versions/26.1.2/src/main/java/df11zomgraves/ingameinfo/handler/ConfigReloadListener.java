package df11zomgraves.ingameinfo.handler;

import df11zomgraves.ingameinfo.InGameInfoCore;
import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

/**
 * Loads the bundled default configuration once the mod resource pack is available.
 * <p>
 * The initial client setup runs before the resource manager has loaded mod packs, so
 * looking up {@code ingameinfo:ingameinfo.xml} there always fails. Reloading from this
 * listener (and on every resource reload, e.g. F3+T) makes the default config work.
 */
@EventBusSubscriber(modid = Names.MODID, value = Dist.CLIENT)
public final class ConfigReloadListener implements ResourceManagerReloadListener {
	private static final Identifier ID = Identifier.fromNamespaceAndPath(Names.MODID, "config");

	@SubscribeEvent
	public static void onAddReloadListeners(final AddClientReloadListenersEvent event) {
		event.addListener(ID, new ConfigReloadListener());
	}

	@Override
	public void onResourceManagerReload(final ResourceManager resourceManager) {
		// The reload may run on a worker thread; the core applies this on the render thread.
		InGameInfoCore.INSTANCE.requestReload();
	}
}
