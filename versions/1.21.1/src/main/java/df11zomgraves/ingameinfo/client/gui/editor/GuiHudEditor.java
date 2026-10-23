package df11zomgraves.ingameinfo.client.gui.editor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import df11zomgraves.ingameinfo.InGameInfoCore;
import df11zomgraves.ingameinfo.client.gui.tag.GuiTags;
import df11zomgraves.ingameinfo.handler.ConfigurationHandler;
import df11zomgraves.ingameinfo.reference.Alignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Bottom-sheet HUD editor. The upper part of the screen stays clear so the live
 * overlay remains visible while lines are edited.
 */
public class GuiHudEditor extends Screen {
	private static final int LINE_HEIGHT = 11;
	private static final int PANEL_HEIGHT = 136;

	private final InGameInfoCore core = InGameInfoCore.INSTANCE;
	private final Map<Alignment, List<String>> lines = new EnumMap<Alignment, List<String>>(Alignment.class);
	private final Button[] alignmentButtons = new Button[Alignment.values().length];

	private boolean initialized;
	private Alignment selectedAlignment = Alignment.TOPLEFT;
	private int selectedLine = -1;
	private int firstLine;
	private int pendingCaret = -1;
	private String status = "";
	private boolean statusError;

	private EditBox lineBox;
	private EditBox xBox;
	private EditBox yBox;
	private boolean updating;

	public GuiHudEditor() {
		super(Component.translatable("gui.ingameinfoxml.editor"));
	}

	@Override
	protected void init() {
		if (!this.initialized) {
			for (final Alignment alignment : Alignment.values()) {
				this.lines.put(alignment, new ArrayList<String>(this.core.getLines(alignment)));
			}
			this.selectedLine = this.lines.get(this.selectedAlignment).isEmpty() ? -1 : 0;
			this.initialized = true;
		} else {
			this.clampSelection();
		}

		final int panelTop = this.panelTop();
		final int buttonY = panelTop + 4;

		addRenderableWidget(this.button("gui.ingameinfoxml.editor.add", 6, buttonY, 32, 16, b -> this.addLine()));
		addRenderableWidget(this.button("gui.ingameinfoxml.editor.remove", 40, buttonY, 32, 16, b -> this.removeLine()));
		addRenderableWidget(this.button("gui.ingameinfoxml.editor.up", 74, buttonY, 32, 16, b -> this.moveLine(-1)));
		addRenderableWidget(this.button("gui.ingameinfoxml.editor.down", 108, buttonY, 32, 16, b -> this.moveLine(1)));

		int index = 0;
		for (final Alignment alignment : Alignment.values()) {
			final int column = index % 3;
			final int row = index / 3;
			final Button button = Button.builder(Component.literal(shortName(alignment)),
					b -> this.selectAlignment(alignment)).bounds(150 + column * 42, panelTop + 4 + row * 18, 40, 16).build();
			button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(alignment.name())));
			this.alignmentButtons[index] = button;
			addRenderableWidget(button);
			index++;
		}

		this.lineBox = new EditBox(this.font, 150, panelTop + 62, this.width - 156, 16,
				Component.translatable("gui.ingameinfoxml.editor.line"));
		this.lineBox.setMaxLength(2048);
		this.lineBox.setResponder(value -> this.onLineChanged());
		addRenderableWidget(this.lineBox);

		addRenderableWidget(this.button("gui.ingameinfoxml.editor.insert", 150, panelTop + 82, 48, 16, b -> this.openTagPicker()));

		this.xBox = new EditBox(this.font, 240, panelTop + 82, 38, 16, Component.literal("X"));
		this.xBox.setMaxLength(6);
		this.xBox.setHint(Component.literal("X"));
		this.xBox.setResponder(value -> this.onOffsetChanged(true));
		addRenderableWidget(this.xBox);

		this.yBox = new EditBox(this.font, 282, panelTop + 82, 38, 16, Component.literal("Y"));
		this.yBox.setMaxLength(6);
		this.yBox.setHint(Component.literal("Y"));
		this.yBox.setResponder(value -> this.onOffsetChanged(false));
		addRenderableWidget(this.yBox);

		addRenderableWidget(this.button("gui.ingameinfoxml.editor.save", 150, panelTop + 104, 54, 16, b -> this.save()));
		addRenderableWidget(this.button("gui.ingameinfoxml.editor.done", 208, panelTop + 104, 54, 16, b -> {
			if (this.save()) {
				this.onClose();
			}
		}));

		this.refreshFields();
	}

	private Button button(final String key, final int x, final int y, final int width, final int height,
			final Button.OnPress onPress) {
		return Button.builder(Component.translatable(key), onPress).bounds(x, y, width, height).build();
	}

	private int panelTop() {
		return Math.max(92, this.height - PANEL_HEIGHT);
	}

	private int listTop() {
		return this.panelTop() + 24;
	}

	private int listBottom() {
		return this.height - 6;
	}

	private int visibleLines() {
		return Math.max(1, (this.listBottom() - this.listTop()) / LINE_HEIGHT);
	}

	private static String shortName(final Alignment alignment) {
		switch (alignment) {
		case TOPLEFT:
			return "TL";
		case TOPCENTER:
			return "TC";
		case TOPRIGHT:
			return "TR";
		case MIDDLELEFT:
			return "ML";
		case MIDDLECENTER:
			return "MC";
		case MIDDLERIGHT:
			return "MR";
		case BOTTOMLEFT:
			return "BL";
		case BOTTOMCENTER:
			return "BC";
		case BOTTOMRIGHT:
			return "BR";
		default:
			return "?";
		}
	}

	private List<String> currentLines() {
		return this.lines.computeIfAbsent(this.selectedAlignment, key -> new ArrayList<String>());
	}

	private void clampSelection() {
		final List<String> list = this.currentLines();
		if (list.isEmpty()) {
			this.selectedLine = -1;
		} else if (this.selectedLine < 0) {
			this.selectedLine = 0;
		} else if (this.selectedLine >= list.size()) {
			this.selectedLine = list.size() - 1;
		}
		final int visible = this.visibleLines();
		this.firstLine = Math.max(0, Math.min(this.firstLine, Math.max(0, list.size() - visible)));
		if (this.selectedLine >= 0) {
			if (this.selectedLine < this.firstLine) {
				this.firstLine = this.selectedLine;
			} else if (this.selectedLine >= this.firstLine + visible) {
				this.firstLine = this.selectedLine - visible + 1;
			}
		}
	}

	private void selectAlignment(final Alignment alignment) {
		this.selectedAlignment = alignment;
		this.selectedLine = this.lines.get(alignment).isEmpty() ? -1 : 0;
		this.firstLine = 0;
		this.refreshFields();
	}

	private void refreshFields() {
		this.clampSelection();
		this.updating = true;
		final List<String> list = this.currentLines();
		if (this.selectedLine >= 0 && this.selectedLine < list.size()) {
			this.lineBox.setEditable(true);
			this.lineBox.setValue(list.get(this.selectedLine));
			if (this.pendingCaret >= 0) {
				this.lineBox.setCursorPosition(Math.min(this.pendingCaret, this.lineBox.getValue().length()));
				this.pendingCaret = -1;
			} else {
				this.lineBox.moveCursorToEnd(true);
			}
		} else {
			this.lineBox.setValue("");
			this.lineBox.setEditable(false);
		}
		this.xBox.setValue(String.valueOf(this.selectedAlignment.x));
		this.yBox.setValue(String.valueOf(this.selectedAlignment.y));
		this.updating = false;
	}

	private void onLineChanged() {
		if (this.updating || this.selectedLine < 0) {
			return;
		}
		final List<String> list = this.currentLines();
		if (this.selectedLine >= list.size()) {
			return;
		}
		list.set(this.selectedLine, this.lineBox.getValue());
		this.applyLines();
	}

	private void onOffsetChanged(final boolean x) {
		if (this.updating) {
			return;
		}
		final EditBox box = x ? this.xBox : this.yBox;
		if (box.getValue().isEmpty()) {
			return;
		}
		try {
			final int value = Integer.parseInt(box.getValue());
			if (x) {
				this.selectedAlignment.x = value;
			} else {
				this.selectedAlignment.y = value;
			}
		} catch (final NumberFormatException ignored) {
			// keep the last valid value while the player is typing
		}
	}

	private boolean applyLines() {
		for (final Alignment alignment : Alignment.values()) {
			if (!this.core.setLines(alignment, this.lines.get(alignment))) {
				this.status = Component.translatable("gui.ingameinfoxml.editor.invalid").getString();
				this.statusError = true;
				return false;
			}
		}
		if (this.statusError) {
			this.status = "";
			this.statusError = false;
		}
		return true;
	}

	private void addLine() {
		final List<String> list = this.currentLines();
		final int at = this.selectedLine < 0 ? list.size() : this.selectedLine + 1;
		list.add(at, "");
		this.selectedLine = at;
		this.applyLines();
		this.refreshFields();
		this.setFocused(this.lineBox);
	}

	private void removeLine() {
		final List<String> list = this.currentLines();
		if (this.selectedLine < 0 || this.selectedLine >= list.size()) {
			return;
		}
		list.remove(this.selectedLine);
		this.clampSelection();
		this.applyLines();
		this.refreshFields();
	}

	private void moveLine(final int direction) {
		final List<String> list = this.currentLines();
		final int target = this.selectedLine + direction;
		if (this.selectedLine < 0 || target < 0 || target >= list.size()) {
			return;
		}
		final String line = list.get(this.selectedLine);
		list.set(this.selectedLine, list.get(target));
		list.set(target, line);
		this.selectedLine = target;
		this.applyLines();
		this.refreshFields();
	}

	private void openTagPicker() {
		if (this.selectedLine < 0) {
			this.status = Component.translatable("gui.ingameinfoxml.editor.selectline").getString();
			this.statusError = true;
			return;
		}
		this.pendingCaret = this.lineBox.getCursorPosition();
		this.minecraft.setScreen(new GuiTags(this, tag -> this.insertTag(tag)));
	}

	private void insertTag(final String tag) {
		if (this.selectedLine < 0) {
			return;
		}
		final List<String> list = this.currentLines();
		if (this.selectedLine >= list.size()) {
			return;
		}
		final String line = list.get(this.selectedLine);
		final int position = Math.min(Math.max(this.pendingCaret, 0), line.length());
		list.set(this.selectedLine, line.substring(0, position) + tag + line.substring(position));
		this.pendingCaret = position + tag.length();
		this.applyLines();
	}

	private boolean save() {
		if (!this.applyLines()) {
			return false;
		}
		final boolean saved = this.core.saveCurrentConfig();
		if (saved) {
			ConfigurationHandler.applyConfiguration();
			this.status = Component.translatable("gui.ingameinfoxml.editor.saved", this.core.getConfigFileName()).getString();
			this.statusError = false;
		} else {
			this.status = Component.translatable("gui.ingameinfoxml.editor.savefailed").getString();
			this.statusError = true;
		}
		return saved;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && this.isOverList(mouseX, mouseY)) {
			final int index = this.firstLine + (int) ((mouseY - this.listTop()) / LINE_HEIGHT);
			if (index >= 0 && index < this.currentLines().size()) {
				this.selectedLine = index;
				this.refreshFields();
				this.setFocused(this.lineBox);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.isOverList(mouseX, mouseY)) {
			final int max = Math.max(0, this.currentLines().size() - this.visibleLines());
			this.firstLine = Math.max(0, Math.min(max, this.firstLine - (int) Math.signum(scrollY)));
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private boolean isOverList(final double mouseX, final double mouseY) {
		return mouseX >= 6 && mouseX < 145 && mouseY >= this.listTop() && mouseY < this.listBottom();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER && this.lineBox != null && this.lineBox.isFocused()) {
			this.addLine();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		// Deliberately no blur: the HUD behind the tray must stay readable while
		// the player edits it. The tray itself is translucent and covers the
		// lower part of the screen.
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		final int panelTop = this.panelTop();
		guiGraphics.fill(0, panelTop, this.width, this.height, 0xD0101010);
		guiGraphics.fill(0, panelTop, this.width, panelTop + 1, 0xFF3A8EE6);
		guiGraphics.fill(147, panelTop + 4, 148, this.height - 6, 0xFF404040);

		super.render(guiGraphics, mouseX, mouseY, partialTick);

		this.renderLines(guiGraphics);
		this.renderSelection(guiGraphics);

		guiGraphics.drawString(this.font, Component.translatable("gui.ingameinfoxml.editor.offset").getString(),
				204, panelTop + 86, 0xA0A0A0);
		if (!this.status.isEmpty()) {
			guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(this.status, 150),
					266, panelTop + 108, this.statusError ? 0xFF7070 : 0x70FF70);
		}
	}

	private void renderLines(final GuiGraphics guiGraphics) {
		final List<String> list = this.currentLines();
		final int top = this.listTop();
		final int bottom = this.listBottom();
		guiGraphics.enableScissor(6, top, 145, bottom);
		for (int i = this.firstLine; i < list.size() && i < this.firstLine + this.visibleLines(); i++) {
			final int y = top + (i - this.firstLine) * LINE_HEIGHT;
			if (i == this.selectedLine) {
				guiGraphics.fill(6, y - 1, 145, y + LINE_HEIGHT - 1, 0x803A8EE6);
			}
			final String text = list.get(i);
			final String display = (i + 1) + ": " + (text.isEmpty() ? "§7(empty)" : text);
			guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(display, 134), 9, y, 0xFFFFFF);
		}
		guiGraphics.disableScissor();
		if (list.isEmpty()) {
			guiGraphics.drawString(this.font, Component.translatable("gui.ingameinfoxml.editor.nolines").getString(),
					9, top + 2, 0x808080);
		}
	}

	private void renderSelection(final GuiGraphics guiGraphics) {
		final int index = this.selectedAlignment.ordinal();
		final Button button = this.alignmentButtons[index];
		if (button == null) {
			return;
		}
		final int x = button.getX() - 1;
		final int y = button.getY() - 1;
		final int right = x + button.getWidth() + 1;
		final int bottom = y + button.getHeight() + 1;
		final int color = 0xFFFFD54A;
		guiGraphics.fill(x, y, right, y + 1, color);
		guiGraphics.fill(x, bottom - 1, right, bottom, color);
		guiGraphics.fill(x, y, x + 1, bottom, color);
		guiGraphics.fill(right - 1, y, right, bottom, color);
	}

	@Override
	public void onClose() {
		this.applyLines();
		this.minecraft.setScreen(null);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
