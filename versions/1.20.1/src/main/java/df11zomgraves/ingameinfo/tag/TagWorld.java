package df11zomgraves.ingameinfo.tag;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.handler.ConfigurationHandler;
import df11zomgraves.ingameinfo.util.ChunkHelper;
import df11zomgraves.ingameinfo.util.StringConvertUtils;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.ServerLevelData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public abstract class TagWorld extends Tag {
	@Override
	public String getCategory() {
		return "world";
	}

	/**
	 * The 1.12.2 implementation read the world name from WorldInfo. In 1.20.1 the
	 * client does not keep a world name for remote servers, so fall back to the
	 * server entry name (or "?" when there is none).
	 */
	public static class Name extends TagWorld {
		@Override
		public String getValue() {
			final MinecraftServer server = minecraft.getSingleplayerServer();
			if (server != null) {
				return server.getWorldData().getLevelName();
			}
			final ServerData serverData = minecraft.getCurrentServer();
			return serverData != null ? serverData.name : "?";
		}
	}

	/**
	 * World size on disk is only available from the integrated server. The value
	 * is cached for a few seconds because walking the save directory every frame
	 * would be far too expensive.
	 */
	private static long cachedSize = -1L;
	private static long cachedSizeAt = 0L;

	private static long getWorldSize() {
		final long now = System.currentTimeMillis();
		if (cachedSize >= 0L && now - cachedSizeAt < 5000L) {
			return cachedSize;
		}
		long size = 0L;
		final MinecraftServer server = minecraft.getSingleplayerServer();
		if (server != null) {
			final Path root = server.getWorldPath(LevelResource.ROOT);
			try (final var stream = Files.walk(root)) {
				size = stream.filter(Files::isRegularFile).mapToLong(path -> {
					try {
						return Files.size(path);
					} catch (final IOException ignored) {
						return 0L;
					}
				}).sum();
			} catch (final IOException ignored) {
			}
		}
		cachedSize = size;
		cachedSizeAt = now;
		return size;
	}

	public static class Size extends TagWorld {
		@Override
		public String getValue() {
			return String.valueOf(getWorldSize());
		}
	}

	public static class SizeMB extends TagWorld {
		@Override
		public String getValue() {
			return String.format(Locale.ENGLISH, "%.1f", getWorldSize() / 1048576.0);
		}
	}

	public static class Seed extends TagWorld {
		@Override
		public String getValue() {
			return String.valueOf(InGameInfoXML.seed);
		}
	}

	public static class Difficulty extends TagWorld {
		@Override
		public String getValue() {
			return I18n.get(world.getDifficulty().getKey());
		}
	}

	public static class DifficultyId extends TagWorld {
		@Override
		public String getValue() {
			return String.valueOf(world.getDifficulty().getId());
		}
	}

	public static class LocalDifficulty extends TagWorld {
		@Override
		public String getValue() {
			final DifficultyInstance diff = world.getCurrentDifficultyAt(playerPosition);
			return String.format(Locale.ENGLISH, "%.2f", diff.getEffectiveDifficulty());
		}
	}

	public static class Dimension extends TagWorld {
		@Override
		public String getValue() {
			final ResourceKey<Level> dim = world.dimension();
			if (dim.equals(Level.NETHER)) {
				return "the_nether";
			}
			if (dim.equals(Level.END)) {
				return "the_end";
			}
			return dim.location().getPath();
		}
	}

	public static class DimensionId extends TagWorld {
		@Override
		public String getValue() {
			final ResourceKey<Level> dim = world.dimension();
			if (dim.equals(Level.NETHER)) {
				return "-1";
			}
			if (dim.equals(Level.END)) {
				return "1";
			}
			return "0";
		}
	}

	public static class CurrentBiome extends TagWorld {
		@Override
		public String getValue() {
			final Holder<Biome> biome = Objects.requireNonNull(world).getBiome(playerPosition);
			return biome.unwrapKey()
					.map(key -> I18n.get(Util.makeDescriptionId("biome", key.location())))
					.orElse("");
		}
	}

	public static class BiomeId extends TagWorld {
		@Override
		public String getValue() {
			final Holder<Biome> biome = world.getBiome(playerPosition);
			final Registry<Biome> registry = world.registryAccess().registryOrThrow(Registries.BIOME);
			return String.valueOf(registry.getId(biome.value()));
		}
	}

	public static class Daytime extends TagWorld {
		@Override
		public String getValue() {
			return String.valueOf(world.getSkyDarken(1.0f) < 4);
		}
	}

	public static class MoonPhase extends TagWorld {
		@Override
		public String getValue() {
			return String.valueOf(world.getMoonPhase());
		}
	}

	public static class Raining extends TagWorld {
		@Override
		public String getValue() {
			final Holder<Biome> biome = world.getBiome(playerPosition);
			final boolean canRain = biome.value().warmEnoughToRain(playerPosition);
			return String.valueOf(world.isRaining() && canRain);
		}
	}

	public static class Thundering extends TagWorld {
		@Override
		public String getValue() {
			final Holder<Biome> biome = world.getBiome(playerPosition);
			final boolean canRain = biome.value().warmEnoughToRain(playerPosition);
			return String.valueOf(world.isThundering() && canRain);
		}
	}

	public static class Snowing extends TagWorld {
		@Override
		public String getValue() {
			final Holder<Biome> biome = world.getBiome(playerPosition);
			final boolean canSnow = biome.value().coldEnoughToSnow(playerPosition);
			return String.valueOf(world.isRaining() && canSnow);
		}
	}

	public static class NextWeatherChange extends TagWorld {
		@Override
		public String getValue() {
			final MinecraftServer server = minecraft.getSingleplayerServer();
			if (server == null) {
				return "?";
			}
			final ServerLevel overworld = server.overworld();
			final ServerLevelData data = (ServerLevelData) overworld.getLevelData();
			final int clearTime = data.getClearWeatherTime();
			final float seconds = (clearTime > 0 ? clearTime : data.getRainTime()) / 20f;
			if (seconds < 60) {
				return String.format(Locale.ENGLISH, "%.1fs", seconds);
			} else if (seconds < 3600) {
				return String.format(Locale.ENGLISH, "%.1fm", seconds / 60);
			}
			return String.format(Locale.ENGLISH, "%.1fh", seconds / 3600);
		}
	}

	public static class Slimes extends TagWorld {
		@Override
		public String getValue() {
			final boolean slimeChunk = ChunkHelper.isSlimeChunk(InGameInfoXML.seed, playerPosition);
			final int playerPos = playerPosition.y;
			final boolean isSwamp = world.getBiome(playerPosition).is(Biomes.SWAMP);
			return String.valueOf(InGameInfoXML.seed != 0 && slimeChunk || isSwamp && playerPos > 50 && playerPos < 70);
		}
	}

	public static class SlimeChunk extends TagWorld {
		@Override
		public String getValue() {
			return String.valueOf(InGameInfoXML.seed != 0 && ChunkHelper.isSlimeChunk(InGameInfoXML.seed, playerPosition));
		}
	}

	public static class Hardcore extends TagWorld {
		@Override
		public String getValue() {
			return String.valueOf(player.level().getLevelData().isHardcore());
		}
	}

	public static class Temperature extends TagWorld {
		@Override
		public String getValue() {
			return String.format(Locale.ENGLISH, "%.0f", world.getBiome(playerPosition).value().getBaseTemperature() * 100);
		}
	}

	public static class LocalTemperature extends TagWorld {
		@Override
		public String getValue() {
			return String.format(Locale.ENGLISH, "%.2f",
					world.getBiome(playerPosition).value().getBaseTemperature() * 100);
		}
	}

	public static class Humidity extends TagWorld {
		@Override
		public String getValue() {
			final float downfall = world.getBiome(playerPosition).value().getModifiedClimateSettings().downfall();
			return String.format(Locale.ENGLISH, "%.0f", downfall * 100);
		}
	}

	public static class TPS extends TagWorld {
		@Override
		public String getValue() {
			InGameInfoXML.existMSPT = true;
			return StringConvertUtils.getFloatDisplayFormat(InGameInfoXML.tps, ConfigurationHandler.tpsDecimalPlace);
		}
	}

	public static class MSPT extends TagWorld {
		@Override
		public String getValue() {
			InGameInfoXML.existMSPT = true;
			return StringConvertUtils.getFloatDisplayFormat(InGameInfoXML.mspt, ConfigurationHandler.msptDecimalPlace);
		}
	}

	public static void register() {
		TagRegistry.INSTANCE.register(new Name().setName("worldname"));
		TagRegistry.INSTANCE.register(new Size().setName("worldsize"));
		TagRegistry.INSTANCE.register(new SizeMB().setName("worldsizemb"));
		TagRegistry.INSTANCE.register(new Seed().setName("seed"));
		TagRegistry.INSTANCE.register(new Difficulty().setName("difficulty"));
		TagRegistry.INSTANCE.register(new DifficultyId().setName("difficultyid"));
		TagRegistry.INSTANCE.register(new LocalDifficulty().setName("localdifficulty"));
		TagRegistry.INSTANCE.register(new Dimension().setName("dimension"));
		TagRegistry.INSTANCE.register(new DimensionId().setName("dimensionid"));
		TagRegistry.INSTANCE.register(new CurrentBiome().setName("biome"));
		TagRegistry.INSTANCE.register(new BiomeId().setName("biomeid"));
		TagRegistry.INSTANCE.register(new Daytime().setName("daytime"));
		TagRegistry.INSTANCE.register(new MoonPhase().setName("moonphase"));
		TagRegistry.INSTANCE.register(new Raining().setName("raining"));
		TagRegistry.INSTANCE.register(new Thundering().setName("thundering"));
		TagRegistry.INSTANCE.register(new Snowing().setName("snowing"));
		TagRegistry.INSTANCE.register(new NextWeatherChange().setName("nextweatherchange").setAliases("nextrain"));
		TagRegistry.INSTANCE.register(new Slimes().setName("slimes"));
		TagRegistry.INSTANCE.register(new SlimeChunk().setName("slimechunk"));
		TagRegistry.INSTANCE.register(new Hardcore().setName("hardcore"));
		TagRegistry.INSTANCE.register(new Temperature().setName("temperature"));
		TagRegistry.INSTANCE.register(new LocalTemperature().setName("localtemperature"));
		TagRegistry.INSTANCE.register(new Humidity().setName("humidity"));
		TagRegistry.INSTANCE.register(new TPS().setName("tps"));
		TagRegistry.INSTANCE.register(new MSPT().setName("mspt"));
	}
}
