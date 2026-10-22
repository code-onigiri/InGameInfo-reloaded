package df11zomgraves.ingameinfo.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import df11zomgraves.ingameinfo.InGameInfoCore;
import df11zomgraves.ingameinfo.handler.ConfigurationHandler;
import df11zomgraves.ingameinfo.reference.Alignment;
import df11zomgraves.ingameinfo.reference.Names;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;

public class GuiConfigScreen extends Screen {
	private static final int ROW_HEIGHT = 24;
	private static final int ROWS_PER_PAGE = 7;

	private final Screen parent;
	private final List<Setting> settings = new ArrayList<>();
	private int page;

	public GuiConfigScreen(Screen parent) {
		super(Component.literal("InGameInfo XML"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		if (this.settings.isEmpty()) {
			buildSettings();
		}
		final int left = this.width / 2 - 155;
		final int control = this.width / 2 + 75;
		final int start = this.page * ROWS_PER_PAGE;
		for (int i = 0; i < ROWS_PER_PAGE && start + i < this.settings.size(); i++) {
			this.settings.get(start + i).init(left, control, 40 + i * ROW_HEIGHT, 150);
		}

		final int bottom = this.height - 28;
		addRenderableWidget(Button.builder(Component.literal("<"), button -> changePage(-1))
				.bounds(this.width / 2 - 160, bottom, 20, 20).build());
		addRenderableWidget(Button.builder(Component.literal(">"), button -> changePage(1))
				.bounds(this.width / 2 - 135, bottom, 20, 20).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.bounds(this.width / 2 + 5, bottom, 150, 20).build());
	}

	private void changePage(int delta) {
		commitSettings();
		final int pages = Math.max(1, (this.settings.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
		this.page = Math.floorMod(this.page + delta, pages);
		rebuildWidgets();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(guiGraphics);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);

		final int left = this.width / 2 - 155;
		final int start = this.page * ROWS_PER_PAGE;
		for (int i = 0; i < ROWS_PER_PAGE && start + i < this.settings.size(); i++) {
			this.settings.get(start + i).renderLabel(guiGraphics, this.font, left, 40 + i * ROW_HEIGHT + 6);
		}

		final int pages = Math.max(1, (this.settings.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
		guiGraphics.drawCenteredString(this.font, Component.literal((this.page + 1) + " / " + pages),
				this.width / 2 - 95, this.height - 22, 0xA0A0A0);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		final String oldName = ConfigurationHandler.configName;
		commitSettings();
		ConfigurationHandler.applyConfiguration();
		if (!oldName.equals(ConfigurationHandler.configName)) {
			InGameInfoCore.INSTANCE.setConfigDirectory(FMLPaths.CONFIGDIR.get().toFile());
			InGameInfoCore.INSTANCE.setConfigFile(ConfigurationHandler.configName);
			InGameInfoCore.INSTANCE.reloadConfig();
		}
		this.minecraft.setScreen(this.parent);
	}

	private void commitSettings() {
		for (Setting setting : this.settings) {
			setting.commit();
		}
	}

	private void buildSettings() {
		settings.add(new TextSetting(Names.Config.FILENAME, () -> ConfigurationHandler.configName,
				value -> ConfigurationHandler.configName = value));
		settings.add(new ScaleSetting());
		settings.add(new IntSetting(Names.Config.FILE_INTERVAL, () -> ConfigurationHandler.fileInterval,
				value -> ConfigurationHandler.fileInterval = value, 1, 60));
		settings.add(new BooleanSetting(Names.Config.REPLACE_DEBUG, () -> ConfigurationHandler.replaceDebug,
				value -> ConfigurationHandler.replaceDebug = value));
		settings.add(new BooleanSetting(Names.Config.SHOW_IN_CHAT, () -> ConfigurationHandler.showInChat,
				value -> ConfigurationHandler.showInChat = value));
		settings.add(new BooleanSetting(Names.Config.SHOW_ON_PLAYER_LIST, () -> ConfigurationHandler.showOnPlayerList,
				value -> ConfigurationHandler.showOnPlayerList = value));
		settings.add(new BooleanSetting(Names.Config.SHOW_OVERLAY_POTIONS, () -> ConfigurationHandler.showOverlayPotions,
				value -> ConfigurationHandler.showOverlayPotions = value));
		settings.add(new BooleanSetting(Names.Config.SHOW_OVERLAY_ITEM_ICONS,
				() -> ConfigurationHandler.showOverlayItemIcons,
				value -> ConfigurationHandler.showOverlayItemIcons = value));
		settings.add(new BooleanSetting(Names.Config.SHOW_SURVIVAL_HUD, () -> ConfigurationHandler.showSurvivalHUD,
				value -> ConfigurationHandler.showSurvivalHUD = value));
		settings.add(new BooleanSetting(Names.Config.NUMERIC_AMPLIFIER, () -> ConfigurationHandler.numericAmplifier,
				value -> ConfigurationHandler.numericAmplifier = value));
		settings.add(new BooleanSetting(Names.Config.SEND_SEED_TO_CHAT, () -> ConfigurationHandler.sendSeedToChat,
				value -> ConfigurationHandler.sendSeedToChat = value));
		settings.add(new BooleanSetting(Names.Config.DISPLAY_INVISIBLE_PLAYER,
				() -> ConfigurationHandler.displayInvisiblePlayer,
				value -> ConfigurationHandler.displayInvisiblePlayer = value));
		for (Alignment alignment : Alignment.values()) {
			settings.add(new AlignmentSetting(alignment));
		}
	}

	private abstract class Setting {
		private final Component label;

		Setting(String key) {
			this.label = Component.translatable(Names.Config.LANG_PREFIX + "." + key);
		}

		abstract void init(int left, int control, int y, int width);

		void renderLabel(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, int x, int y) {
			guiGraphics.drawString(font, this.label, x, y, 0xFFFFFF);
		}

		void commit() {
		}
	}

	private class BooleanSetting extends Setting {
		private final java.util.function.BooleanSupplier getter;
		private final java.util.function.Consumer<Boolean> setter;
		private Button button;

		BooleanSetting(String key, java.util.function.BooleanSupplier getter,
				java.util.function.Consumer<Boolean> setter) {
			super(key);
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		void init(int left, int control, int y, int width) {
			this.button = addRenderableWidget(Button.builder(value(this.getter.getAsBoolean()), button -> {
				final boolean value = !this.getter.getAsBoolean();
				this.setter.accept(value);
				button.setMessage(value(value));
			}).bounds(control, y, 80, 20).build());
		}

		private Component value(boolean value) {
			return Component.translatable(value ? "options.on" : "options.off");
		}
	}

	private class ScaleSetting extends Setting {
		private Button button;

		ScaleSetting() {
			super(Names.Config.SCALE);
		}

		@Override
		void init(int left, int control, int y, int width) {
			this.button = addRenderableWidget(Button.builder(scaleLabel(), button -> {
				final double current = ConfigurationHandler.scale == null ? 1.0D : ConfigurationHandler.scale;
				final double[] values = { 0.5D, 1.0D, 1.5D, 2.0D };
				int index = 0;
				for (int i = 0; i < values.length; i++) {
					if (Math.abs(values[i] - current) < 1.0E-6D) {
						index = i;
					}
				}
				ConfigurationHandler.scale = values[(index + 1) % values.length];
				button.setMessage(scaleLabel());
			}).bounds(control, y, 80, 20).build());
		}

		private Component scaleLabel() {
			final double current = ConfigurationHandler.scale == null ? 1.0D : ConfigurationHandler.scale;
			return Component.literal(String.format(java.util.Locale.ENGLISH, "%.1fx", current));
		}
	}

	private class IntSetting extends Setting {
		private final java.util.function.IntSupplier getter;
		private final java.util.function.IntConsumer setter;
		private final int min;
		private final int max;
		private EditBox box;

		IntSetting(String key, java.util.function.IntSupplier getter, java.util.function.IntConsumer setter, int min,
				int max) {
			super(key);
			this.getter = getter;
			this.setter = setter;
			this.min = min;
			this.max = max;
		}

		@Override
		void init(int left, int control, int y, int width) {
			this.box = addRenderableWidget(new EditBox(GuiConfigScreen.this.font, control, y, 60, 20,
					Component.empty()));
			this.box.setValue(Integer.toString(this.getter.getAsInt()));
			this.box.setFilter(value -> value.matches("-?\\d*"));
		}

		@Override
		void commit() {
			if (this.box == null) {
				return;
			}
			try {
				final int value = Integer.parseInt(this.box.getValue());
				this.setter.accept(Math.max(this.min, Math.min(this.max, value)));
			} catch (NumberFormatException ignored) {
			}
		}
	}

	private class TextSetting extends Setting {
		private final java.util.function.Supplier<String> getter;
		private final java.util.function.Consumer<String> setter;
		private EditBox box;

		TextSetting(String key, java.util.function.Supplier<String> getter,
				java.util.function.Consumer<String> setter) {
			super(key);
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		void init(int left, int control, int y, int width) {
			final int boxWidth = Math.max(60, Math.min(width, GuiConfigScreen.this.width - control - 10));
			this.box = addRenderableWidget(new EditBox(GuiConfigScreen.this.font, control, y, boxWidth, 20,
					Component.empty()));
			this.box.setValue(this.getter.get());
			this.box.setMaxLength(128);
		}

		@Override
		void commit() {
			if (this.box == null) {
				return;
			}
			if (!this.box.getValue().isBlank()) {
				this.setter.accept(this.box.getValue().trim());
			}
		}
	}

	private class AlignmentSetting extends Setting {
		private final Alignment alignment;
		private EditBox xBox;
		private EditBox yBox;

		AlignmentSetting(Alignment alignment) {
			super(alignment.name().toLowerCase(java.util.Locale.ENGLISH));
			this.alignment = alignment;
		}

		@Override
		void init(int left, int control, int y, int width) {
			this.xBox = addRenderableWidget(new EditBox(GuiConfigScreen.this.font, control, y, 50, 20,
					Component.empty()));
			this.yBox = addRenderableWidget(new EditBox(GuiConfigScreen.this.font, control + 55, y, 50, 20,
					Component.empty()));
			this.xBox.setValue(Integer.toString(this.alignment.x));
			this.yBox.setValue(Integer.toString(this.alignment.y));
			this.xBox.setFilter(value -> value.matches("-?\\d*"));
			this.yBox.setFilter(value -> value.matches("-?\\d*"));
		}

		@Override
		void commit() {
			if (this.xBox == null || this.yBox == null) {
				return;
			}
			try {
				this.alignment.x = Integer.parseInt(this.xBox.getValue());
				this.alignment.y = Integer.parseInt(this.yBox.getValue());
			} catch (NumberFormatException ignored) {
			}
		}
	}
}
