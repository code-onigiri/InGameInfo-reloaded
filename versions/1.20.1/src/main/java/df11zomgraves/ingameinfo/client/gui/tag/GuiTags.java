package df11zomgraves.ingameinfo.client.gui.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import df11zomgraves.ingameinfo.tag.Tag;
import df11zomgraves.ingameinfo.tag.TagRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class GuiTags extends Screen {
	private static final Component TITLE = Component.translatable("gui.ingameinfoxml.taglist");
	private static final Component SEARCH = Component.translatable("gui.ingameinfoxml.search");

	private TagList tagList;
	private EditBox searchField;

	public GuiTags() {
		super(TITLE);
	}

	@Override
	protected void init() {
		this.tagList = new TagList(this.minecraft, this.width, this.height, 36, this.height - 56, 22);
		addRenderableWidget(this.tagList);

		this.searchField = new EditBox(this.font, this.width / 2 - 155, this.height - 28, 150, 18, SEARCH);
		this.searchField.setHint(SEARCH);
		this.searchField.setResponder(value -> this.tagList.filter(value.toLowerCase(Locale.ENGLISH)));
		addRenderableWidget(this.searchField);

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
				.bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(guiGraphics);
		guiGraphics.drawCenteredString(this.font, TITLE, this.width / 2, 12, 0xFFFFFF);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}

	private static class TagList extends ObjectSelectionList<TagList.Entry> {
		private final Map<String, List<TagEntry>> categories = new TreeMap<>();

		TagList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
			super(minecraft, width, height, top, bottom, itemHeight);
			for (Tag tag : TagRegistry.INSTANCE.getRegisteredTags()) {
				List<TagEntry> entries = categories.computeIfAbsent(tag.getLocalizedCategory(), key -> new ArrayList<>());
				String name = tag.getFormattedName();
				if (entries.stream().noneMatch(entry -> entry.name.equals(name))) {
					entries.add(new TagEntry(name, tag.getLocalizedDescription()));
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

		@Override
		protected int getScrollbarPosition() {
			return this.width / 2 + getRowWidth() / 2 + 4;
		}

		abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
		}

		class CategoryEntry extends Entry {
			private final String name;

			CategoryEntry(String name) {
				this.name = name;
			}

			@Override
			public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
					int mouseX, int mouseY, boolean hovering, float partialTick) {
				guiGraphics.drawCenteredString(TagList.this.minecraft.font, this.name, left + width / 2, top + 7,
						0xFFFFFF);
			}

			@Override
			public Component getNarration() {
				return Component.literal(this.name);
			}
		}

		class TagEntry extends Entry {
			private final String name;
			private final String description;

			TagEntry(String name, String description) {
				this.name = name;
				this.description = description;
			}

			@Override
			public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
					int mouseX, int mouseY, boolean hovering, float partialTick) {
				guiGraphics.drawString(TagList.this.minecraft.font, this.name, left, top + 7, 0xFFFFFF);
				guiGraphics.drawString(TagList.this.minecraft.font, this.description, left + 150, top + 7, 0xA0A0A0);
			}

			@Override
			public Component getNarration() {
				return Component.literal(this.name + ": " + this.description);
			}
		}
	}
}
