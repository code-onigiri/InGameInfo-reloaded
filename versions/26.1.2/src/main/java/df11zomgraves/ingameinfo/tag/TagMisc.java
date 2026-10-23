package df11zomgraves.ingameinfo.tag;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static class MemoryMaximum extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(Runtime.getRuntime().maxMemory());
		}
	}

	public static class MemoryTotal extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(Runtime.getRuntime().totalMemory());
		}
	}

	public static class MemoryFree extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(Runtime.getRuntime().freeMemory());
		}
	}

	public static class MemoryUsed extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		}
	}

	public static class FPS extends TagMisc {
		@Override
		public String getValue() {
			return String.valueOf(minecraft.getFps());
		}
	}

	private static final Pattern ENTITY_STATISTICS = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");

	private static String[] getEntityStatistics() {
		final String statistics = minecraft.levelRenderer.getEntityStatistics();
		final Matcher matcher = ENTITY_STATISTICS.matcher(statistics);
		if (matcher.find()) {
			return new String[] { matcher.group(1), matcher.group(2) };
		}
		return new String[] { "0", "0" };
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
		
		@Override
		public String getValue() {
			final Collection<Pack> selectedPacks = resourcePackRepository.getSelectedPacks();
			final String defaultPackName = resourcePackRepository.getAvailablePacks().stream()
					.filter(pack -> "vanilla".equals(pack.getId()))
					.findFirst()
					.map(pack -> pack.getTitle().getString())
					.orElse("Default");
			for (final Pack pack : selectedPacks) {
				final String id = pack.getId();
				if ("mod_resources".equals(id) || "vanilla".equals(id)) {
					continue;
				}
				return pack.getTitle().getString();
			}
			return defaultPackName;
		}
	}

	public static class Server extends TagMisc {
		@Override
		public String getValue() {
			final String str = player.connection.getConnection().getRemoteAddress().toString();
			final int i = str.indexOf("/");
			final int j = str.lastIndexOf(":");
			if (i < 0)
				return "localhost";
			final String name = (i == 0) ? str.substring(i + 1, j) : str.substring(0, i);
			final String port = str.substring(j + 1);
			return name + (port.equals("25565") ? "" : ":" + port);
		}
	}

	public static class ServerName extends TagMisc {
		@Override
		public String getValue() {
			final String str = player.connection.getConnection().getRemoteAddress().toString();
			final int i = str.indexOf("/");
			if (i < 0)
				return "localhost";
			if (i == 0)
				return str.substring(i + 1, str.lastIndexOf(":"));
			return str.substring(0, i);
		}
	}

	public static class ServerIP extends TagMisc {
		@Override
		public String getValue() {
			final String str = player.connection.getConnection().getRemoteAddress().toString();
			final int i = str.indexOf("/");
			if (i < 0)
				return "127.0.0.1";
			return str.substring(i + 1, str.lastIndexOf(":"));
		}
	}

	public static class ServerPort extends TagMisc {
		@Override
		public String getValue() {
			final String str = player.connection.getConnection().getRemoteAddress().toString();
			final int i = str.indexOf("/");
			if (i < 0)
				return "-1";
			return str.substring(str.lastIndexOf(":") + 1);
		}
	}

	public static class Ping extends TagMisc {
		@Override
		public String getValue() {
			int ping = -1;
			try {
				 final PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(player.getUUID());
				 ping = playerInfo.getLatency();
			} catch (final Exception e) {
				ping = -1;
			}
			return String.valueOf(ping);
		}
	}

	public static class PingIcon extends TagMisc {
		@Override
		public String getValue() {
			int ping = -1;
			try {
				final PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(player.getUUID());
				ping = playerInfo.getLatency();
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
			}
			return "-1";
		}
	}

	public static class MinecraftVersion extends TagMisc {
		@Override
		public String getValue() {
			return SharedConstants.getCurrentVersion().name();
		}
	}

	public static class ForgeVersion extends TagMisc {
		@Override
		public String getValue() {
			return ModList.get().getModContainerById("neoforge").map(container -> container.getModInfo().getVersion().toString()).orElse("");
		}
	}

	public static class MCPVersion extends TagMisc {
		@Override
		public String getValue() {
			return "n/a";
		}
	}

	public static class ModsTotal extends TagMisc {
		@Override
		public String getValue() {
			return String.format("%d", ModList.get().size());
		}
	}

	public static class ModsActive extends TagMisc {
		@Override
		public String getValue() {
			return String.format("%d", ModList.get().size());
		}
	}

	public static class JavaVersion extends TagMisc {
		@Override
		public String getValue() {
			String javaVersion = String.format("%s %sbit", System.getProperty("java.version"),
					System.getProperty("sun.arch.data.model", "64"));
			return javaVersion;
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
