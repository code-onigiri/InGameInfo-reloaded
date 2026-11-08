package df11zomgraves.ingameinfo.reference;

import java.util.Locale;

import df11zomgraves.ingameinfo.InGameInfoXML;
import df11zomgraves.ingameinfo.handler.ConfigurationHandler;

public enum Alignment {
	TOPLEFT(2, 2),
	TOPCENTER(0, 2),
	TOPRIGHT(-2, 2),
	MIDDLELEFT(2, 0),
	MIDDLECENTER(0, 0),
	MIDDLERIGHT(-2, 0),
	BOTTOMLEFT(2, -2),
	BOTTOMCENTER(0, -45),
	BOTTOMRIGHT(-2, -2);

	private static final int MASK_X = 0x0C;
	private static final int MASK_Y = 0x03;

	private static final int TOP = 0x01;
	private static final int MIDDLE = 0x03;
	private static final int BOTTOM = 0x02;

	private static final int LEFT = 0x04;
	private static final int CENTER = 0x0C;
	private static final int RIGHT = 0x08;

	private int alignment;
	public final int defaultX;
	public final int defaultY;
	/** this constant's name in lower case, cached so the render path does not rebuild it per call */
	public final String lowerCaseName;
	public int x;
	public int y;

	Alignment(final int x, final int y) {
		this.defaultX = x;
		this.defaultY = y;
		this.x = x;
		this.y = y;
		this.alignment = 0;
		this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
	}

	public static Alignment parse(final String str) {
		if (str == null) {
			return null;
		}

		String rest = str.toLowerCase(Locale.ENGLISH);
		String vertical = "MIDDLE";
		boolean matched = false;
		if (rest.startsWith("top")) {
			vertical = "TOP";
			rest = rest.substring("top".length());
			matched = true;
		} else if (rest.startsWith("middle")) {
			vertical = "MIDDLE";
			rest = rest.substring("middle".length());
			matched = true;
		} else if (rest.startsWith("mid")) {
			vertical = "MIDDLE";
			rest = rest.substring("mid".length());
			matched = true;
		} else if (rest.startsWith("bottom")) {
			vertical = "BOTTOM";
			rest = rest.substring("bottom".length());
			matched = true;
		} else if (rest.startsWith("bot")) {
			vertical = "BOTTOM";
			rest = rest.substring("bot".length());
			matched = true;
		}

		final String horizontal;
		switch (rest) {
		case "left":
			horizontal = "LEFT";
			matched = true;
			break;
		case "center":
			horizontal = "CENTER";
			matched = true;
			break;
		case "right":
			horizontal = "RIGHT";
			matched = true;
			break;
		case "":
			horizontal = "CENTER";
			break;
		default:
			return null;
		}

		if (!matched) {
			return null;
		}

		try {
			return valueOf(vertical + horizontal);
		} catch (final Exception e) {
			return null;
		}
	}

	public int getX(final int screenwidth, final int textwidth) {
		String alignmentMiddleCenter = ConfigurationHandler.alignmentMiddleCenter.lowerCaseName;
		switch (this.alignment & MASK_X) {
		case LEFT:
			return this.x;
		case CENTER:
			if ((this.alignment & MASK_Y) == MIDDLE) {
				if (alignmentMiddleCenter.endsWith("left"))
					return this.x + (screenwidth / 2);
				if (alignmentMiddleCenter.endsWith("right"))
					return this.x - textwidth + (screenwidth / 2);
			}
			return this.x + (screenwidth - textwidth) / 2;
		case RIGHT:
			return this.x + screenwidth - textwidth;
		}

		return 0;
	}

	public int getY(final int screenheight, final int textheight) {
		String alignmentMiddleCenter = ConfigurationHandler.alignmentMiddleCenter.lowerCaseName;
		switch (this.alignment & MASK_Y) {
		case TOP:
			return this.y;
		case MIDDLE:
			if ((this.alignment & MASK_X) == CENTER) {
				if (alignmentMiddleCenter.startsWith("top"))
					return this.y + (screenheight / 2);
				if (alignmentMiddleCenter.startsWith("bot"))
					return this.y - textheight + (screenheight / 2);
			}
			return this.y + (screenheight - textheight) / 2;
		case BOTTOM:
			return this.y + screenheight - textheight;
		}

		return 0;
	}

	public String getDefaultXY() {
		return String.format("%d %d", this.defaultX, this.defaultY);
	}

	public String getXY() {
		return String.format("%d %d", this.x, this.y);
	}

	public void setXY(final String str) {
		try {
			final String[] parts = str.split(" ");
			if (parts.length == 2) {
				final int x = Integer.parseInt(parts[0]);
				final int y = Integer.parseInt(parts[1]);
				this.x = x;
				this.y = y;
				return;
			}
		} catch (final Exception e) {
		}
		InGameInfoXML.logger.warn("Ignoring the malformed alignment offset '{}' for {}", str, this.name());
	}

	static {
		TOPLEFT.alignment = TOP | LEFT;
		TOPCENTER.alignment = TOP | CENTER;
		TOPRIGHT.alignment = TOP | RIGHT;
		MIDDLELEFT.alignment = MIDDLE | LEFT;
		MIDDLECENTER.alignment = MIDDLE | CENTER;
		MIDDLERIGHT.alignment = MIDDLE | RIGHT;
		BOTTOMLEFT.alignment = BOTTOM | LEFT;
		BOTTOMCENTER.alignment = BOTTOM | CENTER;
		BOTTOMRIGHT.alignment = BOTTOM | RIGHT;
	}
}
