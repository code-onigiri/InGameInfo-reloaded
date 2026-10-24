package df11zomgraves.ingameinfo.client;

import df11zomgraves.ingameinfo.client.gui.config.GuiConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-only setup. Keeping the config screen registration here prevents the
 * dedicated server from loading client GUI classes through the mod constructor.
 */
public final class ClientSetup {
	private ClientSetup() {
	}

	public static void registerConfigScreen() {
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new GuiConfigScreen(parent)));
	}
}
