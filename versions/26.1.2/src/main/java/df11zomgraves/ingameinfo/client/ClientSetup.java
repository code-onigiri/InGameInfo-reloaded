package df11zomgraves.ingameinfo.client;

import df11zomgraves.ingameinfo.client.gui.config.GuiConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only setup. Keeping the config screen registration here prevents the
 * dedicated server from loading client GUI classes through the mod constructor.
 */
public final class ClientSetup {
	private ClientSetup() {
	}

	public static void registerConfigScreen(ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class,
				(modContainer, parent) -> new GuiConfigScreen(parent));
	}
}
