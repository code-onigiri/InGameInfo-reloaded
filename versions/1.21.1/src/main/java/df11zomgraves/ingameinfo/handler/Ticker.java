package df11zomgraves.ingameinfo.handler;

import df11zomgraves.ingameinfo.InGameInfoCore;
import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.integration.Ae2Integration;
import df11zomgraves.ingameinfo.client.gui.editor.GuiHudEditor;
import df11zomgraves.ingameinfo.client.gui.tag.GuiTags;
import net.neoforged.neoforge.network.PacketDistributor;
import df11zomgraves.ingameinfo.network.RequestMSPTPacket;
import df11zomgraves.ingameinfo.network.RequestSeedPacket;
import df11zomgraves.ingameinfo.reference.Names;
import df11zomgraves.ingameinfo.tag.Tag;
import df11zomgraves.ingameinfo.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

public class Ticker {
	public static final Ticker INSTANCE = new Ticker();
	public static boolean enabled = true;
	private final Minecraft client = Minecraft.getInstance();
	private final InGameInfoCore core = InGameInfoCore.INSTANCE;
	private boolean inGame = false;
	private long lastRemoteUpdate = 0;

	private boolean canRun() {
		if (!enabled)
			return false;
		final Options options = this.client.options;
		if (options == null)
			return false;
		if (ConfigurationHandler.replaceDebug)
			return true;
		return !this.client.getDebugOverlay().showDebugScreen()
				&& !this.client.getDebugOverlay().showFpsCharts()
				&& !this.client.getDebugOverlay().showNetworkCharts()
				&& !this.client.getDebugOverlay().showProfilerChart();
	}

	private boolean isRunning() {
		if (!canRun())
			return false;
		final Options options = this.client.options;
		if (options == null)
			return false;
		if (!ConfigurationHandler.showOnPlayerList && options.keyPlayerList.isDown())
			return false;
		if (options.hideGui)
			return false;
		if (this.client.screen instanceof GameModeSwitcherScreen)
			return true;
		if (this.client.screen instanceof GuiHudEditor || this.client.screen instanceof GuiTags)
			return true;
		return client.screen == null || ConfigurationHandler.showInChat && client.screen instanceof ChatScreen;
	}
	
	@SubscribeEvent
	public void onRenderGuiLayerEvent(final RenderGuiLayerEvent.Pre event) {
		final ResourceLocation overlay = event.getName();
		final boolean isSurvivalHUD = overlay.equals(VanillaGuiLayers.PLAYER_HEALTH)
				|| overlay.equals(VanillaGuiLayers.FOOD_LEVEL)
				|| overlay.equals(VanillaGuiLayers.AIR_LEVEL)
				|| overlay.equals(VanillaGuiLayers.VEHICLE_HEALTH)
				|| overlay.equals(VanillaGuiLayers.ARMOR_LEVEL);

		if (isSurvivalHUD && !ConfigurationHandler.showSurvivalHUD)
			event.setCanceled(true);
		else if (overlay.equals(VanillaGuiLayers.EFFECTS) && !ConfigurationHandler.showOverlayPotions)
			event.setCanceled(true);
		else if (canRun() && overlay.equals(VanillaGuiLayers.DEBUG_OVERLAY))
			event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderGuiEventPost(final RenderGuiEvent.Post event) {
		this.core.processPendingReload();
		if (isRunning()) {
			this.core.onTickClient();
			this.core.onTickRender(event.getGuiGraphics());
		}
	}

	@SubscribeEvent
	public void onClientTick(final net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
		boolean inGameCurrent = (client.level == null ? false : true);
		if (inGame) {
			if (!inGameCurrent) {
				InGameInfoXML.seed = ConfigurationHandler.seed;
				InGameInfoXML.mspt = -1;
				InGameInfoXML.tps = -1;
				InGameInfoXML.serverInstalled = false;
				inGame = false;
			} else if (isRunning() && InGameInfoXML.existMSPT) {
				long delay = (System.currentTimeMillis() - lastRemoteUpdate);
				if (delay > 1500 || delay < 0) {
					IntegratedServer singleServer = client.getSingleplayerServer();
					if (singleServer != null) {
						long[] times = singleServer.getTickTime(client.level.dimension());
						if (times != null) {
							double worldTickTime = MathUtils.mean(times) * 1.0E-6D;
							InGameInfoXML.mspt = worldTickTime;
							InGameInfoXML.tps = (worldTickTime == -1) ? -1 : Math.min(1000.0 / worldTickTime, 20);
						}
					} else if (InGameInfoXML.serverInstalled)
						try {
							InGameInfoXML.serverInstalled = false;
							PacketDistributor.sendToServer(new RequestMSPTPacket());
						} catch (Exception e) {
							InGameInfoXML.mspt = -1;
							InGameInfoXML.tps = -1;
						}
					lastRemoteUpdate = System.currentTimeMillis();
				}
			}
		} else if (inGameCurrent)
			try {
				inGame = true;
				InGameInfoXML.serverInstalled = true;
				showVerionInfo();
				PacketDistributor.sendToServer(new RequestSeedPacket());
			} catch (Exception e) {
				InGameInfoXML.seed = ConfigurationHandler.seed;
			}
		if (!enabled || this.client.options == null)
			Tag.releaseResources();
		Ae2Integration.clientTick();
	}

	private void showVerionInfo() {
		String keyToggle = KeyInputHandler.KEY_TOGGLE.getKey().getName();
		String keyReload = KeyInputHandler.KEY_RELOAD.getKey().getName();
		String keyToggle1 = Component.translatable(keyToggle).getString();
		String keyReload1 = Component.translatable(keyReload).getString();
		client.gui.getChat()
				.addMessage(Component.translatable(Names.VERSION_CHAT, Names.VERSION, keyToggle1, keyReload1));
	}
}
