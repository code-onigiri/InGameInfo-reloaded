package df11zomgraves.ingameinfo;

import df11zomgraves.ingameinfo.gui.overlay.Info;
import df11zomgraves.ingameinfo.gui.overlay.InfoText;
import df11zomgraves.ingameinfo.handler.ConfigurationHandler;
import df11zomgraves.ingameinfo.parser.IParser;
import df11zomgraves.ingameinfo.parser.json.JsonParser;
import df11zomgraves.ingameinfo.parser.text.TextParser;
import df11zomgraves.ingameinfo.parser.xml.XmlParser;
import df11zomgraves.ingameinfo.printer.IPrinter;
import df11zomgraves.ingameinfo.printer.json.JsonPrinter;
import df11zomgraves.ingameinfo.printer.text.TextPrinter;
import df11zomgraves.ingameinfo.printer.text.TextSerializer;
import df11zomgraves.ingameinfo.printer.xml.XmlPrinter;
import df11zomgraves.ingameinfo.reference.Alignment;
import df11zomgraves.ingameinfo.reference.Names;
import df11zomgraves.ingameinfo.tag.Tag;
import df11zomgraves.ingameinfo.value.Value;
import df11zomgraves.ingameinfo.value.ValueComplex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.blaze3d.platform.Window;

public class InGameInfoCore {
	private static final Pattern PATTERN = Pattern.compile("\\{ICON\\|( *)\\}", Pattern.CASE_INSENSITIVE);
	private static final Matcher MATCHER = PATTERN.matcher("");
	public static final InGameInfoCore INSTANCE = new InGameInfoCore();

	private IParser parser;

	private final Minecraft minecraft = Minecraft.getInstance();
	private File configDirectory = null;
	private File configFile = null;
	private boolean usingBundledDefault = false;
	private final Map<Alignment, List<List<Value>>> format = new HashMap<Alignment, List<List<Value>>>();
	private final List<Info> info = new ArrayList<Info>();
	private final List<Info> infoItemQueue = new ArrayList<Info>();
	private volatile boolean reloadRequested = false;

	private InGameInfoCore() {
		Tag.setInfo(this.infoItemQueue);
		Value.setInfo(this.infoItemQueue);
	}

	public boolean setConfigDirectory(final File directory) {
		this.configDirectory = directory;
		return true;
	}

	public File getConfigDirectory() {
		return this.configDirectory;
	}

	public boolean setConfigFile(final String filename) {
		if (filename == null) {
			InGameInfoXML.logger.warn("No config file name given");
			return false;
		}

		if (filename.equalsIgnoreCase("default")) {
			this.configFile = null;
			this.parser = new XmlParser();
			this.usingBundledDefault = true;
			return true;
		}

		final File file = resolveConfigFile(filename);
		if (file != null && file.exists()) {
			final String lowerName = filename.toLowerCase(Locale.ENGLISH);
			if (lowerName.endsWith(Names.Files.EXT_XML)) {
				this.configFile = file;
				this.parser = new XmlParser();
			} else if (lowerName.endsWith(Names.Files.EXT_JSON)) {
				this.configFile = file;
				this.parser = new JsonParser();
			} else if (lowerName.endsWith(Names.Files.EXT_TXT)) {
				this.configFile = file;
				this.parser = new TextParser();
			}
			this.usingBundledDefault = false;
			return true;
		}

		if (file != null && filename.equals(ConfigurationHandler.configName)) {
			// The configured file has not been created yet (e.g. first run): use the bundled default.
			this.configFile = null;
			this.parser = new XmlParser();
			this.usingBundledDefault = true;
			return true;
		}

		InGameInfoXML.logger.warn("The config '{}' does not exist", filename);
		return false;
	}

	/**
	 * Resolves a HUD file name inside the config directory. Names that escape the
	 * directory or lack a supported extension are rejected.
	 *
	 * @return the resolved file, or null when the name is not usable
	 */
	private File resolveConfigFile(final String filename) {
		if (this.configDirectory == null || filename == null) {
			return null;
		}

		final String lowerName = filename.toLowerCase(Locale.ENGLISH);
		if (!lowerName.endsWith(Names.Files.EXT_XML) && !lowerName.endsWith(Names.Files.EXT_JSON)
				&& !lowerName.endsWith(Names.Files.EXT_TXT)) {
			InGameInfoXML.logger.warn("'{}' is an invalid file name", filename);
			return null;
		}

		try {
			final File root = this.configDirectory.getCanonicalFile();
			final File file = new File(root, filename).getCanonicalFile();
			if (!file.getPath().startsWith(root.getPath() + File.separator)) {
				InGameInfoXML.logger.warn("The config '{}' is outside the config directory", filename);
				return null;
			}
			return file;
		} catch (final IOException e) {
			InGameInfoXML.logger.error("Failed to resolve the config '{}'", filename, e);
			return null;
		}
	}

	public void onTickClient() {
		final ClientLevel world = this.minecraft.level;
		if (world == null)
			return;
		Tag.setWorld(world);

		final LocalPlayer player = this.minecraft.player;
		if (player == null)
			return;
		Tag.setPlayer(player);
		
		Window window = this.minecraft.getWindow();
		final int scaledWidth = (int) (window.getGuiScaledWidth() / ConfigurationHandler.scale);
		final int scaledHeight = (int) (window.getGuiScaledHeight() / ConfigurationHandler.scale);
		this.info.clear();
		int x, y;
		for (final Alignment alignment : Alignment.values()) {
			final List<List<Value>> lines = this.format.get(alignment);

			if (lines == null)
				continue;

			final Font fontRenderer = this.minecraft.font;
			final List<Info> queue = new ArrayList<Info>();

			for (final List<Value> line : lines) {
				StringBuilder str = new StringBuilder();

				this.infoItemQueue.clear();
				
				String s;
				for (final Value value : line) {
					s = getValue(value);
					str.append(s);
				}
				
				if (str.length() > 0) {
					final String processed = str.toString().replaceAll("\\{ICON\\|( *)\\}", "$1");

					x = alignment.getX(scaledWidth, fontRenderer.width(processed));
					final InfoText text = new InfoText(fontRenderer, processed, x, 0);
					
					if (this.infoItemQueue.size() > 0) {
						MATCHER.reset(str.toString());

						for (int i = 0; i < this.infoItemQueue.size() && MATCHER.find(); i++) {
							final Info item = this.infoItemQueue.get(i);
							item.x = fontRenderer.width(str.substring(0, MATCHER.start()));
							text.children.add(item);

							str = new StringBuilder(
									str.toString().replaceFirst(Pattern.quote(MATCHER.group(0)), MATCHER.group(1)));
							MATCHER.reset(str.toString());
						}
					}
					queue.add(text);
				}
			}

			y = alignment.getY(scaledHeight, queue.size() * (fontRenderer.lineHeight + 1));
			for (final Info item : queue) {
				item.y = y;
				y += fontRenderer.lineHeight + 1;
			}

			this.info.addAll(queue);
		}

		Tag.releaseResources();
		ValueComplex.ValueFile.tick();
	}

	public void onTickRender(GuiGraphics guiGraphics) {
		final float scale = ConfigurationHandler.scale == null ? 1.0f : ConfigurationHandler.scale.floatValue();
		guiGraphics.pose().pushPose();
		guiGraphics.pose().scale(scale, scale, 1.0f);
		for (final Info info : this.info) {
			info.draw(guiGraphics);
		}
		guiGraphics.pose().popPose();
	}

	/**
	 * Requests a reload that is performed on the render thread. Mod config reloads
	 * are fired from the config file watcher thread, so clearing the HUD lists
	 * there would race with {@link #onTickRender(GuiGraphics)}.
	 */
	public void requestReload() {
		this.reloadRequested = true;
	}

	public void processPendingReload() {
		if (this.reloadRequested) {
			this.reloadRequested = false;
			this.reloadConfig();
		}
	}

	public boolean loadConfig(final String filename) {
		return setConfigFile(filename) && reloadConfig();
	}

	public boolean reloadConfig() {
		if (this.parser == null) {
			return false;
		}

		final InputStream inputStream = getInputStream();
		if (inputStream == null) {
			InGameInfoXML.logger.error("Failed to reload the config: no input stream available");
			return false;
		}

		// Parse into a fresh format first, so a broken file keeps the HUD currently shown.
		final Map<Alignment, List<List<Value>>> newFormat = new HashMap<Alignment, List<List<Value>>>();
		try {
			if (this.parser.load(inputStream) && this.parser.parse(newFormat)) {
				this.format.clear();
				this.format.putAll(newFormat);
				this.info.clear();
				this.infoItemQueue.clear();
				InGameInfoXML.existMSPT = false;
				return true;
			}
		} catch (final Exception e) {
			InGameInfoXML.logger.error("Failed to reload the config", e);
			minecraft.gui.getChat().addMessage(Component.translatable(e.getMessage()));
			return false;
		}

		InGameInfoXML.logger.error("Failed to reload the config: the file could not be parsed");
		return false;
	}

	public boolean saveConfig(final String filename) {
		final File file = resolveConfigFile(filename);
		if (file == null) {
			return false;
		}

		IPrinter printer = null;
		final String lowerName = filename.toLowerCase(Locale.ENGLISH);
		if (lowerName.endsWith(Names.Files.EXT_XML)) {
			printer = new XmlPrinter();
		} else if (lowerName.endsWith(Names.Files.EXT_JSON)) {
			printer = new JsonPrinter();
		} else if (lowerName.endsWith(Names.Files.EXT_TXT)) {
			printer = new TextPrinter();
		}

		if (printer == null) {
			return false;
		}

		final File directory = file.getParentFile();
		if (directory != null && !directory.exists() && !directory.mkdirs()) {
			InGameInfoXML.logger.error("Failed to create the directory '{}'", directory);
			return false;
		}

		return printer.print(file, this.format);
	}

	/** @return the file name currently loaded, or the configured default file name */
	public String getConfigFileName() {
		if (this.configFile != null) {
			return this.configFile.getName();
		}
		if (this.usingBundledDefault) {
			return Names.Files.FILE_XML;
		}
		return ConfigurationHandler.configName;
	}

	/** @return the editable raw lines for one alignment, in text configuration syntax */
	public List<String> getLines(final Alignment alignment) {
		final List<String> result = new ArrayList<String>();
		final List<List<Value>> lines = this.format.get(alignment);
		if (lines != null) {
			for (final List<Value> line : lines) {
				result.add(TextSerializer.serialize(line));
			}
		}
		return result;
	}

	/**
	 * Replaces one alignment with raw text configuration lines.
	 *
	 * @return false when any line is invalid; the previous content is kept
	 */
	public boolean setLines(final Alignment alignment, final List<String> lines) {
		final List<List<Value>> parsed = new ArrayList<List<Value>>();
		for (final String line : lines) {
			final List<Value> values = TextParser.parseLine(line);
			if (values == null) {
				InGameInfoXML.logger.warn("Invalid text line for {}: {}", alignment, line);
				return false;
			}
			parsed.add(values);
		}

		this.format.put(alignment, parsed);
		return true;
	}

	/** Saves the currently loaded format back to its source file. */
	public boolean saveCurrentConfig() {
		final String filename = getConfigFileName();
		if (!this.saveConfig(filename)) {
			return false;
		}
		return this.setConfigFile(filename);
	}

	private InputStream getInputStream() {
		InputStream inputStream = null;

		try {
			if (this.configFile != null && this.configFile.exists()) {
				InGameInfoXML.logger.debug("Loading file config...");
				inputStream = new FileInputStream(this.configFile);
			} else {
				InGameInfoXML.logger.debug("Loading default config...");
				final ResourceLocation resourceLocation = new ResourceLocation("ingameinfo",
						Names.Files.FILE_XML.toLowerCase(Locale.ENGLISH));
				final Optional<Resource> resource = this.minecraft.getResourceManager().getResource(resourceLocation);
				if (resource.isEmpty()) {
					InGameInfoXML.logger.error("The bundled default config '{}' is missing", resourceLocation);
					return null;
				}
				inputStream = resource.get().open();
			}
		} catch (final Exception e) {
			InGameInfoXML.logger.error(e.getMessage(), e);
		}

		return inputStream;
	}

	private String getValue(final Value value) {
		try {
			if (value.isValidSize()) {
				return value.getReplacedValue();
			}
		} catch (final Exception e) {
			InGameInfoXML.logger.debug("Failed to get value!", e);
			return "null";
		}

		return "";
	}
}
