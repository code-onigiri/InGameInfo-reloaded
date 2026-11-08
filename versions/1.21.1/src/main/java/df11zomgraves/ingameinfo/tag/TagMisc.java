package df11zomgraves.ingameinfo.tag;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.gui.overlay.InfoIcon;
import net.minecraft.SharedConstants;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.neoforged.fml.ModList;

public abstract class TagMisc extends Tag {

	@Override
	public String getCategory() {
		return "misc";
	}

	// One shared snapshot, refreshed at most once a second, so the memory tags
	// displayed side by side always agree with each other.
	private static long[] memorySnapshot;
	private static long memorySnapshotAt;

	private static long[] memorySnapshot() {
		final long now = System.currentTimeMillis();
		if (memorySnapshot == null || now - memorySnapshotAt >= 1000L) {
			final Runtime runtime = Runtime.getRuntime();
			memorySnapshot = new long[] { runtime.maxMemory(), runtime.totalMemory(), runtime.freeMemory() };
			memorySnapshotAt = now;
		}
		return memorySnapshot;
	}

	public static class MemoryMaximum extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(memorySnapshot()[0]);
		}
	}

	public static class MemoryTotal extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(memorySnapshot()[1]);
		}
	}

	public static class MemoryFree extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(memorySnapshot()[2]);
		}
	}

	public static class MemoryUsed extends TagMisc {
		@Override
		public String getValue() {
			final long[] snapshot = memorySnapshot();
			return String.valueOf(snapshot[1] - snapshot[2]);
		}
	}

	public static class FPS extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(minecraft.getFps());
		}
	}

	// Anchored to the "E:" prefix of the statistics string, so an earlier digits/digits
	// pair in the string cannot be mistaken for the entity counts. The loose pattern
	// stays as a fallback if the format ever changes.
	private static final Pattern ENTITY_STATISTICS = Pattern.compile("E:\\s*(\\d+)\\s*/\\s*(\\d+)");
	private static final Pattern ENTITY_STATISTICS_LOOSE = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");

	private static String[] cachedEntityStatistics;
	private static long cachedEntityStatisticsAt;

	private static String[] getEntityStatistics() {
		// Both entity tags share one parse instead of re-running the regex per tag.
		final long now = System.currentTimeMillis();
		if (cachedEntityStatistics != null && now - cachedEntityStatisticsAt < 100L) {
			return cachedEntityStatistics;
		}
		final String statistics = minecraft.levelRenderer.getEntityStatistics();
		Matcher matcher = ENTITY_STATISTICS.matcher(statistics);
		boolean found = matcher.find();
		if (!found) {
			matcher = ENTITY_STATISTICS_LOOSE.matcher(statistics);
			found = matcher.find();
		}
		cachedEntityStatistics = found ? new String[] { matcher.group(1), matcher.group(2) } : new String[] { "0", "0" };
		cachedEntityStatisticsAt = now;
		return cachedEntityStatistics;
	}

	public static class EntitiesRendered extends TagMisc {
		@Override
		public String getValue() {
			return getEntityStatistics()[0];
		}
	}

	public static class EntitiesTotal extends TagMisc {
		@Override
		public String getValue() {
			return getEntityStatistics()[1];
		}
	}

	public static class LoadedChunks extends TagMisc {
		@Override
		public String getValue() {
			// No longer exposed on the client in 1.20.1; the original returned -1 too.
			return "-1";
		}
	}
	
	public static class ResourcePack extends TagMisc {
		protected static final PackRepository resourcePackRepository = minecraft.getResourcePackRepository();

		private static String defaultPackName;
		
		@Override
		public String getValue() {
			String topmost = null;
			for (final Pack pack : resourcePackRepository.getSelectedPacks()) {
				final String id = pack.getId();
				if ("mod_resources".equals(id) || "vanilla".equals(id)) {
					continue;
				}
				// Selected packs are ordered bottom-first, so the last match is the pack on top.
				topmost = pack.getTitle().getString();
			}
			if (topmost != null) {
				return topmost;
			}
			if (defaultPackName == null) {
				defaultPackName = resourcePackRepository.getAvailablePacks().stream()
						.filter(pack -> "vanilla".equals(pack.getId()))
						.findFirst()
						.map(pack -> pack.getTitle().getString())
						.orElse("Default");
			}
			return defaultPackName;
		}
	}

	private static InetSocketAddress remoteAddress() {
		if (player == null || player.connection == null) {
			return null;
		}
		final SocketAddress address = player.connection.getConnection().getRemoteAddress();
		return address instanceof InetSocketAddress ? (InetSocketAddress) address : null;
	}

	public static class Server extends TagMisc {
		@Override
		public String getValue() {
			final InetSocketAddress address = remoteAddress();
			if (address == null)
				return "localhost";
			final String name = address.getHostString();
			final int port = address.getPort();
			return name + (port == 25565 ? "" : ":" + port);
		}
	}

	public static class ServerName extends TagMisc {
		@Override
		public String getValue() {
			final InetSocketAddress address = remoteAddress();
			if (address == null)
				return "localhost";
			return address.getHostString();
		}
	}

	public static class ServerIP extends TagMisc {
		@Override
		public String getValue() {
			final InetSocketAddress address = remoteAddress();
			if (address == null)
				return "127.0.0.1";
			return address.getAddress() != null ? address.getAddress().getHostAddress() : address.getHostString();
		}
	}

	public static class ServerPort extends TagMisc {
		@Override
		public String getValue() {
			final InetSocketAddress address = remoteAddress();
			if (address == null)
				return "-1";
			return String.valueOf(address.getPort());
		}
	}

	public static class Ping extends TagMisc {
		private static boolean failureLogged;

		@Override
		public String getValue() {
			try {
				final PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(player.getUUID());
				if (playerInfo != null) {
					return String.valueOf(playerInfo.getLatency());
				}
			} catch (final Exception e) {
				if (!failureLogged) {
					failureLogged = true;
					InGameInfoXML.logger.debug("Could not read the player ping; reporting -1", e);
				}
			}
			return "-1";
		}
	}

	public static class PingIcon extends TagMisc {
		private static boolean failureLogged;

		@Override
		public String getValue() {
			try {
				final PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(player.getUUID());
				if (playerInfo == null) {
					return "-1";
				}
				final int ping = playerInfo.getLatency();
				int pingIndex = 4;
				if (ping < 0)
					pingIndex = 5;
				else if (ping < 150)
					pingIndex = 0;
				else if (ping < 300)
					pingIndex = 1;
				else if (ping < 600)
					pingIndex = 2;
				else if (ping < 1000)
					pingIndex = 3;

				final InfoIcon icon = new InfoIcon("textures/gui/icons.png");
				icon.setDisplayDimensions(0, 0, 10, 8);
				icon.setTextureData(0, 176 + pingIndex * 8, 10, 8, 256, 256);
				info.add(icon);
				return getIconTag(icon);
			} catch (final Exception e) {
				if (!failureLogged) {
					failureLogged = true;
					InGameInfoXML.logger.debug("Could not read the player ping; reporting -1", e);
				}
			}
			return "-1";
		}
	}

	public static class MinecraftVersion extends TagMisc {
		@Override
		public String getValue() {
			return SharedConstants.getCurrentVersion().getName();
		}
	}

	public static class ForgeVersion extends TagMisc {
		private static String cached;

		@Override
		public String getValue() {
			if (cached == null) {
				cached = ModList.get().getModContainerById("neoforge").map(container -> container.getModInfo().getVersion().toString()).orElse("");
			}
			return cached;
		}
	}

	public static class MCPVersion extends TagMisc {
		@Override
		public String getValue() {
			return "n/a";
		}
	}

	public static class ModsTotal extends TagMisc {
		private static int cached = -1;

		@Override
		public String getValue() {
			if (cached < 0) {
				cached = ModList.get().size();
			}
			return String.valueOf(cached);
		}
	}

	public static class ModsActive extends TagMisc {
		private static int cached = -1;

		@Override
		public String getValue() {
			if (cached < 0) {
				cached = ModList.get().size();
			}
			return String.valueOf(cached);
		}
	}

	public static class JavaVersion extends TagMisc {
		private static String cached;

		@Override
		public String getValue() {
			if (cached == null) {
				// sun.arch.data.model is a HotSpot-only property; fall back to os.arch on other JVMs
				// instead of silently claiming 64-bit.
				String bits = System.getProperty("sun.arch.data.model");
				if (bits == null) {
					bits = System.getProperty("os.arch", "").endsWith("64") ? "64" : "32";
				}
				cached = String.format("%s %sbit", System.getProperty("java.version"), bits);
			}
			return cached;
		}
	}

	public static void register() {
		TagRegistry.INSTANCE.register(new EntitiesRendered().setName("entitiesrendered"));
		TagRegistry.INSTANCE.register(new EntitiesTotal().setName("entitiestotal"));
		TagRegistry.INSTANCE.register(new ForgeVersion().setName("forgeversion"));
		TagRegistry.INSTANCE.register(new FPS().setName("fps"));
		TagRegistry.INSTANCE.register(new JavaVersion().setName("javaversion"));
		TagRegistry.INSTANCE.register(new LoadedChunks().setName("loadedchunks"));
		TagRegistry.INSTANCE.register(new MCPVersion().setName("mcpversion"));
		TagRegistry.INSTANCE.register(new MemoryFree().setName("memfree"));
		TagRegistry.INSTANCE.register(new MemoryMaximum().setName("memmax"));
		TagRegistry.INSTANCE.register(new MemoryTotal().setName("memtotal"));
		TagRegistry.INSTANCE.register(new MemoryUsed().setName("memused"));
		TagRegistry.INSTANCE.register(new MinecraftVersion().setName("mcversion"));
		TagRegistry.INSTANCE.register(new ModsActive().setName("modsactive"));
		TagRegistry.INSTANCE.register(new ModsTotal().setName("modstotal"));
		TagRegistry.INSTANCE.register(new PingIcon().setName("pingicon"));
		TagRegistry.INSTANCE.register(new Ping().setName("ping"));
		TagRegistry.INSTANCE.register(new ServerIP().setName("serverip"));
		TagRegistry.INSTANCE.register(new ServerName().setName("servername"));
		TagRegistry.INSTANCE.register(new ServerPort().setName("serverport"));
		TagRegistry.INSTANCE.register(new Server().setName("server"));
		TagRegistry.INSTANCE.register(new ResourcePack().setName("resourcepack"));
	}
}
