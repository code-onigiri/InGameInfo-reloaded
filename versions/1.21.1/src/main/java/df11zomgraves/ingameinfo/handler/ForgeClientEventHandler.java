package df11zomgraves.ingameinfo.handler;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import df11zomgraves.ingameinfo.InGameInfoCore;
import df11zomgraves.ingameinfo.client.gui.editor.GuiHudEditor;
import df11zomgraves.ingameinfo.command.InGameInfoCommand;
import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;

@EventBusSubscriber(modid = Names.MODID, value = Dist.CLIENT)
public class ForgeClientEventHandler {
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		while (KeyInputHandler.KEY_RELOAD.consumeClick()) {
			Minecraft mc = Minecraft.getInstance();
			mc.gui.getChat().addMessage(Component.translatable(Names.Command.Message.RELOAD));
			InGameInfoCore core = InGameInfoCore.INSTANCE;

			Path configPath = FMLPaths.CONFIGDIR.get();
			Path modConfigPath = Paths.get(configPath.toAbsolutePath().toString());

			core.setConfigDirectory(modConfigPath.toFile());
			core.setConfigFile(ConfigurationHandler.configName);
			boolean b = core.reloadConfig();
			if (b)
				mc.gui.getChat().addMessage(Component.translatable(Names.Command.Message.SUCCESS));
			else
				mc.gui.getChat().addMessage(Component.translatable(Names.Command.Message.FAILURE));
		}
		while (KeyInputHandler.KEY_TOGGLE.consumeClick()) {
			Ticker.enabled = !Ticker.enabled;
		}
		while (KeyInputHandler.KEY_EDITOR.consumeClick()) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.screen == null) {
				mc.setScreen(new GuiHudEditor());
			}
		}
	}

	@SubscribeEvent
	public static void onRegisterClientCommands(@Nonnull RegisterClientCommandsEvent event) {
		InGameInfoCommand.register(event.getDispatcher());
	}
}
