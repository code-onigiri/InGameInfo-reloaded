package df11zomgraves.ingameinfo.handler;

import df11zomgraves.ingameinfo.InGameInfoCore;
import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.integration.Ae2Integration;
import df11zomgraves.ingameinfo.client.gui.editor.GuiHudEditor;
import df11zomgraves.ingameinfo.client.gui.tag.GuiTags;
import df11zomgraves.ingameinfo.network.PacketHandler;
import df11zomgraves.ingameinfo.network.RequestMSPTPacket;
import df11zomgraves.ingameinfo.network.RequestSeedPacket;
import df11zomgraves.ingameinfo.reference.Names;
import df11zomgraves.ingameinfo.tag.Tag;
import df11zomgraves.ingameinfo.tag.TagWorld;
import df11zomgraves.ingameinfo.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Ticker {
	public static final Ticker INSTANCE = new Ticker();
	public static boolean enabled = true;
	private final Minecraft client = Minecraft.getInstance();
	private final InGameInfoCore core = InGameInfoCore.INSTANCE;
	private boolean inGame = false;
	private long lastRemoteUpdate = 0;
	/** How long MSPT requests may go unanswered before the server counts as not having the mod. */
	private static final long MSPT_RESPONSE_TIMEOUT = 10000;

	private boolean canRun() {
		if (!enabled)
			return false;
		final Options options = this.client.options;
		if (options == null)
			return false;
		if (ConfigurationHandler.replaceDebug)
			return true;
		return !options.renderDebug && !options.renderDebugCharts;
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
	public void onRenderGuiOverlayEvent(final RenderGuiOverlayEvent.Pre event) {
		NamedGuiOverlay overlay = event.getOverlay();
		boolean isSurvivalHUD =
				overlay == VanillaGuiOverlay.PLAYER_HEALTH.type() ||
				overlay == VanillaGuiOverlay.FOOD_LEVEL.type() ||
				overlay == VanillaGuiOverlay.AIR_LEVEL.type() ||
				overlay == VanillaGuiOverlay.MOUNT_HEALTH.type() ||
				overlay == VanillaGuiOverlay.ARMOR_LEVEL.type();

		if (isSurvivalHUD && !ConfigurationHandler.showSurvivalHUD)
			event.setCanceled(true);
		else if (overlay == VanillaGuiOverlay.POTION_ICONS.type() && !ConfigurationHandler.showOverlayPotions)
			event.setCanceled(true);
		else if (canRun() && overlay == VanillaGuiOverlay.DEBUG_TEXT.type())
			event.setCanceled(true);
		else if (canRun() && ConfigurationHandler.replaceDebug && overlay == VanillaGuiOverlay.FPS_GRAPH.type())
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
	public void onClientTick(final TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		boolean inGameCurrent = (client.level == null ? false : true);
		if (inGame) {
			if (!inGameCurrent) {
				InGameInfoXML.seed = ConfigurationHandler.seed;
				InGameInfoXML.mspt = -1;
				InGameInfoXML.tps = -1;
				InGameInfoXML.serverInstalled = false;
				InGameInfoXML.seedSetLocally = false;
				InGameInfoXML.msptRequestSentAt = 0;
				TagWorld.setSeedReceived(false);
				inGame = false;
			} else if (isRunning() && InGameInfoXML.existMSPT) {
				long delay = (System.currentTimeMillis() - lastRemoteUpdate);
				if (delay > 1500 || delay < 0) {
					IntegratedServer singleServer = client.getSingleplayerServer();
					if (singleServer != null) {
						long[] times = singleServer.getTickTime(client.level.dimension());
						if (times != null) {
							double worldTickTime = MathUtils.mean(times) * 1.0E-6D;
							// A mean of 0 means no ticks measured yet; keep the
							// "no data" state instead of publishing a fake 20 TPS.
							if (worldTickTime > 0) {
								InGameInfoXML.mspt = worldTickTime;
								InGameInfoXML.tps = Math.min(1000.0 / worldTickTime, 20);
							}
						}
					} else {
						final long now = System.currentTimeMillis();
						if (InGameInfoXML.msptRequestSentAt != 0
								&& now - InGameInfoXML.msptRequestSentAt > MSPT_RESPONSE_TIMEOUT) {
							// Requests kept going unanswered: the server does not
							// have the mod, so stop asking. A single lost response
							// no longer freezes TPS/MSPT for the whole session.
							InGameInfoXML.serverInstalled = false;
							InGameInfoXML.msptRequestSentAt = 0;
						}
						if (InGameInfoXML.serverInstalled)
							try {
								PacketHandler.INSTANCE.sendToServer(new RequestMSPTPacket());
								if (InGameInfoXML.msptRequestSentAt == 0)
									InGameInfoXML.msptRequestSentAt = now;
							} catch (Exception e) {
								InGameInfoXML.mspt = -1;
								InGameInfoXML.tps = -1;
							}
					}
					lastRemoteUpdate = System.currentTimeMillis();
				}
			}
		} else if (inGameCurrent)
			try {
				inGame = true;
				InGameInfoXML.serverInstalled = true;
				InGameInfoXML.seedSetLocally = false;
				InGameInfoXML.msptRequestSentAt = 0;
				showVerionInfo();
				PacketHandler.INSTANCE.sendToServer(new RequestSeedPacket());
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