package df11zomgraves.ingameinfo.tag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

import df11zomgraves.ingameinfo.gui.overlay.Info;
import df11zomgraves.ingameinfo.reference.Names;
import df11zomgraves.ingameinfo.util.MBlockPos;
import df11zomgraves.ingameinfo.util.Vector3f;

public abstract class Tag {
	protected static final Minecraft minecraft = Minecraft.getInstance();
	protected static final MBlockPos playerPosition = new MBlockPos();
	protected static final Vector3f playerMotion = new Vector3f();
	protected static ClientLevel world;
	protected static LocalPlayer player;
	protected static List<Info> info;

	private String name = null;
	private String[] aliases = new String[0];

	public Tag setName(final String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return this.name;
	}

	public Tag setAliases(final String... aliases) {
		this.aliases = aliases;
		return this;
	}

	public String[] getAliases() {
		return this.aliases;
	}

	public boolean isIndexed() {
		return false;
	}

	public int getMaximumIndex() {
		return -1;
	}

	public String getRawName() {
		return this.name;
	}

	public String getFormattedName() {
		return this.name + (isIndexed() ? String.format("[0..%d]", getMaximumIndex()) : "");
	}

	public String getLocalizedCategory() {
		return Component.translatable(Names.MODID + ".tag.category." + getCategory() + ".name").getString();
	}

	public String getLocalizedDescription() {
		return Component.translatable(Names.MODID + ".tag." + getRawName() + ".desc").getString();
	}

	public abstract String getCategory();
	public abstract String getValue();
	
	public static void setWorld(final ClientLevel world) {
		Tag.world = world;
	}

	public static void setPlayer(final LocalPlayer player) {
		Tag.player = player;
		if (player != null) {
			playerPosition.set(player);
			playerMotion.set((float) (player.getX() - player.xOld), (float) (player.getY() - player.yOld),
					(float) (player.getZ() - player.zOld));
		}
	}

	public static void setInfo(final List<Info> info) {
		Tag.info = info;
	}

	public static void releaseResources() {
		setWorld(null);
		setPlayer(null);
		TagNearbyPlayer.releaseResources();
		TagPlayerEffect.releaseResources();
	}

	/**
	 * Evaluates a tag for the debug inspector without leaking icon or item entries
	 * into the live HUD queue, and without letting one broken tag stop the list.
	 */
	public static String getValueForDebug(final Tag tag) {
		final List<Info> previous = Tag.info;
		Tag.info = new ArrayList<Info>();
		try {
			final String value = tag.getValue();
			return value == null ? "" : value;
		} catch (final Throwable t) {
			final String message = t.getMessage();
			return "ERROR: " + t.getClass().getSimpleName() + (message == null ? "" : ": " + message);
		} finally {
			Tag.info = previous;
		}
	}

	public static String getIconTag(final Info info) {
		String str = "";
		for (int i = 0; i < info.getWidth() && minecraft.font.width(str) < info.getWidth(); i++) {
			str += " ";
		}
		return String.format("{ICON|%s}", str);
	}
}
