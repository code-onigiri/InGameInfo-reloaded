package df11zomgraves.ingameinfo.client.gui.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import df11zomgraves.ingameinfo.tag.Tag;
import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class GuiTags extends Screen {
	private static final Component TITLE = Component.translatable("gui.ingameinfoxml.taglist");
	private static final Component SEARCH = Component.translatable("gui.ingameinfoxml.search");

	private TagList tagList;
	private EditBox searchField;
	private final Screen parent;
	private final Consumer<String> tagConsumer;

	public GuiTags() {
		this(null, null);
	}

	/**
	 * @param parent      screen to return to, or {@code null} to return to the game
	 * @param tagConsumer receives the text syntax for the clicked tag
	 */
	public GuiTags(final Screen parent, final Consumer<String> tagConsumer) {
		super(TITLE);
		this.parent = parent;
		this.tagConsumer = tagConsumer;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	@Override
	protected void init() {
		this.tagList = new TagList(this.minecraft, this.width, this.height, 36, this.height - 56, 22,
				this.parent, this.tagConsumer);
		addRenderableWidget(this.tagList);

		this.searchField = new EditBox(this.font, this.width / 2 - 155, this.height - 28, 150, 18, SEARCH);
		this.searchField.setHint(SEARCH);
		this.searchField.setResponder(value -> this.tagList.filter(value.toLowerCase(Locale.ENGLISH)));
		addRenderableWidget(this.searchField);

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		// Screen は背景を先に描き、そのあと extractRenderState を呼ぶ。
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.centeredText(this.font, TITLE, this.width / 2, 12, 0xFFFFFFFF);
	}

	private static class TagList extends ObjectSelectionList<TagList.Entry> {
		private final Map<String, List<TagEntry>> categories = new TreeMap<>();
		private final Screen parent;
		private final Consumer<String> tagConsumer;

		TagList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight,
				Screen parent, Consumer<String> tagConsumer) {
			super(minecraft, width, bottom - top, top, itemHeight);
			this.parent = parent;
			this.tagConsumer = tagConsumer;
			for (Tag tag : TagRegistry.INSTANCE.getRegisteredTags()) {
				List<TagEntry> entries = categories.computeIfAbsent(tag.getLocalizedCategory(), key -> new ArrayList<>());
				String name = tag.getFormattedName();
				if (entries.stream().noneMatch(entry -> entry.name.equals(name))) {
					String insert = tag.isIndexed() ? tag.getRawName() + "0" : tag.getRawName();
					entries.add(new TagEntry(name, tag.getLocalizedDescription(), insert));
				}
			}
			filter("");
		}

		void filter(String pattern) {
			clearEntries();
			for (Map.Entry<String, List<TagEntry>> category : categories.entrySet()) {
				List<TagEntry> matches = new ArrayList<>();
				for (TagEntry tag : category.getValue()) {
					if (tag.name.toLowerCase(Locale.ENGLISH).contains(pattern)
							|| tag.description.toLowerCase(Locale.ENGLISH).contains(pattern)) {
						matches.add(tag);
					}
				}
				if (!matches.isEmpty()) {
					matches.sort(java.util.Comparator.comparing(tag -> tag.name, String.CASE_INSENSITIVE_ORDER));
					addEntry(new CategoryEntry(category.getKey()));
					for (TagEntry tag : matches) {
						addEntry(tag);
					}
				}
			}
			setScrollAmount(0);
		}

		@Override
		public int getRowWidth() {
			return Math.min(Math.max(this.width - 40, 220), 440);
		}


		abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
		}

		class CategoryEntry extends Entry {
			private final String name;

			CategoryEntry(String name) {
				this.name = name;
			}

			@Override
			public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered,
					float partialTick) {
				guiGraphics.centeredText(TagList.this.minecraft.font, this.name, getContentXMiddle(), getContentY() + 5,
						0xFFFFFFFF);
			}

			@Override
			public Component getNarration() {
				return Component.literal(this.name);
			}
		}

		class TagEntry extends Entry {
			private final String name;
			private final String description;
			private final String insertName;

			TagEntry(String name, String description, String insertName) {
				this.name = name;
				this.description = description;
				this.insertName = insertName;
			}

			@Override
			public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
				if (event.button() == 0 && TagList.this.tagConsumer != null) {
					TagList.this.tagConsumer.accept("<" + this.insertName + ">");
					TagList.this.minecraft.setScreen(TagList.this.parent);
					return true;
				}
				return super.mouseClicked(event, doubleClick);
			}

			@Override
			public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered,
					float partialTick) {
				guiGraphics.text(TagList.this.minecraft.font, this.name, getContentX(), getContentY() + 5, 0xFFFFFFFF);
				guiGraphics.text(TagList.this.minecraft.font, this.description, getContentX() + 150, getContentY() + 5,
					0xFFA0A0A0);
			}

			@Override
			public Component getNarration() {
				return Component.literal(this.name + ": " + this.description);
			}
		}
	}
}
