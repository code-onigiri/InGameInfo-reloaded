package df11zomgraves.ingameinfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import df11zomgraves.ingameinfo.api.PluginLoader;
import df11zomgraves.ingameinfo.client.gui.config.GuiConfigScreen;
import df11zomgraves.ingameinfo.command.arguments.InGameInfoArgumentTypes;
import df11zomgraves.ingameinfo.handler.ConfigurationHandler;
import df11zomgraves.ingameinfo.handler.Ticker;
import df11zomgraves.ingameinfo.network.PacketHandler;
import df11zomgraves.ingameinfo.reference.Names;
import df11zomgraves.ingameinfo.tag.TagRegistry;
import df11zomgraves.ingameinfo.value.ValueRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Names.MODID)
public class InGameInfoXML {
	public static Logger logger = LogManager.getLogger(Names.MODID);
	public static long seed = 0;
	public static double mspt = -1;
	public static double tps = -1;
	public static boolean serverInstalled = false;
	public static boolean existMSPT = false;

	public InGameInfoXML(IEventBus modEventBus, ModContainer container) {
		PacketHandler.init(modEventBus);
		container.registerConfig(ModConfig.Type.CLIENT, ConfigurationHandler.SPEC);
		container.registerExtensionPoint(IConfigScreenFactory.class,
				(modContainer, parent) -> new GuiConfigScreen(parent));
		modEventBus.addListener(this::clientSetup);
		InGameInfoArgumentTypes.COMMAND_ARGUMENT_TYPES.register(modEventBus);
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		if (ModList.get().getModFileById("chloride") != null)
			logger.warn("Chloride installed. may cause FPS do not display. use Xenon instead.");

		// InGameInfoCore has no @SubscribeEvent methods; Ticker drives it directly.
		NeoForge.EVENT_BUS.register(Ticker.INSTANCE);
		ValueRegistry.INSTANCE.init();
		TagRegistry.INSTANCE.init();
		PluginLoader.load();
		InGameInfoCore core = InGameInfoCore.INSTANCE;
		Path configPath = FMLPaths.CONFIGDIR.get();
		Path modConfigPath = Paths.get(configPath.toAbsolutePath().toString());
		core.setConfigDirectory(modConfigPath.toFile());
		core.setConfigFile(ConfigurationHandler.configName);
		// The resource manager has not loaded mod packs yet; ConfigReloadListener
		// reloads the default config once the packs are available.
		event.enqueueWork(InGameInfoArgumentTypes::registerArgumentTypes);
	}
}
