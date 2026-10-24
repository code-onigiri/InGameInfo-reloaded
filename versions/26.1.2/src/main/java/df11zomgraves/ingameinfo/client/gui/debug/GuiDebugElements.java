package df11zomgraves.ingameinfo.client.gui.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import df11zomgraves.ingameinfo.tag.Tag;
import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Read-only inspector for every registered tag. It shows the live value next to
 * each tag so optional mod integrations can be checked without editing the HUD.
 */
public class GuiDebugElements extends Screen {
	private static final int ROW_HEIGHT = 12;
	private static final int NAME_WIDTH = 150;
	private static final Component SEARCH = Component.translatable("gui.ingameinfoxml.debug.search");

	private final Map<String, List<Tag>> categories = new LinkedHashMap<String, List<Tag>>();
	private final Map<String, String> categoryIds = new HashMap<String, String>();
	private final Map<String, String> values = new HashMap<String, String>();
	private final List<Row> rows = new ArrayList<Row>();

	private EditBox searchField;
	private String filter = "";
	private int scroll;
	private int valueTick;
	private int tagCount;
	private int categoryCount;
	private String status = "";
	private boolean statusError;

	public GuiDebugElements() {
		super(Component.translatable("gui.ingameinfoxml.debug"));
	}

	@Override
	protected void init() {
		this.buildTags();
		this.rebuildRows();

		this.searchField = new EditBox(this.font, this.width / 2 - 155, this.height - 28, 150, 18, SEARCH);
		this.searchField.setHint(SEARCH);
		this.searchField.setValue(this.filter);
		this.searchField.setResponder(value -> {
			this.filter = value.toLowerCase(Locale.ENGLISH);
			this.rebuildRows();
			this.scroll = 0;
		});
		addRenderableWidget(this.searchField);
		this.setFocused(this.searchField);

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
		this.refreshValues();
	}

	private void buildTags() {
		if (!this.categories.isEmpty()) {
			return;
		}
		final Set<String> seen = new HashSet<String>();
		for (final Tag tag : TagRegistry.INSTANCE.getUniqueTags()) {
			if (!seen.add(displayName(tag))) {
				continue;
			}
			final String category = tag.getLocalizedCategory();
			this.categoryIds.put(category, tag.getCategory());
			this.categories.computeIfAbsent(category, key -> new ArrayList<Tag>()).add(tag);
		}
		// Formatting codes are the least useful rows, so keep them at the end.
		final List<String> order = new ArrayList<String>(this.categories.keySet());
		order.sort((a, b) -> {
			final boolean formattingA = "formatting".equals(this.categoryIds.get(a));
			final boolean formattingB = "formatting".equals(this.categoryIds.get(b));
			if (formattingA != formattingB) {
				return formattingA ? 1 : -1;
			}
			return a.compareToIgnoreCase(b);
		});
		final Map<String, List<Tag>> sorted = new LinkedHashMap<String, List<Tag>>();
		for (final String category : order) {
			sorted.put(category, this.categories.get(category));
		}
		this.categories.clear();
		this.categories.putAll(sorted);
		this.categoryCount = this.categories.size();
		for (final List<Tag> list : this.categories.values()) {
			list.sort((a, b) -> displayName(a).compareToIgnoreCase(displayName(b)));
			this.tagCount += list.size();
		}
	}

	private void rebuildRows() {
		this.rows.clear();
		for (final Map.Entry<String, List<Tag>> category : this.categories.entrySet()) {
			final List<Tag> matches = new ArrayList<Tag>();
			for (final Tag tag : category.getValue()) {
				if (this.filter.isEmpty() || displayName(tag).toLowerCase(Locale.ENGLISH).contains(this.filter)
						|| tag.getLocalizedDescription().toLowerCase(Locale.ENGLISH).contains(this.filter)) {
					matches.add(tag);
				}
			}
			if (matches.isEmpty()) {
				continue;
			}
			this.rows.add(Row.header(category.getKey() + " (" + matches.size() + ")"));
			for (final Tag tag : matches) {
				this.rows.add(Row.tag(tag));
			}
		}
	}

	private void refreshValues() {
		Tag.setWorld(this.minecraft.level);
		Tag.setPlayer(this.minecraft.player);
		this.values.clear();
		for (final List<Tag> list : this.categories.values()) {
			for (final Tag tag : list) {
				this.values.put(displayName(tag), Tag.getValueForDebug(tag));
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (++this.valueTick >= 20) {
			this.valueTick = 0;
			this.refreshValues();
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		// Screen は背景を先に描き、そのあと extractRenderState を呼ぶ。
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		this.renderInspector(guiGraphics, mouseX, mouseY);
	}

	private void renderInspector(final GuiGraphicsExtractor guiGraphics, final int mouseX, final int mouseY) {
		this.scroll = Math.max(0, Math.min(this.scroll, Math.max(0, this.rows.size() - this.visibleRows())));
		final int left = this.listLeft();
		final int right = this.listRight();
		final int top = this.listTop();
		final int bottom = this.listBottom();

		guiGraphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
		guiGraphics.centeredText(this.font,
				Component.translatable("gui.ingameinfoxml.debug.count", this.tagCount, this.categoryCount),
				this.width / 2, 24, 0xFFA0A0A0);

		guiGraphics.fill(left - 2, top - 2, right + 2, bottom + 2, 0x90000000);
		guiGraphics.enableScissor(left, top, right, bottom);
		final int end = Math.min(this.rows.size(), this.scroll + this.visibleRows());
		for (int i = this.scroll; i < end; i++) {
			final Row row = this.rows.get(i);
			final int y = top + (i - this.scroll) * ROW_HEIGHT;
			if (row.header) {
				guiGraphics.text(this.font, "\u00a77" + row.label, left + 4, y + 2, 0xFFA0A0A0);
				continue;
			}
			if (mouseX >= left && mouseX < right && mouseY >= y && mouseY < y + ROW_HEIGHT) {
				guiGraphics.fill(left, y, right, y + ROW_HEIGHT, 0x40FFFFFF);
			}
			final String value = this.values.getOrDefault(row.label, "");
			final int color = value.startsWith("ERROR:") ? 0xFFFF7070 : 0xFFB0B0B0;
			guiGraphics.text(this.font, row.label, left + 4, y + 2, 0xFFFFFFFF);
			guiGraphics.text(this.font,
					this.font.plainSubstrByWidth(value, Math.max(0, right - (left + NAME_WIDTH) - 8)),
					left + NAME_WIDTH, y + 2, color);
		}
		guiGraphics.disableScissor();

		if (this.rows.isEmpty()) {
			guiGraphics.centeredText(this.font, Component.translatable("gui.ingameinfoxml.debug.empty"),
					this.width / 2, top + 6, 0xFF808080);
		}
		if (this.searchField != null && this.searchField.isFocused() && this.searchField.getValue().isEmpty()) {
			guiGraphics.text(this.font, SEARCH.getString(), this.width / 2 - 155 + 4, this.height - 28 + 5, 0xFF808080);
		}
		this.renderScrollbar(guiGraphics, right, top, bottom);
		guiGraphics.centeredText(this.font,
				this.status.isEmpty() ? Component.translatable("gui.ingameinfoxml.debug.hint") : Component.literal(this.status),
				this.width / 2, bottom + 4, this.statusError ? 0xFFFF7070 : 0xFF70FF70);
	}

	private void renderScrollbar(final GuiGraphicsExtractor guiGraphics, final int right, final int top, final int bottom) {
		final int maximum = Math.max(0, this.rows.size() - this.visibleRows());
		if (maximum <= 0) {
			return;
		}
		final int x = right - 4;
		final int height = bottom - top;
		final int thumbHeight = Math.max(10, height * this.visibleRows() / this.rows.size());
		final int thumbTop = top + (height - thumbHeight) * this.scroll / maximum;
		guiGraphics.fill(x, top, x + 3, bottom, 0x40FFFFFF);
		guiGraphics.fill(x, thumbTop, x + 3, thumbTop + thumbHeight, 0xFFA0A0A0);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && this.isOverList(event.x(), event.y())) {
			final int index = this.scroll + (int) ((event.y() - this.listTop()) / ROW_HEIGHT);
			if (index >= 0 && index < this.rows.size()) {
				final Row row = this.rows.get(index);
				if (!row.header) {
					this.copy(row);
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.isOverList(mouseX, mouseY)) {
			final int maximum = Math.max(0, this.rows.size() - this.visibleRows());
			this.scroll = Math.max(0, Math.min(maximum, this.scroll - (int) Math.signum(scrollY) * 3));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private void copy(final Row row) {
		final String text = "<" + row.insert + ">";
		this.minecraft.keyboardHandler.setClipboard(text);
		this.status = Component.translatable("gui.ingameinfoxml.debug.copied", text).getString();
		this.statusError = false;
	}

	private boolean isOverList(final double mouseX, final double mouseY) {
		return mouseX >= this.listLeft() && mouseX < this.listRight() && mouseY >= this.listTop()
				&& mouseY < this.listBottom();
	}

	private int listLeft() {
		return Math.max(8, this.width / 2 - 260);
	}

	private int listRight() {
		return Math.min(this.width - 8, this.width / 2 + 260);
	}

	private int listTop() {
		return 40;
	}

	private int listBottom() {
		return this.height - 54;
	}

	private int visibleRows() {
		return Math.max(1, (this.listBottom() - this.listTop()) / ROW_HEIGHT);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static String displayName(final Tag tag) {
		return tag.isIndexed() ? tag.getName() : tag.getFormattedName();
	}

	private static final class Row {
		private final boolean header;
		private final String label;
		private final String insert;

		private Row(final boolean header, final String label, final String insert) {
			this.header = header;
			this.label = label;
			this.insert = insert;
		}

		private static Row header(final String label) {
			return new Row(true, label, "");
		}

		private static Row tag(final Tag tag) {
			final String name = displayName(tag);
			return new Row(false, name, tag.isIndexed() ? name : tag.getRawName());
		}
	}
}
