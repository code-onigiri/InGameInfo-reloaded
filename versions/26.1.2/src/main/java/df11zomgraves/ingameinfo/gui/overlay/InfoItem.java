package df11zomgraves.ingameinfo.gui.overlay;

import df11zomgraves.ingameinfo.handler.ConfigurationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public class InfoItem extends Info {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private final ItemStack itemStack;
	private final boolean large;
	private final int size;
	private boolean forceRender;

	public InfoItem(final ItemStack itemStack) {
		this(itemStack, false);
	}

	public InfoItem(final ItemStack itemStack, final boolean large) {
		this(itemStack, large, 0, 0);
	}

	public InfoItem(final ItemStack itemStack, final boolean large, final int x, final int y) {
		this(itemStack, large, x, y, false);
	}

	public InfoItem(final ItemStack itemStack, final boolean large, final int x, final int y,
			boolean forceRender) {
		super(x, y);
		this.itemStack = itemStack;
		this.large = large;
		this.size = large ? 16 : 8;
		if (large) {
			this.y = -4;
		}
		this.forceRender = forceRender;
	}

	@Override
	public void drawInfo(GuiGraphicsExtractor guiGraphics) {
		if (this.itemStack.isEmpty())
			return;
		guiGraphics.pose().pushMatrix();
		if (!large) {
			guiGraphics.pose().scale(0.5f, 0.5f);
			guiGraphics.pose().translate(getX() * 2.0f, getY() * 2.0f);
		} else {
			guiGraphics.pose().translate(getX(), getY());
		}
		guiGraphics.item(itemStack, 0, 0);
		if (forceRender)
			guiGraphics.itemDecorations(MINECRAFT.font, itemStack, 0, 0);
		else if (ConfigurationHandler.showOverlayItemIcons)
			guiGraphics.itemDecorations(MINECRAFT.font, itemStack, 0, 0, "");
		guiGraphics.pose().popMatrix();
	}

	@Override
	public int getWidth() {
		return this.forceRender ? this.size : !this.itemStack.isEmpty() ? this.size : 0;
	}

	@Override
	public int getHeight() {
		return this.forceRender ? this.size : !this.itemStack.isEmpty() ? this.size : 0;
	}

	@Override
	public String toString() {
		return String.format("InfoItem{itemStack: %s, x: %d, y: %d, offsetX: %d, offsetY: %d, children: %s}",
				this.itemStack, this.x, this.y, this.offsetX, this.offsetY, this.children);
	}
}
