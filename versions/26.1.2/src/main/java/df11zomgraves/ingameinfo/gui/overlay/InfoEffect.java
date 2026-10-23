package df11zomgraves.ingameinfo.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public class InfoEffect extends Info {
	private final Holder<MobEffect> effect;
	private final int size;

	public InfoEffect(Holder<MobEffect> effect) {
		this(effect, false, 0, 0);
	}

	public InfoEffect(Holder<MobEffect> effect, final boolean large) {
		this(effect, large, 0, 0);
	}

	public InfoEffect(Holder<MobEffect> effect, final boolean large, int x, int y) {
		super(x, y);
		this.effect = effect;
		this.size = large ? 18 : 9;
		if (large) {
			this.y = -2;
		}
	}

	@Override
	public int getWidth() {
		return this.size;
	}

	@Override
	public int getHeight() {
		return this.size;
	}

	@Override
	public String toString() {
		return String.format("InfoEffect{MobEffect: %s, x: %d, y: %d, offsetX: %d, offsetY: %d, children: %s}",
				this.effect, this.x, this.y, this.offsetX, this.offsetY, this.children);
	}

	@Override
	public void drawInfo(GuiGraphicsExtractor guiGraphics) {
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(this.effect), getX(), getY(),
				this.size, this.size);
	}

}
